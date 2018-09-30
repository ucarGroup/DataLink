package com.ucar.datalink.manager.core.monitor.impl;

import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.biz.service.TaskStatusService;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.manager.core.monitor.Monitor;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by csf on 17/5/26.
 */
@Service
public class TaskStatusMonitor extends Monitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStatusMonitor.class);

    @Autowired
    TaskConfigService taskService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    AlarmService alarmService;

    @Override
    public void doMonitor() {
        List<TaskInfo> taskInfos = taskService.getList();
        Collection<TaskStatus> allStatus = taskStatusService.getAll();
        taskInfos.stream().forEach(t ->
                {
                    boolean flag = false;
                    for (TaskStatus s : allStatus) {
                        if (t.getId().toString().equals(s.getId())) {
                            if (TaskStatus.State.RUNNING.name().equals(s.getState().name())
                                    && TargetState.STARTED.name().equals(t.getTargetState().name())) {
                                //do nothing
                            } else if (TaskStatus.State.PAUSED.name().equals(s.getState().name()) &&
                                    TargetState.PAUSED.name().equals(t.getTargetState().name())) {
                                //do nothing
                            } else {
                                alarmAndRestart(t, t.getTargetState(), s.getState());
                            }
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        alarmAndRestart(t, t.getTargetState(), TaskStatus.State.UNASSIGNED);
                    }
                }
        );
    }

    private void alarmAndRestart(TaskInfo taskInfo, TargetState targetState, TaskStatus.State actualState) {
        MonitorInfo monitorInfo = monitorService.getByResourceAndType(taskInfo.getId(), MonitorType.TASK_STATUS_MONITOR);
        if (isAlarm(taskInfo.getId(), Long.MAX_VALUE, monitorInfo)) {
            //do alarm
            alarmService.alarmTaskStatus(monitorInfo, taskInfo, targetState, actualState);
        }

        //do restart
        if (targetState == TargetState.STARTED && actualState == TaskStatus.State.FAILED) {
            restartTask(String.valueOf(taskInfo.getId()));
        }
    }

    private void restartTask(String taskId) {
        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();
        ClusterState.MemberData memberData = clusterState.getMemberData(Long.valueOf(taskId));
        String url = "http://" + memberData.getWorkerState().url() + "/tasks/" + taskId + "/restart";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity request = new HttpEntity(null, headers);
        Map<String, String> result = new RestTemplate().postForObject(url, request, Map.class);
    }
}
