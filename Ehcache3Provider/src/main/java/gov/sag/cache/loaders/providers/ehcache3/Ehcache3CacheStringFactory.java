package gov.sag.cache.loaders.providers.ehcache3;

import gov.sag.cache.loaders.maindriver.cache.GenericCache;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheConfiguration;
import gov.sag.cache.loaders.maindriver.cache.GenericCacheFactory;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

public class Ehcache3CacheStringFactory implements GenericCacheFactory<String, String> {
    private final Logger logger = LoggerFactory.getLogger(Ehcache3CacheStringFactory.class);

    private CacheManager cacheManager;

    @Override
    public GenericCache<String, String> getCache(String cacheName) {
        if(null == cacheManager)
            throw new IllegalArgumentException("Cache manager cannot be null...make sure you called init() first, or Check logs for errors");

        return new Ehcache3StringCache(cacheManager, cacheName);
    }

    @Override
    public void init(GenericCacheConfiguration cacheConfiguration) {
        if(null == cacheManager) {
            synchronized (Ehcache3CacheStringFactory.class) {
                if (null == cacheManager) {
                    if(cacheConfiguration.createFromXml())
                        try {
                            this.cacheManager = new CacheManagerUtils().getCacheManagerFromXmlFile(
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

        cacheManager.init();
    }

    @Override
    public void shutdown() {
        cacheManager.close();
    }
}