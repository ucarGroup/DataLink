package com.ucar.datalink.domain.job;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by yang.wang09 on 2019-04-02 18:28.
 *
 CREATE TABLE t_dl_flinker_job_queue (
 `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '表主键',
 `job_name` VARCHAR(255) NOT NULL COMMENT 'job名称',
 `queue_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '关联的队列id',
 `table_name` VARCHAR(255) NOT NULL COMMENT 'job对应的表名称',
 `job_state` VARCHAR(255) NOT NULL COMMENT 'job状态',
 `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
 `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY (`id`)
 )ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='job队列表'


 */
@Alias("jobQueue")
public class JobQueue implements Serializable, Storable {

    private Long id;

    private String jobName;

    private Long queueId;

    private String tableName;

    private String jobState;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 修改时间
     */
    private Timestamp modifyTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getJobState() {
        return jobState;
    }

    public void setJobState(String jobState) {
        this.jobState = jobState;
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
        return "JobQueue{" +
                "id=" + id +
                ", jobName='" + jobName + '\'' +
                ", queueId=" + queueId +
                ", tableName='" + tableName + '\'' +
                ", jobState='" + jobState + '\'' +
                '}';
    }
}
