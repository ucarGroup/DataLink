package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.task.TaskSyncStatus;

import java.util.Collection;

/**
 * Created by lubiao on 2019/2/18.
 */
public interface TaskSyncStatusService {

    /**
     * 更新指定task的同步状态信息
     *
     * @param taskId
     */
    void updateSyncStatus(String taskId, TaskSyncStatus syncStatus);

    /**
     * 获取指定Task的同步状态信息，如果没有则返回null
     * @param taskId
     * @return
     */
    TaskSyncStatus getSyncStatus(String taskId);

    /**
     * 获取所有Task的同步状态信息
     */
    Collection<TaskSyncStatus> getAll();
}
