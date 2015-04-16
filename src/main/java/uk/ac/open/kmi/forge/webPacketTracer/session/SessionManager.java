package uk.ac.open.kmi.forge.webPacketTracer.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import uk.ac.open.kmi.forge.webPacketTracer.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.webPacketTracer.properties.RedisConnectionProperties;
import java.util.Set;


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
    // TODO use subscriptions to ensure that after deleting a busy-instance-key it is inserted again in the list of available ones.
    private static final String USED_INSTANCES = "used";
    private static final String INSTANCE_PREFIX = "instance:";
    private static final String INSTANCE_HOSTNAME = "hostname";
    private static final String INSTANCE_PORT = "port";
    private static final String INSTANCE_BUSY_POSTFIX= ":busy";

    /**
     * List of IDs of session that ever existed
     */
    private static final String SESSION_PREFIX = "session:";


    final Jedis jedis;

    protected SessionManager(String hostname, int port, int dbNumber) {
        this.jedis = new Jedis(hostname, port);
        this.jedis.select(dbNumber);
    }

    public static SessionManager create() {
        // FIXME should we cache these values to avoid reading the file over and over again???
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
        final Transaction t = this.jedis.multi();
        t.sadd(AVAILABLE_INSTANCES, instanceId);
        t.hset(instanceId, INSTANCE_HOSTNAME, hostname);
        t.hset(instanceId, INSTANCE_PORT, String.valueOf(port));
        t.exec();
    }

    /**
     * @param instanceId
     *      The instance allocated for the new session.
     * @return The new session id.
     */
    private String createSession(String instanceId) {
        final Long session = this.jedis.incr(NUMBER_SESSIONS);
        final String sessionId = SESSION_PREFIX + session;
        final String busyInstanceId = instanceId + INSTANCE_BUSY_POSTFIX;
        final int expirationAfter = RESERVATION_TIME * 60;

        final Transaction t = this.jedis.multi();
        // Use hset if more details are needed
        t.set(sessionId, instanceId);
        t.expire(sessionId, expirationAfter);
        t.set(busyInstanceId, sessionId);
        t.expire(busyInstanceId, expirationAfter);
        t.sadd(USED_INSTANCES, instanceId);
        t.exec();

        return sessionId;
    }

    private void freeInstancesAssignedToExpiredSessions() {
        for(String instanceId: this.jedis.smembers(USED_INSTANCES)) {
            if( !jedis.exists(instanceId + INSTANCE_BUSY_POSTFIX)) {
                final Transaction t = jedis.multi();
                t.srem(USED_INSTANCES, instanceId);
                t.sadd(AVAILABLE_INSTANCES, instanceId);
                t.exec();
            }
        }
    }

    /**
     * Assigns an available PT instance to a new session.
     * @return The new session id.
     */
    public String createSession() throws BusyInstancesException {
        freeInstancesAssignedToExpiredSessions();
        final String instanceId = this.jedis.spop(AVAILABLE_INSTANCES);
        if (instanceId==null) throw new BusyInstancesException();
        return createSession(instanceId);
    }

    public Set<String> getCurrentSessions() {
        return this.jedis.keys(SESSION_PREFIX + "*");
    }

    public boolean doesExist(String sessionId) {
        return this.jedis.exists(sessionId);
    }

    /**
     * Delete session from DB and marks the used instance as available.
     * @param sessionId
     */
    public void deleteSession(String sessionId) {
        final String assignedInstanceId = this.jedis.get(sessionId);
        final String assignedSessionId = this.jedis.get(assignedInstanceId + INSTANCE_BUSY_POSTFIX);

        final Transaction t = this.jedis.multi();
        if (assignedSessionId!=null)  // Delete only if it exists.
            t.del(sessionId);
        if (sessionId.equals(assignedSessionId)) { // Delete only is the session using the instance is the same.
            t.del(assignedInstanceId + INSTANCE_BUSY_POSTFIX);
            // Move to the available list.
            t.srem(USED_INSTANCES, assignedInstanceId);
            t.sadd(AVAILABLE_INSTANCES, assignedInstanceId);
        }
        t.exec();
    }
}