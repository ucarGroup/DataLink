package com.ucar.datalink.manager.core.web.dto.group;

import java.util.Date;

/**
 * group视图类.
 * Created by csf on 2017/2/22.
 */
public class GroupView {

    private Long id;

    private String groupName;

    private String groupDesc;

    private Date createTime;

    private Date modifyTime;

    private String groupState = "";

    private Integer generationId = 0;

    private String lastReblanceTime = "";//分组最近一次的Reblance时间

    public String getGroupState() {
        return groupState;
    }

    public void setGroupState(String groupState) {
        this.groupState = groupState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
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

    public Integer getGenerationId() {
        return generationId;
    }

    public void setGenerationId(Integer generationId) {
        this.generationId = generationId;
    }

    public String getLastReblanceTime() {
        return lastReblanceTime;
    }

    public void setLastReblanceTime(String lastReblanceTime) {
        this.lastReblanceTime = lastReblanceTime;
    }
}
