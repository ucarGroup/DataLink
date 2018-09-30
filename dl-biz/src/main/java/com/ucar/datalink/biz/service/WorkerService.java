package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.worker.WorkerInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by lubiao on 2017/1/17.
 */
public interface WorkerService {

    List<WorkerInfo> getList();

    Boolean insert(WorkerInfo workerInfo);

    Boolean update(WorkerInfo workerInfo);

    Boolean delete(Long id);

    WorkerInfo getById(Long id);

    WorkerInfo getByAddress(String ip);

    Integer workerCount();

    List<StatisDetail> getCountByGroup();

    List<WorkerInfo> getListForQuery(@Param("groupId")Long groupId);
}
