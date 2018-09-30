package com.ucar.datalink.manager.core.monitor.impl;

import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.biz.service.WorkerService;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.manager.core.monitor.Monitor;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sqq on 2018/1/17.
 */
@Service
public class WorkerRunningStateMonitor extends Monitor {

    @Autowired
    WorkerService workerService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    AlarmService alarmService;

    @Override
    public void doMonitor() {
        List<WorkerInfo> workerList = workerService.getList();
        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();
        List<ClusterState.MemberData> memberList = clusterState.getAllMemberData();
        for (WorkerInfo worker : workerList) {
            MonitorInfo monitorInfo = monitorService.getByResourceAndType(worker.getId(), MonitorType.WORKER_RUNNING_STATE_MONITOR);
            if (memberList != null && memberList.size() > 0) {
                if (!memberList.stream().filter(m -> m.getClientId().equals(worker.getId().toString())).findAny().isPresent()) {
                    if (isAlarm(worker.getId(), Long.MAX_VALUE, monitorInfo)) {
                        alarmService.alarmWorkerStatus(monitorInfo, worker);
                    }
                }
            }

        }
    }
}
