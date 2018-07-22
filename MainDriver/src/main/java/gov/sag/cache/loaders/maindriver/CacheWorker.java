package gov.sag.cache.loaders.maindriver;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;


public class CacheWorker extends Thread {

    private final long testDuration;
    private final int requestPerSecond;
    private final Callable<Void> callable;
    private final Timer statsTimer;
    private final CountDownLatch stopLatch;

    private volatile boolean isRunning = true;

    public CacheWorker(CountDownLatch stopLatch, long testDuration, int requestPerSecond, Timer statsTimer, Callable<Void> callable) {
        super();
        this.testDuration = testDuration;
        this.requestPerSecond = requestPerSecond;
        this.callable = callable;
        this.statsTimer = statsTimer;
        this.stopLatch = stopLatch;
    }

    @Override
    public void run() {
        // Verification of the running time in a separate thread so that no additional load arises.
        if (testDuration != CacheTestCase.UNLIMITED_RUNTIME) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(testDuration);
                        isRunning=false;
                    } catch (InterruptedException ex) {
                    }
                }
            }).start();
        }

        //if operation time is faster than the targetDurationRequest, sleep a bit
        if(requestPerSecond != CacheTestCase.UNLIMITED_REQPERSECOND) {
            int targetDurationRequestInMillis = requestPerSecond != CacheTestCase.UNLIMITED_REQPERSECOND ? (1000 / requestPerSecond) : 0;
            int timeToWaitInMillis = 0;
            while (isRunning) {
                try {
                    long t1 = System.currentTimeMillis();

                    Context ctx = statsTimer.time();
                    callable.call();
                    ctx.stop();

                    //if operation time is faster than the targetDurationRequest, sleep a bit
                    timeToWaitInMillis = targetDurationRequestInMillis - (int) (System.currentTimeMillis() - t1);
                    if (timeToWaitInMillis > 0) {
                        Thread.sleep(timeToWaitInMillis);
                    }
                } catch (InterruptedException ex) {
                    interrupt();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally{
                    Thread.currentThread().yield();
                }
            }
        } else {
            while (isRunning) {
                try {
                    Context ctx = statsTimer.time();
                    callable.call();
                    ctx.stop();
                } catch (InterruptedException ex) {
                    interrupt();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally{
                    Thread.currentThread().yield();
                }
            }
        }

        //count down when this is done
        stopLatch.countDown();
    }
}
