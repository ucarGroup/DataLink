package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.TaskStatisticDAO;
import com.ucar.datalink.biz.service.TaskStatisticService;
import com.ucar.datalink.domain.task.TaskStatisticInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2018/2/28.
 */
@Service
public class TaskStatisticServiceImpl implements TaskStatisticService{

    @Autowired
    TaskStatisticDAO taskStatisticDAO;

    @Override
    public Boolean insert(TaskStatisticInfo taskStatisticInfo) {
        Integer num = taskStatisticDAO.insert(taskStatisticInfo);
        return num > 0;
    }

    @Override
    public List<TaskStatisticInfo> getListByTaskIdForQuery(@Param("taskId") Long taskId, @Param("startTime") Date startTime, @Param("endTime") Date endTime) {
        return taskStatisticDAO.getListByTaskIdForQuery(taskId, startTime, endTime);
    }
}
