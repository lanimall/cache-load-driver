package gov.sag.cache.loaders.maindriver.cache.impl;

import gov.sag.cache.loaders.maindriver.cache.GenericCacheConfiguration;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheFactory;
import gov.sag.cache.loaders.maindriver.cache.GenericCache;

public class NoopCacheFactory<K, V> implements GenericCacheFactory<K, V> {

    @Override
    public GenericCache<K, V> getCache(String cacheName) {
        return new NoopCache<>();
    }

    @Override
    public void init(GenericCacheConfiguration cacheConfiguration) {

    }

    @Override
    public void shutdown() {
        ;;
    }
}
