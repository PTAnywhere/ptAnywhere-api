package uk.ac.open.kmi.forge.ptAnywhere.session.impl;

import javax.ws.rs.client.Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import uk.ac.open.kmi.forge.ptAnywhere.session.management.AllocationResourceClient;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


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

    private void simpleDeleteRequest(String instanceUrl) {
        final AllocationResourceClient cli = new AllocationResourceClient(instanceUrl, this.httpClient);
        cli.delete();  // If it throws an exception the element is not deleted.
    }

    private void requestDelete(String instanceUrl) {
        if (this.clientLock==null) {
            simpleDeleteRequest(instanceUrl);
        } else {
            this.clientLock.lock();
            try {
                simpleDeleteRequest(instanceUrl);
            } finally {
                this.clientLock.unlock();
            }
        }
    }

    public void deleteRSession(String rSessionId) {
        try (Jedis jedis = this.pool.getResource()) {
            final String instanceUrl = jedis.get(RedisKeys.URL_PREFIX + rSessionId);
            if (instanceUrl!=null) {
                requestDelete(instanceUrl);

                // If everything went well...
                final Transaction t = jedis.multi();
                t.del(rSessionId); // If it has expired then no problem?
                t.del(RedisKeys.URL_PREFIX + rSessionId);
                //t.set(rSessionId + "_DELETED", "true");  // For debuging with redis... 0:-)
                t.exec();

                LOGGER.debug("Expired instance removed for " + rSessionId + ".");
            }
        }
    }
}
