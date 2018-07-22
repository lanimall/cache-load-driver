package gov.sag.cache.loaders.providers.ehcache3;

import gov.sag.cache.loaders.commonutils.FileUtils;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ResourcePoolsBuilder.heap;
import static org.ehcache.config.units.MemoryUnit.MB;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManager;

/**
 * Created by fabien.sanglier on 4/12/16.
 */
public class CacheManagerUtils {
    private final Logger logger = LoggerFactory.getLogger(CacheManagerUtils.class);

    //The cache manager name in ENV_CACHE_CONFIGPATH XML file should be the same as value defined in ENV_CACHE_MANAGER_NAME
    public static final String ENV_CACHE_MANAGER_NAME = "ehcache.cachemanager.name";
    public static final String ENV_CACHE_CONFIGPATH = "ehcache.config.path";

    public CacheManager getCacheManagerFromXmlFile() throws MalformedURLException {
        String xmlUrlPath = null;
        if(null != System.getProperty(ENV_CACHE_CONFIGPATH)){
            xmlUrlPath = System.getProperty(ENV_CACHE_CONFIGPATH);
        }

        if(null == xmlUrlPath) {
            throw new IllegalArgumentException("No url path set...cannot do much from here");
        }

        CacheManager cacheManager = getCacheManagerFromXmlFile(xmlUrlPath);
        return cacheManager;
    }

    public CacheManager getCacheManagerFromXmlFile(String xmlUrlPath) throws MalformedURLException {
        CacheManager cacheManager;
        if(null == xmlUrlPath)
            cacheManager = getCacheManagerFromXmlFile();
        else {
            URL ehcacheXml = FileUtils.getFileURL(xmlUrlPath);
            cacheManager = getCacheManagerFromXmlFileURL(ehcacheXml);
        }
        return cacheManager;
    }

    public CacheManager getCacheManagerFromXmlFileURL(URL xmlUrl){
        Configuration xmlConfig = new XmlConfiguration(xmlUrl);
        CacheManager cacheManager = newCacheManager(xmlConfig);
        return cacheManager;
    }

    public CacheManager createProgrammatic(){
        logger.info("Creating cache manager programmatically");
        CacheManager cacheManager = newCacheManagerBuilder()
                .withCache(
                        "basicCache",
                        newCacheConfigurationBuilder(Long.class, String.class, heap(100).offheap(1, MB))
                )
                .build(true);

        return cacheManager;
    }
}