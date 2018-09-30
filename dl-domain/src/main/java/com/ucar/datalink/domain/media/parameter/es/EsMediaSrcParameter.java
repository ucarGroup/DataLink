package com.ucar.datalink.domain.media.parameter.es;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;

/**
 * Created by lubiao on 2017/6/16.
 */
public class EsMediaSrcParameter extends MediaSrcParameter {
    private String clusterHosts;
    private Integer httpPort;
    private Integer tcpPort;
    private String userName;
    private String password;

    public String getClusterHosts() {
        return clusterHosts;
    }

    public void setClusterHosts(String clusterHosts) {
        this.clusterHosts = clusterHosts;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(Integer tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
