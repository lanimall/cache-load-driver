package gov.sag.cache.loaders.providers.ehcache2;

import gov.sag.cache.loaders.maindriver.cache.GenericCache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

public class Ehcache2Cache<K, V> implements GenericCache<K, V> {

    private final Ehcache cache;

    public Ehcache2Cache(final CacheManager cacheManager, final String cacheName) {
        if(!cacheManager.cacheExists(cacheName))
            throw new IllegalArgumentException("cache not found");

        this.cache = cacheManager.getEhcache(cacheName);
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public long getSize() {
        return cache.getSize();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key) {
        V obj = null;
        Element element = cache.get(key);
        if (element != null) {
            obj = (V) element.getObjectValue();
        }

        return obj;
    }


    @Override
    public void putWithWriter(K key, V value) {
        final Element ele = new Element(key, value);
        cache.putWithWriter(ele);
    }

    @Override
    public void put(K key, V value) {
        final Element ele = new Element(key, value);
        cache.put(ele);
    }

    @Override
    public void putIfAbsent(K key, V value) {
        final Element ele = new Element(key, value);
        cache.putIfAbsent(ele);
    }

    @Override
    public boolean isBulkLoadAvailable() {
        return true;
    }

    @Override
    public void enableBulkLoad() {
        cache.setNodeBulkLoadEnabled(true);
    }

    @Override
    public void disableBulkLoad() {
        cache.setNodeBulkLoadEnabled(false);
    }

    @Override
    public void putWithLock(K key, V value, final long lockTimeout) {
        final Element elementToPut = new Element(key, value);

        try {
            if (cache.tryWriteLockOnKey(elementToPut.getObjectKey(), lockTimeout)) {
                cache.put(elementToPut);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(cache.isWriteLockedByCurrentThread(elementToPut.getObjectKey()))
                cache.releaseWriteLockOnKey(elementToPut.getObjectKey());
        }
    }

    @Override
    public V getWithLock(K key, final long lockTimeout) {
        V obj = null;

        try {
            if (cache.tryReadLockOnKey(key, lockTimeout)) {
                Element element = cache.get(key);
                if (element != null) {
                    obj = (V) element.getObjectValue();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            cache.releaseReadLockOnKey(key);
        }
        return obj;
    }

    @Override
    public void delete(K key) {
        cache.remove(key);
    }


    @Override
    public void clear() {
        cache.removeAll();
    }
}
