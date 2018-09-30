package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.domain.plugin.PluginReaderParameter;
import com.ucar.datalink.worker.api.position.PositionManager;
import com.ucar.datalink.worker.api.task.TaskAttributes;
import com.ucar.datalink.worker.api.task.TaskReaderContext;
import com.ucar.datalink.worker.api.task.TaskSession;


/**
 * Implementation of the TaskReaderContext.
 * <p>
 * Created by lubiao on 2017/3/9.
 */
public class WorkerTaskReaderContext implements TaskReaderContext {
    private final WorkerTaskContext workerTaskContext;
    private final PositionManager positionManager;
    private final PluginReaderParameter readerParameter;
    private final WorkerTaskAttributes taskReaderAttributes;
    private final WorkerTaskSession taskReaderSession;

    public WorkerTaskReaderContext(WorkerTaskContext workerTaskContext, PositionManager positionManager,
                                   PluginReaderParameter readerParameter) {
        this.workerTaskContext = workerTaskContext;
        this.positionManager = positionManager;
        this.readerParameter = readerParameter;
        this.taskReaderAttributes = new WorkerTaskAttributes();
        this.taskReaderSession = new WorkerTaskSession();
    }

    @Override
    public PositionManager positionManager() {
        return positionManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PluginReaderParameter> T getReaderParameter() {
        return (T) readerParameter;
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
    public TaskSession taskReaderSession() {
        return taskReaderSession;
    }

    @Override
    public TaskAttributes taskAttributes() {
        return workerTaskContext.taskAttributes();
    }

    @Override
    public TaskAttributes taskReaderAttributes() {
        return taskReaderAttributes;
    }

    @Override
    public void beginSession() {
        //一次会话的起点是TaskReader,so,在此触发Global-Session的begin操作
        workerTaskContext.beginSession();
        taskReaderSession.reset();
    }
}
