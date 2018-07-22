package gov.sag.cache.loaders.maindriver.metrics;

import com.codahale.metrics.*;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabien.sanglier on 7/25/17.
 */
public class MetricsSingleton {
    public static MetricsSingleton instance = new MetricsSingleton();

    private final ConcurrentHashMap<String, MetricRegistry> registries;
    private final ConcurrentHashMap<String, ScheduledReporter> reporters;

    private MetricsSingleton() {
        registries = new ConcurrentHashMap<>();
        reporters = new ConcurrentHashMap<>();
    }

    public MetricRegistry getOrCreateRegistry(String registryName){
        MetricRegistry old;
        old = registries.putIfAbsent(registryName, new MetricRegistry());
        if(null == old){
            old = registries.get(registryName);
        }
        return old;
    }

    public ScheduledReporter getOrCreateReporter(String registryName){
        MetricRegistry registry = getOrCreateRegistry(registryName);

        //ScheduledReporter reporter = JmxReporter.forRegistry(registry).build();
        //ScheduledReporter reporter = CsvReporter.forRegistry(registry).build();
        //ScheduledReporter reporter = Slf4jReporter.forRegistry(registry).build();
        ScheduledReporter reporter = ConsoleReporter.forRegistry(registry).build();
        ScheduledReporter old;
        old = reporters.putIfAbsent(registryName, reporter);
        if(null == old){
            old = reporters.get(registryName);
        }
        return old;
    }

    public void startReporterAll() {
        for(String key : registries.keySet()){
            startReporter(key);
        }
    }

    public void stopReporterAll() {
        for(String key : registries.keySet()){
            stopReporter(key);
        }
    }

    public void startReporter(String registryName) {
        ScheduledReporter reporter = getOrCreateReporter(registryName);
        reporter.start(10, TimeUnit.SECONDS);
    }

    public void stopReporter(String registryName) {
        ScheduledReporter reporter = getOrCreateReporter(registryName);
        reporter.stop();
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
