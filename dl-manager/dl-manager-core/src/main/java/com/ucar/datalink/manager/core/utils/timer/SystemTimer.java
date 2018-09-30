package com.ucar.datalink.manager.core.utils.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Created by lubiao on 2016/12/12.
 */
public class SystemTimer implements Timer {
    private static final Logger log = LoggerFactory.getLogger(SystemTimer.class);

    private final String executorName;
    private final Long tickMs;
    private final Integer wheelSize;
    private final Long startMs;

    // timeout timer
    private final ExecutorService taskExecutor;

    private final DelayQueue<TimerTaskList> delayQueue;
    private final AtomicInteger taskCounter;
    private final TimingWheel timingWheel;

    private final Consumer<TimerTaskEntry> reinsert = timerTaskEntry -> addTimerTaskEntry(timerTaskEntry);

    // Locks used to protect data structures while ticking
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    public SystemTimer(String executorName, Long tickMs, Integer wheelSize, Long startMs) {
        this.executorName = executorName;
        this.tickMs = tickMs != null ? tickMs : 1L;
        this.wheelSize = wheelSize != null ? wheelSize : 20;
        this.startMs = startMs != null ? startMs : TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        this.taskExecutor = Executors.newFixedThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable, "executor-" + executorName);
            thread.setDaemon(false);
            thread.setUncaughtExceptionHandler((t, e) -> log.error("Uncaught errors in thread '" + t.getName() + "':", e));
            return thread;
        });
        this.delayQueue = new DelayQueue<>();
        this.taskCounter = new AtomicInteger(0);
        this.timingWheel = new TimingWheel(this.tickMs, this.wheelSize, this.startMs, taskCounter, delayQueue);
    }

    @Override
    public void add(TimerTask timerTask) {
        readLock.lock();
        try {
            addTimerTaskEntry(new TimerTaskEntry(timerTask, timerTask.getDelayMs() + TimeUnit.NANOSECONDS.toMillis(System.nanoTime())));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Boolean advanceClock(Long timeoutMs) {
        TimerTaskList bucket = null;
        try {
            bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("InterruptedException occurrs when execute advanceClock method", e);
        }

        if (bucket != null) {
            writeLock.lock();
            try {
                while (bucket != null) {
                    timingWheel.advanceClock(bucket.getExpiration());
                    bucket.flush(reinsert);
                    bucket = delayQueue.poll();
                }
            } finally {
                writeLock.unlock();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Integer size() {
        return taskCounter.get();
    }

    @Override
    public void shutdown() {
        taskExecutor.shutdown();
    }

    private void addTimerTaskEntry(TimerTaskEntry timerTaskEntry) {
        if (!timingWheel.add(timerTaskEntry)) {
            // Already expired or cancelled
            if (!timerTaskEntry.cancelled()) {
                taskExecutor.submit(timerTaskEntry.getTimerTask());
            }
        }
    }
}
