package com.ucar.datalink.manager.core.monitor.impl;

import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.domain.alarm.AlarmStrategyInfo;
import com.ucar.datalink.domain.alarm.StrategyConfig;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskSyncStatus;
import com.ucar.datalink.manager.core.monitor.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by qianqian.shi on 2019/3/22.
 */
@Service
public class TaskSyncStatusMonitor extends Monitor {

    private static final Logger logger = LoggerFactory.getLogger(TaskSyncStatusMonitor.class);

    @Autowired
    TaskSyncStatusService taskSyncStatusService;

    @Autowired
    TaskConfigService taskService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    AlarmService alarmService;

    @Autowired
    private AlarmStrategyService alarmStrategyService;

    private Map<String, Long> taskLastUpdateTime = new ConcurrentHashMap<>();

    @Override
    public void doMonitor() {

        try{

            Collection<TaskSyncStatus> allSyncStatus = taskSyncStatusService.getAll();
            for (TaskSyncStatus syncStatus : allSyncStatus) {
                Long taskId = Long.valueOf(syncStatus.getId());
                TaskInfo taskInfo = taskService.getTask(taskId);
                if(taskInfo == null || syncStatus.getState() == null){
                    continue;
                }
                if (taskInfo.getTargetState() != TargetState.PAUSED && syncStatus.getState() == TaskSyncStatus.State.Busy) {
                    Long currentUpdateTime = syncStatus.getUpdateTime();
                    Long lastUpdateTime = taskLastUpdateTime.putIfAbsent(syncStatus.getId(), currentUpdateTime);
                    if (currentUpdateTime.equals(lastUpdateTime)) {
                        Long currentTime = System.currentTimeMillis();
                        Long busyTime = currentTime - currentUpdateTime;
                        MonitorInfo monitorInfo = monitorService.getByResourceAndType(Long.valueOf(syncStatus.getId()), MonitorType.TASK_SYNC_STATUS_MONITOR);
                        AlarmStrategyInfo alarmStrategyInfo = alarmStrategyService.getByTaskIdAndType(monitorInfo.getResourceId(),monitorInfo.getMonitorType());
                        if(alarmStrategyInfo != null) {
                            StrategyConfig config = alarmStrategyService.getStrategyConfig(alarmStrategyInfo.getStrategys());
                            monitorService.copyStrategy(config,monitorInfo);
                        }
                        if (isAlarm(taskId, busyTime, monitorInfo)) {
                            alarmService.alarmTaskSyncStatus(monitorInfo, busyTime);
                        }
                    }
                }
            }

        }catch (Exception e){
            logger.error("监控任务同步状态出现异常,该异常不影响业务，只日志记录即可,异常是: {}", e);
        }

    }
}
