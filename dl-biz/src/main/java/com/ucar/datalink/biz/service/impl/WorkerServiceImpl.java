package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.MonitorDAO;
import com.ucar.datalink.biz.dal.WorkerDAO;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.monitor.MonitorCat;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.biz.service.WorkerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lubiao on 2017/1/17.
 */
@Service
public class WorkerServiceImpl implements WorkerService {

    public static final Logger LOGGER = LoggerFactory.getLogger(WorkerServiceImpl.class);

    @Autowired
    private WorkerDAO workerDAO;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private MonitorDAO monitorDAO;


    @Override
    public List<WorkerInfo> getList() {
        return workerDAO.getList();
    }

    @Override
    @Transactional
    public Boolean insert(WorkerInfo workerInfo) {
        Integer num = workerDAO.insert(workerInfo);
        monitorService.createAllMonitor(workerInfo.getId(), MonitorCat.WORKER_MONITOR);
        if (num > 0) {
            return true;
        }

        return false;
    }

    @Override
    public Boolean update(WorkerInfo workerInfo) {
        Integer num = workerDAO.update(workerInfo);
        if (num >= 0) {
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Integer num = workerDAO.delete(id);
        monitorDAO.deleteByResourceIdAndCat(id, MonitorCat.WORKER_MONITOR.getKey());
        if (num >= 0) {
            return true;
        }

        return false;
    }

    @Override
    public WorkerInfo getById(Long id) {
        return workerDAO.getById(id);
    }

    @Override
    public WorkerInfo getByAddress(String ip) {
        List<WorkerInfo> workerInfos = workerDAO.getByAddress(ip);
        if (workerInfos != null && !workerInfos.isEmpty()) {
            if (workerInfos.size() > 1) {
                throw new DatalinkException("The Count of Worker with IP " + ip + " is more than one.");
            } else {
                return workerInfos.get(0);
            }
        } else {
            return null;
        }
    }

    @Override
    public Integer workerCount() {
        return workerDAO.workerCount();
    }

    @Override
    public List<StatisDetail> getCountByGroup() {
        return workerDAO.getCountByGroup();
    }

    @Override
    public List<WorkerInfo> getListForQuery(Long groupId) {
        return workerDAO.getListForQuery(groupId);
    }
}
