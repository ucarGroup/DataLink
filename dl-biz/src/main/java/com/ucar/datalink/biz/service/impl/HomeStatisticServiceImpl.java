package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.HomeStatisticDAO;
import com.ucar.datalink.biz.service.HomeStatisticService;
import com.ucar.datalink.domain.statis.HomeStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2018/4/17.
 */
@Service
public class HomeStatisticServiceImpl implements HomeStatisticService {

    @Autowired
    HomeStatisticDAO homeStatisticDAO;

    @Override
    public List<HomeStatistic> taskSizeStatistic(Long groupId, Date startTime, Date endTime) {
        return homeStatisticDAO.taskSizeStatistic(groupId, startTime, endTime);
    }

    @Override
    public List<HomeStatistic> taskRecordStatistic(Long groupId, Date startTime, Date endTime) {
        return homeStatisticDAO.taskRecordStatistic(groupId, startTime, endTime);
    }

    @Override
    public List<HomeStatistic> taskDelayStatistic(Long groupId, Date startTime, Date endTime) {
        return homeStatisticDAO.taskDelayStatistic(groupId, startTime, endTime);
    }

    @Override
    public List<HomeStatistic> workerJvmUsedStatistic(Long groupId, Date startTime, Date endTime) {
        return homeStatisticDAO.workerJvmUsedStatistic(groupId, startTime, endTime);
    }

    @Override
    public List<HomeStatistic> workerYoungGCCountStatistic(Long groupId, Date startTime, Date endTime) {
        return homeStatisticDAO.workerYoungGCCountStatistic(groupId, startTime, endTime);
    }

    @Override
    public List<HomeStatistic> workerNetTrafficStatistic(Long groupId, Date startTime, Date endTime) {
        return homeStatisticDAO.workerNetTrafficStatistic(groupId, startTime, endTime);
    }
}
