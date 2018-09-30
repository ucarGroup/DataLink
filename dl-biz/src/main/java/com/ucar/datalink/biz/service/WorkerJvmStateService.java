package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.worker.WorkerJvmStateInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2018/1/19.
 */
public interface WorkerJvmStateService {

    Boolean insert(WorkerJvmStateInfo jvmMonitorInfo);

    List<WorkerJvmStateInfo> getLatestList();

    WorkerJvmStateInfo getLatestByWorkerId(Long workerId);

    List<WorkerJvmStateInfo> getListByWorkerIdForQuery(@Param("workerId") Long workerId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
