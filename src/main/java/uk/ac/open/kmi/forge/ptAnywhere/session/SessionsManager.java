package uk.ac.open.kmi.forge.ptAnywhere.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.*;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.exceptions.NoPTInstanceAvailableException;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.ptAnywhere.properties.RedisConnectionProperties;
import uk.ac.open.kmi.forge.ptAnywhere.session.management.Instance;
import uk.ac.open.kmi.forge.ptAnywhere.session.management.InstanceResourceClient;
import uk.ac.open.kmi.forge.ptAnywhere.session.management.PTManagementClient;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Redis client to manage the mapping between web sessions and the
 * PacketTracer instances supporting them.
 */
public class SessionsManager {

    private static final Log LOGGER = LogFactory.getLog(SessionsManager.class);

    /**
     * Minutes that a reservation will last.
     */
    private static final int RESERVATION_TIME = 5;


    private static final String AVAILABLE_APIS = "apis";
    // TODO use subscriptions to ensure that after deleting a busy-instance-key it is inserted again in the list of available ones.
    private static final String INSTANCE_URL = "url";
    private static final String INSTANCE_HOSTNAME = "hostname";
    private static final String INSTANCE_PORT = "port";
    protected static int dbNumber;

    /**
     * List of IDs of session that ever existed
     */
    private static final String SESSION_PREFIX = "session:";
    private static final String URL_PREFIX = INSTANCE_URL + ":";

    // Pool usage recommended in the official documentation:
    //   "You can store the pool somewhere statically, it is thread-safe."
    protected static JedisPool pool;


    protected SessionsManager() {}

    private static void createPoolIfNeeded() {
        if (pool==null) {
            final PropertyFileManager pfm = new PropertyFileManager();
            final RedisConnectionProperties rcp = pfm.getRedisConnectionDetails();
            dbNumber = rcp.getDbNumber();
            // 2000 and null are the default values used in JedisPool...
            pool = new JedisPool(new JedisPoolConfig(), rcp.getHostname(), rcp.getPort(), 2000, null, dbNumber);
        }
    }

    public static SessionsManager create() {
        createPoolIfNeeded();
        return new SessionsManager();
    }

    /**
     * WARNING: Returns a runnable which calls to a Jedis blocking operation.
     */
    public static ExpirationSubscriber createExpirationSubscription() {
        createPoolIfNeeded();
        return new ExpirationSubscriberImpl(dbNumber);
    }

    public void clear() {
        try (Jedis jedis = pool.getResource()) {
            jedis.flushDB();
        }
    }

    /**
     * Registers a PacketTracer management API in the DB.
     * @param apiUrls
     */
    public void addManagementAPIs(String... apiUrls) {
        try (Jedis jedis = pool.getResource()) {
            // Is it better to set it in the config file? http://redis.io/commands/config-set
            jedis.configSet("notify-keyspace-events", "Ex");  // Activate notifications on expiration
            jedis.sadd(AVAILABLE_APIS, apiUrls);
        }
    }

    private String generateSessionId() {
        return Utils.toSimplifiedId(UUID.randomUUID());
    }

    private String toRedisSessionId(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    private String fromRedisSessionId(String redisSessionId) {
        return redisSessionId.substring(SESSION_PREFIX.length());
    }

    /**
     * @param instanceUrl
     *      The URL for managing the PT instance.
     * @param ptHost
     *      Hostname of the PT instance.
     * @param ptPort
     *      Port of the PT instance.
     * @return The new session id.
     */
    private String createSession(String instanceUrl, String ptHost, int ptPort) {
        final String sessionId  = generateSessionId();
        final String rSessionId = toRedisSessionId(sessionId);
        final int expirationAfter = RESERVATION_TIME * 60;

        try (Jedis jedis = pool.getResource()) {
            final Transaction t = jedis.multi();
            // Use hset if more details are needed
            t.hset(rSessionId, INSTANCE_URL, instanceUrl);
            t.hset(rSessionId, INSTANCE_HOSTNAME, ptHost);
            t.hset(rSessionId, INSTANCE_PORT, String.valueOf(ptPort));
            // We could also expire the last thing whenever the keyspace events work
            t.expire(rSessionId, expirationAfter);
            // Also stored in a separate key to ensure that we still have the URL after the key expires
            // (to be able to delete the Docker container in the expiration listener!).
            t.set(URL_PREFIX + rSessionId, instanceUrl);

            t.exec();

            return sessionId;
        }
    }

    /**
     * Assigns an available PT instance to a new session.
     * @return The new session id.
     */
    public String createSession() throws NoPTInstanceAvailableException {
        try (Jedis jedis = pool.getResource()) {
            for (String apiUrl : jedis.smembers(AVAILABLE_APIS)) {
                try {
                    final PTManagementClient cli = new PTManagementClient(apiUrl);
                    final Instance i = cli.createInstance();
                    return createSession(i.getUrl(), i.getPacketTracerHostname(), i.getPacketTracerPort());
                } catch (NoPTInstanceAvailableException e) {
                    // Let's try with the next API...
                }
            }
            throw new NoPTInstanceAvailableException();
        }
    }

    public Set<String> getCurrentSessions() {
        final Set<String> ret = new HashSet<String>();
        try (Jedis jedis = pool.getResource()) {
            for (String rSessionId : jedis.keys(SESSION_PREFIX + "*")) {
                ret.add(fromRedisSessionId(rSessionId));
            }
            return ret;
        }
    }

    protected  PTInstanceDetails getInstanceWithRSessionId(String rSessionId) {
        try (Jedis jedis = pool.getResource()) {
            final Map<String, String> details = jedis.hgetAll(rSessionId);
            if (details != null && details.containsKey(INSTANCE_URL) &&
                    details.containsKey(INSTANCE_HOSTNAME) && details.containsKey(INSTANCE_PORT)) {
                return new PTInstanceDetails(details.get(INSTANCE_URL),
                        details.get(INSTANCE_HOSTNAME),
                        Integer.valueOf(details.get(INSTANCE_PORT)));
            }
            return null;
        }
    }

    public PTInstanceDetails getInstance(String sessionId) {
        return getInstanceWithRSessionId(toRedisSessionId(sessionId));
    }

    public boolean doesExist(String sessionId) {
        final String rSessionId = toRedisSessionId(sessionId);
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(rSessionId);
        }
    }

    /**
     * Delete session from DB and marks the used instance as available.
     * @param sessionId
     */
    public void deleteSession(String sessionId) {
        final String rSessionId = toRedisSessionId(sessionId);
        deleteRSession(rSessionId);
    }

    protected void deleteRSession(String rSessionId) {
        try (Jedis jedis = pool.getResource()) {
            final String instanceUrl = jedis.get(URL_PREFIX + rSessionId);
            if (instanceUrl!=null) {
                final InstanceResourceClient cli = new InstanceResourceClient(instanceUrl);
                cli.delete();  // If it throws an exception the element is not deleted.

                // If everything went well...
                final Transaction t = jedis.multi();
                t.del(rSessionId); // If it has expired then no problem?
                t.del(URL_PREFIX + rSessionId);
                //t.set(rSessionId + "_DELETED", "true");  // For debuging with redis... 0:-)
                t.exec();

                LOGGER.debug("Expired instance removed for " + rSessionId + ".");
            }
        }
    }

    /* Methods to ease webapp management */
    public Set<PTInstanceDetails> getAllInstances() {
        final Set<PTInstanceDetails> ret = new HashSet<PTInstanceDetails>();
        try (Jedis jedis = pool.getResource()) {
            for (String rSessionId : jedis.keys(SESSION_PREFIX + "*")) {
                final PTInstanceDetails details = getInstanceWithRSessionId(rSessionId);
                if (details != null) ret.add(details);
            }
            return ret;
        }
    }
}


class ExpirationListener extends JedisPubSub {
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        // channel == "__keyevent@0__:expired" and message == "session:id"
        final SessionsManager sessionManager = SessionsManager.create();
        sessionManager.deleteRSession(message);
    }
}

class ExpirationSubscriberImpl implements ExpirationSubscriber {
    final ExpirationListener listener = new ExpirationListener();
    final int dbNumber;
    public ExpirationSubscriberImpl(int dbNumber) {
        this.dbNumber = dbNumber;
    }

    @Override
    public void run() {
        try (Jedis jedis = SessionsManager.pool.getResource()) {
            // Recommended readings:
            //   + http://redis.io/topics/notifications
            //   + https://github.com/xetorthio/jedis/wiki/AdvancedUsage
            jedis.psubscribe(this.listener, "__keyevent@" + this.dbNumber + "__:expired");
        }
    }

    @Override
    public void stop() {
        this.listener.unsubscribe();
    }
}