package gov.sag.cache.loaders.maindriver.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;


public class WorkerStatisticsController extends BaseStatisticsController {

    public WorkerStatisticsBuilder getBuilder(){
        return new WorkerStatisticsBuilder();
    }

    public class WorkerStatisticsBuilder {
        private String requestTimerName = null;
        private String requestWaitsTimerName = null;
        private String exceptionCounterName = null;

        public WorkerStatisticsBuilder addRequestTimer() {
            return addRequestTimer("requests");
        }

        public WorkerStatisticsBuilder addRequestTimerWithPrefix(String prefix) {
            return addRequestTimer(prefix, "requests");
        }

        public WorkerStatisticsBuilder addRequestTimer(String name, String... names) {
            this.requestTimerName = MetricRegistry.name(name, names);
            return this;
        }

        public WorkerStatisticsBuilder addRequestWaitsTimer() {
            return addRequestWaitsTimer("requests-waits");
        }

        public WorkerStatisticsBuilder addRequestWaitsTimerWithPrefix(String prefix) {
            return addRequestWaitsTimer(prefix, "requests-waits");
        }

        public WorkerStatisticsBuilder addRequestWaitsTimer(String name, String... names) {
            this.requestWaitsTimerName = MetricRegistry.name(name, names);
            return this;
        }

        public WorkerStatisticsBuilder addExceptionsCounter() {
            return addExceptionsCounter("exceptions");
        }

        public WorkerStatisticsBuilder addExceptionsCounterWithPrefix(String prefix) {
            return addExceptionsCounter(prefix, "exceptions");
        }

        public WorkerStatisticsBuilder addExceptionsCounter(String name, String... names) {
            this.exceptionCounterName = MetricRegistry.name(name, names);
            return this;
        }

        public WorkerStatistics build() {
            WorkerStatisticsImpl workerStatistics = new WorkerStatisticsImpl();

            if (null != requestTimerName && !"".equals(requestTimerName))
                workerStatistics.requestTimer = getRegistry().timer(requestTimerName);

            if (null != requestWaitsTimerName && !"".equals(requestWaitsTimerName))
                workerStatistics.requestWaitsTimer = getRegistry().timer(requestWaitsTimerName);

            if (null != exceptionCounterName && !"".equals(exceptionCounterName))
                workerStatistics.exceptionCounter = getRegistry().counter(exceptionCounterName);

            return workerStatistics;
        }
    }

    private class WorkerStatisticsImpl implements WorkerStatistics {
        private Timer requestTimer = null;
        private Timer requestWaitsTimer = null;
        private Counter exceptionCounter = null;

        @Override
        public Timer getRequestTimer() {
            return requestTimer;
        }

        @Override
        public Timer getRequestWaitsTimer() {
            return requestWaitsTimer;
        }

        @Override
        public Counter getExceptionsCounter() {
            return exceptionCounter;
        }

        @Override
        public String toString() {
            return "WorkerStatisticsImpl{" +
                    "requestTimer=" + ((null !=requestTimer)?requestTimer:"null") +
                    ", requestWaitsTimer=" + ((null !=requestWaitsTimer)?requestWaitsTimer:"null") +
                    ", exceptionCounter=" + ((null !=exceptionCounter)?exceptionCounter:"null") +
                    '}';
        }
    }
}