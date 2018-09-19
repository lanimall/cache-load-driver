package gov.sag.cache.loaders.maindriver.cache;

import java.net.MalformedURLException;

/**
 * Created by fabien.sanglier on 7/20/18.
 */
public class CacheProviderFactory {

    public GenericCacheFactory create(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, MalformedURLException {
        if(null == className || "".equals(className))
            throw new IllegalArgumentException("Must provide a valid class name");

        Class clazz = Class.forName(className);

        if (!GenericCacheFactory.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Class " + className + " must implement " + GenericCacheFactory.class.getName());

        GenericCacheFactory cacheFactory = (GenericCacheFactory)clazz.newInstance();
        return cacheFactory;
    }
}
