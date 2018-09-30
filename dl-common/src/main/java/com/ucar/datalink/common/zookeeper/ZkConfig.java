package com.ucar.datalink.common.zookeeper;

import org.apache.commons.lang.StringUtils;

import java.security.InvalidParameterException;

/**
 * Created by lubiao on 2016/12/8.
 */
public class ZkConfig {
    private String zkServers;//格式：IP:Port,IP:Port
    private Integer sessionTimeout;//单位：ms
    private Integer connectionTimeout;//单位：ms

    public ZkConfig(String zkServers, Integer sessionTimeout, Integer connectionTimeout) {
        if (StringUtils.isEmpty(zkServers)) {
            throw new InvalidParameterException("zkServers can not be null.");
        }
        if (sessionTimeout == null) {
            throw new InvalidParameterException("sessionTimeout can not be null.");
        }
        if (connectionTimeout == null) {
            throw new InvalidParameterException("connectionTimeout can not be null.");
        }

        this.zkServers = zkServers;
        this.sessionTimeout = sessionTimeout;
        this.connectionTimeout = connectionTimeout;
    }

    public String getZkServers() {
        return zkServers;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZkConfig zkConfig = (ZkConfig) o;

        return zkServers.equals(zkConfig.zkServers);

    }

    @Override
    public int hashCode() {
        return zkServers.hashCode();
    }

    public int parsePort() {
        if(StringUtils.isEmpty(zkServers)) {
            throw new RuntimeException("servers is empty");
        }
        int port = -1;
        try {
            String[] ips = zkServers.split(",");
            for(String ip : ips) {
                port = Integer.parseInt( ip.split(":")[1] );
                if(port > -1) {
                    break;
                }
            }
            if(port == -1) {
                throw new RuntimeException("parse port failure");
            }
            return port;
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String parseServersToString() {
        if(StringUtils.isEmpty(zkServers)) {
            throw new RuntimeException("server is empty");
        }
        StringBuilder sb = new StringBuilder();
        try {
            String[] ips = zkServers.split(",");
            for(String ip : ips) {
                sb.append( ip.split(":")[0] );
                sb.append(",");
            }
            if(sb.length() > 0) {
                sb.deleteCharAt(sb.length()-1);
            } else {
                throw new RuntimeException("parse servers ip failure");
            }
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return sb.toString();
    }
}
