package com.ucar.datalink.domain.task;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by csf on 17/5/1.
 */
@Alias("delaytime")
public class TaskDelayTimeInfo implements Serializable, Storable {

    private Long id;

    private Long delayTime;

    private Date createTime;

    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "TaskDelayTimeInfo{" +
                "id=" + id +
                ", delayTime=" + delayTime +
                ", createTime=" + createTime +
                ", taskId=" + taskId +
                '}';
    }
}
