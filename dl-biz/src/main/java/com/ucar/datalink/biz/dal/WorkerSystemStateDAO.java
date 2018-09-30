package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.worker.WorkerSystemStateInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by qianqian.shi on 2018/5/23.
 */
public interface WorkerSystemStateDAO {

    void insert(WorkerSystemStateInfo workerSystemStateInfo);

    List<WorkerSystemStateInfo> getListByWorkerIdForQuery(@Param("workerId") Long workerId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
