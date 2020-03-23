package com.ucar.datalink.domain.job;

import org.apache.commons.lang.StringUtils;

/**
 * Created by yang.wang09 on 2019-01-21 14:56.
 */
public class HostNodeInfo implements Comparable {

    private String host;

    private String port;

    private String other;

    private String userName;

    private String password;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HostNodeInfo that = (HostNodeInfo) o;

        if (!host.equals(that.host)) return false;
        if (port != null ? !port.equals(that.port) : that.port != null) return false;
        return !(other != null ? !other.equals(that.other) : that.other != null);

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (other != null ? other.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object o) {
        if(!(o instanceof HostNodeInfo)) {
            return -1;
        }
        HostNodeInfo dest = (HostNodeInfo)o;
        int compare = 0;
        if(StringUtils.isBlank(host)) {
            return -1;
        }
        if(StringUtils.isBlank(dest.getHost())) {
            return 1;
        }
        compare = host.compareTo(dest.getHost());
        if( compare!=0 ) {
            return compare;
        }

        if(StringUtils.isNotBlank(port) && StringUtils.isNotBlank(dest.getPort())) {
            compare = port.compareTo(dest.getPort());
            if( compare!=0 ) {
                return compare;
            }
        }


        if(StringUtils.isNotBlank(other) && StringUtils.isNotBlank(dest.getOther())) {
            compare = other.compareTo(dest.getOther());
            if(compare != 0) {
                return compare;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return "HostNodeInfo{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", other='" + other + '\'' +
                '}';
    }
}
