package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.TaskTraceDAO;
import com.ucar.datalink.biz.service.TaskTraceService;
import com.ucar.datalink.domain.task.TaskTraceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TaskTraceServiceImpl implements TaskTraceService {

    @Autowired
    TaskTraceDAO taskTraceDAO;

    @Override
    public Boolean insert(TaskTraceInfo taskTraceInfo) {
        Integer num = taskTraceDAO.insert(taskTraceInfo);
        return num > 0;
    }

    @Override
    public Boolean update(TaskTraceInfo taskTraceInfo){
        Integer num = taskTraceDAO.update(taskTraceInfo);
        return num > 0;
    }


    @Override
    public Boolean updateAndInsert(TaskTraceInfo oldTraceInfo,TaskTraceInfo newTraceInfo) {
        Integer num1 = taskTraceDAO.update(oldTraceInfo);
        Integer num2 = taskTraceDAO.insert(newTraceInfo);
        return num1 > 0 && num2 > 0;
    }

    @Override
    public List<TaskTraceInfo> findListByTaskId(Long taskId, Date startTime, Date endTime) {
        return taskTraceDAO.findListByTaskId(taskId,startTime,endTime);
    }

}
