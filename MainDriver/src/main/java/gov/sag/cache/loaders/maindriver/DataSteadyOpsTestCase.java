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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DataSteadyOpsTestCase implements TestCase {
    private final Logger logger = LoggerFactory.getLogger(DataSteadyOpsTestCase.class);

    private static final boolean usePutWithWriter = Boolean.parseBoolean(System.getProperty("ehcache.put.usewriter", "false"));
    private static final boolean useWriteLocksOnPuts = Boolean.parseBoolean(System.getProperty("ehcache.put.writeLocks", "false"));
    private static final long writeLocksOnPutsTimout = Long.parseLong(System.getProperty("ehcache.put.writeLocks.timeout", "10000"));
    private static final boolean useReadLocksOnGets = Boolean.parseBoolean(System.getProperty("ehcache.get.readLocks", "false"));
    private static final long readLocksOnGetsTimout = Long.parseLong(System.getProperty("ehcache.get.readLocks.timeout", "10000"));

    private final GenericCacheFactory cacheFactory;
    private GenericCache cache;
    private final ProgramOptions options;

    private final List<CacheWorker> workerList;
    private final CountDownLatch stopLatch;

    //instantiate the main statistics controller
    WorkerStatisticsController workerStatisticsController = new WorkerStatisticsController();

    public DataSteadyOpsTestCase(final GenericCacheFactory cacheFactory, ProgramOptions programOptions) {
        this.options = programOptions;
        this.cacheFactory = cacheFactory;
        this.stopLatch = new CountDownLatch(options.getReadThreadCount() + options.getWriteThreadCount() + options.getDeleteThreadCount());
        this.workerList = new ArrayList<CacheWorker>();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Stop Worker-Threads...");

                for (CacheWorker worker : workerList) {
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
        cache = cacheFactory.getCache(options.getCache());
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

        // write worker
        if(options.getWriteThreadCount() > 0) {
            WorkerStatistics workerStatistics = workerStatisticsController.getBuilder()
                    .addRequestTimerWithPrefix("writes")
                    .addExceptionsCounterWithPrefix("writes")
                    .build();

            if(logger.isDebugEnabled())
                workerStatistics = workerStatisticsController.getBuilder()
                        .addRequestTimerWithPrefix("writes")
                        .addExceptionsCounterWithPrefix("writes")
                        .addRequestWaitsTimerWithPrefix("writes")
                        .build();

            logger.info("Starting {} write Workers with write locks settings = {}", options.getWriteThreadCount(), useWriteLocksOnPuts);
            logger.info("Rate limiting = {} requests/second", options.getWriteRequestsPerSecond());

            int maxRequestPerSecondPerThread = Math.round(options.getWriteRequestsPerSecond() / options.getWriteThreadCount());
            logger.info("Rate limiting per thread  = {} requests/second/thread", maxRequestPerSecondPerThread);
            for (int i = 0; i < options.getWriteThreadCount(); i++) {
                if (usePutWithWriter) {
                    workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, maxRequestPerSecondPerThread, workerStatistics, new Callable() {
                        RandomUtil rdm = new RandomUtil(System.nanoTime());

                        @Override
                        public Object call() throws Exception {
                            cache.putWithWriter(String.valueOf(rdm.generateRandomLong(options.getEntryCount())), rdm.generateRandomDouble() + cacheEntryValuePayload);
                            return null;
                        }
                    }));
                } else {
                    if (useWriteLocksOnPuts) {
                        workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, maxRequestPerSecondPerThread, workerStatistics, new Callable() {
                            RandomUtil rdm = new RandomUtil(System.nanoTime());

                            @Override
                            public Object call() throws Exception {
                                cache.putWithLock(String.valueOf(rdm.generateRandomLong(options.getEntryCount())), rdm.generateRandomDouble() + cacheEntryValuePayload, writeLocksOnPutsTimout);
                                return null;
                            }
                        }));
                    } else {
                        workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, maxRequestPerSecondPerThread, workerStatistics, new Callable() {
                            RandomUtil rdm = new RandomUtil(System.nanoTime());

                            @Override
                            public Object call() throws Exception {
                                cache.put(String.valueOf(rdm.generateRandomLong(options.getEntryCount())), rdm.generateRandomDouble() + cacheEntryValuePayload);
                                return null;
                            }
                        }));
                    }
                }

                //thread sleep for a little bit to make sure the random seeds are different
                Thread.sleep(100);
            }
        }

        // delete worker
        if(options.getDeleteThreadCount() > 0) {
            WorkerStatistics workerStatistics = workerStatisticsController.getBuilder()
                    .addRequestTimerWithPrefix("deletes")
                    .addExceptionsCounterWithPrefix("deletes")
                    .build();

            if(logger.isDebugEnabled())
                workerStatistics = workerStatisticsController.getBuilder()
                        .addRequestTimerWithPrefix("deletes")
                        .addExceptionsCounterWithPrefix("deletes")
                        .addRequestWaitsTimerWithPrefix("deletes")
                        .build();

            logger.info("Starting {} delete Workers", options.getDeleteThreadCount());
            logger.info("Rate limiting = {} requests/second", options.getDeleteRequestsPerSecond());

            int maxRequestPerSecondPerThread = Math.round(options.getDeleteRequestsPerSecond() / options.getDeleteThreadCount());
            logger.info("Rate limiting per thread  = {} requests/second/thread", maxRequestPerSecondPerThread);
            for (int i = 0; i < options.getDeleteThreadCount(); i++) {
                workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, maxRequestPerSecondPerThread, workerStatistics, new Callable() {
                    RandomUtil rdm = new RandomUtil(System.nanoTime());

                    @Override
                    public Object call() throws Exception {
                        cache.delete(String.valueOf(rdm.generateRandomLong(options.getEntryCount())));
                        return null;
                    }
                }));

                //thread sleep for a little bit to make sure the random seeds are different
                Thread.sleep(100);
            }
        }

        // read worker
        if(options.getReadThreadCount() > 0) {
            WorkerStatistics workerStatistics = workerStatisticsController.getBuilder()
                    .addRequestTimerWithPrefix("reads")
                    .addExceptionsCounterWithPrefix("reads")
                    .build();

            if(logger.isDebugEnabled())
                workerStatistics = workerStatisticsController.getBuilder()
                        .addRequestTimerWithPrefix("reads")
                        .addExceptionsCounterWithPrefix("reads")
                        .addRequestWaitsTimerWithPrefix("reads")
                        .build();

            logger.info("Starting {} read Workers with Read locks = {}", options.getReadThreadCount(), useReadLocksOnGets);
            logger.info("Rate limiting Total = {} requests/second", options.getReadRequestsPerSecond());

            int maxRequestPerSecondPerThread = Math.round(options.getReadRequestsPerSecond() / options.getReadThreadCount());
            logger.info("Rate limiting per thread  = {} requests/second/thread", maxRequestPerSecondPerThread);
            for (int i = 0; i < options.getReadThreadCount(); i++) {
                if (useReadLocksOnGets) {
                    workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, maxRequestPerSecondPerThread, workerStatistics, new Callable() {
                        RandomUtil rdm = new RandomUtil(System.nanoTime());

                        @Override
                        public Object call() throws Exception {
                            cache.getWithLock(String.valueOf(rdm.generateRandomLong(options.getEntryCount())), readLocksOnGetsTimout);
                            return null;
                        }
                    }));
                } else {
                    workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, maxRequestPerSecondPerThread, workerStatistics, new Callable() {
                        RandomUtil rdm = new RandomUtil(System.nanoTime());

                        @Override
                        public Object call() throws Exception {
                            cache.get(String.valueOf(rdm.generateRandomLong(options.getEntryCount())));
                            return null;
                        }
                    }));
                }

                //thread sleep for a little bit to make sure the random seeds are different
                Thread.sleep(100);
            }
        }

        //start the workers
        for (CacheWorker worker : workerList) {
            worker.start();
        }

        //wait that all operations are finished
        stopLatch.await();

        //stop reporter
        workerStatisticsController.stopRegistryReporter();
    }
}
