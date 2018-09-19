package gov.sag.cache.loaders.maindriver;

import gov.sag.cache.loaders.commonutils.RandomUtil;
import gov.sag.cache.loaders.maindriver.cache.GenericCache;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheConfiguration;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheFactory;
import gov.sag.cache.loaders.maindriver.cache.impl.FileBasedCacheConfiguration;
import gov.sag.cache.loaders.maindriver.metrics.WorkerStatistics;
import gov.sag.cache.loaders.maindriver.metrics.WorkerStatisticsController;
import gov.sag.cache.loaders.maindriver.utils.ObjectSizeFetcherAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DataFillTestCase implements TestCase {
    private final Logger logger = LoggerFactory.getLogger(DataFillTestCase.class);

    private final GenericCacheFactory cacheFactory;
    private GenericCache cache;

    private final ProgramOptions options;

    private final List<PartitionedCacheFillerWorker> workerList;
    private final CountDownLatch stopLatch;

    //instantiate the main statistics controller
    WorkerStatisticsController workerStatisticsController = new WorkerStatisticsController();

    public DataFillTestCase(final GenericCacheFactory cacheFactory, ProgramOptions programOptions) {
        this.options = programOptions;
        this.cacheFactory = cacheFactory;
        this.stopLatch = new CountDownLatch(options.getFillCacheThreadCount());
        this.workerList = new ArrayList<PartitionedCacheFillerWorker>();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Stop Worker-Threads...");
                for (PartitionedCacheFillerWorker worker : workerList) {
                    worker.interrupt();
                }
                try {
                    // Make sure that all the threads have finished
                    if (!stopLatch.await(10, TimeUnit.SECONDS)) {
                        logger.warn("Not all threads are finished");
                    }
                    logger.info("All thread have finished");
                } catch (InterruptedException ex) {
                    logger.error("Error during shutdown", ex);
                }
            }
        });
    }

    @Override
    public void init() {
        //init the cache from configurations
        GenericCacheConfiguration genericCacheConfiguration = new FileBasedCacheConfiguration(null, options.getCache(), null);
        cacheFactory.init(genericCacheConfiguration);

        //get the cache instance
        this.cache = cacheFactory.getCache(options.getCache());
    }

    @Override
    public void cleanup() {
        //shutting down
        cacheFactory.shutdown();
        cache = null;
    }

    @Override
    public void runTest() throws InterruptedException {
        if(null == cache)
            throw new IllegalStateException("Cache may not be null...make sure to call init() first.");

        RandomUtil randomUtil = new RandomUtil(System.nanoTime());
        final String cacheEntryValuePayload = randomUtil.generateAlphaNumericRandom(options.getSize());
        final String sampleCacheEntryKey = String.valueOf(randomUtil.generateRandomLong(options.getEntryCount()));

        //calculate and print size of object for verification/troubleshooting purpose
        ObjectSizeFetcherAgent.printSize("Object cacheEntryValuePayload", cacheEntryValuePayload);
        ObjectSizeFetcherAgent.printSize("Object sampleCacheEntryKey", sampleCacheEntryKey);

        //start the reporter
        workerStatisticsController.startRegistryReporter();

        //perform pre-operations first
        if (options.isClearCacheFirst()) {
            logger.info("Clearing cache {}", cache.getName());
            cache.clear();
        }

        WorkerStatistics workerStatistics = workerStatisticsController.getBuilder()
                .addRequestTimerWithPrefix("cachefill")
                .addExceptionsCounterWithPrefix("cachefill")
                .build();

        logger.info("Adding {} entries in cache {} with {} concurrent threads", options.getEntryCount(), options.getCache(), options.getFillCacheThreadCount());
        final long partition = options.getEntryCount() / options.getFillCacheThreadCount();

        for (int i = 0; i < options.getFillCacheThreadCount(); i++) {
            workerList.add(
                    new PartitionedCacheFillerWorker(
                            new Integer(i),
                            partition,
                            stopLatch,
                            cache,
                            cacheEntryValuePayload,
                            workerStatistics
                    )
            );

            //thread sleep for a little bit to make sure the random seeds are different
            Thread.sleep(100);
        }

        try {
            if (!options.disableBulkLoadOnFill() && cache.isBulkLoadAvailable()) {
                logger.info("Enabling BulkLoading for cache {}", cache.getName());
                cache.enableBulkLoad();
            }

            //start the workers
            for (PartitionedCacheFillerWorker worker : workerList) {
                worker.start();
            }

            //wait that all operations are finished
            logger.info("Load in progress for cache {}", cache.getName());

            stopLatch.await();

            logger.info("Load done for cache {} - Final Size={}", cache.getName(), cache.getSize());
        } finally {
            if (!options.disableBulkLoadOnFill() && cache.isBulkLoadAvailable()) {
                logger.info("Disabling BulkLoading for cache {}", cache.getName());
                cache.disableBulkLoad();
            }
        }

        //stop reporter
        workerStatisticsController.stopRegistryReporter();
    }

    class PartitionedCacheFillerWorker extends Thread {
        private final int offset;
        private final long partition;
        private final CountDownLatch stopLatch;
        private final GenericCache<String, String> cache;
        private final String payload;
        private final WorkerStatistics cacheFillStatistics;
        private final RandomUtil randomUtil = new RandomUtil(System.nanoTime());

        private PartitionedCacheFillerWorker(final int offset, final long partition, final CountDownLatch stopLatch, final GenericCache<String, String> cache, final String payload, WorkerStatistics cacheFillStatistics) {
            this.offset = offset;
            this.partition = partition;
            this.stopLatch = stopLatch;
            this.cache = cache;
            this.payload = payload;
            this.cacheFillStatistics = cacheFillStatistics;
        }

        @Override
        public void run() {
            for(long j=offset*partition;j<(offset+1)*partition;j++){
                try {
                    long t1 = System.nanoTime();
                    cache.put(String.valueOf(j), randomUtil.generateRandomDouble() + payload);
                    long t1elapsedNanos = System.nanoTime() - t1;

                    cacheFillStatistics.addRequestTime(t1elapsedNanos, TimeUnit.NANOSECONDS);
                } catch (Exception exc){
                    cacheFillStatistics.addException();
                }
            }
            stopLatch.countDown();
        }
    }
}
