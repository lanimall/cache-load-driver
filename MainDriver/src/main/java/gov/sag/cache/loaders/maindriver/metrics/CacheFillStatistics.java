package gov.sag.cache.loaders.maindriver.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;


public class CacheFillStatistics {
    private final MetricRegistry metrics = MetricsSingleton.instance.getOrCreateRegistry(CacheFillStatistics.class.getName());
    private final Timer fillRequestPerSecond;

    public CacheFillStatistics() {
        fillRequestPerSecond = getMetrics().timer("Fill-Requests / sec");
    }

    public MetricRegistry getMetrics() {
        return metrics;
    }

    public Timer getFillRequestPerSecond() {
        return fillRequestPerSecond;
    }

    @Override
    public String toString() {
        return "TestStatistic{" +
                "metrics=" + metrics +
                ", fillRequestPerSecond=" + fillRequestPerSecond +
                '}';
    }
}