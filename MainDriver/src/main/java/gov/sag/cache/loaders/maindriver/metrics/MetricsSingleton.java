package gov.sag.cache.loaders.maindriver.metrics;

import com.codahale.metrics.*;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabien.sanglier on 7/25/17.
 */
public class MetricsSingleton {
    public static MetricsSingleton instance = new MetricsSingleton();

    private final ConcurrentHashMap<String, MetricRegistry> registries;
    private final ConcurrentHashMap<String, Reporter> reporters;
    private final ConcurrentHashMap<String, Reporter> startedReporters;

    private MetricsSingleton() {
        registries = new ConcurrentHashMap<>();
        reporters = new ConcurrentHashMap<>();
        startedReporters = new ConcurrentHashMap<>();
    }

    public MetricRegistry getOrCreateRegistry(String registryName){
        MetricRegistry old;
        if(null == (old = registries.get(registryName))) {
            old = registries.putIfAbsent(registryName, new MetricRegistry());
            if (null == old) {
                old = registries.get(registryName);
            }
        }
        return old;
    }

    public enum ReporterType {
        CONSOLE {
            @Override
            Reporter build(MetricRegistry registry) {
                return ConsoleReporter.forRegistry(registry).build();
            }

            @Override
            void start(Reporter reporter) {
                if(null != reporter)
                    ((ConsoleReporter)reporter).start(10, TimeUnit.SECONDS);
            }

            @Override
            void stop(Reporter reporter) {
                if(null != reporter)
                    ((ConsoleReporter)reporter).stop();
            }
        },JMX{
            @Override
            Reporter build(MetricRegistry registry) {
                return JmxReporter.forRegistry(registry).build();
            }
            @Override
            void start(Reporter reporter) {
                if(null != reporter)
                    ((JmxReporter)reporter).start();
            }

            @Override
            void stop(Reporter reporter) {
                if(null != reporter)
                    ((JmxReporter)reporter).stop();
            }
        },CSV{
            @Override
            Reporter build(MetricRegistry registry) {
                Path csvPath = FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"),"csvReporterOutput.txt");
                File csvFile = new File(csvPath.toUri());
                return CsvReporter.forRegistry(registry).build(csvFile);
            }
            @Override
            void start(Reporter reporter) {
                if(null != reporter)
                    ((CsvReporter)reporter).start(10, TimeUnit.SECONDS);
            }

            @Override
            void stop(Reporter reporter) {
                if(null != reporter)
                    ((CsvReporter)reporter).stop();
            }
        },SLF4J{
            @Override
            Reporter build(MetricRegistry registry) {
                return Slf4jReporter.forRegistry(registry).build();
            }
            @Override
            void start(Reporter reporter) {
                if(null != reporter)
                    ((Slf4jReporter)reporter).start(10, TimeUnit.SECONDS);
            }

            @Override
            void stop(Reporter reporter) {
                if(null != reporter)
                    ((Slf4jReporter)reporter).stop();
            }
        },NONE{
            @Override
            Reporter build(MetricRegistry registry) {
                return new Reporter() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                };
            }
            @Override
            void start(Reporter reporter) {
                //do nothing
                ;;
            }

            @Override
            void stop(Reporter reporter) {
                //do nothing
                ;;
            }
        };

        abstract Reporter build(MetricRegistry registry);
        abstract void start(Reporter reporter);
        abstract void stop(Reporter reporter);

        public static ReporterType valueOfIgnoreCase(String reporterTypeStr){
            if(null != reporterTypeStr && !"".equals(reporterTypeStr)) {
                if (ReporterType.CONSOLE.name().equalsIgnoreCase(reporterTypeStr))
                    return ReporterType.CONSOLE;
                else if (ReporterType.JMX.name().equalsIgnoreCase(reporterTypeStr))
                    return ReporterType.JMX;
                else if (ReporterType.CSV.name().equalsIgnoreCase(reporterTypeStr))
                    return ReporterType.CSV;
                else if (ReporterType.SLF4J.name().equalsIgnoreCase(reporterTypeStr))
                    return ReporterType.SLF4J;
                else if (ReporterType.NONE.name().equalsIgnoreCase(reporterTypeStr))
                    return ReporterType.NONE;
                else
                    throw new IllegalArgumentException("ReporterType [" + ((null != reporterTypeStr) ? reporterTypeStr : "null") + "] is not valid");
            } else {
                return ReporterType.NONE; // return the empty reporter if null / empty string is passed
            }
        }
    }

    public Reporter getOrCreateReporter(String registryName, ReporterType reporterType){
        Reporter old;
        if(null == (old = reporters.get(registryName))) {
            MetricRegistry registry = getOrCreateRegistry(registryName);
            old = reporters.putIfAbsent(registryName, reporterType.build(registry));
            if (null == old) {
                old = reporters.get(registryName);
            }
        }
        return old;
    }

    public void startAllReporters(ReporterType reporterType) {
        for(String key : registries.keySet()){
            startReporter(key, reporterType);
        }
    }

    public void stopAllReporters(ReporterType reporterType) {
        for(String key : registries.keySet()){
            stopReporter(key, reporterType);
        }
    }

    public Reporter startReporter(String registryName, ReporterType reporterType) {
        Reporter started;
        if(null == (started = startedReporters.get(registryName))) {
            started = startedReporters.putIfAbsent(registryName, getOrCreateReporter(registryName, reporterType)); //CAS
            if (null == started) { //here, only 1 thread should be able to enter in this...
                started = startedReporters.get(registryName);
                reporterType.start(started);
            }
        }
        return started;
    }

    public void stopReporter(String registryName, ReporterType reporterType) {
        Reporter started;
        if(null != (started = startedReporters.get(registryName))) {
            boolean removed = startedReporters.remove(registryName, started); //CAS
            if (removed) { //here, only 1 thread should be able to enter in this...
                reporterType.stop(started);
            }
        }
    }

    public String printRegistries() {
        StringBuilder sb = new StringBuilder();
        for(String key : registries.keySet()){
            sb.append("\n").append(String.format("Registry [%s]", key)).append("\n");
            sb.append(printRegistry(key)).append("\n\n");
        }
        return sb.toString();
    }

    public String printRegistry(String registryName) {
        StringBuffer sb = new StringBuffer();
        MetricRegistry registry = getOrCreateRegistry(registryName);
        for(Map.Entry<String, Meter> e : registry.getMeters().entrySet()){
            sb.append("Meter [").append(e.getKey()).append("] ===> ").append("\n");
            sb.append("{").append("\n");
            sb.append("Count=").append(e.getValue().getCount()).append("\n");
            sb.append("OneMinuteRate=").append(e.getValue().getOneMinuteRate()).append(" calls/second").append("\n");
            sb.append("FiveMinuteRate=").append(e.getValue().getFiveMinuteRate()).append(" calls/second").append("\n");
            sb.append("FifteenMinuteRate=").append(e.getValue().getFifteenMinuteRate()).append(" calls/second").append("\n");
            sb.append("MeanRate=").append(e.getValue().getMeanRate()).append(" calls/second").append("\n");
            sb.append("}").append("\n");
        }
        for(Map.Entry<String, Counter> e : registry.getCounters().entrySet()){
            sb.append("Counter [").append(e.getKey()).append("] ===> ").append("\n");
            sb.append("{").append("\n");
            sb.append("Count=").append(e.getValue().getCount()).append("\n");
            sb.append("}").append("\n");
        }
        for(Map.Entry<String, Timer> e : registry.getTimers().entrySet()){
            sb.append("Timer [").append(e.getKey()).append("] ===> ").append("\n");
            sb.append("{").append("\n");
            sb.append("Count=").append(e.getValue().getCount()).append("\n");
            sb.append("OneMinuteRate=").append(e.getValue().getOneMinuteRate()).append(" calls/second").append("\n");
            sb.append("FiveMinuteRate=").append(e.getValue().getFiveMinuteRate()).append(" calls/second").append("\n");
            sb.append("FifteenMinuteRate=").append(e.getValue().getFifteenMinuteRate()).append(" calls/second").append("\n");
            sb.append("MeanRate=").append(e.getValue().getMeanRate()).append(" calls/second").append("\n");

            Snapshot snapshot = e.getValue().getSnapshot();
            sb.append("Min=").append(snapshot.getMin()).append(" nanosecs").append("\n");
            sb.append("Max=").append(snapshot.getMax()).append(" nanosecs").append("\n");
            sb.append("Mean=").append(snapshot.getMean()).append(" nanosecs").append("\n");
            sb.append("StdDev=").append(snapshot.getStdDev()).append(" nanosecs").append("\n");
            sb.append("Median=").append(snapshot.getMedian()).append(" nanosecs").append("\n");
            sb.append("75%=").append(snapshot.get75thPercentile()).append(" nanosecs").append("\n");
            sb.append("95%=").append(snapshot.get95thPercentile()).append(" nanosecs").append("\n");
            sb.append("98%=").append(snapshot.get98thPercentile()).append(" nanosecs").append("\n");
            sb.append("99%=").append(snapshot.get99thPercentile()).append(" nanosecs").append("\n");
            sb.append("999%=").append(snapshot.get999thPercentile()).append(" nanosecs").append("\n");

            sb.append("}").append("\n");
        }
        return sb.toString();
    }
}
