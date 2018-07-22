package gov.sag.cache.loaders.providers.ehcache2.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import gov.sag.cache.loaders.maindriver.metrics.MetricsSingleton;

public class EhCacheWriterStatistics {
    private final MetricRegistry metrics = MetricsSingleton.instance.getOrCreateRegistry(EhCacheWriterStatistics.class.getName());

    private final Counter throwAwayRequests;
    private final Counter deleteRequests;
    private final Counter writeRequests;

    public EhCacheWriterStatistics(String cacheName) {
        throwAwayRequests = getMetrics().counter(MetricRegistry.name(EhCacheWriterStatistics.class, cacheName, "writer-throwaway-requests"));
        deleteRequests = getMetrics().counter(MetricRegistry.name(EhCacheWriterStatistics.class, cacheName, "writer-delete-requests"));
        writeRequests = getMetrics().counter(MetricRegistry.name(EhCacheWriterStatistics.class, cacheName, "writer-write-requests"));
    }

    public MetricRegistry getMetrics() {
        return metrics;
    }

    public Counter getThrowAwayRequests() {
        return throwAwayRequests;
    }

    public Counter getDeleteRequests() {
        return deleteRequests;
    }

    public Counter getWriteRequests() {
        return writeRequests;
    }

    @Override
    public String toString() {
        return "TestCacheWriterCounter{" +
                "metrics=" + metrics +
                ", throwAwayRequests=" + throwAwayRequests +
                ", deleteRequests=" + deleteRequests +
                ", writeRequests=" + writeRequests +
                '}';
    }
}