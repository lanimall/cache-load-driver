package gov.sag.cache.loaders.providers.ehcache2.metrics;

import com.codahale.metrics.Counter;
import gov.sag.cache.loaders.maindriver.metrics.StatisticsController;

/**
 * Created by fabien.sanglier on 9/6/18.
 */
public interface EhcacheWriterStatistics {
    Counter getThrowAwayRequests();

    Counter getDeleteRequests();

    Counter getWriteRequests();
}
