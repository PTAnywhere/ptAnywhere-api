package uk.ac.open.kmi.forge.webPacketTracer.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import uk.ac.open.kmi.forge.webPacketTracer.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.webPacketTracer.properties.RedisConnectionProperties;


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

    public static SessionManager createSessionManager() {
        final PropertyFileManager pfm = new PropertyFileManager();
        final RedisConnectionProperties rcp = pfm.getRedisConnectionDetails();
        return new SessionManager(rcp.getHostname(), rcp.getPort(), rcp.getDbNumber());
    }
}