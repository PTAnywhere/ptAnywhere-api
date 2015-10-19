package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import uk.ac.open.kmi.forge.ptAnywhere.gateway.Cache;

import java.util.*;


/**
 * Memory based cache implementation for tests.
 *
 * Not persistent within requests, therefore it is not too useful.
 */
public class MemoryCache implements Cache {

    final Map<String, Map<String, String>> cache = new HashMap<>();
    final Map<String, Map<String, Set<String>>> repeated = new HashMap<>();

    protected Map<String, String> getNetworkCache(String id) {
        if (!this.cache.containsKey(id))
            this.cache.put(id, new HashMap<String, String>());
        return this.cache.get(id);
    }
    /**
     * @return Is the name assigned to another id?
     */
    protected boolean addRepeated(String networkId, String id, String name) {
        if (!this.repeated.containsKey(networkId))
            this.repeated.put(networkId, new HashMap<String, Set<String>>());
        if (!this.repeated.get(networkId).containsKey(name))
            this.repeated.get(networkId).put(name, new HashSet<String>());
        final Set<String> l = this.repeated.get(networkId).get(name);
        l.add(id);
        return l.size()>1;
    }
    @Override
    public void add(String networkId, String identifier, String name) {
        final boolean repeated = addRepeated(networkId, identifier, name);
        if (repeated) {
            getNetworkCache(identifier).remove(identifier);
        } else {
            getNetworkCache(networkId).put(identifier, name);
        }
    }
    @Override
    public void remove(String networkId, String identifier) {
        String name = null;
        if (this.cache.containsKey(networkId))
            name = this.cache.get(networkId).remove(identifier);
        if (this.repeated.containsKey(networkId))
            this.repeated.get(networkId).get(name).remove(identifier);
    }
    @Override
    public void removeAll(String networkId) {
        if (this.cache.containsKey(networkId))
            this.cache.get(networkId).clear();
        if (this.repeated.containsKey(networkId))
            this.repeated.get(networkId).clear();
    }
    @Override
    public String getName(String networkId, String identifier) {
        if (this.cache.containsKey(networkId))
            return this.cache.get(networkId).get(identifier);
        return null;
    }
}
