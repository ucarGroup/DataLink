package com.ucar.datalink.domain.media.parameter.zk;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import org.apache.commons.lang.StringUtils;

/**
 * Created by sqq on 2017/5/11.
 */
public class ZkMediaSrcParameter extends MediaSrcParameter {
    private String servers;
    private Long sessionTimeout = 6000L;
    private Long connectionTimeout = 6000L;

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }


    public int parsePort() {
        if(StringUtils.isEmpty(servers)) {
            throw new RuntimeException("servers is empty");
        }
        int port = -1;
        try {
            String[] ips = servers.split(",");
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
        if(StringUtils.isEmpty(servers)) {
            throw new RuntimeException("server is empty");
        }
        StringBuilder sb = new StringBuilder();
        try {
            String[] ips = servers.split(",");
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
