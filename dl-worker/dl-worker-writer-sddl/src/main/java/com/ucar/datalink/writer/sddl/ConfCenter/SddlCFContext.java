package com.ucar.datalink.writer.sddl.ConfCenter;

import java.io.Serializable;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 03/11/2017.
 */
public class SddlCFContext implements Serializable{
    private static final long serialVersionUID = 1L;

    private String projectName;  // 配置中心里的项目名称
    private String cfProjectName;// 默认取sddladmin
    private String cfKey;//写死的sddladmin的key，Enum中配置的密钥
    private String serverDomain;//配置中心各个环境的域名
    private String businessLine;//配置中心sddladmin项目的业务线名称

    public SddlCFContext() {
    }

    public SddlCFContext(String projectName, String cfKey, String serverDomain, String businessLine) {
        this.cfProjectName = "sddladmin"; // yw.chen 后期sddladmin升级到TCDS后，此项目名称需要修改
        this.projectName = projectName;
        this.cfKey = cfKey;
        this.serverDomain = serverDomain;
        this.businessLine = businessLine;
    }


    public String getCfProjectName() {
        return cfProjectName;
    }

    public void setCfProjectName(String cfProjectName) {
        this.cfProjectName = cfProjectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCfKey() {
        return cfKey;
    }

    public void setCfKey(String cfKey) {
        this.cfKey = cfKey;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }

    public String getBusinessLine() {
        return businessLine;
    }

    public void setBusinessLine(String businessLine) {
        this.businessLine = businessLine;
    }
}
