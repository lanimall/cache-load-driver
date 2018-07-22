package gov.sag.cache.loaders.maindriver.cache;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by fabien.sanglier on 7/20/18.
 */
public class CacheProviderFactory {

    public GenericCacheFactory create(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, MalformedURLException {
        if(null == className || "".equals(className))
            throw new IllegalArgumentException("Must provide a valid class name");

//        URLClassLoader child = new URLClassLoader (
//                new URL[] {
//                        new URL("file:/Users/fabien.sanglier/MyDev/MyCodeMisc/Terracotta/CacheLoaders/EhcacheLoader/Ehcache2Provider/target/appassembler/repo/MainDriver-1.0.0.jar"),
//                        new URL("file:/Users/fabien.sanglier/MyDev/MyCodeMisc/Terracotta/CacheLoaders/EhcacheLoader/Ehcache2Provider/target/appassembler/repo/MainDriver-1.0.0.jar")
//                }, CacheProviderFactory.class.getClassLoader());
//
//        Class clazz = Class.forName(className, true, child);

        Class clazz = Class.forName(className);

        if (!GenericCacheFactory.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Class " + className + " must implement " + GenericCacheFactory.class.getName());

        GenericCacheFactory cacheFactory = (GenericCacheFactory)clazz.newInstance();
        return cacheFactory;
    }
}
