package gov.sag.cache.loaders.maindriver;

import com.codahale.metrics.Timer;
import gov.sag.cache.loaders.commonutils.RandomUtil;
import gov.sag.cache.loaders.maindriver.cache.GenericCache;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheConfiguration;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheFactory;
import gov.sag.cache.loaders.maindriver.cache.impl.FileBasedCacheConfiguration;
import gov.sag.cache.loaders.maindriver.metrics.CacheFillStatistics;
import gov.sag.cache.loaders.maindriver.metrics.MainDriverStatistics;
import gov.sag.cache.loaders.maindriver.metrics.MetricsSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CacheTestCase implements TestCase {
    private final Logger logger = LoggerFactory.getLogger(CacheTestCase.class);

    public static final int UNLIMITED_REQPERSECOND = 0;
    public static final int UNLIMITED_RUNTIME = 0;

    private static final boolean usePutWithWriter = Boolean.parseBoolean(System.getProperty("ehcache.put.usewriter", "false"));
    private static final boolean useWriteLocksOnPuts = Boolean.parseBoolean(System.getProperty("ehcache.put.writeLocks", "false"));
    private static final long writeLocksOnPutsTimout = Long.parseLong(System.getProperty("ehcache.put.writeLocks.timeout", "10000"));
    private static final boolean useReadLocksOnGets = Boolean.parseBoolean(System.getProperty("ehcache.get.readLocks", "false"));
    private static final long readLocksOnGetsTimout = Long.parseLong(System.getProperty("ehcache.get.readLocks.timeout", "10000"));

    private final GenericCacheFactory cacheFactory;
    private final ProgramOptions options;

    private final MainDriverStatistics driverStatistics = new MainDriverStatistics();
    private final CacheFillStatistics cacheFillStatistics = new CacheFillStatistics();

    private List<CacheWorker> workerList;
    private CountDownLatch stopLatch;

    public CacheTestCase(final GenericCacheFactory cacheFactory, ProgramOptions programOptions) {
        this.options = programOptions;
        this.cacheFactory = cacheFactory;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Stop Worker-Threads...");
                for (CacheWorker worker : getWorkerList()) {
                    worker.interrupt();
                }
                try {
                    // Make sure that all the threads have finished
                    if (!getStopLatch().await(10, TimeUnit.SECONDS)) {
                        logger.warn("Not all threads are finished");
                    }
                    logger.info("All thread have finished");

                    //shutting down
                    cacheFactory.shutdown();
                } catch (InterruptedException ex) {
                    logger.error("Error during shutdown", ex);
                }
            }
        });
    }

    public void clearCache(GenericCache cache) {
        logger.info("Clearing cache {}", cache.getName());
        cache.clear();
    }

    class partitionedCacheFiller implements Runnable {
        private final int offset;
        private final long partition;
        private final CountDownLatch waitLatch;
        private final GenericCache<String, String> cache;
        private final String payload;

        partitionedCacheFiller(final int offset, final long partition, final CountDownLatch waitLatch, final GenericCache<String, String> cache, final String payload) {
            this.offset = offset;
            this.partition = partition;
            this.waitLatch = waitLatch;
            this.cache = cache;
            this.payload = payload;
        }

        @Override
        public void run() {
            for(long j=offset*partition;j<(offset+1)*partition;j++){
                Timer.Context ctx = cacheFillStatistics.getFillRequestPerSecond().time();
                cache.put(String.valueOf(j), System.currentTimeMillis() + payload);
                ctx.stop();
            }
            waitLatch.countDown();
        }
    }

    private void fillCache(GenericCache cache, String payload) throws InterruptedException {
        logger.info("Adding {} entries in cache {} with {} concurrent threads", options.getEntryCount(),options.getCache(), options.getFillCacheThreadCount());
        final long partition = options.getEntryCount() / options.getFillCacheThreadCount();
        final CountDownLatch waitLatch = new CountDownLatch(options.getFillCacheThreadCount());
        for (int i = 0; i < options.getFillCacheThreadCount(); i++) {
            new Thread(new partitionedCacheFiller(new Integer(i),partition,waitLatch,cache,payload)).start();
        }

        logger.info("Bulk load in progress for cache {}", cache.getName());
        waitLatch.await();
        logger.info("Bulk load done for cache {} - Final Size={}", cache.getName(), cache.getSize());
    }

    @Override
    public void runTest() throws InterruptedException {
        //init the cache from configurations
        GenericCacheConfiguration genericCacheConfiguration = new FileBasedCacheConfiguration(null, options.getCache(), null);
        cacheFactory.init(genericCacheConfiguration);

        //get the cache
        final GenericCache cache = cacheFactory.getCache(options.getCache());

        final String payload = new String(new byte[options.getSize()]);

        //perform pre-operations first
        if (options.isClearCacheFirst()) {
            clearCache(cache);
        }

        //MetricsSingleton.instance.startRecording(cacheFillStatistics.getClass().getName());
        if (options.isFillCacheFirst()) {
            fillCache(cache,payload);
        }
        //MetricsSingleton.instance.stopRecording(cacheFillStatistics.getClass().getName());


        stopLatch = new CountDownLatch(options.getReadThreadCount() + options.getWriteThreadCount() + options.getDeleteThreadCount());

        workerList = new ArrayList<>();

        // write worker
        logger.info("Starting {} write Workers with write locks settings = {}", options.getWriteThreadCount(), useWriteLocksOnPuts);
        for (int i = 0; i < options.getWriteThreadCount(); i++) {
            if(usePutWithWriter){
                workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, options.getWriteRequestsPerSecond(), driverStatistics.getWriteRequestPerSecond(), new Callable<Void>() {
                    RandomUtil rdm = new RandomUtil(System.nanoTime());

                    @Override
                    public Void call() throws Exception {
                        cache.putWithWriter(String.valueOf(rdm.generateRandomLong(options.getEntryCount())), rdm.generateRandomDouble() + payload);
                        return null;
                    }
                }));
            } else {
                if (useWriteLocksOnPuts) {
                    workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, options.getWriteRequestsPerSecond(), driverStatistics.getWriteRequestPerSecond(), new Callable<Void>() {
                        RandomUtil rdm = new RandomUtil(System.nanoTime());

                        @Override
                        public Void call() throws Exception {
                            cache.putWithLock(String.valueOf(rdm.generateRandomLong(options.getEntryCount())), rdm.generateRandomDouble() + payload, writeLocksOnPutsTimout);
                            return null;
                        }
                    }));
                } else {
                    workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, options.getWriteRequestsPerSecond(), driverStatistics.getWriteRequestPerSecond(), new Callable<Void>() {
                        RandomUtil rdm = new RandomUtil(System.nanoTime());

                        @Override
                        public Void call() throws Exception {
                            cache.put(String.valueOf(rdm.generateRandomLong(options.getEntryCount())), rdm.generateRandomDouble() + payload);
                            return null;
                        }
                    }));
                }
            }

            //thread sleep for a little bit to make sure the random seeds are different
            Thread.sleep(100);
        }

        // delete worker
        logger.info("Starting {} delete Workers", options.getDeleteThreadCount());
        for (int i = 0; i < options.getDeleteThreadCount(); i++) {
            workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, options.getDeleteRequestsPerSecond(), driverStatistics.getDeleteRequestPerSecond(), new Callable<Void>() {
                RandomUtil rdm = new RandomUtil(System.nanoTime());

                @Override
                public Void call() throws Exception {
                    cache.delete(String.valueOf(rdm.generateRandomLong(options.getEntryCount())));
                    return null;
                }
            }));

            //thread sleep for a little bit to make sure the random seeds are different
            Thread.sleep(100);
        }

        // read worker
        logger.info("Starting {} read Workers with Read locks = {}", options.getReadThreadCount(), useReadLocksOnGets);
        for (int i = 0; i < options.getReadThreadCount(); i++) {
            if(useReadLocksOnGets){
                workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, options.getReadRequestsPerSecond(), driverStatistics.getReadRequestPerSecond(), new Callable<Void>() {
                    RandomUtil rdm = new RandomUtil(System.nanoTime());

                    @Override
                    public Void call() throws Exception {
                        cache.getWithLock(String.valueOf(rdm.generateRandomLong(options.getEntryCount())), readLocksOnGetsTimout);
                        return null;
                    }
                }));
            } else {
                workerList.add(new CacheWorker(stopLatch, options.getDuration() * 1000, options.getReadRequestsPerSecond(), driverStatistics.getReadRequestPerSecond(), new Callable<Void>() {
                    RandomUtil rdm = new RandomUtil(System.nanoTime());

                    @Override
                    public Void call() throws Exception {
                        cache.get(String.valueOf(rdm.generateRandomLong(options.getEntryCount())));
                        return null;
                    }
                }));
            }

            //thread sleep for a little bit to make sure the random seeds are different
            Thread.sleep(100);
        }


        //start recording
//        MetricsSingleton.instance.startRecording(MainDriverStatistics.class.getName());

        for (CacheWorker worker : workerList) {
            worker.start();
        }

        //wait that all operations are finished
        getStopLatch().await();

        //stop recording
//        MetricsSingleton.instance.stopRecording(MainDriverStatistics.class.getName());
    }

    private List<CacheWorker> getWorkerList() {
        return workerList;
    }


    private CountDownLatch getStopLatch() {
        return stopLatch;
    }
}
