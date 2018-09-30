package com.ucar.datalink.worker.api.util.priority;

import java.util.LinkedList;
import java.util.List;

/**
 * 优先级Bucket，存储相同Priority下的执行数据.
 *
 * Created by lubiao on 2018/5/13.
 */
public class PriorityBucket<T> implements Comparable<PriorityBucket> {

    private final long priority;
    private final LinkedList<T> items;

    public PriorityBucket(long priority) {
        this.priority = priority;
        this.items = new LinkedList<>();
    }

    public long getPriority() {
        return priority;
    }

    public List<T> getItems() {
        return items;
    }

    public void addLast(T item) {
        this.items.addLast(item);
    }

    public int compareTo(PriorityBucket o) {
        if (this.getPriority() > o.getPriority()) {
            return 1;
        } else if (this.getPriority() == o.getPriority()) {
            return 0;
        } else {
            return -1;
        }
    }
}
