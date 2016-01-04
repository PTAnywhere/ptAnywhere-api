package uk.ac.open.kmi.forge.ptAnywhere.session.impl;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import uk.ac.open.kmi.forge.ptAnywhere.properties.RedisConnectionProperties;
import uk.ac.open.kmi.forge.ptAnywhere.session.ExpirationSubscriber;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MultipleSessionsManagerFactory implements SessionsManagerFactory {

    // Pool usage recommended in the official documentation:
    //   "You can store the pool somewhere statically, it is thread-safe."
    protected static JedisPool pool;
    private static Lock poolLock = new ReentrantLock();  // Lock used to ensure that the pool is not created or destroyed twice by concurrent Threads.
    protected int dbNumber;


    protected MultipleSessionsManagerFactory(RedisConnectionProperties redis) {
        this.dbNumber = redis.getDbNumber();
        poolLock.lock();
        try {
            if (pool==null) {
                // 2000 and null are the default values used in JedisPool...
                pool = new JedisPool(new JedisPoolConfig(), redis.getHostname(), redis.getPort(), 2000, null, this.dbNumber);
            }
        } finally {
            poolLock.unlock();
        }
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

    @Override
    public void destroy() {
        poolLock.lock();
        try {
            if (pool!=null) {
                pool.destroy();
                pool = null;
            }
        } finally {
            poolLock.unlock();
        }
    }
}
