package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.AlarmPriorityDAO;
import com.ucar.datalink.biz.service.AlarmPriorityService;
import com.ucar.datalink.domain.alarm.AlarmPriorityInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlarmPriorityServiceImpl implements AlarmPriorityService {

    @Autowired
    private AlarmPriorityDAO taskPriorityDAO;

    @Override
    public List<AlarmPriorityInfo> getTaskPriorityList(String name, Integer priority) {
        return taskPriorityDAO.getTaskPriorityList(name,priority);
    }

    public Boolean insert(AlarmPriorityInfo taskPriorityInfo) {
        Integer num = taskPriorityDAO.insert(taskPriorityInfo);
        if (num > 0) {
            return true;
        }

        return false;
    }

    @Override
    public AlarmPriorityInfo getById(Long id) {
        return taskPriorityDAO.getById(id);
    }

    @Override
    public Boolean update(AlarmPriorityInfo taskPriorityInfo) {
        Integer num = taskPriorityDAO.update(taskPriorityInfo);
        if (num > 0) {
            return true;
        }

        return false;
    }

    @Override
    public Boolean delete(Long id) {
        Integer num = taskPriorityDAO.delete(id);
        if (num > 0) {
            return true;
        }

        return false;
    }

    @Override
    public List<AlarmPriorityInfo> getAll() {
        return taskPriorityDAO.getAll();
    }

    @Override
    public List<AlarmPriorityInfo> getPriorityListByIds(List<Long> priorityIds) {
        if(priorityIds == null || priorityIds.size()<1) {
            return new ArrayList<>();
        }
        return taskPriorityDAO.getPriorityListByIds(priorityIds);
    }
}
