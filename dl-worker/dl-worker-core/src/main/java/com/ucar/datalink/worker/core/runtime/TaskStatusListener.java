package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.errors.TaskConflictException;

/**
 * Created by lubiao on 2018/4/24.
 */
public interface TaskStatusListener {
    /**
     * Invoke before start, do mutex check , if the task were running in other place, throw TaskConflictException.
     *
     * @throws TaskConflictException
     */
    void onPrepare(TaskStatusEvent event) throws TaskConflictException;

    /**
     * Invoked after successful startup of the task.
     *
     */
    void onStartup(TaskStatusEvent event);

    /**
     * Invoked after the task has been paused.
     *
     */
    void onPause(TaskStatusEvent event);

    /**
     * Invoked after the task has been resumed.
     *
     */
    void onResume(TaskStatusEvent event);

    /**
     * Invoked if the task raises an error. No shutdown event will follow.
     *
     * @param cause  The error raised by the task.
     */
    void onFailure(TaskStatusEvent event, Throwable cause);

    /**
     * Invoked after successful shutdown of the task.
     *
     */
    void onShutdown(TaskStatusEvent event);
}
