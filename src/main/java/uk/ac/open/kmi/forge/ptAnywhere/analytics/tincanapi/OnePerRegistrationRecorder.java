package uk.ac.open.kmi.forge.ptAnywhere.analytics.tincanapi;

import com.rusticisoftware.tincan.RemoteLRS;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.TCAPIVersion;
import com.rusticisoftware.tincan.lrsresponses.StatementsResultLRSResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * This class makes sure that at each moment no more than one request is sent per registration (i.e., session).
 *
 * This way we will always ensure that the interactions for a given registration are recorded in order.
 * Sending each statement in its own request/thread the order can be different for evets that happened close in time.
 *
 * For each new statement to be recorded:
 *     1) If another one is being recorded with the same registration ID, store it in a cache and wait for the previous one to finish.
 *     2) Otherwise, sent the request.
 */
public class OnePerRegistrationRecorder implements StatementRecorder {

    private static final Log LOGGER = LogFactory.getLog(SimpleStatementRecorder.class);

    final RemoteLRS lrs = new RemoteLRS();  // I'm assuming that using record() is Thread-safe
    final ExecutorService executor;

    final ReadWriteLock unsentLock;
    // The absence of a key means that there is no current thread sending something related to it.
    final Map<String, CopyOnWriteArrayList<Statement>> unsent;


    // Constructor used by the factory
    public OnePerRegistrationRecorder(String endpoint, String username, String password, ExecutorService executor) throws MalformedURLException {
        this.executor = executor;
        this.lrs.setEndpoint(endpoint);
        this.lrs.setVersion(TCAPIVersion.V100);
        this.lrs.setUsername(username);
        this.lrs.setPassword(password);
        this.unsentLock = new ReentrantReadWriteLock();
        this.unsent = new HashMap<>();  // I don't think ConcurrentMap can be used for this...
    }

    public void record(final Statement statement) {
        final String regId = statement.getContext().getRegistration().toString();
        this.unsentLock.readLock().lock();
        if (this.unsent.containsKey(regId)) {
            this.unsent.get(regId).add(statement);
            this.unsentLock.readLock().unlock();
        } else {
            // Must release read lock before acquiring write lock
            this.unsentLock.readLock().unlock();
            this.unsentLock.writeLock().lock();
            try {
                this.unsent.put(regId, new CopyOnWriteArrayList<Statement>());
            } finally {
                this.unsentLock.writeLock().unlock();
            }
            // Make request
            this.executor.submit(new RecordRequest(statement));
        }
    }

    // Always executed at the end of another thread
    protected void sendPendingStatements(String registrationId) {
        final List<Statement> pending;
        unsentLock.writeLock().lock();
        try {
            pending = this.unsent.get(registrationId);
            if (pending.isEmpty()) {
                // Mark as unused
                this.unsent.remove(registrationId);
            } else {
                this.unsent.put(registrationId, new CopyOnWriteArrayList<Statement>());
            }
        } finally {
            this.unsentLock.writeLock().unlock();
        }

        // In a new RecordRequest instead of making a while inside it to avoid making endless ones.
        // This way, already queued ones can gain priority.
        if (!pending.isEmpty()) {
            this.executor.submit(new RecordRequest(pending));
        }
    }


    /**
     * Sends new request and when it finishes it repeats the process until
     * no more statements are found.
     */
    class RecordRequest implements Runnable {
        String registrationId;
        List<Statement> statements;

        RecordRequest(Statement statement) {
            this.registrationId = statement.getContext().getRegistration().toString();
            this.statements = new ArrayList<>();
            this.statements.add(statement);
        }

        RecordRequest(List<Statement> statements) {
            this.registrationId = statements.get(0).getContext().getRegistration().toString();
            this.statements = StatementConsolidator.consolidate(statements);
        }

        // To avoid adding uneeded delays in the HTTP request which is recording
        // the statement, we do it in a different Thread...
        @Override
        public void run() {
            try {
                final StatementsResultLRSResponse lrsRes = lrs.saveStatements(this.statements);
                if (lrsRes.getSuccess()) {
                    // success, use lrsRes.getContent() to get the statement back
                    LOGGER.debug("Everything went ok.");
                } else {
                    // failure, error information is available in lrsRes.getErrMsg()
                    LOGGER.error("Something went wrong recording the sentence.");
                    LOGGER.error("    HTTP error: " + lrsRes.getResponse().getStatusMsg());
                    LOGGER.error("    HTTP response: " + lrsRes.getResponse().getContent());
                }
            } finally {
                sendPendingStatements(this.registrationId);
            }
        }
    }
}
