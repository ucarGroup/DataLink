package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.task.TaskExceptionInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2018/3/1.
 */
public interface TaskExceptionService {

    Boolean insert(TaskExceptionInfo taskExceptionInfo);

    TaskExceptionInfo getById(Long id);

    List<TaskExceptionInfo> getListByTaskIdForQuery(@Param("taskId") Long taskId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
