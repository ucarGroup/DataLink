package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.worker.api.task.TaskAttributes;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lubiao on 2018/5/14.
 */
public class WorkerTaskAttributes implements TaskAttributes {
    private final ConcurrentHashMap<String, Object> attributes;

    public WorkerTaskAttributes() {
        this.attributes = new ConcurrentHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return attributes.keys();
    }
}
