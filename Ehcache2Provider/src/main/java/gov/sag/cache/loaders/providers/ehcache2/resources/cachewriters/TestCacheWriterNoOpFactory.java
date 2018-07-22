package gov.sag.cache.loaders.providers.ehcache2.resources.cachewriters;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.CacheWriterFactory;

import java.util.Properties;


public class TestCacheWriterNoOpFactory extends CacheWriterFactory {

    public CacheWriter createCacheWriter(Ehcache cache, Properties arg1) {
        return new TestCacheWriterNoOp(cache);
    }
}
