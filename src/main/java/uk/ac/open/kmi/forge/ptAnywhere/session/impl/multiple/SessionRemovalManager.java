package uk.ac.open.kmi.forge.ptAnywhere.session.impl.multiple;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.ws.rs.client.Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import uk.ac.open.kmi.forge.ptAnywhere.session.management.AllocationResourceClient;


public class SessionRemovalManager {

    static final Log LOGGER = LogFactory.getLog(SessionRemovalManager.class);

    final JedisPool pool;
    final Client httpClient;

    // Lock access to the http client.
    final Lock clientLock;


    private SessionRemovalManager(JedisPool pool, Client client, Lock lock) {
        this.pool = pool;
        this.httpClient = client;
        this.clientLock = lock;
    }

    // This is particularly useful whenever this object is reused (e.g., in ExpirationSubscriberImpl).
    protected static SessionRemovalManager createReusable(JedisPool pool, Client client) {
        return new SessionRemovalManager(pool, client, new ReentrantLock());
    }

    protected static SessionRemovalManager create(JedisPool pool, Client client) {
        return new SessionRemovalManager(pool, client, null);
    }

    private void simpleDeleteRequest(String allocationUrl) {
        final AllocationResourceClient cli = new AllocationResourceClient(allocationUrl, this.httpClient);
        cli.delete();  // If it throws an exception the element is not deleted.
    }

    private void requestDelete(String allocationUrl) {
        if (this.clientLock==null) {
            simpleDeleteRequest(allocationUrl);
        } else {
            this.clientLock.lock();
            try {
                simpleDeleteRequest(allocationUrl);
            } finally {
                this.clientLock.unlock();
            }
        }
    }

    public void deleteRSession(String rSessionId) {
        try (Jedis jedis = this.pool.getResource()) {
            final String allocationUrl = jedis.get(RedisKeys.URL_PREFIX + rSessionId);
            if (allocationUrl!=null) {
                requestDelete(allocationUrl);

                // If everything went well...
                final Transaction t = jedis.multi();
                t.del(rSessionId); // If it has expired then no problem?
                t.del(RedisKeys.URL_PREFIX + rSessionId);
                //t.set(rSessionId + "_DELETED", "true");  // For debugging with redis... 0:-)
                t.exec();

                LOGGER.debug("Expired instance removed for " + rSessionId + ".");
            }
        }
    }
}
