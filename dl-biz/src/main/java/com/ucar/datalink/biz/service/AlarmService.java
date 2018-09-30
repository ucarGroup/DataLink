package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.domain.task.TaskStatusMismatchLogInfo;
import com.ucar.datalink.domain.worker.WorkerInfo;

/**
 * Created by lubiao on 2018/3/1.
 */
public interface AlarmService {

    void alarmDelay(MonitorInfo monitorInfo, long delayTime);

    void alarmError(MonitorInfo monitorInfo, String errorMsg);

    void alarmTaskStatus(MonitorInfo monitorInfo, TaskInfo taskInfo, TargetState targetState, TaskStatus.State actualState);

    void alarmWorkerJvmState(MonitorInfo monitorInfo, Long jvmUsedRate);

    void alarmWorkerStatus(MonitorInfo monitorInfo, WorkerInfo workerInfo);

    void alarmTaskStatusMismatch(MonitorInfo monitorInfo, TaskStatusMismatchLogInfo taskStatusMismatchLogInfo);
}
