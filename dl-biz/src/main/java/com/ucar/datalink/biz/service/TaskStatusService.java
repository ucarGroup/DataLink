package com.ucar.datalink.biz.service;

import com.ucar.datalink.common.errors.TaskConflictException;
import com.ucar.datalink.domain.task.TaskStatus;

import java.util.Collection;
import java.util.Set;

/**
 * Created by lubiao on 2016/12/5.
 */
public interface TaskStatusService {

    /**
     * create status and set the state of the task to the given value .
     *
     * @param status the status of the task
     */
    void addStatus(TaskStatus status) throws TaskConflictException;

    /**
     * update the state of the task to the given value.
     *
     * @param status the status of the task
     */
    void updateStatus(TaskStatus status);

    /**
     * delete the state of the task
     *
     * @param taskId the identifier of the task.
     */
    void removeStatus(String taskId);

    /**
     * Get the states of all tasks.
     *
     * @return a map parseFrom task ids to their respective status
     */
    Collection<TaskStatus> getAll();

    /**
     * Get the state of the specified task,if not exist return null.
     *
     * @param taskId the identifier of the task.
     * @return
     */
    TaskStatus getStatus(String taskId);

    /**
     * Get all cached tasks.
     *
     * @return the set of tasks names
     */
    Set<String> tasks();
}
