package com.ucar.datalink.manager.core.monitor.impl;

import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.biz.service.TaskStatusMismatchLogService;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.domain.task.TaskStatusMismatchLogInfo;
import com.ucar.datalink.manager.core.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by qianqian.shi on 2018/5/29.
 */
@Service
public class TaskStatusMismatchMonitor extends Monitor {

    @Autowired
    TaskStatusMismatchLogService taskStatusMismatchLogService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    AlarmService alarmService;

    @Override
    public void doMonitor() {
        List<TaskStatusMismatchLogInfo> list = taskStatusMismatchLogService.getLatestList();
        if (list == null || list.size() == 0) {
            return;
        }
        for (TaskStatusMismatchLogInfo statusMismatchLogInfo : list) {
            MonitorInfo monitorInfo = monitorService.getByResourceAndType(statusMismatchLogInfo.getTaskId(), MonitorType.TASK_STATUS_MISMATCH_MONITOR);
            if (!isMoreThan5Min(statusMismatchLogInfo.getCreateTime()) && isAlarm(statusMismatchLogInfo.getTaskId(), Long.MAX_VALUE, monitorInfo)) {
                alarmService.alarmTaskStatusMismatch(monitorInfo, statusMismatchLogInfo);
            }
        }
    }

    private Boolean isMoreThan5Min(Date createTime) {
        Long currentTime = System.currentTimeMillis();
        return currentTime - createTime.getTime() >= 1000 * 60 * 5;
    }
}
