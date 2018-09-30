package com.ucar.datalink.worker.core.runtime;

/**
 *
 * Created by lubiao on 2017/1/3.
 */
public interface TaskConfigUpdateListener {
    /**
     * Invoked when a task configuration has been removed
     *
     * @param taskId identifier of the task
     */
    void onTaskConfigRemove(String taskId);

    /**
     * Invoked when a task configuration has been added.
     *
     * @param taskId identifier of the task
     */
    void onTaskConfigAdd(String taskId);

    /**
     * Invoked when task configs are updated.
     *
     * @param taskId identifier of the task
     */
    void onTaskConfigUpdate(String taskId);

    /**
     * Invoked when task state are changed.
     *
     * @param taskId identifier of the task
     */
    void onTaskStateChanged(String taskId);
}
