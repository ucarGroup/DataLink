package com.ucar.datalink.manager.core.monitor.impl;

import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.service.TaskDelayTimeService;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.domain.task.TaskDelayTimeInfo;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.manager.core.monitor.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by csf on 17/5/2.
 */
@Service
public class TaskDelayTimeMonitor extends Monitor {

    public static final Logger LOGGER = LoggerFactory.getLogger(TaskDelayTimeMonitor.class);

    @Autowired
    TaskDelayTimeService taskDelayTimeService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    AlarmService alarmService;

    @Override
    public void doMonitor() {
        List<TaskDelayTimeInfo> list = taskDelayTimeService.getList();
        if (list == null || list.size() == 0) {
            return;
        }
        for (TaskDelayTimeInfo delayTime : list) {
            MonitorInfo monitorInfo = monitorService.getByResourceAndType(delayTime.getTaskId(), MonitorType.TASK_DELAY_MONITOR);
            if (!isMoreThan3min(delayTime.getCreateTime()) && isAlarm(delayTime.getTaskId(), delayTime.getDelayTime(), monitorInfo)) {
                alarmService.alarmDelay(monitorInfo, delayTime.getDelayTime());
            }
        }
    }

    private Boolean isMoreThan3min(Date createTime) {
        Long currentTime = System.currentTimeMillis();
        if (currentTime - createTime.getTime() >= 1000 * 60 * 3) {
            return true;
        }
        return false;
    }
}
