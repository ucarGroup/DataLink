package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.TaskExceptionDAO;
import com.ucar.datalink.biz.service.TaskExceptionService;
import com.ucar.datalink.domain.task.TaskExceptionInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2018/3/1.
 */
@Service
public class TaskExceptionServiceImpl implements TaskExceptionService {

    @Autowired
    TaskExceptionDAO taskExceptionDAO;

    @Override
    public Boolean insert(TaskExceptionInfo taskExceptionInfo) {
        Integer num = taskExceptionDAO.insert(taskExceptionInfo);
        return num > 0;
    }

    @Override
    public TaskExceptionInfo getById(Long id) {
        return taskExceptionDAO.getById(id);
    }

    @Override
    public List<TaskExceptionInfo> getListByTaskIdForQuery(@Param("taskId") Long taskId, @Param("startTime") Date startTime, @Param("endTime") Date endTime) {
        return taskExceptionDAO.getListByTaskIdForQuery(taskId, startTime, endTime);
    }
}
