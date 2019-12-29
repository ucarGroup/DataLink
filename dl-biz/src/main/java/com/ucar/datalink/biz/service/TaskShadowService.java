package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.task.TaskShadowInfo;

import java.util.List;

/**
 * Created by lubiao on 2019/8/5.
 */
public interface TaskShadowService {
    void createTaskShadow(TaskShadowInfo taskShadowInfo);

    TaskShadowInfo getTaskShadowById(Long id);

    TaskShadowInfo getExecutingTaskShadow(Long taskId);

    TaskShadowInfo takeOneTaskShadow(Long taskId);

    void startTaskShadow(TaskShadowInfo taskShadowInfo);

    void completeTaskShadow(TaskShadowInfo taskShadowInfo);

    boolean discardTaskShadow(long id);

    List<TaskShadowInfo> taskShadowListsForQueryPage(Long taskId, String state);
}
