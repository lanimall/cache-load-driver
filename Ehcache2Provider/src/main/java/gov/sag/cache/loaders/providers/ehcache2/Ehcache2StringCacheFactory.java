package gov.sag.cache.loaders.providers.ehcache2;

import gov.sag.cache.loaders.maindriver.cache.GenericCache;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheConfiguration;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheFactory;
import gov.sag.cache.loaders.providers.ehcache2.resources.CacheManagerUtils;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

public class Ehcache2StringCacheFactory implements GenericCacheFactory<String, String> {
    private final Logger logger = LoggerFactory.getLogger(Ehcache2StringCacheFactory.class);

    private CacheManager cacheManager;

    @Override
    public GenericCache<String, String> getCache(String cacheName) {
        if(null == cacheManager)
            throw new IllegalArgumentException("Cache manager cannot be null...make sure you called init() first, or Check logs for errors");

        return new Ehcache2Cache<>(cacheManager, cacheName);
    }

    @Override
    public void init(GenericCacheConfiguration cacheConfiguration) {
        if(null == cacheManager) {
            synchronized (Ehcache2StringCacheFactory.class) {
                if (null == cacheManager) {
                    if(cacheConfiguration.createFromXml())
                        try {
                            this.cacheManager = new CacheManagerUtils().getCacheManagerFromXmlFile(
                                    cacheConfiguration.getCacheManagerName(),
                                    cacheConfiguration.getConfigurationXmlPath()
                            );
                        } catch (MalformedURLException e) {
                            logger.error("Error building the cache manager from XML", e);
                        }
                    else {
                        //build cache manager programmatically
                    }
                }
            }
        }
    }

    @Override
    public synchronized void shutdown() {
        if(null == cacheManager)
            throw new IllegalArgumentException("Cache manager should not be null...make sure you called init() first, or Check logs for errors");

        cacheManager.shutdown();
        cacheManager = null;
    }
}
