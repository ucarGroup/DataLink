package com.ucar.datalink.domain.alarm;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.domain.Storable;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Alias("alarmStrategy")
public class AlarmStrategyInfo implements Serializable, Storable {
    private Long id;
    private String name;
    private Long priorityId;
    private Integer monitorType;
    private String config;
    private Date createTime;
    private Date modifyTime;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(Long priorityId) {
        this.priorityId = priorityId;
    }

    public Integer getMonitorType() {
        return monitorType;
    }

    public void setMonitorType(Integer monitorType) {
        this.monitorType = monitorType;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
        if (!StringUtils.isEmpty(config)) {
            strategys = JSONObject.parseArray(config, StrategyConfig.class);
        } else {
            strategys = null;
        }
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    private transient List<StrategyConfig> strategys;

    public List<StrategyConfig> getStrategys() {
        return strategys;
    }

    public void setStrategys(List<StrategyConfig> strategys) {
        this.strategys = strategys;
        if(strategys!=null && strategys.size()>0) {
            this.config = JSONArray.toJSONString(strategys);
        }
    }
}
