package gov.sag.cache.loaders.providers.ehcache3;

import gov.sag.cache.loaders.maindriver.cache.GenericCache;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.Status;

import java.io.IOException;

public class Ehcache3StringCache implements GenericCache<String, String> {

    // Create a cache manager
    private final CacheManager cacheManager;
    private final Cache<String, String> cache;
    private final String cacheName;

    public Ehcache3StringCache(final CacheManager cacheManager, final String cacheName) {
        this.cacheManager = cacheManager;
        this.cacheName = cacheName;

        if(null == cacheManager)
            throw new IllegalArgumentException("Cache manager cannot be null");

        if(Status.AVAILABLE != cacheManager.getStatus())
            throw new IllegalArgumentException("Cache manager not available");

        this.cache = cacheManager.getCache(cacheName, String.class, String.class);
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public long getSize() {
        return -1;
    }

    @Override
    public String get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, String value) {
        cache.put(key,value);
    }

    @Override
    public void putWithWriter(String key, String value) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void putIfAbsent(String key, String value) {
        cache.putIfAbsent(key, value);
    }

    @Override
    public void putWithLock(String key, String value, long lockTimeout) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getWithLock(String key, long lockTimeout) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }

    @Override
    public void batchPut(Runnable operation) {
        operation.run();
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
