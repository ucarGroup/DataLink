package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.biz.service.TaskStatusMismatchLogService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.TaskConflictException;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.biz.service.TaskStatusService;
import com.ucar.datalink.domain.task.TaskStatusMismatchLogInfo;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Task任务状态管理器.
 *
 * Created by lubiao on 2017/2/23.
 */
public class TaskStatusManager {
    private final static Logger logger = LoggerFactory.getLogger(TaskStatusManager.class);

    private final TaskStatusService taskStatusService;
    private final Map<String, TaskStatus> taskStatusMap;
    private final IZkStateListener zkStateListener;

    public TaskStatusManager(TaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
        this.taskStatusMap = new ConcurrentHashMap<>();
        this.zkStateListener = new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                logger.info("received handle state changed event.");
            }

            @Override
            public void handleNewSession() throws Exception {
                logger.info("received handle new session event.");
                reRegisterStatus();
            }
        };
    }

    public synchronized void start() {
        DLinkZkUtils.get()
                .zkClient()
                .subscribeStateChanges(zkStateListener);
    }

    public synchronized void stop() {
        this.taskStatusMap.clear();
        DLinkZkUtils.get()
                .zkClient()
                .unsubscribeStateChanges(zkStateListener);
    }

    public synchronized void addStatus(TaskStatus taskStatus) throws TaskConflictException {
        this.taskStatusService.addStatus(taskStatus);
        this.taskStatusMap.put(taskStatus.getId(), taskStatus);
    }

    public synchronized void updateStatus(TaskStatus taskStatus) {
        if (isMatch(taskStatus, TaskStatusMismatchLogInfo.ActionType.UPDATE)) {
            this.taskStatusService.updateStatus(taskStatus);
        }
        this.taskStatusMap.put(taskStatus.getId(), taskStatus);
    }

    public synchronized void removeStatus(String taskId) {
        if (isMatch(taskStatusMap.get(taskId), TaskStatusMismatchLogInfo.ActionType.REMOVE)) {
            this.taskStatusService.removeStatus(taskId);
        }
        this.taskStatusMap.remove(taskId);
    }

    private boolean isMatch(TaskStatus taskStatus, TaskStatusMismatchLogInfo.ActionType actionType) {
        TaskStatus remoteStatus = this.taskStatusService.getStatus(taskStatus.getId());
        if (remoteStatus != null && StringUtils.equals(remoteStatus.getExecutionId(), taskStatus.getExecutionId())) {
            return true;
        } else {
            logger.error(String.format("Task Status Mismatch when %s ,local status is %s,remote status is %s.",
                    actionType, taskStatus, remoteStatus));
            log4Query(taskStatus, remoteStatus, actionType);
            return false;
        }
    }

    private void log4Query(TaskStatus localStatus, TaskStatus remoteStatus, TaskStatusMismatchLogInfo.ActionType actionType) {
        TaskStatusMismatchLogService service = DataLinkFactory.getObject(TaskStatusMismatchLogService.class);
        TaskStatusMismatchLogInfo log = new TaskStatusMismatchLogInfo();
        log.setTaskId(Long.valueOf(localStatus.getId()));
        log.setWorkerId(Long.valueOf(localStatus.getWorkerId()));
        log.setActionType(actionType);
        log.setLocalStatus(localStatus.toString());
        log.setRemoteStatus(remoteStatus != null ? remoteStatus.toString() : null);
        service.insert(log);
    }

    private synchronized void reRegisterStatus() {
        if (!taskStatusMap.isEmpty()) {
            taskStatusMap.entrySet().stream().forEach(i -> {
                try {
                    this.taskStatusService.addStatus(i.getValue());
                } catch (Exception e) {
                    logger.error("something goes wrong when do re-register task status for task " + i.getKey(), e);
                }
            });
        }
    }
}
