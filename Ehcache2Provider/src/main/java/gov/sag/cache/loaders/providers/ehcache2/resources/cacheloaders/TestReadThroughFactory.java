package gov.sag.cache.loaders.providers.ehcache2.resources.cacheloaders;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Implement the CacheEntryFactory that allows the cache to provide
 * the read-through strategy
 */

public class TestReadThroughFactory implements CacheEntryFactory {

    //Return data from dataStore
    public Object createEntry(Object key) throws Exception {
        if (new Integer(key.toString()).intValue() % 2 == 0) {
            System.out.println("Creating an entry using ReadAhead...key is: " + key);
            return new Integer(key.toString());
        }
        System.out.println("Returning null for key: " + key + "...entry object is still returned for that call...but the entry itself is not being saved to the cache." +
                "Any subsequent call will be going again in that method for that key.");
        return null;
    }
}