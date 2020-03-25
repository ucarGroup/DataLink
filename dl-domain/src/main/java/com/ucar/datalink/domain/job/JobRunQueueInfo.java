package com.ucar.datalink.domain.job;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by user on 2017/12/26.
 *
 CREATE TABLE `t_dl_flinker_job_run_queue` (
 `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键，自增类型',
 `job_id_list` varchar(2000) NOT NULL COMMENT 'job id列表用逗号分隔',
 `current_process_id` varchar(200) DEFAULT NULL COMMENT '当前正在运行的id列表，[job_id]-[job_execution_id],... 这种格式',
 `queue_state` varchar(20) DEFAULT NULL COMMENT '整个队列的运行状态，未执行，有错误，执行中，执行完',
 `job_count` int(11) DEFAULT NULL COMMENT '整个队列的job数量',
 `success_list` varchar(200) DEFAULT NULL COMMENT '已经执行完的job数量',
 `failure_list` varchar(200) DEFAULT NULL COMMENT '已执行失败的job数量',
 `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `modify_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '修改时间',
 `top_time` timestamp NULL DEFAULT NULL,
 PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='job运行队列表'

 */
@Alias("jobRunQueue")
public class JobRunQueueInfo implements Serializable, Storable {

    private long id;

    private String jobIdList;

    private String currentPorcessId = "";

    private String queueState = JobRunQueueState.INIT;

    private int jobCount;

    private String successList = "";

    private String failureList = "";

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 修改时间
     */
    private Timestamp modifyTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJobIdList() {
        return jobIdList;
    }

    public void setJobIdList(String jobIdList) {
        this.jobIdList = jobIdList;
    }

    public String getCurrentPorcessId() {
        return currentPorcessId;
    }

    public void setCurrentPorcessId(String currentPorcessId) {
        this.currentPorcessId = currentPorcessId;
    }

    public String getQueueState() {
        return queueState;
    }

    public void setQueueState(String queueState) {
        this.queueState = queueState;
    }

    public int getJobCount() {
        return jobCount;
    }

    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }

    public String getSuccessList() {
        return successList;
    }

    public void setSuccessList(String successList) {
        this.successList = successList;
    }

    public String getFailureList() {
        return failureList;
    }

    public void setFailureList(String failureList) {
        this.failureList = failureList;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }


    @Override
    public String toString() {
        return "JobRunQueueInfo{" +
                "id=" + id +
                ", jobIdList='" + jobIdList + '\'' +
                ", currentPorcessId='" + currentPorcessId + '\'' +
                ", queueState='" + queueState + '\'' +
                ", jobCount=" + jobCount +
                ", successList='" + successList + '\'' +
                ", failureList='" + failureList + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                '}';
    }

}
