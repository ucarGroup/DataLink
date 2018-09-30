package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.task.TaskStatusMismatchLogInfo;

import java.util.List;

/**
 * Created by lubiao on 2018/4/25.
 */
public interface TaskStatusMismatchLogService {

    void insert(TaskStatusMismatchLogInfo taskStatusMismatchLogInfo);

    List<TaskStatusMismatchLogInfo> getLatestList();
}
