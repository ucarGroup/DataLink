package com.ucar.datalink.domain;

import com.ucar.datalink.domain.task.TaskInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An immutable snapshot of the configuration state of tasks in a Datalink cluster.
 */
public class ClusterConfigState {
    public static final long NO_VERSION = -1;
    public static final ClusterConfigState EMPTY = new ClusterConfigState(
            NO_VERSION,
            Collections.<TaskInfo>emptyList());

    private final long version;//version是所有Task的版本，不仅仅局限于当前分组
    private final List<TaskInfo> taskInfos;//TaskInfo只是当前分组的Task配置

    public ClusterConfigState(long version,
                              List<TaskInfo> taskInfos) {
        this.version = version;
        this.taskInfos = taskInfos;
    }

    /**
     * Get the last version read to generate this config state. This version is not guaranteed to be perfectly consistent
     * with the recorded state because some partial updates to task configs may have been read.
     *
     * @return the latest config version
     */
    public long version() {
        return version;
    }

    /**
     * Check whether this snapshot contains configuration for a task.
     *
     * @param taskId identifier of the task
     * @return true if this state contains configuration for the task, false otherwise
     */
    public boolean contains(String taskId) {
        return taskInfos.stream().anyMatch(t->t.idString().equals(taskId));
    }

    /**
     * Get a list of the tasks in this configuration
     */
    public Set<String> tasks() {
        return taskInfos.stream().map(j -> String.valueOf(j.getId())).collect(Collectors.toSet());
    }

    /**
     * Get the Task information for a task.
     *
     * @param taskId identifier of the task
     * @return the taskinfo model object.
     */
    public TaskInfo taskConfig(String taskId) {
        return taskInfos.stream().filter(j -> j.getId().toString().equals(taskId)).findFirst().get();
    }

    public List<TaskInfo> allTaskConfigs(){
        return taskInfos;
    }
}
