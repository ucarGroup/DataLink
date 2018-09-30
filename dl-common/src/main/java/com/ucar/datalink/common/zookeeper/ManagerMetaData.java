package com.ucar.datalink.common.zookeeper;

/**
 * Created by lubiao on 2016/12/8.
 */
public class ManagerMetaData {

    private String address;
    private int port;
    private int httpPort;

    public ManagerMetaData() {

    }

    public ManagerMetaData(String address, int port, int httpPort) {
        this.address = address;
        this.port = port;
        this.httpPort = httpPort;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ManagerMetaData that = (ManagerMetaData) o;

        if (port != that.port) return false;
        if (httpPort != that.httpPort) return false;
        return address.equals(that.address);

    }

    @Override
    public int hashCode() {
        int result = address.hashCode();
        result = 31 * result + port;
        result = 31 * result + httpPort;
        return result;
    }
}
