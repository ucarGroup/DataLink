package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.WorkerJvmStateDAO;
import com.ucar.datalink.biz.service.WorkerJvmStateService;
import com.ucar.datalink.domain.worker.WorkerJvmStateInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2018/1/19.
 */
@Service
public class WorkerJvmStateServiceImpl implements WorkerJvmStateService {

    @Autowired
    WorkerJvmStateDAO workerJvmStateDAO;

    @Override
    public Boolean insert(WorkerJvmStateInfo jvmMonitorInfo) {
        Integer num = workerJvmStateDAO.insert(jvmMonitorInfo);
        return num > 0;
    }

    @Override
    public List<WorkerJvmStateInfo> getLatestList() {
        return workerJvmStateDAO.getLatestList();
    }

    @Override
    public WorkerJvmStateInfo getLatestByWorkerId(Long workerId) {
        return workerJvmStateDAO.getLatestByWorkerId(workerId);
    }

    @Override
    public List<WorkerJvmStateInfo> getListByWorkerIdForQuery(@Param("workerId") Long workerId, @Param("startTime") Date startTime, @Param("endTime") Date endTime) {
        return workerJvmStateDAO.getListByWorkerIdForQuery(workerId, startTime, endTime);
    }
}
