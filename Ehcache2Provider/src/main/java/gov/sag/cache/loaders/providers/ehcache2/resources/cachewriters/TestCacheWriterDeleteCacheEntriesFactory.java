package gov.sag.cache.loaders.providers.ehcache2.resources.cachewriters;

import gov.sag.cache.loaders.providers.ehcache2.metrics.EhcacheWriterStatisticsController;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.CacheWriterFactory;

import java.util.Properties;


public class TestCacheWriterDeleteCacheEntriesFactory extends CacheWriterFactory {
    EhcacheWriterStatisticsController ehcacheWriterStatisticsController = new EhcacheWriterStatisticsController();

    public CacheWriter createCacheWriter(Ehcache cache, Properties arg1) {
        return new TestCacheWriterDeleteCacheEntries(
                cache,
                ehcacheWriterStatisticsController.getBuilder()
                        .addDeleteRequestsCounterWithPrefix(cache.getName())
                        .addThrowAwayRequestsCounterWithPrefix(cache.getName())
                        .addWriteRequestsCounterWithPrefix(cache.getName())
                        .build()
        );
    }
}
