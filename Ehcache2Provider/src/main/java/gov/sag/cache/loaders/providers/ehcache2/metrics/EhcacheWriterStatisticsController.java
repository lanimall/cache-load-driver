package gov.sag.cache.loaders.providers.ehcache2.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import gov.sag.cache.loaders.maindriver.metrics.BaseStatisticsController;
import gov.sag.cache.loaders.maindriver.metrics.WorkerStatistics;

public class EhcacheWriterStatisticsController extends BaseStatisticsController {

    public EhcacheWriterStatisticsBuilder getBuilder(){
        return new EhcacheWriterStatisticsBuilder();
    }

    public class EhcacheWriterStatisticsBuilder {
        private String throwAwayRequestsName = null;
        private String deleteRequestsName = null;
        private String writeRequestsName = null;

        public EhcacheWriterStatisticsBuilder addThrowAwayRequestsCounter() {
            return addThrowAwayRequestsCounter("throwaway-requests");
        }

        public EhcacheWriterStatisticsBuilder addThrowAwayRequestsCounterWithPrefix(String prefix) {
            return addThrowAwayRequestsCounter(prefix, "throwaway-requests");
        }

        public EhcacheWriterStatisticsBuilder addThrowAwayRequestsCounter(String name, String... names) {
            throwAwayRequestsName = MetricRegistry.name(name, names);
            return this;
        }

        public EhcacheWriterStatisticsBuilder addDeleteRequestsCounter() {
            return addDeleteRequestsCounter("delete-requests");
        }

        public EhcacheWriterStatisticsBuilder addDeleteRequestsCounterWithPrefix(String prefix) {
            return addDeleteRequestsCounter(prefix, "delete-requests");
        }

        public EhcacheWriterStatisticsBuilder addDeleteRequestsCounter(String name, String... names) {
            deleteRequestsName = MetricRegistry.name(name, names);
            return this;
        }

        public EhcacheWriterStatisticsBuilder addWriteRequestsCounter() {
            return addWriteRequestsCounter("write-requests");
        }

        public EhcacheWriterStatisticsBuilder addWriteRequestsCounterWithPrefix(String prefix) {
            return addWriteRequestsCounter(prefix, "write-requests");
        }

        public EhcacheWriterStatisticsBuilder addWriteRequestsCounter(String name, String... names) {
            writeRequestsName = MetricRegistry.name(name, names);
            return this;
        }

        public EhcacheWriterStatistics build() {
            EhcacheWriterStatisticsImpl statistics = new EhcacheWriterStatisticsImpl();

            if (null != throwAwayRequestsName && !"".equals(throwAwayRequestsName))
                statistics.throwAwayRequests = getRegistry().counter(throwAwayRequestsName);

            if (null != deleteRequestsName && !"".equals(deleteRequestsName))
                statistics.deleteRequests = getRegistry().counter(deleteRequestsName);

            if (null != writeRequestsName && !"".equals(writeRequestsName))
                statistics.writeRequests = getRegistry().counter(writeRequestsName);

            return statistics;
        }
    }

    private class EhcacheWriterStatisticsImpl implements EhcacheWriterStatistics {
        private Counter throwAwayRequests = null;
        private Counter deleteRequests = null;
        private Counter writeRequests = null;

        @Override
        public Counter getThrowAwayRequests() {
            return throwAwayRequests;
        }

        @Override
        public Counter getDeleteRequests() {
            return deleteRequests;
        }

        @Override
        public Counter getWriteRequests() {
            return writeRequests;
        }
    }
}