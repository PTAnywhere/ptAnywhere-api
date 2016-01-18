package uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical;

import redis.clients.jedis.JedisPool;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;
import uk.ac.open.kmi.forge.ptAnywhere.identity.factories.IdentityFactory;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.IdentityFinder;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.cache.RedisCacheFinder;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.lrs.LRSFinder;
import uk.ac.open.kmi.forge.ptAnywhere.properties.InteractionRecordingProperties;


public class HistoricalAnonymousFinder implements IdentityFinder<BySessionId> {

    final RedisCacheFinder cacheFinder;
    final LRSFinder lrsFinder;

    // Method for testing
    protected HistoricalAnonymousFinder(RedisCacheFinder cacheFinder, LRSFinder lrsFinder) {
        this.cacheFinder = cacheFinder;
        this.lrsFinder = lrsFinder;
    }

    public HistoricalAnonymousFinder(InteractionRecordingProperties lrsProperties, JedisPool pool) {
        this.cacheFinder = new RedisCacheFinder(pool);
        this.lrsFinder = new LRSFinder(lrsProperties);
    }

    public Identifiable findIdentity(BySessionId criterion) {
        return findIdentity(criterion, null);
    }

    public Identifiable findIdentity(BySessionId criterion, IdentityFactory factory) {
        Identifiable ret = this.cacheFinder.findIdentity(criterion);
        // If not in the cache, look for it in LRS
        if (ret == null) {
            ret = this.lrsFinder.findIdentity(criterion);
            if (ret!=null) {
                // cache it to save time in the next request
                this.cacheFinder.cacheIdentity(criterion.getPreviousSessionId(), ret);
            }
        }
        // If not in cache and LRS, create a new anonymous user
        if (factory!=null) {
            if (ret == null) {
                ret = factory.create();
                // cache it to save time in the next request
                this.cacheFinder.cacheIdentity(factory.getKey(), ret);
            } else {
                // Update with a newer session id
                this.cacheFinder.cacheIdentity(factory.getKey(), ret);
                this.cacheFinder.uncache(criterion.getPreviousSessionId());
            }
        }
        return ret;
    }
}
