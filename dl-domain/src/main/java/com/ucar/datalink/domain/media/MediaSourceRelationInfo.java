package com.ucar.datalink.domain.media;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * 虚拟DB和实际DB关系
 */
@Alias("mediaSourceRelation")
public class MediaSourceRelationInfo implements Serializable, Storable {

    private Long id;
    private Long virtualMsId;
    private Long realMsId;
    private Date createTime;
    private Date modifyTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVirtualMsId() {
        return virtualMsId;
    }

    public void setVirtualMsId(Long virtualMsId) {
        this.virtualMsId = virtualMsId;
    }

    public Long getRealMsId() {
        return realMsId;
    }

    public void setRealMsId(Long realMsId) {
        this.realMsId = realMsId;
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
}
