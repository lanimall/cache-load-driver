package gov.sag.cache.loaders.providers.ehcache2.resources.cacheloaders;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.loader.CacheLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class CustomCacheLoader implements CacheLoader {

    @Override
    public Object load(Object o) throws CacheException {
        System.out.println("Calling Load ... Object is: " + o);
        return "Value";
    }

    @Override
    public Map loadAll(Collection clctn) {
        HashMap<Object, Object> hashMap = new HashMap<Object, Object>(clctn.size());
        System.out.println("Calling loadAll ...");
        for (Object key : clctn) {
            hashMap.put(key, "Value");
        }
        return hashMap;
    }

    @Override
    public Object load(Object key, Object args) {
        String[] argumentsPassed = (String[]) args;
        System.out.println("Calling Load(key, args) ... ");
        return getValue(argumentsPassed);
    }

    private String getValue(String[] argumentsPassed) {
        if (argumentsPassed.length == 0)
            return "No arguments passed";
        else {
            return argumentsPassed[0];
        }
    }

    @Override
    public Map loadAll(Collection o, Object args) {
        System.out.println("Calling LoadAll(o, o1) ...");

        HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        Iterator iter = o.iterator();
        String[] argsPassed = (String[]) args;
        String value = getValue(argsPassed);
        int i = 0;
        while (iter.hasNext()) {
            Integer key = (Integer) iter.next();
            hashMap.put(key, value);
            i++;
        }
        return hashMap;
    }

    @Override
    public String getName() {
        System.out.println("Calling getName ...");
        return "Name";
    }

    @Override
    public void init() {
        System.out.println("Calling init...");
    }

    @Override
    public void dispose() {
        System.out.println("Calling dispose ...");
    }

    @Override
    public CacheLoader clone(Ehcache arg0) throws CloneNotSupportedException {
        System.out.println("Calling clone ...");
        return null;
    }

    @Override
    public Status getStatus() {
        System.out.println("Calling getDriverStatistics ...");
        return null;
    }
}