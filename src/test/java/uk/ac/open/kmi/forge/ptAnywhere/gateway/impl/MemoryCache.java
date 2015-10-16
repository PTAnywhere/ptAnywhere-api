package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import uk.ac.open.kmi.forge.ptAnywhere.gateway.Cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Memory based cache implementation for tests.
 */
public class MemoryCache implements Cache {

    final Map<String, Map<String, String>> cache = new HashMap<>();

    public Map<String, String> getNetworkCache(String id) {
        if (this.cache.containsKey(id))
            this.cache.put(id, new HashMap<String, String>());
        return this.cache.get(id);
    }

    public void add(String networkId, String identifier, String name) {
        getNetworkCache(networkId).put(identifier, name);
    }
    public void remove(String networkId, String identifier) {
        getNetworkCache(networkId).remove(identifier, identifier);
    }
    public void removeAll(String networkId) {
        getNetworkCache(networkId).clear();
    }
    public String getName(String networkId, String identifier) {
        if (!this.cache.containsKey(networkId)) return null;
        return getNetworkCache(networkId).get(identifier);
    }
}
