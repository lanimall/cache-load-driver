package gov.sag.cache.loaders.maindriver;

import com.lexicalscope.jewel.cli.CliFactory;
import gov.sag.cache.loaders.maindriver.cache.CacheProviderFactory;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheFactory;
import gov.sag.cache.loaders.maindriver.cache.impl.NoopCacheFactory;
import gov.sag.cache.loaders.maindriver.metrics.MetricsSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String CacheProviderFactory_PropertyName = "CacheProviderFactoryClass";
    private static final String cacheProviderFactoryClassName = System.getProperty(CacheProviderFactory_PropertyName);

    public static void main(String[] args) throws InterruptedException, IllegalAccessException, InstantiationException, ClassNotFoundException, MalformedURLException {
        ProgramOptions po = CliFactory.parseArguments(ProgramOptions.class, args);

        //get the right provider factory from system property
        String genericCacheFactoryClassname = cacheProviderFactoryClassName;
        if(null == genericCacheFactoryClassname)
            genericCacheFactoryClassname = NoopCacheFactory.class.getName();

        CacheProviderFactory cacheProviderFactory = new CacheProviderFactory();
        GenericCacheFactory cacheFactory = cacheProviderFactory.create(genericCacheFactoryClassname);

        logger.info("############### Loading tests with Provider Factory:" + cacheFactory.getClass().getName());

        List<TestCase> testCases = new ArrayList<TestCase>();

        if (po.isFillCacheFirst()) {
            testCases.add(new DataFillTestCase(cacheFactory, po));
        }

        if(po.getWriteThreadCount() > 0 || po.getReadThreadCount() > 0 || po.getDeleteThreadCount() > 0) {
            testCases.add(new DataSteadyOpsTestCase(cacheFactory, po));
        }

        logger.info("############### Starting tests");
        for(TestCase testCase : testCases) {
            try {
                testCase.init();
                testCase.runTest();
            } catch (Exception exc) {
                logger.error("unexpected error", exc);
            } finally {
                testCase.cleanup();
            }
        }
        logger.info("############### Ending tests");

        if(po.getSleepBeforeExit() > 0) {
            logger.info("############### Sleeping before exit");
            long sleepTime = Math.abs(po.getSleepBeforeExit()) * 1000;
            long sleptSoFar = 0;
            int sleepInterval = 5000;
            while (sleptSoFar < sleepTime) {
                Thread.sleep(sleepInterval);
                sleptSoFar += sleepInterval;

                logger.info(String.format("############### Slept so far: %d millis", sleptSoFar));
                logger.info("############### Printing statistics");
                logger.info(MetricsSingleton.instance.printRegistries());
            }
        }

        logger.info("############### Printing final statistics before exit");
        logger.info(MetricsSingleton.instance.printRegistries());

        System.exit(0);
    }
}