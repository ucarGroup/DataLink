package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.worker.WorkerInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by lubiao on 2017/1/16.
 */
public interface WorkerDAO {

    List<WorkerInfo> getList();

    Integer insert(WorkerInfo workerInfo);

    Integer update(WorkerInfo workerInfo);

    Integer delete(Long id);

    WorkerInfo getById(Long id);

    List<WorkerInfo> listByGroupId(Long groupId);

    List<WorkerInfo> getByAddress(String address);

    Integer workerCount();

    List<StatisDetail> getCountByGroup();

    List<WorkerInfo> getListForQuery(@Param("groupId")Long groupId);
}
