package uk.ac.open.kmi.forge.ptAnywhere;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.ptAnywhere.properties.RedisConnectionProperties;

import javax.ws.rs.client.ClientBuilder;
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

    // Default timeouts
    private final int connectTimeout;
    private final int socketTimeout;
    private final int connectionRequestTimeout;

    // Documentation of limits:
    //     JedisPoolConfig:
    //            max total: 8, max idle: 8, min idle: 0,
    //            blockWhenExhausted: true,
    //            borrowMaxWaitMillis: -1 (blocks indefinitely)
    //     Thread pool: 200 threads, unbounded queue

    public PoolManager(PropertyFileManager properties) {
        this.executor = Executors.newFixedThreadPool(200, new SimpleDaemonFactory());
        this.connectTimeout = properties.getConnectTimeout();
        this.socketTimeout = properties.getSocketTimeout();
        this.connectionRequestTimeout = properties.getConnectionRequestTimeout();
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


    // STATIC methods to help other objects configure pools and still have all these parameters centralized in this class.


    /**
     * Adds a client pool to the client configuration and returns the pool manager which will be used.
     *
     * @param clientConfig
     *      The client configuration to be modified.
     * @return
     *      Shared pool manager which will be used.
     *      WARNING: the pool must be appropriately closed after using it.
     */
    public static PoolingHttpClientConnectionManager configureClientPool(ClientConfig clientConfig) {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        // Default: "no more than than 2 concurrent connections per given route and no more 20 connections in total".
        //connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(10);
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
        // Avoid HTTP clients from closing pool by themselves.
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER_SHARED, true);
        return connectionManager;
    }

    /**
     * Sets the timeouts to the requests of the provided client configuration.
     *
     * See https://github.com/PTAnywhere/ptAnywhere-api/issues/20
     *
     * @param clientConfig
     *      Client configuration to be set.
     * @param connectTimeout
     *      Timeout "until a connection is established"
     * @param socketTimeout
     *      "Timeout for waiting for data a maximum period inactivity between two consecutive data packets"
     * @param connectionRequestTimeout
     *      Timeout of requesting a connection from the connection manager.
     */
    protected void configureTimeouts(ClientConfig clientConfig, int connectTimeout, int socketTimeout, int connectionRequestTimeout) {
        final RequestConfig requestConfig = RequestConfig.custom()
                                                            .setConnectTimeout(connectTimeout)
                                                            .setSocketTimeout(socketTimeout)
                                                            .setConnectionRequestTimeout(connectionRequestTimeout)
                                                            .build();
        clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, requestConfig); // jersey specific
    }

    /**
     * Sets the timeouts defined as properties to the requests of the provided client configuration.
     *
     * @param clientConfig
     *      Client configuration to be set.
     */
    protected void configureDefaultTimeouts(ClientConfig clientConfig) {
        configureTimeouts(clientConfig, this.connectTimeout, this.socketTimeout, this.connectionRequestTimeout);
    }

    /*
     * Creates configuration for an Apache HTTP client.
     * See https://github.com/PTAnywhere/ptAnywhere-api/issues/27
     *
     * Default behaviour:
     * <ul>
     *     <li>Only one connection will be established per factory.
     *          To use a client pool, call the configureClientPool() method.
     *          See https://github.com/PTAnywhere/ptAnywhere-api/issues/30
     *     </li>
     *     <li>Sets the timeouts defined in configureTimeout().</li>
     * </ul>
     */
    public ClientConfig getApacheClientConfig() {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        configureDefaultTimeouts(clientConfig);
        return clientConfig;
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