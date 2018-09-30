package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.TaskStatusMismatchLogDAO;
import com.ucar.datalink.biz.service.TaskStatusMismatchLogService;
import com.ucar.datalink.domain.task.TaskStatusMismatchLogInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lubiao on 2018/4/25.
 */
@Service
public class TaskStatusMismatchLogServiceImpl implements TaskStatusMismatchLogService {

    @Autowired
    private TaskStatusMismatchLogDAO taskStatusMismatchLogDAO;

    @Override
    public void insert(TaskStatusMismatchLogInfo taskStatusMismatchLogInfo) {
        taskStatusMismatchLogDAO.insert(taskStatusMismatchLogInfo);
    }

    @Override
    public List<TaskStatusMismatchLogInfo> getLatestList() {
        return taskStatusMismatchLogDAO.getLatestList();
    }
}
