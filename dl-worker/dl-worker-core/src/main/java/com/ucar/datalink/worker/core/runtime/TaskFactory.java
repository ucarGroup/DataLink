package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.worker.api.task.TaskReader;
import com.ucar.datalink.worker.api.task.TaskWriter;

/**
 * Task工厂类.
 * <p>
 * Created by lubiao on 2016/12/6.
 */
public class TaskFactory {
    public static <T extends TaskReader> T newTaskReader(Class<T> taskClass) {
        return instantiate(taskClass);
    }

    public static <T extends TaskWriter> T newTaskWriter(Class<T> taskClass) {
        return instantiate(taskClass);
    }

    private static <T> T instantiate(Class<? extends T> cls) {
        try {
            return newInstance(cls);
        } catch (Throwable t) {
            throw new DatalinkException("Instantiation error", t);
        }
    }

    /**
     * Instantiate the class
     */
    private static <T> T newInstance(Class<T> c) {
        try {
            return c.newInstance();
        } catch (IllegalAccessException e) {
            throw new DatalinkException("Could not instantiate class " + c.getName(), e);
        } catch (InstantiationException e) {
            throw new DatalinkException("Could not instantiate class " + c.getName() + " Does it have a public no-argument constructor?", e);
        } catch (NullPointerException e) {
            throw new DatalinkException("Requested class was null", e);
        }
    }
}
