package gov.sag.cache.loaders.maindriver.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fabien.sanglier on 9/6/18.
 */
public abstract class BaseStatisticsController implements StatisticsController {
    private final Logger logger = LoggerFactory.getLogger(BaseStatisticsController.class);
    private static final String REPORTERTYPE_PROPNAME_PREFIX = "metrics.reporter.type.";

    public String getRegistryName(){
        return this.getClass().getName();
    }

    public String getRegistryReporterTypeName() {
        String reporterTypeStr = System.getProperty(REPORTERTYPE_PROPNAME_PREFIX + getRegistryName(), "");
        if(logger.isInfoEnabled())
            logger.info("ReporterType from system property {} is [{}]", REPORTERTYPE_PROPNAME_PREFIX + getRegistryName(), reporterTypeStr);

        return reporterTypeStr;
    }

    public MetricsSingleton.ReporterType getRegistryReporterType() {
        return MetricsSingleton.ReporterType.valueOfIgnoreCase(getRegistryReporterTypeName());
    }

    public MetricRegistry getRegistry() {
        return MetricsSingleton.instance.getOrCreateRegistry(getRegistryName());
    }


    public Reporter startRegistryReporter() {
        return MetricsSingleton.instance.startReporter(getRegistryName(), getRegistryReporterType());
    }

    public void stopRegistryReporter() {
        MetricsSingleton.instance.stopReporter(getRegistryName(), getRegistryReporterType());
    }
}
