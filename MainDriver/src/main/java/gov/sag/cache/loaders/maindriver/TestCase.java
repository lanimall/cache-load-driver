package gov.sag.cache.loaders.maindriver;

public interface TestCase {
    void init();
    void runTest() throws InterruptedException;
    void cleanup();
}
