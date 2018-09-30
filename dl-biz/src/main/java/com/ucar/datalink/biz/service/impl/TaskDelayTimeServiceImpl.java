package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.TaskDelayTimeDAO;
import com.ucar.datalink.biz.service.TaskDelayTimeService;
import com.ucar.datalink.domain.task.TaskDelayTimeInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by csf on 17/5/2.
 */
@Service
public class TaskDelayTimeServiceImpl implements TaskDelayTimeService {

    @Autowired
    private TaskDelayTimeDAO taskDelayTimeDAO;

    @Override
    public List<TaskDelayTimeInfo> getList() {
        return taskDelayTimeDAO.getList();
    }

    @Override
    public Boolean insert(TaskDelayTimeInfo alarmInfo) {
        Integer num = taskDelayTimeDAO.insert(alarmInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public List<TaskDelayTimeInfo> getListByTaskIdForQuery(@Param("taskId") Long taskId, @Param("startTime") Date startTime, @Param("endTime") Date endTime) {
        return taskDelayTimeDAO.getListByTaskIdForQuery(taskId, startTime, endTime);
    }
}
