package gov.sag.cache.loaders.maindriver;

import com.lexicalscope.jewel.cli.Option;

public interface ProgramOptions {

    @Option(
            shortName = "c",
            longName = "cache",
            description = "GenericCache")
    public String getCache();

    @Option(
            shortName = "r",
            longName = "read-thread-count",
            description = "Read threads",
            defaultValue = "0")
    public int getReadThreadCount();

    @Option(
            shortName = "w",
            longName = "write-thread-count",
            description = "write threads",
            defaultValue = "0")
    public int getWriteThreadCount();

    @Option(
            shortName = "d",
            longName = "delete-thread-count",
            description = "delete threads",
            defaultValue = "0")
    public int getDeleteThreadCount();

    @Option(
            shortName = "i",
            longName = "write-requests-per-second",
            description = "Number of Write-Requests per sec per Thread.",
            defaultValue = "0")
    public int getWriteRequestsPerSecond();


    @Option(
            shortName = "j",
            longName = "read-requests-per-second",
            description = "Number of Read-Requests per sec per Thread.",
            defaultValue = "0")
    public int getReadRequestsPerSecond();

    @Option(
            shortName = "k",
            longName = "delete-requests-per-second",
            description = "Number of Delete-Requests per sec per Thread.",
            defaultValue = "0")
    public int getDeleteRequestsPerSecond();

    @Option(
            shortName = "l",
            longName = "duration-length",
            description = "Duration of test (in seconds)",
            defaultValue = "60")
    public int getDuration();

    @Option(
            longName = "sleep-before-exit",
            description = "Length of sleep (in sec) at end of test (useful to wait for background operations to finish...eg. write-behind. value <= 0 means infinite.",
            defaultValue = "0")
    public int getSleepBeforeExit();

    @Option(
            shortName = "s",
            longName = "size",
            description = "Data size in bytes.",
            defaultValue = "1024")
    public int getSize();


    @Option(
            helpRequest = true,
            shortName = "h")
    public boolean getHelp();

    @Option(
            longName = "empty-cache",
            description = "Empty the cache first before testing")
    public boolean isClearCacheFirst();

    @Option(
            shortName = "f",
            longName = "fill-cache",
            description = "Fill the cache first before testing")
    public boolean isFillCacheFirst();

    @Option(
            longName = "fill-thread-count",
            description = "Number of thread to use to fill the cache",
            defaultValue = "4")
    public int getFillCacheThreadCount();

    @Option(
            shortName = "e",
            longName = "entries",
            description = "number of entries",
            defaultValue = "100000")
    public long getEntryCount();

}
