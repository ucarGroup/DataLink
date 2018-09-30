package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.domain.ClusterConfigState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.biz.service.TaskConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task配置信息管理器，负责发现Task配置变更并进行事件通知.
 * 以组为单位进行监控管理，不属于本组的变更不予关注.
 *
 * Created by lubiao on 2016/12/20.
 */
public class TaskConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(TaskConfigManager.class);

    private TaskConfigService taskConfigService;
    private TaskConfigUpdateListener updateListener;
    private final String groupId;
    private volatile long version;
    private List<TaskInfo> taskInfoList;

    public TaskConfigManager(String groupId, TaskConfigService taskConfigService) {
        this.groupId = groupId;
        this.taskConfigService = taskConfigService;
        this.version = -1;
    }

    public void start() {
        logger.info("Starting TaskConfigManager");
        forceRefresh();
        logger.info("Started TaskConfigManager");
    }

    public void stop() {
    }

    public void setUpdateListener(TaskConfigUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    /**
     * Get a snapshot of the current configuration state including all task configurations.
     *
     * @return the cluster config state
     */
    public synchronized ClusterConfigState snapshot() {
        return new ClusterConfigState(
                version,
                taskInfoList == null ? new ArrayList<>() : new ArrayList<>(taskInfoList));
    }

    /**
     * force to refresh and get the latest configs.
     */
    public synchronized void forceRefresh() {
        refresh();
    }

    /**
     * check if need to refresh the task configs,if so,do refresh and check which tasks has changed and call the update listener.
     * if config has changed return true,else return false.
     */
    public synchronized boolean tryRefresh() {
        Long version = taskConfigService.getTaskConfigVersion();
        if (version > this.version) {
            ClusterConfigState oldConfigState = snapshot();
            refresh();
            List<TaskInfo> addAndUpdateTasks = taskInfoList.stream().filter(t -> t.getModifyTimeMillSeconds() > oldConfigState.version()).collect(Collectors.toList());

            for (TaskInfo newConfig : addAndUpdateTasks) {
                if (oldConfigState.tasks().contains(newConfig.idString())) {
                    //process updated configs.
                    TaskInfo oldConfig = oldConfigState.taskConfig(newConfig.idString());
                    if (isRestartableUpdate(newConfig, oldConfig)) {
                        updateListener.onTaskConfigUpdate(newConfig.idString());
                    } else if (isStateChanged(newConfig, oldConfig)) {
                        updateListener.onTaskStateChanged(newConfig.idString());
                    } else {
                        logger.info("task config is changed,and with no need for calling updatelistener.", newConfig.idString());
                    }
                } else {
                    // process newly added configs.
                    //if the task is not contained in the olds, it is newly added.
                    updateListener.onTaskConfigAdd(newConfig.idString());
                }
            }
            for (String taskId : oldConfigState.tasks()) {
                //process deleted configs.
                if (!taskInfoList.stream().filter(t -> t.idString().equals(taskId)).findFirst().isPresent()) {
                    updateListener.onTaskConfigRemove(taskId);
                }
            }
            return true;
        }
        return false;
    }

    //初始化了taskConfigList和version
    private synchronized void refresh() {
        taskInfoList = taskConfigService.getActiveTaskConfigsByGroup(Long.valueOf(groupId));
        version = taskInfoList.isEmpty() ? 0 : taskInfoList.get(0).getVersion();
    }

    private boolean isRestartableUpdate(TaskInfo newConfig, TaskInfo oldConfig) {
        return !newConfig.getTaskParameter().equals(oldConfig.getTaskParameter())
                || !newConfig.getTaskReaderParameter().equals(oldConfig.getTaskReaderParameter())
                || !newConfig.getTaskWriterParameter().equals(oldConfig.getTaskWriterParameter());
    }

    private boolean isStateChanged(TaskInfo newConfig, TaskInfo oldConfig) {
        return !newConfig.getTargetState().equals(oldConfig.getTargetState());
    }
}
