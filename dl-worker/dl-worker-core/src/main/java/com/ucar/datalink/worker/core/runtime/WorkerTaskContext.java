package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.worker.api.task.TaskAttributes;
import com.ucar.datalink.worker.api.task.TaskContext;
import com.ucar.datalink.worker.api.task.TaskSession;

/**
 * The implement of the TaskContext.
 * <p>
 * Created by lubiao on 2016/12/26.
 */
public class WorkerTaskContext implements TaskContext {
    private final String taskId;
    private final String taskExecutionId;
    private final WorkerTaskAttributes taskAttributes;
    private final WorkerTaskSession taskSession;

    public WorkerTaskContext(String taskId, String taskExecutionId) {
        this.taskId = taskId;
        this.taskExecutionId = taskExecutionId;
        this.taskAttributes = new WorkerTaskAttributes();
        this.taskSession = new WorkerTaskSession();
    }

    @Override
    public String taskId() {
        return taskId;
    }

    @Override
    public String taskExecutionId() {
        return taskExecutionId;
    }

    @Override
    public <T> T getService(Class<? extends T> clazz) {
        return DataLinkFactory.getObject(clazz);
    }

    @Override
    public TaskSession taskSession() {
        return taskSession;
    }

    @Override
    public TaskAttributes taskAttributes() {
        return taskAttributes;
    }

    @Override
    public void beginSession() {
        taskSession.reset();
    }
}
