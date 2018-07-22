package gov.sag.cache.loaders.providers.ehcache2.resources.cachelisteners;

import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

import java.util.Properties;


public class CacheConsoleEventListenerFactory extends CacheEventListenerFactory {

    @Override
    public CacheEventListener createCacheEventListener(
            Properties paramProperties) {
        // TODO Auto-generated method stub
        if (paramProperties.contains("myEventCache")) {
            return new CacheConsoleEventListener();
        }
        return new CacheConsoleEventListener();
    }

}
