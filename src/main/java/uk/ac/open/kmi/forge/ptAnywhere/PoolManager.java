package uk.ac.open.kmi.forge.ptAnywhere;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.ptAnywhere.properties.RedisConnectionProperties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This class simply centralizes the management of thread and clients pools shared across the application.
 */
public class PoolManager {

    // Executor used by the application for:
    //   1. Ensuring that the TinCanAPI clients execute in their own threads.
    //   2. Have a blocking Redis subscription object running during the application lifetime.
    private ExecutorService executor;


    // "You can store the pool somewhere statically, it is thread-safe."
    // Accessed by PTConnection and HistoricalAnonymousFinder
    private static JedisPool cachePool;

    // Documentation of limits:
    //     JedisPoolConfig:
    //            max total: 8, max idle: 8, min idle: 0,
    //            blockWhenExhausted: true,
    //            borrowMaxWaitMillis: -1 (blocks indefinitely)
    //     Thread pool: 200 threads, unbounded queue

    public PoolManager(PropertyFileManager properties) {
        this.executor = Executors.newFixedThreadPool(200, new SimpleDaemonFactory());
        createCache(properties);
    }

    public ExecutorService getGeneralExecutor() {
        return this.executor;
    }

    // For PTConnection.getDataAccessObject(), it could return null.
    public static JedisPool getCachePool() {
        return cachePool;
    }

    // cachePool is static, but handled by the unique instance of Pool object.
    private void createCache(PropertyFileManager properties) {
        final RedisConnectionProperties rProp = properties.getCacheDetails();
        cachePool = new JedisPool(new JedisPoolConfig(), rProp.getHostname(), rProp.getPort(), 2000, null, rProp.getDbNumber());
    }

    private void destroyCache() {
        // Uncomment to clean the cache DB once the app stops.
        /*try (Jedis jedis = cachePool.getResource()) {
            jedis.flushDB();
        }*/
        cachePool.destroy();
        cachePool = null;
    }

    public void close() {
        destroyCache();
        this.executor.shutdownNow();
    }
}


// Following the advice from:
//   http://stackoverflow.com/questions/3745905/what-is-recommended-way-for-spawning-threads-from-a-servlet-in-tomcat
class SimpleDaemonFactory implements ThreadFactory {
    public Thread newThread(Runnable r) {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        t.setUncaughtExceptionHandler(new UEHLogger());
        return t;
    }
}

class UEHLogger implements Thread.UncaughtExceptionHandler {
    private static final Log LOGGER = LogFactory.getLog(UEHLogger.class);
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.fatal("Thread terminated with exception: " + t.getName(), e);
    }
}