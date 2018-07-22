package gov.sag.cache.loaders.maindriver.cache;

/**
 * Created by fabien.sanglier on 7/20/18.
 */
public interface GenericCacheConfiguration {
    String getCacheManagerName();
    String getCacheName();
    boolean createFromXml();
    String getConfigurationXmlPath(); //if this is not null, will use that instead of the other params
    int getMaxEntryCount();
    int getMaxLocalHeapEntryCount();
    String getMaxLocalOffheapBytes();
    int getExpirationTimeToLiveSeconds();
    boolean isDistributed();
}
