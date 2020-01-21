package com.ucar.datalink.domain.sync;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.Storable;
import com.ucar.datalink.domain.user.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sqq on 2017/9/20.
 */
@Alias("syncApply")
public class SyncApplyInfo implements Serializable, Storable {
    //basic
    private Long id;
    private SyncApplyStatus applyStatus;
    private String applyType;
    private Boolean isInitialData;
    private String applyContent;
    private String applyRemark;
    private Long applyUserId;
    private Long operateUserId;
    private String replyRemark;
    private Boolean needNotify;
    private Date createTime;
    private Date modifyTime;

    //other
    private transient SyncApplyContent applyContentObj;
    private UserInfo applyUserInfo;
    private UserInfo operateUserInfo;

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
        if (!StringUtils.isEmpty(applyContent)) {
            applyContentObj = JSONObject.parseObject(applyContent, SyncApplyContent.class);
        } else {
            applyContentObj = null;
        }
    }

    public String getApplyRemark() {
        return applyRemark;
    }

    public void setApplyRemark(String applyRemark) {
        this.applyRemark = applyRemark;
    }

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public Long getOperateUserId() {
        return operateUserId;
    }

    public void setOperateUserId(Long operateUserId) {
        this.operateUserId = operateUserId;
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

    public SyncApplyContent getApplyContentObj() {
        if (applyContentObj == null) {
            throw new DatalinkException("SyncApplyParameter can not be null.");
        }
        return applyContentObj;
    }

    public UserInfo getApplyUserInfo() {
        return applyUserInfo;
    }

    public void setApplyUserInfo(UserInfo applyUserInfo) {
        this.applyUserInfo = applyUserInfo;
    }

    public UserInfo getOperateUserInfo() {
        return operateUserInfo;
    }

    public void setOperateUserInfo(UserInfo operateUserInfo) {
        this.operateUserInfo = operateUserInfo;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SyncApplyInfo{");
        sb.append("id=").append(id);
        sb.append(", applyStatus=").append(applyStatus);
        sb.append(", applyType='").append(applyType).append('\'');
        sb.append(", isInitialData=").append(isInitialData);
        sb.append(", applyContent='").append(applyContent).append('\'');
        sb.append(", applyRemark='").append(applyRemark).append('\'');
        sb.append(", applyUserId=").append(applyUserId);
        sb.append(", operateUserId=").append(operateUserId);
        sb.append(", replyRemark='").append(replyRemark).append('\'');
        sb.append(", needNotify=").append(needNotify);
        sb.append(", createTime=").append(createTime);
        sb.append(", modifyTime=").append(modifyTime);
        sb.append(", applyContentObj=").append(applyContentObj);
        sb.append(", applyUserInfo=").append(applyUserInfo);
        sb.append(", operateUserInfo=").append(operateUserInfo);
        sb.append('}');
        return sb.toString();
    }
}
