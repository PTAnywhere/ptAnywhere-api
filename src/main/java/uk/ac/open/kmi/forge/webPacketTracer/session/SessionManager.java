package uk.ac.open.kmi.forge.webPacketTracer.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.Properties;


/**
 * Redis client to manage the mapping between web sessions and the
 * PacketTracer instances supporting them.
 */
public class SessionManager {

    private static final Log LOGGER = LogFactory.getLog(SessionManager.class);

    final Jedis jedis;

    protected SessionManager(String hostname, int port, int dbNumber) {
        this.jedis = new Jedis(hostname, port);
        this.jedis.select(dbNumber);
    }

    /**
     * By default, the Redis server runs in the same machine as the web server.
     */
    public static SessionManager createSessionManager() {
        final Properties props = new Properties();
        try {
            props.load(SessionManager.class.getClassLoader().getResourceAsStream("environment.properties"));
        } catch(IOException e) {
            LOGGER.error("The Redis connection details could not be read from the properties file, using default value.");
        } finally {
            final String host = props.getProperty("redis-host", "localhost");
            final int port = Integer.parseInt(props.getProperty("redis-port", "6379"));
            final int dbNum = Integer.parseInt(props.getProperty("redis-db", "0"));
            return new SessionManager(host, port, dbNum);
        }
    }

}