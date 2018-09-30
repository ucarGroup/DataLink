package com.ucar.datalink.manager.core.utils.timer;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Created by lubiao on 2016/12/12.
 */
class TimerTaskList implements Delayed {

    // TimerTaskList forms a doubly linked cyclic list using a dummy root entry
    // root.next points to the head
    // root.prev points to the tail
    private final TimerTaskEntry root = new TimerTaskEntry(null, -1);

    private final AtomicLong expiration = new AtomicLong(-1L);

    private final AtomicInteger taskCounter;

    TimerTaskList(AtomicInteger taskCounter) {
        root.setNext(root);
        root.setPrev(root);
        this.taskCounter = taskCounter;
    }

    // Apply the supplied function to each of tasks in this list
    void foreach(Consumer<TimerTask> f) {
        synchronized (this) {
            TimerTaskEntry entry = root.getNext();
            while (entry != root) {
                final TimerTaskEntry nextEntry = entry.getNext();

                if (!entry.cancelled()) {
                    f.accept(entry.getTimerTask());
                }

                entry = nextEntry;
            }
        }
    }

    // Add a timer task entry to this list
    void add(TimerTaskEntry timerTaskEntry) {
        boolean done = false;
        while (!done) {
            // Remove the timer task entry if it is already in any other list
            // We do this outside of the sync block below to avoid deadlocking.
            // We may retry until timerTaskEntry.list becomes null.
            timerTaskEntry.remove();

            synchronized (this) {
                synchronized (timerTaskEntry) {
                    if (timerTaskEntry.getList() == null) {
                        // updateStatus the timer task entry to the end of the list. (root.prev points to the tail entry)
                        final TimerTaskEntry tail = root.getPrev();
                        timerTaskEntry.setNext(root);
                        timerTaskEntry.setPrev(tail);
                        timerTaskEntry.setList(this);
                        tail.setNext(timerTaskEntry);
                        root.setPrev(timerTaskEntry);
                        taskCounter.incrementAndGet();
                        done = true;
                    }
                }
            }
        }
    }

    // Remove the specified timer task entry parseFrom this list
    void remove(TimerTaskEntry timerTaskEntry) {
        synchronized (this) {
            synchronized (timerTaskEntry) {
                if (timerTaskEntry.getList() == this) {
                    timerTaskEntry.getNext().setPrev(timerTaskEntry.getPrev());
                    timerTaskEntry.getPrev().setNext(timerTaskEntry.getNext());
                    timerTaskEntry.setNext(null);
                    timerTaskEntry.setPrev(null);
                    timerTaskEntry.setList(null);
                    taskCounter.decrementAndGet();
                }
            }
        }
    }

    // Remove all task entries and apply the supplied function to each of them
    void flush(Consumer<TimerTaskEntry> f) {
        synchronized (this) {
            TimerTaskEntry head = root.getNext();
            while (head != root) {
                remove(head);
                f.accept(head);
                head = root.getNext();
            }
            expiration.set(-1L);
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(
                Math.max(getExpiration() - TimeUnit.NANOSECONDS.toMillis(System.nanoTime()), 0),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        TimerTaskList other = (TimerTaskList) o;

        if (getExpiration() < other.getExpiration()) {
            return -1;
        } else if (getExpiration() > other.getExpiration()) {
            return 1;
        } else {
            return 0;
        }
    }

    // Get the bucket's expiration time
    Long getExpiration() {
        return expiration.get();
    }

    // Set the bucket's expiration time
    // Returns true if the expiration time is changed
    boolean setExpiration(Long expirationMs) {
        return expiration.getAndSet(expirationMs) != expirationMs;
    }
}
