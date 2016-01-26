package uk.ac.open.kmi.forge.ptAnywhere.session.impl;

import org.apache.http.client.config.RequestConfig;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import uk.ac.open.kmi.forge.ptAnywhere.properties.RedisConnectionProperties;
import uk.ac.open.kmi.forge.ptAnywhere.session.ExpirationSubscriber;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;
import javax.ws.rs.client.ClientBuilder;


public class MultipleSessionsManagerFactory implements SessionsManagerFactory {
    // Pool usage recommended in the official documentation:
    //   "You can store the pool somewhere statically, it is thread-safe."
    protected final JedisPool pool;
    protected final int maxLength;
    protected final int dbNumber;

    private final javax.ws.rs.client.Client reusableClient;


    public MultipleSessionsManagerFactory(RedisConnectionProperties redis, int maximumLength) {
        this.maxLength = maximumLength;
        this.dbNumber = redis.getDbNumber();
        this.reusableClient = createReusableClient();
        // 2000 and null are the default values used in JedisPool...
        this.pool = new JedisPool(new JedisPoolConfig(), redis.getHostname(), redis.getPort(), 2000, null, this.dbNumber);
    }

    /*
     * Creates a reusable http client.
     *
     * From the ApacheConnector documentation:
     * "Client operations are thread safe, the HTTP connection may be shared between different threads."
     */
    private javax.ws.rs.client.Client createReusableClient() {
        final ClientConfig clientConfig = new ClientConfig();
        /*
         * Note that currently only a connection will be established per factory.
         *     https://github.com/PTAnywhere/ptAnywhere-api/issues/30
         */
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        final RequestConfig reqConfig = RequestConfig.custom()
                .setConnectTimeout(2000)
                .setSocketTimeout(2000)
                .setConnectionRequestTimeout(200)
                .build();
        clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, reqConfig); // jersey specific
        return ClientBuilder.newClient(clientConfig);
    }

    @Override
    public MultipleSessionsManager create() {
        return new MultipleSessionsManager(this.pool, this.dbNumber, this.maxLength, this.reusableClient);
    }

    /**
     * WARNING: Returns a runnable which calls to a Jedis blocking operation.
     */
    @Override
    public ExpirationSubscriber createExpirationSubscription() {
        return new ExpirationSubscriberImpl(create(), this.dbNumber, this.pool);
    }

    @Override
    public void destroy() {
        this.pool.destroy();
        // From this moment on, the operations will throw an exception.
        // Hopefully, there would not be any MultipleSessionsManagers being used.
        this.reusableClient.close();
    }
}
