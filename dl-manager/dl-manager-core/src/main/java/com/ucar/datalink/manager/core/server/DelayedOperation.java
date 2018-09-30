package com.ucar.datalink.manager.core.server;

import com.ucar.datalink.manager.core.utils.timer.TimerTask;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An operation whose processing needs to be delayed for at most the given delayMs. For example
 * a delayed produce operation could be waiting for specified number of acks; or
 * a delayed fetch operation could be waiting for a given number of bytes to accumulate.
 * <p>
 * The logic upon completing a delayed operation is defined in onComplete() and will be called exactly once.
 * Once an operation is completed, isCompleted() will return true. onComplete() can be triggered by either
 * forceComplete(), which forces calling onComplete() after delayMs if the operation is not yet completed,
 * or tryComplete(), which first checks if the operation can be completed or not now, and if yes calls
 * forceComplete().
 * <p>
 * A subclass of DelayedOperation needs to provide an implementation of both onComplete() and tryComplete().
 * <p>
 * Created by lubiao on 2016/12/12.
 */
public abstract class DelayedOperation extends TimerTask {

    private final AtomicBoolean completed = new AtomicBoolean(false);

    public DelayedOperation(Long delayMs) {
        super(delayMs);
    }

    /*
 * Force completing the delayed operation, if not already completed.
 * This function can be triggered when
 *
 * 1. The operation has been verified to be completable inside tryComplete()
 * 2. The operation has expired and hence needs to be completed right now
 *
 * Return true iff the operation is completed by the caller: note that
 * concurrent threads can try to complete the same operation, but only
 * the first thread will succeed in completing the operation and return
 * true, others will still return false
 */
    public boolean forceComplete() {
        if (completed.compareAndSet(false, true)) {
            // cancel the timeout timer
            cancel();
            onComplete();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if the delayed operation is already completed
     */
    public boolean isCompleted() {
        return completed.get();
    }

    /**
     * Call-back to execute when a delayed operation gets expired and hence forced to complete.
     */
    public abstract void onExpiration();

    /**
     * Process for completing an operation; This function needs to be defined
     * in subclasses and will be called exactly once in forceComplete()
     */
    public abstract void onComplete();


    /*
     * Try to complete the delayed operation by first checking if the operation
     * can be completed by now. If yes execute the completion logic by calling
     * forceComplete() and return true iff forceComplete returns true; otherwise return false
     *
     * This function needs to be defined in subclasses
     */
    public abstract boolean tryComplete();


    /*
     * run() method defines a task that is executed on timeout
     */
    @Override
    public void run() {
        if (forceComplete())
            onExpiration();
    }
}
