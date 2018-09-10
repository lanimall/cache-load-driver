package gov.sag.cache.loaders.maindriver.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.concurrent.TimeUnit;


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
            RequestStatsImpl workerStatistics;

            //this one is more for debug
            if (null != requestWaitsTimerName && !"".equals(requestWaitsTimerName)) {
                workerStatistics = new RequestStatsImpl();
                workerStatistics.requestWaitsTimer = getRegistry().timer(requestWaitsTimerName);
            } else {
                workerStatistics = new RequestStatsNoWaitImpl();
            }

            if (null != requestTimerName && !"".equals(requestTimerName)) {
                workerStatistics.requestTimer = getRegistry().timer(requestTimerName);
            }

            if (null != exceptionCounterName && !"".equals(exceptionCounterName)) {
                workerStatistics.exceptionCounter = getRegistry().counter(exceptionCounterName);
            }

            return workerStatistics;
        }
    }

    private class RequestStatsImpl implements WorkerStatistics {
        protected Timer requestTimer = null;
        protected Counter exceptionCounter = null;
        protected Timer requestWaitsTimer = null;

        @Override
        public void addRequestTime(long duration, TimeUnit unit) {
            requestTimer.update(duration,unit);
        }

        @Override
        public void addRequestWaitTime(long duration, TimeUnit unit) {
            requestWaitsTimer.update(duration,unit);
        }

        @Override
        public void addException() {
            exceptionCounter.inc();
        }

        @Override
        public String toString() {
            return "RequestStatsImpl{" +
                    "requestTimer=" + ((null != requestTimer)?requestTimer.toString():"null") +
                    ", exceptionCounter=" + ((null != exceptionCounter)?exceptionCounter.toString():"null") +
                    ", requestWaitsTimer=" + ((null != requestWaitsTimer)?requestWaitsTimer.toString():"null") +
                    '}';
        }
    }

    private class RequestStatsNoWaitImpl extends RequestStatsImpl {
        @Override
        public void addRequestWaitTime(long duration, TimeUnit unit) {
            ;; //do nothing
        }
    }
}