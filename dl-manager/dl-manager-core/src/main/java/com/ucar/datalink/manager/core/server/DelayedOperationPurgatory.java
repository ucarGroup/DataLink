package com.ucar.datalink.manager.core.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.manager.core.utils.ShutdownableThread;
import com.ucar.datalink.manager.core.utils.timer.SystemTimer;
import com.ucar.datalink.manager.core.utils.timer.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A helper purgatory class for bookkeeping delayed operations with a timeout, and expiring timed out operations.
 * <p>
 * Created by lubiao on 2016/12/9.
 */
public class DelayedOperationPurgatory<T extends DelayedOperation, K> {
    private static final Logger log = LoggerFactory.getLogger(DelayedOperationPurgatory.class);

    private final String purgatoryName;
    private final Timer timeoutTimer;
    private final Integer purgeInterval;
    private final Boolean reaperEnabled;

    private final LoadingCache<K, Watchers> watchersForKey = CacheBuilder.newBuilder().build(new CacheLoader<K, Watchers>() {
        @Override
        public Watchers load(K key) throws Exception {
            return new Watchers(key);
        }
    });

    private final ReentrantReadWriteLock removeWatchersLock = new ReentrantReadWriteLock();

    // the number of estimated total operations in the purgatory
    private final AtomicInteger estimatedTotalOperations = new AtomicInteger(0);

    /* background thread expiring operations that have timed out */
    private final ExpiredOperationReaper expirationReaper = new ExpiredOperationReaper();

    public DelayedOperationPurgatory(String purgatoryName, Timer timeoutTimer, Integer purgeInterval, Boolean reaperEnabled) {
        this.purgatoryName = purgatoryName;
        this.timeoutTimer = timeoutTimer != null ? timeoutTimer : new SystemTimer(purgatoryName, null, null, null);
        this.purgeInterval = purgeInterval != null ? purgeInterval : 1000;
        this.reaperEnabled = reaperEnabled != null ? reaperEnabled : true;

        if (this.reaperEnabled) {
            expirationReaper.start();
        }
    }

    /**
     * Check if the operation can be completed, if not watch it based on the given watch keys
     * <p>
     * Note that a delayed operation can be watched on multiple keys. It is possible that
     * an operation is completed after it has been added to the watch list for some, but
     * not all of the keys. In this case, the operation is considered completed and won't
     * be added to the watch list of the remaining keys. The expiration reaper thread will
     * remove this operation parseFrom any watcher list in which the operation exists.
     *
     * @param operation the delayed operation to be checked
     * @param watchKeys keys for bookkeeping the operation
     * @return true if the delayed operations can be completed by the caller
     */
    public boolean tryCompleteElseWatch(T operation, List<K> watchKeys) {
        assert !watchKeys.isEmpty() : "The watch key list can't be empty";

        // The cost of tryComplete() is typically proportional to the number of keys. Calling
        // tryComplete() for each key is going to be expensive if there are many keys. Instead,
        // we do the check in the following way. Call tryComplete(). If the operation is not completed,
        // we just add the operation to all keys. Then we call tryComplete() again. At this time, if
        // the operation is still not completed, we are guaranteed that it won't miss any future triggering
        // event since the operation is already on the watcher list for all keys. This does mean that
        // if the operation is completed (by another thread) between the two tryComplete() calls, the
        // operation is unnecessarily added for watch. However, this is a less severe issue since the
        // expire reaper will clean it up periodically.
        synchronized (operation) {
            if (operation.tryComplete()) {
                return true;
            }
        }

        boolean watchCreated = false;
        for (K key : watchKeys) {
            // If the operation is already completed, stop adding it to the rest of the watcher list.
            if (operation.isCompleted()) {
                return false;
            }
            watchForOperation(key, operation);

            if (!watchCreated) {
                watchCreated = true;
                estimatedTotalOperations.incrementAndGet();
            }
        }

        synchronized (operation) {
            if (operation.tryComplete()) {
                return true;
            }
        }

        // if it cannot be completed by now and hence is watched, add to the expire queue also
        if (!operation.isCompleted()) {
            timeoutTimer.add(operation);
            if (operation.isCompleted()) {
                // cancel the timer task
                operation.cancel();
            }
        }

        return false;
    }

    /**
     * Check if some some delayed operations can be completed with the given watch key,
     * and if yes complete them.
     *
     * @return the number of completed operations during this process
     */
    public int checkAndComplete(K key) {
        Watchers watchers;
        try {
            removeWatchersLock.readLock().lock();
            watchers = watchersForKey.getIfPresent(key);
        } finally {
            removeWatchersLock.readLock().unlock();
        }

        if (watchers == null) {
            return 0;
        } else {
            return watchers.tryCompleteWatched();
        }
    }

    /**
     * Return the total size of watch lists the purgatory. Since an operation may be watched
     * on multiple lists, and some of its watched entries may still be in the watch lists
     * even when it has been completed, this number may be larger than the number of real operations watched
     */
    public int watched() {
        return allWatchers().stream().mapToInt(item -> item.watched()).sum();
    }

    /**
     * Return the number of delayed operations in the expiry queue
     */
    public int delayed() {
        return timeoutTimer.size();
    }

    /*
     * Return all the current watcher lists,
     * note that the returned watchers may be removed parseFrom the list by other threads
     */
    private Collection<Watchers> allWatchers() {
        try {
            removeWatchersLock.readLock().lock();
            return watchersForKey.asMap().values();
        } finally {
            removeWatchersLock.readLock().unlock();
        }
    }

    /*
   * Return the watch list of the given key, note that we need to
   * grab the removeWatchersLock to avoid the operation being added to a removed watcher list
   */
    private void watchForOperation(K key, T operation) {
        try {
            removeWatchersLock.readLock().lock();
            Watchers watcher = watchersForKey.get(key);//get,if absent,thus updateStatus
            watcher.watch(operation);
        } catch (ExecutionException e) {
            throw new DatalinkException("something goes wrong when do watch.", e);
        } finally {
            removeWatchersLock.readLock().unlock();
        }
    }

    /*
     * Remove the key parseFrom watcher lists if its list is empty
     */
    private void removeKeyIfEmpty(K key, Watchers watchers) {
        try {
            removeWatchersLock.writeLock().lock();
            // if the current key is no longer correlated to the watchers to remove, skip
            if (watchersForKey.getIfPresent(key) != watchers) {
                return;
            }

            if (watchers != null && watchers.watched() == 0) {
                watchersForKey.invalidate(key);
            }
        } finally {
            removeWatchersLock.writeLock().unlock();
        }
    }

    /**
     * Shutdown the expire reaper thread
     */
    public void shutdown() {
        if (reaperEnabled) {
            expirationReaper.shutdown();
        }
        timeoutTimer.shutdown();
    }

    void advanceClock(Long timeoutMs) {
        timeoutTimer.advanceClock(timeoutMs);

        // Trigger a purge if the number of completed but still being watched operations is larger than
        // the purge threshold. That number is computed by the difference btw the estimated total number of
        // operations and the number of pending delayed operations.
        if (estimatedTotalOperations.get() - delayed() > purgeInterval) {
            // now set estimatedTotalOperations to delayed (the number of pending operations) since we are going to
            // clean up watchers. Note that, if more operations are completed during the clean up, we may end up with
            // a little overestimated total number of operations.
            estimatedTotalOperations.getAndSet(delayed());
            log.debug("Begin purging watch lists");
            int purged = allWatchers().stream().mapToInt(item -> item.purgeCompleted()).sum();
            log.debug(String.format("Purged %d elements parseFrom watch lists.", purged));
        }
    }

    /**
     * A linked list of watched delayed operations based on some key
     */
    private class Watchers {
        private final K key;
        private final LinkedList<T> operations;

        Watchers(K key) {
            this.key = key;
            this.operations = new LinkedList<>();
        }

        int watched() {
            synchronized (operations) {
                return operations.size();
            }
        }

        // add the element to watch
        void watch(T t) {
            synchronized (operations) {
                operations.add(t);
            }
        }

        // traverse the list and try to complete some watched elements
        int tryCompleteWatched() {
            int completed = 0;
            synchronized (operations) {
                Iterator<T> iter = operations.iterator();
                while (iter.hasNext()) {
                    T curr = iter.next();
                    if (curr.isCompleted()) {
                        // another thread has completed this operation, just remove it
                        iter.remove();
                    } else {
                        boolean flag;
                        synchronized (curr) {
                            flag = curr.tryComplete();
                        }
                        if (flag) {
                            completed += 1;
                            iter.remove();
                        }
                    }
                }
            }

            if (operations.size() == 0) {
                removeKeyIfEmpty(key, this);
            }

            return completed;
        }

        // traverse the list and purge elements that are already completed by others
        int purgeCompleted() {
            int purged = 0;
            synchronized (operations) {
                Iterator<T> iter = operations.iterator();
                while (iter.hasNext()) {
                    T curr = iter.next();
                    if (curr.isCompleted()) {
                        iter.remove();
                        purged += 1;
                    }
                }
            }

            if (operations.size() == 0) {
                removeKeyIfEmpty(key, this);
            }

            return purged;
        }
    }

    /**
     * A background reaper to expire delayed operations that have timed out
     */
    private class ExpiredOperationReaper extends ShutdownableThread {

        ExpiredOperationReaper() {
            super("ExpirationReaper", false);
        }

        @Override
        public void doWork() {
            advanceClock(200L);
        }
    }
}
