package com.ucar.datalink.domain.job;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by yang.wang09 on 2018-07-19 20:11.
 * CREATE TABLE `t_dadax_job_schedule` (
 `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '定时表主键',
 `job_id` bigint(20) DEFAULT NULL COMMENT '任务id job表id',
 `cron_expression` varchar(128) DEFAULT NULL COMMENT 'quartz 运行时间规则表达式',
 `is_retry` tinyint(1) DEFAULT '0' COMMENT '是否重试 0 不重试 1 重试',
 `retry_number` tinyint(1) DEFAULT NULL COMMENT '最大重试次数 不超过5次 高频率的不重试',
 `retry_interval` tinyint(3) DEFAULT NULL COMMENT '每次重试间隔（秒） 不少于60秒 最大 900s',
 `max_running_time` bigint(20) DEFAULT NULL COMMENT '最大运行时间 超过时间 则抛出异常/报警 -1 为不限制',
 `online_state` tinyint(1) DEFAULT NULL COMMENT '任务状态,0:未上线,1:已上线,2:已下线',
 `is_suspend` tinyint(1) DEFAULT NULL COMMENT '是否暂定 0 否 运行中 1 暂停 task挂起中',
 `is_delete` tinyint(1) DEFAULT NULL COMMENT '是否删除 0 未删除 1 已删除 （此为逻辑删除）',
 `remark` varchar(255) DEFAULT NULL COMMENT '定时备注',
 `creator_id` bigint(20) DEFAULT NULL COMMENT '创建定时任务的人员id',
 `creator_name` varchar(255) DEFAULT NULL COMMENT '创建定时任务的人员名称',
 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `modifie_time` datetime DEFAULT NULL COMMENT '最后修改时间',
 PRIMARY KEY (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务运行定时表'
 */
@Alias("jobSchedule")
public class JobScheduleInfo implements Serializable, Storable {

    private Long id;

    private Long jobId;

    private String cron;

    private String scheduleName;

    private Boolean isRetry;

    private Integer retryNumber;

    private Integer retryInterval;

    private Long maxRunningTime;

    private Integer onlineState;

    private Boolean isSuspend;

    private Boolean isDelete;

    private String remark;

    private Long creatorId;

    private String creatorName;

    private Boolean scheduleState;

    private Timestamp createTime;

    private Timestamp modifieTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Boolean getIsRetry() {
        return isRetry;
    }

    public void setIsRetry(Boolean isRetry) {
        this.isRetry = isRetry;
    }

    public Integer getRetryNumber() {
        return retryNumber;
    }

    public void setRetryNumber(Integer retryNumber) {
        this.retryNumber = retryNumber;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Long getMaxRunningTime() {
        return maxRunningTime;
    }

    public void setMaxRunningTime(Long maxRunningTime) {
        this.maxRunningTime = maxRunningTime;
    }

    public Integer getOnlineState() {
        return onlineState;
    }

    public void setOnlineState(Integer onlineState) {
        this.onlineState = onlineState;
    }

    public Boolean getIsSuspend() {
        return isSuspend;
    }

    public void setIsSuspend(Boolean isSuspend) {
        this.isSuspend = isSuspend;
    }

    public Boolean getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Boolean isDelete) {
        this.isDelete = isDelete;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getModifieTime() {
        return modifieTime;
    }

    public void setModifieTime(Timestamp modifieTime) {
        this.modifieTime = modifieTime;
    }

    public Boolean getScheduleState() {
        return scheduleState;
    }

    public void setScheduleState(Boolean scheduleState) {
        this.scheduleState = scheduleState;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }


    @Override
    public String toString() {
        return "JobScheduleInfo{" +
                "id=" + id +
                ", jobId=" + jobId +
                ", cron='" + cron + '\'' +
                ", scheduleName='" + scheduleName + '\'' +
                '}';
    }

}

