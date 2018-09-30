package com.ucar.datalink.manager.core.web.dto.interceptor;

import com.ucar.datalink.domain.interceptor.InterceptorType;

import java.util.Date;

/**
 * Created by sqq on 2017/5/24.
 */
public class InterceptorView {
    private Long id;
    private String name;
    private String desc;
    private InterceptorType type;
    private String content;
    private Date createTime;
    private Date modifyTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public InterceptorType getType() {
        return type;
    }

    public void setType(InterceptorType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
