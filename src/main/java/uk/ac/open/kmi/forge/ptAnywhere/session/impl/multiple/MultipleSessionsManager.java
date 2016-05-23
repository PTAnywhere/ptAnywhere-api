package uk.ac.open.kmi.forge.ptAnywhere.session.impl.multiple;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.client.Client;
import javax.ws.rs.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.NoPTInstanceAvailableException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.UnresolvableFileUrlException;
import uk.ac.open.kmi.forge.ptAnywhere.session.FileLoadingTask;
import uk.ac.open.kmi.forge.ptAnywhere.session.PTInstanceDetails;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.management.Allocation;
import uk.ac.open.kmi.forge.ptAnywhere.session.management.AllocationResourceClient;
import uk.ac.open.kmi.forge.ptAnywhere.session.management.PTManagementClient;


/**
 * Redis client to manage the mapping between web sessions and the
 * PacketTracer instances supporting them.
 */
public class MultipleSessionsManager implements SessionsManager {

    protected static final Log LOGGER = LogFactory.getLog(MultipleSessionsManager.class);

    protected final JedisPool pool;
    protected final int dbNumber;
    protected final int maximumLength;
    private final Client httpClient;


    protected MultipleSessionsManager(JedisPool pool, int dbNumber, int maximumLength, Client client) {
        this.pool = pool;
        this.dbNumber = dbNumber;
        this.maximumLength = maximumLength;
        this.httpClient = client;
    }

    @Override
    public void clear() {
        try (Jedis jedis = this.pool.getResource()) {
            // Make sure that no unfinished sessions are left behind!
            final Set<PTInstanceDetails> unfinished = getAllInstances();
            for (PTInstanceDetails instance: unfinished) {
                try {
                    final AllocationResourceClient cli = new AllocationResourceClient(instance.getUrl(), this.httpClient);
                    cli.delete();  // If it throws an exception the element is not deleted.
                } catch(NotFoundException e) {
                    LOGGER.error("The instance " + instance.getUrl() +  " could not be removed (maybe another thread already delete it?).");
                }
            }
            jedis.flushDB();
        }
    }

    /**
     * Registers a PacketTracer management API in the DB.
     * @param apiUrls
     */
    @Override
    public void addManagementAPIs(String... apiUrls) {
        try (Jedis jedis = this.pool.getResource()) {
            // Is it better to set it in the config file? http://redis.io/commands/config-set
            jedis.configSet("notify-keyspace-events", "Ex");  // Activate notifications on expiration
            jedis.sadd(RedisKeys.AVAILABLE_APIS, apiUrls);
        }
    }

    private String generateSessionId() {
        return Utils.toSimplifiedId(UUID.randomUUID());
    }

    private String toRedisSessionId(String sessionId) {
        return RedisKeys.SESSION_PREFIX + sessionId;
    }

    private String fromRedisSessionId(String redisSessionId) {
        return redisSessionId.substring(RedisKeys.SESSION_PREFIX.length());
    }

    /**
     * @param inputFilename
     *      The local path (relative to the containers) of the file to be used as a base.
     * @param instanceUrl
     *      The URL for managing the PT instance.
     * @param ptHost
     *      Hostname of the PT instance.
     * @param ptPort
     *      Port of the PT instance.
     * @return The new session id.
     */
    private String createSession(String instanceUrl, String ptHost, int ptPort, String inputFilename, int maximumReservationTime) {
        final String sessionId  = generateSessionId();
        final String rSessionId = toRedisSessionId(sessionId);
        final int expirationAfter = maximumReservationTime * 60;

        try (Jedis jedis = this.pool.getResource()) {
            final Transaction t = jedis.multi();
            // Use hset if more details are needed
            t.hset(rSessionId, RedisKeys.INSTANCE_URL, instanceUrl);
            t.hset(rSessionId, RedisKeys.INSTANCE_HOSTNAME, ptHost);
            t.hset(rSessionId, RedisKeys.INSTANCE_PORT, String.valueOf(ptPort));
            if (inputFilename!=null)
                t.hset(rSessionId, RedisKeys.INSTANCE_INPUT_FILE, inputFilename);
            // We could also expire the last thing whenever the keyspace events work
            t.expire(rSessionId, expirationAfter);
            // Also stored in a separate key to ensure that we still have the URL after the key expires
            // (to be able to delete the Docker container in the expiration listener!).
            t.set(RedisKeys.URL_PREFIX + rSessionId, instanceUrl);

            t.exec();

            return sessionId;
        }
    }

    /**
     * Assigns an available PT instance to a new session.
     * @param inputFileUrl
     * @param maximumLength
     * @return The new session id.
     */
    private String createSession(String inputFileUrl, int maximumLength) throws NoPTInstanceAvailableException {
        try (Jedis jedis = this.pool.getResource()) {
            for (String apiUrl : jedis.smembers(RedisKeys.AVAILABLE_APIS)) {
                try {
                    LOGGER.info("Attempting session creation in API: " + apiUrl);
                    final PTManagementClient cli = new PTManagementClient(apiUrl, this.httpClient);
                    final Allocation al = cli.createInstance();
                    try {

                        // We cache the file in that API and store the cached file local path instead of its URL.
                        // Pros: 1) we avoid storing the API associated to the instance to cache it later
                        //       2) we can directly store the local file path instead to the URL
                        // cons: 1) The files are not cached close to when they are opened.
                        //          In the meantime (worse case scenario: matter of seconds) they could be destroyed.
                        final String filename = cli.getCachedFile(inputFileUrl).getFilename();
                        return createSession(al.getUrl(), al.getPacketTracerHostname(), al.getPacketTracerPort(), filename, maximumLength);
                    } catch (UnresolvableFileUrlException ufue) {
                        // The init file could not be cached, so let's unallocate the instance as it won't be used.
                        final AllocationResourceClient arc = new AllocationResourceClient(al.getUrl(), this.httpClient);
                        arc.delete();
                        throw ufue;
                    }
                } catch (NoPTInstanceAvailableException e) {
                    // Let's try with the next API...
                    LOGGER.error("API not available: " + apiUrl);
                    LOGGER.error(e.getMessage());
                }
            }
            throw new NoPTInstanceAvailableException();
        }
    }

    /**
     * Assigns an available PT instance to a new session.
     * @param inputFileUrl
     * @return The new session id.
     */
    @Override
    public String createSession(String inputFileUrl) throws NoPTInstanceAvailableException {
        return createSession(inputFileUrl, this.maximumLength);
    }

    @Override
    public Set<String> getCurrentSessions() {
        final Set<String> ret = new HashSet<String>();
        try (Jedis jedis = this.pool.getResource()) {
            for (String rSessionId : jedis.keys(RedisKeys.SESSION_PREFIX + "*")) {
                ret.add(fromRedisSessionId(rSessionId));
            }
            return ret;
        }
    }

    class FileLoadingTaskImpl implements FileLoadingTask {

        final String inputFilePath;
        final String rSessionId;

        FileLoadingTaskImpl(String inputFilePath, String rSessionId) {
            this.inputFilePath = inputFilePath;
            this.rSessionId = rSessionId;
        }

        public String getInputFilePath() {
            return inputFilePath;
        }

        public void markAsLoaded() {
            try (Jedis jedis = pool.getResource()) {
                jedis.hdel(rSessionId, RedisKeys.INSTANCE_INPUT_FILE);
            }
        }
    }

    protected PTInstanceDetails getInstanceWithRSessionId(String rSessionId) {
        try (Jedis jedis = this.pool.getResource()) {
            final Map<String, String> details = jedis.hgetAll(rSessionId);
            if (details != null && details.containsKey(RedisKeys.INSTANCE_URL) &&
                    details.containsKey(RedisKeys.INSTANCE_HOSTNAME) &&
                    details.containsKey(RedisKeys.INSTANCE_PORT)) {
                final String inputFileUrl = details.get(RedisKeys.INSTANCE_INPUT_FILE);
                return new PTInstanceDetails(details.get(RedisKeys.INSTANCE_URL),
                        details.get(RedisKeys.INSTANCE_HOSTNAME),
                        Integer.valueOf(details.get(RedisKeys.INSTANCE_PORT)),
                        (inputFileUrl==null)? null: new FileLoadingTaskImpl(inputFileUrl, rSessionId) );
            }
            return null;
        }
    }

    @Override
    public PTInstanceDetails getInstance(String sessionId) {
        return getInstanceWithRSessionId(toRedisSessionId(sessionId));
    }

    @Override
    public boolean doesExist(String sessionId) {
        final String rSessionId = toRedisSessionId(sessionId);
        try (Jedis jedis = this.pool.getResource()) {
            return jedis.exists(rSessionId);
        }
    }

    /**
     * Delete session from DB and marks the used instance as available.
     * @param sessionId
     */
    @Override
    public void deleteSession(String sessionId) {
        final String rSessionId = toRedisSessionId(sessionId);
        final SessionRemovalManager srm = SessionRemovalManager.create(this.pool, this.httpClient);
        srm.deleteRSession(rSessionId);
    }

    /* Methods to ease webapp management */
    @Override
    public Set<PTInstanceDetails> getAllInstances() {
        final Set<PTInstanceDetails> ret = new HashSet<PTInstanceDetails>();
        try (Jedis jedis = this.pool.getResource()) {
            for (String rSessionId : jedis.keys(RedisKeys.SESSION_PREFIX + "*")) {
                final PTInstanceDetails details = getInstanceWithRSessionId(rSessionId);
                if (details != null) ret.add(details);
            }
            return ret;
        }
    }
}