package com.ucar.datalink.manager.core.web.dto.sync;

import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.sync.SyncApplyStatus;
import com.ucar.datalink.domain.user.RoleType;

import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2017/9/25.
 */
public class SyncApplyView {

    private Long id;
    private SyncApplyStatus applyStatus;
    private String applyType;
    private Boolean isInitialData;
    private String applyContent;
    private String applyRemark;
    private Long srcMediaSourceId;
    private String srcMediaSourceName;
    private MediaSourceType srcMediaSourceType;
    private List<String> srcTableName;
    private Long targetMediaSourceId;
    private String targetMediaSourceName;
    private MediaSourceType targetMediaSourceType;
    private List<String> targetTableName;
    private Long applyUserId;
    private String applyUserName;
    private Long operateUserId;
    private String operateUserName;
    private String replyRemark;
    private Boolean needNotify;
    private Date createTime;
    private Date modifyTime;
    private String approveUserId;
    private String approveRemark;
    private Boolean canApprove;
    private RoleType loginRoleType;
    private Boolean isAutoKeeper;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SyncApplyStatus getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(SyncApplyStatus applyStatus) {
        this.applyStatus = applyStatus;
    }

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    public Boolean getIsInitialData() {
        return isInitialData;
    }

    public void setIsInitialData(Boolean isInitialData) {
        this.isInitialData = isInitialData;
    }

    public String getApplyContent() {
        return applyContent;
    }

    public void setApplyContent(String applyContent) {
        this.applyContent = applyContent;
    }

    public String getApplyRemark() {
        return applyRemark;
    }

    public void setApplyRemark(String applyRemark) {
        this.applyRemark = applyRemark;
    }

    public Long getSrcMediaSourceId() {
        return srcMediaSourceId;
    }

    public void setSrcMediaSourceId(Long srcMediaSourceId) {
        this.srcMediaSourceId = srcMediaSourceId;
    }

    public String getSrcMediaSourceName() {
        return srcMediaSourceName;
    }

    public MediaSourceType getSrcMediaSourceType() {
        return srcMediaSourceType;
    }

    public void setSrcMediaSourceType(MediaSourceType srcMediaSourceType) {
        this.srcMediaSourceType = srcMediaSourceType;
    }

    public MediaSourceType getTargetMediaSourceType() {
        return targetMediaSourceType;
    }

    public List<String> getSrcTableName() {
        return srcTableName;
    }

    public void setSrcTableName(List<String> srcTableName) {
        this.srcTableName = srcTableName;
    }

    public List<String> getTargetTableName() {
        return targetTableName;
    }

    public void setTargetTableName(List<String> targetTableName) {
        this.targetTableName = targetTableName;
    }

    public void setTargetMediaSourceType(MediaSourceType targetMediaSourceType) {
        this.targetMediaSourceType = targetMediaSourceType;
    }

    public void setSrcMediaSourceName(String srcMediaSourceName) {
        this.srcMediaSourceName = srcMediaSourceName;
    }

    public Long getTargetMediaSourceId() {
        return targetMediaSourceId;
    }

    public void setTargetMediaSourceId(Long targetMediaSourceId) {
        this.targetMediaSourceId = targetMediaSourceId;
    }

    public String getTargetMediaSourceName() {
        return targetMediaSourceName;
    }

    public void setTargetMediaSourceName(String targetMediaSourceName) {
        this.targetMediaSourceName = targetMediaSourceName;
    }

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public Long getOperateUserId() {
        return operateUserId;
    }

    public void setOperateUserId(Long operateUserId) {
        this.operateUserId = operateUserId;
    }

    public String getOperateUserName() {
        return operateUserName;
    }

    public void setOperateUserName(String operateUserName) {
        this.operateUserName = operateUserName;
    }

    public String getReplyRemark() {
        return replyRemark;
    }

    public void setReplyRemark(String replyRemark) {
        this.replyRemark = replyRemark;
    }

    public Boolean getNeedNotify() {
        return needNotify;
    }

    public void setNeedNotify(Boolean needNotify) {
        this.needNotify = needNotify;
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

    public String getApproveUserId() {
        return approveUserId;
    }

    public void setApproveUserId(String approveUserId) {
        this.approveUserId = approveUserId;
    }

    public String getApproveRemark() {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark) {
        this.approveRemark = approveRemark;
    }

    public Boolean getCanApprove() {
        return canApprove;
    }

    public void setCanApprove(Boolean canApprove) {
        this.canApprove = canApprove;
    }

    public RoleType getLoginRoleType() {
        return loginRoleType;
    }

    public void setLoginRoleType(RoleType loginRoleType) {
        this.loginRoleType = loginRoleType;
    }

    public Boolean getIsAutoKeeper() {
        return isAutoKeeper;
    }

    public void setIsAutoKeeper(Boolean isAutoKeeper) {
        this.isAutoKeeper = isAutoKeeper;
    }
}
