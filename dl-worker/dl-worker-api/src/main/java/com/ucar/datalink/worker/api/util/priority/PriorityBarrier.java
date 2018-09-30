package com.ucar.datalink.worker.api.util.priority;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于priority的Barrier
 *
 * @author lubiao
 */
public class PriorityBarrier {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile long threshold;

    public PriorityBarrier(long priority) {
        this.threshold = priority;
    }

    /**
     * 等待，一直等到该优先级获得执行允许
     */
    public void await(long priority) throws InterruptedException {
        try {
            lock.lockInterruptibly();
            while (!isPermit(priority)) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 等待，一直等到该优先级获得执行允许或超时
     */
    public void await(long priority, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        try {
            lock.lockInterruptibly();
            while (!isPermit(priority)) {
                condition.await(timeout, unit);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 重新设置priority信息
     */
    public void signal(long priority) throws InterruptedException {
        try {
            lock.lockInterruptibly();
            threshold = priority;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean isPermit(long state) {
        return state <= state();
    }

    private long state() {
        return threshold;
    }
}
