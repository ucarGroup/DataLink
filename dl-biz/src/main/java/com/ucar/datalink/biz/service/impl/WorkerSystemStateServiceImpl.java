package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.WorkerSystemStateDAO;
import com.ucar.datalink.biz.service.WorkerSystemStateService;
import com.ucar.datalink.domain.worker.WorkerSystemStateInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by qianqian.shi on 2018/5/23.
 */
@Service
public class WorkerSystemStateServiceImpl implements WorkerSystemStateService {

    @Autowired
    WorkerSystemStateDAO workerSystemStateDAO;

    @Override
    public void insert(WorkerSystemStateInfo workerSystemStateInfo) {
        workerSystemStateDAO.insert(workerSystemStateInfo);
    }

    @Override
    public List<WorkerSystemStateInfo> getListByWorkerIdForQuery(@Param("workerId") Long workerId, @Param("startTime") Date startTime, @Param("endTime") Date endTime) {
        return workerSystemStateDAO.getListByWorkerIdForQuery(workerId, startTime, endTime);
    }
}
