package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.worker.api.task.TaskSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lubiao on 2017/3/17.
 */
public class WorkerTaskSession implements TaskSession {
    /**
     * Session数据保存容器
     */
    private ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();

    @Override
    public <K, V> void setData(K key, V value) {
        map.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> V getData(K key) {
        return (V)map.get(key);
    }

    @Override
    public <K> void removeData(K key) {
        map.remove(key);
    }

    public void reset() {
        map.clear();
    }
}
