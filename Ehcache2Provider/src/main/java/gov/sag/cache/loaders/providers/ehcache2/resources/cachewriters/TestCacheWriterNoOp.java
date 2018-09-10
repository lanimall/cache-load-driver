package gov.sag.cache.loaders.providers.ehcache2.resources.cachewriters;

import gov.sag.cache.loaders.providers.ehcache2.metrics.EhcacheWriterStatistics;
import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.writebehind.operations.SingleOperationType;

import java.util.Collection;


public class TestCacheWriterNoOp implements CacheWriter {
    private final EhcacheWriterStatistics statistics;
    private final Ehcache parentCache;

    public TestCacheWriterNoOp(Ehcache cache, EhcacheWriterStatistics statistics) {
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
            delete(e);
    }

    @Override
    public void dispose() throws CacheException {
    }

    @Override
    public void write(Element arg0) throws CacheException {
        statistics.addWriteRequest();
    }

    @Override
    public void writeAll(Collection<Element> arg0) throws CacheException {
        for(Element e : arg0)
            write(e);
    }
}
