package gov.sag.cache.loaders.maindriver.cache.impl;

import gov.sag.cache.loaders.maindriver.cache.GenericCacheConfiguration;

/**
 * Created by fabien.sanglier on 7/21/18.
 */
public class FileBasedCacheConfiguration implements GenericCacheConfiguration {

    private final String cacheManagerName;
    private final String cacheName;
    private final String configurationXmlPath;

    public FileBasedCacheConfiguration(String cacheManagerName, String cacheName, String configurationXmlPath) {
        this.cacheManagerName = cacheManagerName;
        this.cacheName = cacheName;
        this.configurationXmlPath = configurationXmlPath;
    }

    @Override
    public String getCacheManagerName() {
        return null;
    }

    @Override
    public String getCacheName() {
        return null;
    }

    @Override
    public boolean createFromXml() {
        return true;
    }

    @Override
    public String getConfigurationXmlPath() {
        return null;
    }

    @Override
    public int getMaxEntryCount() {
        return 0;
    }

    @Override
    public int getMaxLocalHeapEntryCount() {
        return 0;
    }

    @Override
    public String getMaxLocalOffheapBytes() {
        return null;
    }

    @Override
    public int getExpirationTimeToLiveSeconds() {
        return 0;
    }

    @Override
    public boolean isDistributed() {
        return false;
    }
}
