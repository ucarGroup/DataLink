package com.ucar.datalink.domain.auditLog;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yifan.liu02
 * @date 2018/12/27
 */
@Alias("auditLog")
public class AuditLogInfo implements Serializable, Storable {
    private Long id;
    private Long userId;
    private String menuCode;
    private String operType;
    private Date operTime;
    private String operTimeStr;
    private Long operKey;
    private String operName;
    private String operRecord;

    private String userName;
    private String menuName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType;
    }

    public Date getOperTime() {
        return operTime;
    }

    public void setOperTime(Date operTime) {
        this.operTime = operTime;
    }

    public Long getOperKey() {
        return operKey;
    }

    public void setOperKey(Long operKey) {
        this.operKey = operKey;
    }

    public String getOperName() {
        return operName;
    }

    public void setOperName(String operName) {
        this.operName = operName==null?"":operName;
    }

    public String getOperRecord() {
        return operRecord;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public void setOperRecord(String operRecord) {
//        this.operRecord=operRecord.length()>=998?operRecord.substring(0,998):operRecord;
        this.operRecord=operRecord;
    }

    public String getOperTimeStr() {
        return operTimeStr;
    }

    public void setOperTimeStr(String operTimeStr) {
        this.operTimeStr = operTimeStr;
    }
}
