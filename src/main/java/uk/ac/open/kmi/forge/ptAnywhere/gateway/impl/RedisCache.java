package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.Cache;


/**
 * Cache implementation which uses Redis.
 *
 * Persistent within requests, but each piece of information expires after a while.
 *
 * Using this cache you can reduce the amount of requests sent to a Packet Tracer instance.
 * However, be aware that it might not save time.
 */
public class RedisCache  implements Cache {

    private final String NETWORK_PREFIX = "network:";
    private final String DEVICE_INFIX = ":deviceId:";
    private final String NAME_INFIX = ":deviceName:";

    final JedisPool cache;

    public RedisCache(JedisPool cache) {
        this.cache = cache;
    }

    private String getKeyForDeviceId(String networkId, String id) {
        // We could probably safely ignore the network id as the device ID should be pretty unique,
        // but just in case.
        return NETWORK_PREFIX + networkId + DEVICE_INFIX + id;
    }

    private String getKeyForNameSet(String networkId, String name) {
        return NETWORK_PREFIX + networkId + NAME_INFIX + name;
    }


    @Override
    public void add(String networkId, String identifier, String name) {
        final int expireAfter = 60*15;  // TODO By now, expire in 15 minutes;
        final String idKey = getKeyForDeviceId(networkId, identifier);
        final String nameKey = getKeyForNameSet(networkId, name);

        try (Jedis jedis = this.cache.getResource()) {
            final boolean makeItExpire = jedis.exists(nameKey);

            final Transaction t = jedis.multi();
            t.set(idKey, name);
            t.expire(idKey, expireAfter);
            t.sadd(nameKey, identifier);
            // Small race condition, but it is not a big deal to make it expire a little bit later.
            if (makeItExpire) {  // Only the first time a set is added
                t.expire(nameKey, expireAfter);  // TODO Expire in 15 minutes
            }
            t.exec();
        }
    }

    @Override
    public void remove(String networkId, String identifier) {
        final String idKey = getKeyForDeviceId(networkId, identifier);
        try (Jedis jedis = this.cache.getResource()) {
            final String name = jedis.get(idKey);

            // Race condition with previous get
            final Transaction t = jedis.multi();
            t.del(idKey);
            t.srem(getKeyForNameSet(networkId, name), identifier);
            t.exec();
        }
    }

    @Override
    public void removeAll(String networkId) {
        try (Jedis jedis = this.cache.getResource()) {
            final Set<String> keys = jedis.keys(NETWORK_PREFIX + "*");
            // FIXME race condition here
            // If there is any key added in between this two sentences will not be properly deleted.
            // However, this (these) key(s) will expire anyway.
            final Transaction t = jedis.multi();
            for (String key: keys) {
                t.del(key);
            }
            t.exec();
        }
    }

    @Override
    public String getName(String networkId, String identifier) {
        try (Jedis jedis = this.cache.getResource()) {
            final String name = jedis.get(getKeyForDeviceId(networkId, identifier));

            final Set<String> names = jedis.smembers(getKeyForNameSet(networkId, name));
            return (names.size()==1)? name: null;
        }
    }
}