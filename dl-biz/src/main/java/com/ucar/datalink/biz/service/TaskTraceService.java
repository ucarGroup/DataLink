package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.task.TaskExceptionInfo;
import com.ucar.datalink.domain.task.TaskTraceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by djj on 2019/4/24.
 */
public interface TaskTraceService {

    Boolean insert(TaskTraceInfo taskTraceInfo);

    Boolean update(TaskTraceInfo taskTraceInfo);

    Boolean updateAndInsert(TaskTraceInfo oldTraceInfo,TaskTraceInfo newTraceInfo);

    List<TaskTraceInfo> findListByTaskId(Long taskId,Date startTime,Date endTime);
}
