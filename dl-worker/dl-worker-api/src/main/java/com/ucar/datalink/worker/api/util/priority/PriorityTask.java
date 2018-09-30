package com.ucar.datalink.worker.api.util.priority;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.TreeMap;

/**
 * 优先级Task.
 * <p>
 * Created by lubiao on 2017/3/8.
 */
public class PriorityTask<T> {
    private final TreeMap<Long, PriorityBucket<T>> buckets;
    private final PriorityTaskCallback<T> callback;

    public PriorityTask(PriorityTaskCallback<T> callback) {
        this.buckets = new TreeMap<>();
        this.callback = callback;
    }

    /**
     * 获取priorities列表，从小到大的排序结果
     */
    public synchronized List<Long> priorities() {
        return Lists.newArrayList(buckets.keySet());
    }

    /**
     * 将数据放到priority对应的Bucket中
     */
    public synchronized void addItem(long priority, T item) {
        if (buckets.containsKey(priority)) {
            buckets.get(priority).addLast(item);
        } else {
            PriorityBucket<T> bucket = new PriorityBucket<>(priority);
            bucket.addLast(item);
            buckets.put(priority, bucket);
        }
    }

    /**
     * 获取指定优先级对应的Bucket
     */
    public synchronized PriorityBucket<T> getBucket(long priority) {
        if (buckets.containsKey(priority)) {
            return buckets.get(priority);
        } else {
            throw new IllegalStateException("Bucket not found for priority " + priority);
        }
    }

    public PriorityTaskCallback<T> getCallback() {
        return callback;
    }
}