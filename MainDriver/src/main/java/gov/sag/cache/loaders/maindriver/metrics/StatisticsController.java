package gov.sag.cache.loaders.maindriver.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;

/**
 * Created by fabien.sanglier on 9/5/18.
 */
public interface StatisticsController {

    String getRegistryName();

    MetricRegistry getRegistry();

    MetricsSingleton.ReporterType getRegistryReporterType();

    Reporter startRegistryReporter();

    void stopRegistryReporter();
}
