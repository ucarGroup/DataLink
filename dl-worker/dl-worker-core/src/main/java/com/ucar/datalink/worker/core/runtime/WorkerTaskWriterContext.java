package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.worker.api.task.TaskAttributes;
import com.ucar.datalink.worker.api.task.TaskSession;
import com.ucar.datalink.worker.api.task.TaskWriterContext;

/**
 * Implementation of the TaskWriterContext.
 * <p>
 * Created by lubiao on 2017/3/9.
 */
public class WorkerTaskWriterContext implements TaskWriterContext {
    private final WorkerTaskContext workerTaskContext;
    private final PluginWriterParameter writerParameter;
    private final WorkerTaskAttributes taskWriterAttributes;
    private final WorkerTaskSession taskWriterSession;

    public WorkerTaskWriterContext(WorkerTaskContext workerTaskContext, PluginWriterParameter writerParameter) {
        this.workerTaskContext = workerTaskContext;
        this.writerParameter = writerParameter;
        this.taskWriterAttributes = new WorkerTaskAttributes();
        this.taskWriterSession = new WorkerTaskSession();
    }

    @Override
    public PluginWriterParameter getWriterParameter() {
        return writerParameter;
    }

    @Override
    public String taskId() {
        return workerTaskContext.taskId();
    }

    @Override
    public String taskExecutionId() {
        return workerTaskContext.taskExecutionId();
    }

    @Override
    public <T> T getService(Class<? extends T> clazz) {
        return workerTaskContext.getService(clazz);
    }

    @Override
    public TaskSession taskSession() {
        return workerTaskContext.taskSession();
    }

    @Override
    public TaskSession taskWriterSession() {
        return taskWriterSession;
    }

    @Override
    public TaskAttributes taskAttributes() {
        return workerTaskContext.taskAttributes();
    }

    @Override
    public TaskAttributes taskWriterAttributes() {
        return taskWriterAttributes;
    }

    @Override
    public void beginSession() {
        taskWriterSession.reset();
    }
}
