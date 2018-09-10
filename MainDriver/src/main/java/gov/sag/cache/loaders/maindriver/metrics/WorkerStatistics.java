package gov.sag.cache.loaders.maindriver.metrics;

import java.util.concurrent.TimeUnit;

/**
 * Created by fabien.sanglier on 9/7/18.
 */
public interface WorkerStatistics {
    void addRequestTime(long duration, TimeUnit unit);
    void addRequestWaitTime(long duration, TimeUnit unit);
    void addException();
}
