package uk.ac.open.kmi.forge.ptAnywhere.analytics.tincanapi;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import com.rusticisoftware.tincan.RemoteLRS;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.TCAPIVersion;
import com.rusticisoftware.tincan.http.HTTPResponse;
import com.rusticisoftware.tincan.lrsresponses.StatementLRSResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * Records statements as they come creating a new Thread for each request.
 * Therefore, the order in which they are sent/stored depends on the executor.
 */
public class SimpleStatementRecorder implements StatementRecorder {

    private static final Log LOGGER = LogFactory.getLog(SimpleStatementRecorder.class);

    final RemoteLRS lrs = new RemoteLRS();
    final ExecutorService executor;

    // For testing
    protected SimpleStatementRecorder() {
        this.executor = null;
    }

    // Constructor used by the factory
    public SimpleStatementRecorder(String endpoint, String username, String password, ExecutorService executor) throws MalformedURLException {
        this.executor = executor;
        this.lrs.setEndpoint(endpoint);
        this.lrs.setVersion(TCAPIVersion.V100);
        this.lrs.setUsername(username);
        this.lrs.setPassword(password);
    }

    // Copied from TinCanJava code.
    protected DateTime getDateHeader(HTTPResponse response) {
        DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
                DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC();
        try {
            return DateTime.parse(response.getHeader("Date"), RFC1123_DATE_TIME_FORMATTER);
        }
        catch (Exception parseException) {
            return null;
        }
    }

    public void record(final Statement statement) {
        final Runnable saveTask = new Runnable() {
            @Override
            public void run() {
                final StatementLRSResponse lrsRes = lrs.saveStatement(statement);
                if (lrsRes.getSuccess()) {
                    // success, use lrsRes.getContent() to get the statement back
                    LOGGER.debug("Everything went ok.");
                } else {
                    // failure, error information is available in lrsRes.getErrMsg()
                    LOGGER.error("Something went wrong recording the sentence.");
                    LOGGER.error("    HTTP error: " + lrsRes.getResponse().getStatusMsg());
                    LOGGER.error("    HTTP response: " + lrsRes.getResponse().getContent());
                }
            }
        };
        // To avoid adding uneeded delays in the HTTP request which is recording
        // the statement, we do it in a different Thread...
        this.executor.submit(saveTask);
    }
}
