package uk.ac.open.kmi.forge.webPacketTracer.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import uk.ac.open.kmi.forge.webPacketTracer.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.webPacketTracer.properties.RedisConnectionProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * Redis client to manage the mapping between web sessions and the
 * PacketTracer instances supporting them.
 */
public class SessionManager {

    private static final Log LOGGER = LogFactory.getLog(SessionManager.class);

    private static final String AVAILABLE_INSTANCES = "available";
    private static final String INSTANCE_PREFIX = "instance_";
    private static final String INSTANCE_HOSTNAME = "hostname";
    private static final String INSTANCE_PORT = "port";
    private static final String INSTANCE_SESSION = "session";
    private static final String INSTANCE_UNASSIGNED_VALUE = "none";

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

    public void clear() {
        this.jedis.flushDB();
    }

    private String getInstanceId(String hostname, int port) {
        return INSTANCE_PREFIX + (hostname + ":" + port).hashCode();
    }

    public void addAvailableInstance(String hostname, int port) {
        final String instanceId = getInstanceId(hostname, port);
        this.jedis.sadd(AVAILABLE_INSTANCES, instanceId);
        final Map<String, String> instanceDetails = new HashMap<String, String>();
        instanceDetails.put(INSTANCE_HOSTNAME, hostname);
        instanceDetails.put(INSTANCE_PORT, String.valueOf(port));
        instanceDetails.put(INSTANCE_SESSION, INSTANCE_UNASSIGNED_VALUE);
        this.jedis.hmset(instanceId, instanceDetails);
    }

    // current_sessions
    // expired_sessions
}