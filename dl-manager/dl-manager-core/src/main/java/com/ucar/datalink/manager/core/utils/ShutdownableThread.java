package com.ucar.datalink.manager.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public abstract class ShutdownableThread extends Thread {
    private static final Logger log = LoggerFactory.getLogger(ShutdownableThread.class);

    private final Boolean isInterruptible;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    public ShutdownableThread(String name) {
        this(name, true);
    }

    public ShutdownableThread(String name, boolean isInterruptible) {
        super(name);
        this.isInterruptible = isInterruptible;
        this.setDaemon(false);
    }

    public void shutdown() {
        this.initiateShutdown();
        this.awaitShutdown();
    }

    private boolean initiateShutdown() {
        if (isRunning.compareAndSet(true, false)) {
            log.info("Shutting down");
            isRunning.set(false);
            if (isInterruptible) {
                interrupt();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * After calling initiateShutdown(), use this API to wait until the shutdown is complete
     */
    void awaitShutdown() {
        try {
            shutdownLatch.await();
            log.info("Shutdown completed");
        } catch (Throwable t) {
            log.error("Shutdown error", t);
        }
    }

    /**
     * This method is repeatedly invoked until the thread shuts down or this method throws an errors
     */
    protected abstract void doWork();


    @Override
    public void run() {
        log.info("Starting ");
        try {
            while (isRunning.get()) {
                doWork();
            }
        } catch (Throwable e) {
            if (isRunning.get()) {
                log.error("Error due to ", e);
            }
        }
        shutdownLatch.countDown();
        log.info("Stopped ");
    }
}