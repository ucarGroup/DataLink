package com.ucar.datalink.domain.decorate;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Date;

/**
 * @author xy.li
 * @date 2019/05/29
 */
public class TaskDecorateDetail {

    private long id;
    private long decorateId;
    private long taskId;
    private String taskName;
    private String tableName;
    private int status;
    private Date createTime;
    private Date updateTime;
    private String executedLog;
    private String statement;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDecorateId() {
        return decorateId;
    }

    public void setDecorateId(long decorateId) {
        this.decorateId = decorateId;
    }

    public String getStatusName() {
        return TaskDecorateStatus.NEW_CREATED.getValueByCode(this.status);
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getExecutedLog() {
        return executedLog;
    }

    public void setExecutedLog(String executedLog) {
        this.executedLog = executedLog;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }


    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
