package uk.ac.open.kmi.forge.ptAnywhere.identity.finder;

import redis.clients.jedis.JedisPool;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.HistoricalAnonymousFinder;
import uk.ac.open.kmi.forge.ptAnywhere.properties.InteractionRecordingProperties;


public class IdentityFinderFactory {

    public static IdentityFinder createHistoricalAnonymous(InteractionRecordingProperties lrsProperties, JedisPool pool) {
        return new HistoricalAnonymousFinder(lrsProperties, pool);
    }

}
