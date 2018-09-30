package com.ucar.datalink.manager.core.monitor.impl;

import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.biz.service.WorkerJvmStateService;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.domain.worker.WorkerJvmStateInfo;
import com.ucar.datalink.manager.core.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sqq on 2018/1/17.
 */
@Service
public class WorkerJvmStateMonitor extends Monitor {

    @Autowired
    WorkerJvmStateService workerJvmStateService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    AlarmService alarmService;

    @Override
    public void doMonitor() {
        List<WorkerJvmStateInfo> list = workerJvmStateService.getLatestList();
        if (list == null || list.size() == 0) {
            return;
        }
        for (WorkerJvmStateInfo jvmStateInfo : list) {
            MonitorInfo monitorInfo = monitorService.getByResourceAndType(jvmStateInfo.getWorkerId(), MonitorType.WORKER_JVM_STATE_MONITOR);
            Double jvmUsedDouble = (double) (jvmStateInfo.getYoungMemUsed() + jvmStateInfo.getOldMemUsed()) * 100 / (double) (jvmStateInfo.getYoungMemMax() + jvmStateInfo.getOldMemMax());
            Long jvmUsedRate = jvmUsedDouble.longValue();
            if (isAlarm(jvmStateInfo.getWorkerId(), jvmUsedRate, monitorInfo)) {
                alarmService.alarmWorkerJvmState(monitorInfo, jvmUsedRate);
            }
        }
    }


}
