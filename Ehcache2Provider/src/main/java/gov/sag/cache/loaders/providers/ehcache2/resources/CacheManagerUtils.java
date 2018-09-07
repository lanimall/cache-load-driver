package gov.sag.cache.loaders.providers.ehcache2.resources;

import gov.sag.cache.loaders.commonutils.FileUtils;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by fabien.sanglier on 4/12/16.
 */
public class CacheManagerUtils {
    private final Logger logger = LoggerFactory.getLogger(CacheManagerUtils.class);

    //The cache manager name in ENV_CACHE_CONFIGPATH XML file should be the same as value defined in ENV_CACHE_MANAGER_NAME
    public static final String ENV_CACHE_MANAGER_NAME = "ehcache.cachemanager.name";
    public static final String ENV_CACHE_CONFIGPATH = "ehcache.config.path";

    public CacheManagerUtils() {
    }

    public CacheManager getCacheManagerFromXmlFile() throws MalformedURLException {
        return getCacheManagerFromXmlFile(System.getProperty(ENV_CACHE_MANAGER_NAME));
    }

    public CacheManager getCacheManagerFromXmlFile(String cacheManagerName) throws MalformedURLException {
        CacheManager cacheManager = CacheManager.getCacheManager(cacheManagerName);
        if(null == cacheManager) {
            String xmlUrlPath = null;
            if(null != System.getProperty(ENV_CACHE_CONFIGPATH)){
                xmlUrlPath = System.getProperty(ENV_CACHE_CONFIGPATH);
            }

            if(null == xmlUrlPath) {
                throw new IllegalArgumentException("No url path set...cannot do much from here");
            }

            cacheManager = getCacheManagerFromXmlFile(cacheManagerName, xmlUrlPath);
        }
        return cacheManager;
    }

    public CacheManager getCacheManagerFromXmlFile(String cacheManagerName, String xmlUrlPath) throws MalformedURLException {
        CacheManager cacheManager = CacheManager.getCacheManager(cacheManagerName);
        if(null == cacheManager) {
            if(null == xmlUrlPath)
                cacheManager = getCacheManagerFromXmlFile(cacheManagerName);
            else {
                URL ehcacheXml = FileUtils.getFileURL(xmlUrlPath);
                cacheManager = getCacheManagerFromXmlFileURL(cacheManagerName, ehcacheXml);
            }
        }
        return cacheManager;
    }

    public CacheManager getCacheManagerFromXmlFileURL(String cacheManagerName, URL xmlUrl){
        CacheManager cacheManager = CacheManager.getCacheManager(cacheManagerName);
        if(null == cacheManager) {
            if(null == xmlUrl) {
                throw new IllegalArgumentException("No ehcache config found...cannot do much from here");
            }
            cacheManager = CacheManager.create(xmlUrl);
        }
        return cacheManager;
    }
}