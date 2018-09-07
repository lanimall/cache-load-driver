package gov.sag.cache.loaders.maindriver.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;

/**
 * Created by fabien.sanglier on 9/7/18.
 */
public interface WorkerStatistics {
    Timer getRequestTimer();

    Timer getRequestWaitsTimer();

    Counter getExceptionsCounter();
}
