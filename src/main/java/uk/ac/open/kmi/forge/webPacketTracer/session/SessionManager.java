package uk.ac.open.kmi.forge.webPacketTracer.session;

import oracle.jrockit.jfr.StringConstantPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import uk.ac.open.kmi.forge.webPacketTracer.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.webPacketTracer.properties.RedisConnectionProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * Redis client to manage the mapping between web sessions and the
 * PacketTracer instances supporting them.
 */
public class SessionManager {

    private static final Log LOGGER = LogFactory.getLog(SessionManager.class);

    private static final String NUMBER_SESSIONS = "sessions";
    /**
     * Minutes that a reservation will last.
     */
    private static final int RESERVATION_TIME = 2;

    private static final String AVAILABLE_INSTANCES = "available";
    private static final String INSTANCE_PREFIX = "instance_";
    private static final String INSTANCE_HOSTNAME = "hostname";
    private static final String INSTANCE_PORT = "port";
    private static final String INSTANCE_SESSION = "session";
    private static final String UNASSIGNED_VALUE = "none";

    private static final String CURRENT_SESSIONS = "current";
    private static final String EXPIRED_SESSIONS = "expired";
    private static final String SESSION_PREFIX = "session_";
    private static final String SESSION_CREATION = "creation";
    private static final String SESSION_EXPIRATION = "expiration";
    private static final String SESSION_INSTANCE = "instance";


    final Jedis jedis;

    protected SessionManager(String hostname, int port, int dbNumber) {
        this.jedis = new Jedis(hostname, port);
        this.jedis.select(dbNumber);
    }

    public static SessionManager create() {
        final PropertyFileManager pfm = new PropertyFileManager();
        final RedisConnectionProperties rcp = pfm.getRedisConnectionDetails();
        return new SessionManager(rcp.getHostname(), rcp.getPort(), rcp.getDbNumber());
    }

    public void clear() {
        this.jedis.flushDB();
        this.jedis.set(NUMBER_SESSIONS, "0");
    }

    private String getInstanceId(String hostname, int port) {
        return INSTANCE_PREFIX + (hostname + ":" + port).hashCode();
    }

    /**
     * Registers a PacketTracer instance in the DB.
     * @param hostname
     * @param port
     */
    public void addAvailableInstance(String hostname, int port) {
        final String instanceId = getInstanceId(hostname, port);
        this.jedis.sadd(AVAILABLE_INSTANCES, instanceId);
        final Map<String, String> instanceDetails = new HashMap<String, String>();
        instanceDetails.put(INSTANCE_HOSTNAME, hostname);
        instanceDetails.put(INSTANCE_PORT, String.valueOf(port));
        instanceDetails.put(INSTANCE_SESSION, UNASSIGNED_VALUE);
        this.jedis.hmset(instanceId, instanceDetails);
    }

    /**
     * @param instanceId
     *      The instance allocated for the new session.
     * @return The new session id.
     */
    private String createSessionManager(String instanceId) {
        final Long session = this.jedis.incr(NUMBER_SESSIONS);
        final String sessionId = SESSION_PREFIX + session;

        final long current = System.currentTimeMillis();
        final long expiration = System.currentTimeMillis() + (RESERVATION_TIME * 60000);
        this.jedis.hset(sessionId, SESSION_CREATION, String.valueOf(current));
        this.jedis.hset(sessionId, SESSION_EXPIRATION, String.valueOf(expiration));
        this.jedis.hset(sessionId, SESSION_INSTANCE, instanceId);

        this.jedis.sadd(CURRENT_SESSIONS, sessionId);
        return sessionId;
    }

    /**
     * Mark this session as expired.
     * @param sessionId
     */
    public void expireSession(String sessionId) {
        final Long remRet = this.jedis.srem(CURRENT_SESSIONS, sessionId);
        if (remRet==1) {
            this.jedis.sadd(EXPIRED_SESSIONS, sessionId);
            final String assignedInstanceId = jedis.hget(sessionId, SESSION_INSTANCE);
            // Mark it as unassigned only if the instance has not already been marked as unassigned.
            if (!assignedInstanceId.equals(UNASSIGNED_VALUE)) {
                this.jedis.hset(sessionId, SESSION_INSTANCE, UNASSIGNED_VALUE);

                // Checking that the instance has not already been assigned to a different session.
                // Otherwise, it would not make sense to mark it as unassigned.
                final String assignedSessionId = jedis.hget(assignedInstanceId, INSTANCE_SESSION);
                if (assignedSessionId.equals(sessionId)) {
                    this.jedis.hset(sessionId, INSTANCE_SESSION, UNASSIGNED_VALUE);
                }
            }
        }
    }

    /**
     * @param sessionId
     * @return Has the session expired? In other words, there is no instance assigned to this session anymore.
     */
    public boolean hasExpired(String sessionId) {
        long expiresAt = Long.valueOf(this.jedis.hget(sessionId, SESSION_EXPIRATION));
        return expiresAt < System.currentTimeMillis();
    }

    private void freeInstancesAssignedToExpiredSessions() {
        for(String sessionId: this.jedis.smembers(CURRENT_SESSIONS)) {
            if(hasExpired(sessionId))
                expireSession(sessionId);
        }
    }

    /**
     * Assigns an available PT instance to a new session.
     * @return The new session id.
     */
    public String createSession(String sessionId) throws BusyInstancesException {
        freeInstancesAssignedToExpiredSessions();
        final String instanceId = this.jedis.spop(AVAILABLE_INSTANCES);
        if (instanceId==null) throw new BusyInstancesException();
        return createSession(instanceId);
    }
}