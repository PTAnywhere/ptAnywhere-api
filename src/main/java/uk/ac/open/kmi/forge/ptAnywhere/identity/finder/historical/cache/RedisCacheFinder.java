package uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;
import uk.ac.open.kmi.forge.ptAnywhere.identity.factories.IdentityFactory;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.IdentityFinder;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.BySessionId;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;


/**
 * To avoid asking to the LRS continuously, we cache identities in Redis.
 */
public class RedisCacheFinder implements IdentityFinder<BySessionId> {

    final static int MAX_SIZE = 100;

    final static String LRU_SESSION = "previousSessions";
    final static String USER_PREFIX = "user:";

    final static String NAME = "name";
    final static String HOMEPAGE = "homepage";

    protected WeakReference<JedisPool> pool;

    public RedisCacheFinder(JedisPool pool) {
        this.pool = new WeakReference<JedisPool>(pool);
    }

    private JedisPool getPool() {
        return this.pool.get();
    }

    @Override
    public Identifiable findIdentity(BySessionId criteria) {
        return findIdentity(criteria, null);
    }

    @Override
    public Identifiable findIdentity(BySessionId criteria, IdentityFactory factory) {
        try (Jedis jedis = getPool().getResource()) {
            final Map<String, String> userHash = jedis.hgetAll(USER_PREFIX + criteria.getPreviousSessionId());
            Identifiable id = null;
            if (userHash.isEmpty()) {
                if (factory!=null) {
                    id = factory.create();
                    cacheIdentity(factory.getKey(), id);
                }
            } else {
                id = new RedisIdentity(userHash);
                // Update score
                jedis.zadd(LRU_SESSION, System.currentTimeMillis(), criteria.getPreviousSessionId());
            }
            return id;
        }
    }

    protected void removeLastAccessedIfLimitExceeded(Jedis jedis) {
        //jedis.zremrangeByRank(LRU_SESSION, MAX_SIZE, -1);
        final Set<String> sessionsToRemove = jedis.zrevrange(LRU_SESSION, MAX_SIZE, -1);
        final Transaction t = jedis.multi();
        for (String sessionId: sessionsToRemove) {
            uncache(sessionId, t);
        }
        t.exec();
    }

    public void cacheIdentity(String sessionId, Identifiable identity) {
        try (Jedis jedis = getPool().getResource()) {
            Transaction t = jedis.multi();
            t.zadd(LRU_SESSION, System.currentTimeMillis(), sessionId);
            t.hset(USER_PREFIX + sessionId, NAME, identity.getName());
            t.hset(USER_PREFIX + sessionId, HOMEPAGE, identity.getHomePage());
            t.exec();
            removeLastAccessedIfLimitExceeded(jedis);
        }
    }

    protected void uncache(String sessionId, Transaction transaction) {
        transaction.del(USER_PREFIX + sessionId);
        transaction.zrem(LRU_SESSION, sessionId);
    }

    public void uncache(String sessionId) {
        try (Jedis jedis = getPool().getResource()) {
            final Transaction t = jedis.multi();
            uncache(sessionId, t);
            t.exec();
        }
    }

    static class RedisIdentity implements Identifiable {
        final Map<String, String> redisHash;

        public RedisIdentity(Map<String, String> redisHash) {
            this.redisHash = redisHash;
        }

        @Override
        public String getName() {
            return this.redisHash.get(NAME);
        }

        @Override
        public String getHomePage() {
            return this.redisHash.get(HOMEPAGE);
        }
    }
}