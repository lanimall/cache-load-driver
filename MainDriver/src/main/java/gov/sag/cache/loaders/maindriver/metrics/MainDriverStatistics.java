package gov.sag.cache.loaders.maindriver.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;


public class MainDriverStatistics {
    private final MetricRegistry metrics = MetricsSingleton.instance.getOrCreateRegistry(MainDriverStatistics.class.getName());
    private final Timer readRequestPerSecond;
    private final Timer writeRequestPerSecond;
    private final Timer deleteRequestPerSecond;

    public MainDriverStatistics() {
        readRequestPerSecond = getMetrics().timer("Read-Requests / sec");
        writeRequestPerSecond = getMetrics().timer("Write-Requests / sec");
        deleteRequestPerSecond = getMetrics().timer("Delete-Requests / sec");
    }

    public MetricRegistry getMetrics() {
        return metrics;
    }

    public Timer getReadRequestPerSecond() {
        return readRequestPerSecond;
    }

    public Timer getWriteRequestPerSecond() {
        return writeRequestPerSecond;
    }

    public Timer getDeleteRequestPerSecond() {
        return deleteRequestPerSecond;
    }

    @Override
    public String toString() {
        return "TestStatistic{" +
                "metrics=" + metrics +
                ", readRequestPerSecond=" + readRequestPerSecond +
                ", writeRequestPerSecond=" + writeRequestPerSecond +
                ", deleteRequestPerSecond=" + deleteRequestPerSecond +
                '}';
    }
}