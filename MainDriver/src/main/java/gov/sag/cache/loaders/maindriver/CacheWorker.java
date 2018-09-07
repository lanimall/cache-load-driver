package gov.sag.cache.loaders.maindriver;

import com.codahale.metrics.Timer.Context;
import gov.sag.cache.loaders.maindriver.metrics.WorkerStatistics;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class CacheWorker extends Thread {
    private final long testDurationMillis;
    private final int maxRequestPerSecond;
    private final Callable callable;
    private final WorkerStatistics workerStatistics;
    private final CountDownLatch stopLatch;

    private volatile boolean isRunning = true;

    public CacheWorker(CountDownLatch stopLatch, long testDurationMillis, int maxRequestPerSecond, WorkerStatistics workerStatistics, Callable callable) {
        super();
        this.testDurationMillis = testDurationMillis;
        this.maxRequestPerSecond = maxRequestPerSecond;
        this.callable = callable;
        this.workerStatistics = workerStatistics;
        this.stopLatch = stopLatch;
    }

    @Override
    public void run() {
        // Verification of the running time in a separate thread so that no additional load arises.
        if (testDurationMillis > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sleep(testDurationMillis);
                        isRunning=false;
                    } catch (InterruptedException ex) {
                    }
                }
            }).start();
        }

        //if operation time is faster than the targetDurationRequest, sleep a bit
        if(maxRequestPerSecond > 0) {
            Double targetDurationRequestInNanos = Math.pow(10, 9) / maxRequestPerSecond;
            long timeToWaitInMillis;
            while (isRunning) {
                try {
                    long t1 = System.nanoTime();
                    callable.call();
                    long elapsedNanos = System.nanoTime() - t1;

                    //update the timer
                    workerStatistics.getRequestTimer().update(elapsedNanos, TimeUnit.NANOSECONDS);

                    //if operation time is faster than the targetDurationRequest, sleep a bit
                    timeToWaitInMillis = Math.round((targetDurationRequestInNanos - elapsedNanos) / Math.pow(10, 6));
                    if (timeToWaitInMillis > 0L) {
                        Context ctx = workerStatistics.getRequestWaitsTimer().time();
                        sleep(timeToWaitInMillis);
                        ctx.stop();
                    }
                } catch (InterruptedException ex) {
                    interrupt();
                } catch (Exception e) {
                    workerStatistics.getExceptionsCounter().inc();
                } finally{
                    Thread.currentThread().yield();
                }
            }
        } else {
            while (isRunning) {
                try {
                    workerStatistics.getRequestTimer().time(callable);
                } catch (InterruptedException ex) {
                    interrupt();
                } catch (Exception e) {
                    workerStatistics.getExceptionsCounter().inc();
                } finally{
                    Thread.currentThread().yield();
                }
            }
        }

        //count down when this is done
        stopLatch.countDown();
    }
}
