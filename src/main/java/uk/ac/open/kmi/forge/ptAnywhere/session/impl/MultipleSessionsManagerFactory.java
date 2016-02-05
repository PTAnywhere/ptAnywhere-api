package uk.ac.open.kmi.forge.ptAnywhere.session.impl;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.client.ClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import uk.ac.open.kmi.forge.ptAnywhere.PoolManager;
import uk.ac.open.kmi.forge.ptAnywhere.properties.RedisConnectionProperties;
import uk.ac.open.kmi.forge.ptAnywhere.session.ExpirationSubscriber;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;


public class MultipleSessionsManagerFactory implements SessionsManagerFactory {
    // Pool usage recommended in the official documentation:
    //   "You can store the pool somewhere statically, it is thread-safe."
    protected final JedisPool pool;
    protected final int maxLength;
    protected final int dbNumber;

    final ClientConfig reusableClientConfiguration;
    final PoolingHttpClientConnectionManager connectionManager;
    final PoolManager poolManager;  // Not managed by this object.


    public MultipleSessionsManagerFactory(RedisConnectionProperties redis, PoolManager poolManager, int maximumLength) {
        this.maxLength = maximumLength;
        this.dbNumber = redis.getDbNumber();
        // 2000 and null are the default values used in JedisPool...
        this.pool = new JedisPool(new JedisPoolConfig(), redis.getHostname(), redis.getPort(), 2000, null, this.dbNumber);

        // Reusable client configuration for clients created by the pool.
        this.reusableClientConfiguration = poolManager.getApacheClientConfig();
        poolManager.configureDefaultTimeouts(this.reusableClientConfiguration);
        this.connectionManager = poolManager.configureClientPool(this.reusableClientConfiguration);

        this.poolManager = poolManager;  // For in createExpirationSubscription
    }

    /*
     * Creates an http client probably taking it from the pool.
     *
     * From the ApacheConnector documentation:
     *  "Client operations are thread safe, the HTTP connection may be shared between different threads."
     */
    private Client createReusableClient() {
        return ClientBuilder.newClient(this.reusableClientConfiguration);
    }

    @Override
    public MultipleSessionsManager create() {
        return new MultipleSessionsManager(this.pool, this.dbNumber, this.maxLength, createReusableClient());
    }

    /**
     * WARNING: Returns a runnable which calls to a Jedis blocking operation.
     */
    @Override
    public ExpirationSubscriber createExpirationSubscription() {
        final ClientConfig config = this.poolManager.getApacheClientConfig();
        this.poolManager.configureDefaultTimeoutsAndInfiniteConnectionRequestTimeout(config);
        final Client httpClient = ClientBuilder.newClient(config);
        return new ExpirationSubscriberImpl(this.pool, this.dbNumber, httpClient);
    }

    @Override
    public void destroy() {
        this.pool.destroy();
        // From this moment on, the operations will throw an exception.
        // Hopefully, there would not be any MultipleSessionsManagers being used.
        this.connectionManager.close();
    }
}
