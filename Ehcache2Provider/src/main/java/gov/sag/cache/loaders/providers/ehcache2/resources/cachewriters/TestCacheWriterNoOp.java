package gov.sag.cache.loaders.providers.ehcache2.resources.cachewriters;

import com.codahale.metrics.ConsoleReporter;
import gov.sag.cache.loaders.providers.ehcache2.metrics.EhCacheWriterStatistics;
import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.writebehind.operations.SingleOperationType;

import java.util.Collection;
import java.util.concurrent.TimeUnit;


public class TestCacheWriterNoOp implements CacheWriter {
    private final EhCacheWriterStatistics counters;
    private final Ehcache parentCache;

    public TestCacheWriterNoOp(Ehcache cache) {
        if(null == cache)
            throw new IllegalArgumentException("Cache cannot be null");

        parentCache = cache;
        counters = new EhCacheWriterStatistics(parentCache.getName());
    }

    @Override
    public void init() {
        ConsoleReporter.forRegistry(counters.getMetrics()).build().start(10, TimeUnit.SECONDS);
    }

    @Override
    public void throwAway(Element element, SingleOperationType singleOperationType, RuntimeException e) {
        counters.getThrowAwayRequests().inc();
    }

    @Override
    public CacheWriter clone(Ehcache arg0) throws CloneNotSupportedException {
        System.out.println("Calling clone ...");
        return null;
    }

    @Override
    public void delete(CacheEntry arg0) throws CacheException {
        counters.getDeleteRequests().inc();
    }

    @Override
    public void deleteAll(Collection<CacheEntry> arg0) throws CacheException {
        for(CacheEntry e : arg0)
            delete(e);
    }

    @Override
    public void dispose() throws CacheException {
        ConsoleReporter.forRegistry(counters.getMetrics()).build().stop();
    }

    @Override
    public void write(Element arg0) throws CacheException {
        counters.getWriteRequests().inc();
    }

    @Override
    public void writeAll(Collection<Element> arg0) throws CacheException {
        for(Element e : arg0)
            write(e);
    }
}
