package gov.sag.cache.loaders.maindriver.cache;

import java.io.Closeable;


public interface GenericCache<K, V> {

    String getName();

    V get(K key);

    void put(K key, V value);

    void putWithWriter(K key, V value);

    void putIfAbsent(K key, V value);

    void putWithLock(K key, V value, long lockTimeout);

    V getWithLock(K key, long lockTimeout);

    void batchPut(Runnable operation);

    void delete(K key);

    long getSize();

    void clear();
}