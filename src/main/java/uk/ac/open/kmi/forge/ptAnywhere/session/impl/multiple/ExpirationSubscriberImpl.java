package uk.ac.open.kmi.forge.ptAnywhere.session.impl.multiple;

import javax.ws.rs.client.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import uk.ac.open.kmi.forge.ptAnywhere.session.ExpirationSubscriber;


public class ExpirationSubscriberImpl implements ExpirationSubscriber {

    final int dbNumber;
    final JedisPool pool;
    final Client httpClient;
    final ExpirationListener listener;

    /**
     * @param pool
     * @param dbNumber
     * @param httpClient An Http client whose lifecycle will be managed by this object.
     */
    protected ExpirationSubscriberImpl(JedisPool pool, int dbNumber, Client httpClient) {
        this.httpClient = httpClient;
        this.listener = new ExpirationListener(SessionRemovalManager.createReusable(pool, httpClient));
        this.dbNumber = dbNumber;
        this.pool = pool;
    }

    @Override
    public void run() {
        try (Jedis jedis = this.pool.getResource()) {            // Recommended readings:
            //   + http://redis.io/topics/notifications
            //   + https://github.com/xetorthio/jedis/wiki/AdvancedUsage
            jedis.psubscribe(this.listener, "__keyevent@" + this.dbNumber + "__:expired");
        }
    }

    @Override
    public void stop() {
        this.listener.punsubscribe();
        this.httpClient.close();
    }
}


class ExpirationListener extends JedisPubSub {

    final SessionRemovalManager ownManager;

    ExpirationListener(SessionRemovalManager ownManager) {

        this.ownManager = ownManager;
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        // channel == "__keyevent@0__:expired" and message == "session:id"
        this.ownManager.deleteRSession(message);
    }
}