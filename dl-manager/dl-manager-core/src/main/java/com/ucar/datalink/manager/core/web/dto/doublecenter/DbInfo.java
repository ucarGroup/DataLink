package com.ucar.datalink.manager.core.web.dto.doublecenter;

import org.apache.commons.lang.StringUtils;

public class DbInfo {

    private String uuid;
    private String dbName;
    private String ipAddress;
    private String dbType;
    private Integer idcId;
    private String idcName;//该字段用不着
    private String productName;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        String lowerName = StringUtils.isBlank(dbName) ? "" : dbName.toLowerCase();
        this.dbName = lowerName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public Integer getIdcId() {
        return idcId;
    }

    public void setIdcId(Integer idcId) {
        this.idcId = idcId;
    }

    public String getIdcName() {
        return idcName;
    }

    public void setIdcName(String idcName) {
        this.idcName = idcName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

}
