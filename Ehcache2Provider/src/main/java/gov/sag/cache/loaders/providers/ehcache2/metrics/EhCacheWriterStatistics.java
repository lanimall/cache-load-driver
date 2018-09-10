package gov.sag.cache.loaders.providers.ehcache2.metrics;

import com.codahale.metrics.Counter;

/**
 * Created by fabien.sanglier on 9/6/18.
 */
public interface EhcacheWriterStatistics {
    void addThrowAwayRequest();
    void addDeleteRequest();
    void addWriteRequest();
}
