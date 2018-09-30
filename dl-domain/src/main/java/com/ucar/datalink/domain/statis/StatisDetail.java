package com.ucar.datalink.domain.statis;

/**
 * Created by sqq on 2017/6/2.
 */
public class StatisDetail {
    private String type;
    private Integer countByType;
    private Integer groupCount;
    private Integer workerCount;
    private Integer msCount;
    private Integer taskCount;
    private Integer mappingCount;
    private String allManagers;
    private String activeManager;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCountByType() {
        return countByType;
    }

    public void setCountByType(Integer countByType) {
        this.countByType = countByType;
    }

    public Integer getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(Integer groupCount) {
        this.groupCount = groupCount;
    }

    public Integer getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(Integer workerCount) {
        this.workerCount = workerCount;
    }

    public Integer getMsCount() {
        return msCount;
    }

    public void setMsCount(Integer msCount) {
        this.msCount = msCount;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public Integer getMappingCount() {
        return mappingCount;
    }

    public void setMappingCount(Integer mappingCount) {
        this.mappingCount = mappingCount;
    }

    public String getAllManagers() {
        return allManagers;
    }

    public void setAllManagers(String allManagers) {
        this.allManagers = allManagers;
    }

    public String getActiveManager() {
        return activeManager;
    }

    public void setActiveManager(String activeManager) {
        this.activeManager = activeManager;
    }
}
