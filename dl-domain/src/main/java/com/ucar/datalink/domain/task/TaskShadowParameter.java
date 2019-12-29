package com.ucar.datalink.domain.task;

import com.ucar.datalink.domain.Parameter;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by lubiao on 2019/8/5.
 */
public class TaskShadowParameter extends Parameter implements Serializable{
    private Set<Long> shadowMappingIds;
    private Long timeStamp;

    public Set<Long> getShadowMappingIds() {
        return shadowMappingIds;
    }

    public void setShadowMappingIds(Set<Long> shadowMappingIds) {
        this.shadowMappingIds = shadowMappingIds;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
