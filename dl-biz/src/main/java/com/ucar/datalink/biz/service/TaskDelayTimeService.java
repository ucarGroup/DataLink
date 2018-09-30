package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.task.TaskDelayTimeInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by csf on 17/5/2.
 */
public interface TaskDelayTimeService {

    List<TaskDelayTimeInfo> getList();

    Boolean insert(TaskDelayTimeInfo alarmInfo);

    List<TaskDelayTimeInfo> getListByTaskIdForQuery(@Param("taskId") Long taskId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

}
