package com.ucar.datalink.domain.task;

import java.util.List;

/**
 * Created by lubiao on 2019/8/27.
 */
public class ActiveTasks {
    private Long groupId;
    private Long version;
    private List<TaskInfo> tasks;

    public ActiveTasks() {
    }

    public ActiveTasks(Long groupId, Long version, List<TaskInfo> tasks) {
        this.groupId = groupId;
        this.version = version;
        this.tasks = tasks;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<TaskInfo> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskInfo> tasks) {
        this.tasks = tasks;
    }
}
