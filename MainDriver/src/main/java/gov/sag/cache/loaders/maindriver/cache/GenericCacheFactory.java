package gov.sag.cache.loaders.maindriver.cache;

/**
 * Created by fabien.sanglier on 7/20/18.
 */
public interface GenericCacheFactory<K, V> {
    GenericCache<K, V> getCache(final String cacheName);

    void init(GenericCacheConfiguration cacheConfiguration);

    void shutdown();
}
