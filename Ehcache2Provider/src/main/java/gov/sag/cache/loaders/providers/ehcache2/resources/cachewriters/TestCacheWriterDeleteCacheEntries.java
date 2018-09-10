package gov.sag.cache.loaders.providers.ehcache2.resources.cachewriters;

import gov.sag.cache.loaders.providers.ehcache2.metrics.EhcacheWriterStatistics;
import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.writebehind.operations.SingleOperationType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class TestCacheWriterDeleteCacheEntries implements CacheWriter {
    private final EhcacheWriterStatistics statistics;
    private final Ehcache parentCache;

    public TestCacheWriterDeleteCacheEntries(Ehcache cache, EhcacheWriterStatistics statistics) {
        if(null == cache)
            throw new IllegalArgumentException("Cache cannot be null");

        this.parentCache = cache;
        this.statistics = statistics;
    }

    @Override
    public void init() {
    }

    @Override
    public void throwAway(Element element, SingleOperationType singleOperationType, RuntimeException e) {
        statistics.addThrowAwayRequest();
    }

    @Override
    public CacheWriter clone(Ehcache arg0) throws CloneNotSupportedException {
        System.out.println("Calling clone ...");
        return null;
    }

    @Override
    public void delete(CacheEntry arg0) throws CacheException {
        statistics.addDeleteRequest();
    }

    @Override
    public void deleteAll(Collection<CacheEntry> arg0) throws CacheException {
        for(CacheEntry e : arg0)
            statistics.addDeleteRequest();
    }

    @Override
    public void dispose() throws CacheException {
    }

    @Override
    public void write(Element e) throws CacheException {
        //parentCache.removeElement(e); //CAS
        parentCache.remove(e.getObjectKey());
        statistics.addWriteRequest();
    }

    @Override
    public void writeAll(Collection<Element> elements) throws CacheException {
        // remove the elements from the cache
        Set<Object> elementKeySet = new HashSet<Object>();
        for(Element e : elements)
            elementKeySet.add(e.getObjectKey());
        parentCache.removeAll(elementKeySet);

        for(Element e : elements)
            statistics.addWriteRequest();
    }
}
