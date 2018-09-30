package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.task.TaskStatusMismatchLogInfo;

import java.util.List;

/**
 * Created by lubiao on 2018/4/25.
 */
public interface TaskStatusMismatchLogDAO {

    void insert(TaskStatusMismatchLogInfo taskStatusMismatchLogInfo);

    List<TaskStatusMismatchLogInfo> getLatestList();
}
