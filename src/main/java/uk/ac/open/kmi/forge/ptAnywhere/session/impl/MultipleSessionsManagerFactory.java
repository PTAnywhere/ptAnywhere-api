package uk.ac.open.kmi.forge.ptAnywhere.session.impl;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import uk.ac.open.kmi.forge.ptAnywhere.properties.RedisConnectionProperties;
import uk.ac.open.kmi.forge.ptAnywhere.session.ExpirationSubscriber;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;


public class MultipleSessionsManagerFactory implements SessionsManagerFactory {

    // Pool usage recommended in the official documentation:
    //   "You can store the pool somewhere statically, it is thread-safe."
    protected static JedisPool pool;
    protected int dbNumber;


    protected MultipleSessionsManagerFactory(RedisConnectionProperties redis) {
        this.dbNumber = redis.getDbNumber();
        // 2000 and null are the default values used in JedisPool...
        pool = new JedisPool(new JedisPoolConfig(), redis.getHostname(), redis.getPort(), 2000, null, this.dbNumber);
    }

    @Override
    public MultipleSessionsManager create() {
        return new MultipleSessionsManager(pool, this.dbNumber);
    }

    /**
     * WARNING: Returns a runnable which calls to a Jedis blocking operation.
     */
    @Override
    public ExpirationSubscriber createExpirationSubscription() {
        return new ExpirationSubscriberImpl(create(), this.dbNumber, pool);
    }

}
