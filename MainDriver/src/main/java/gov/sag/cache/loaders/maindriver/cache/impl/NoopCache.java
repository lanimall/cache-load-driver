package gov.sag.cache.loaders.maindriver.cache.impl;

import gov.sag.cache.loaders.maindriver.cache.GenericCache;

/**
 * Created by fabien.sanglier on 7/20/18.
 */
public class NoopCache<K, V> implements GenericCache<K, V> {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public void putWithWriter(K key, V value) {

    }

    @Override
    public void putIfAbsent(K key, V value) {

    }

    @Override
    public void putWithLock(K key, V value, long lockTimeout) {

    }

    @Override
    public V getWithLock(K key, long lockTimeout) {
        return null;
    }

    @Override
    public void delete(K key) {

    }

    @Override
    public boolean isBulkLoadAvailable() {
        return false;
    }

    @Override
    public void enableBulkLoad() {

    }

    @Override
    public void disableBulkLoad() {

    }

    @Override
    public void clear() {

    }
}
