package com.ucar.datalink.domain.meta;

import java.util.List;

public class HbaseStatus {


    private Integer serversSize;
    private Integer version;
    private Integer backupMastersSize;
    private Integer deadServers;
    private Integer regionsCount;
    private Boolean balancerOn;
    private String clusterId;
    private String hBaseVersion;
    private Integer averageLoad;
    private List<ServerInfo> backupMasters;
    private ServerInfo master;
    private List<ServerInfo> serverInfo;

    public Integer getServersSize() {
        return serversSize;
    }

    public void setServersSize(Integer serversSize) {
        this.serversSize = serversSize;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getBackupMastersSize() {
        return backupMastersSize;
    }

    public void setBackupMastersSize(Integer backupMastersSize) {
        this.backupMastersSize = backupMastersSize;
    }

    public Integer getDeadServers() {
        return deadServers;
    }

    public void setDeadServers(Integer deadServers) {
        this.deadServers = deadServers;
    }

    public Integer getRegionsCount() {
        return regionsCount;
    }

    public void setRegionsCount(Integer regionsCount) {
        this.regionsCount = regionsCount;
    }

    public Boolean getBalancerOn() {
        return balancerOn;
    }

    public void setBalancerOn(Boolean balancerOn) {
        this.balancerOn = balancerOn;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String gethBaseVersion() {
        return hBaseVersion;
    }

    public void sethBaseVersion(String hBaseVersion) {
        this.hBaseVersion = hBaseVersion;
    }

    public Integer getAverageLoad() {
        return averageLoad;
    }

    public void setAverageLoad(Integer averageLoad) {
        this.averageLoad = averageLoad;
    }

    public List<ServerInfo> getBackupMasters() {
        return backupMasters;
    }

    public void setBackupMasters(List<ServerInfo> backupMasters) {
        this.backupMasters = backupMasters;
    }

    public ServerInfo getMaster() {
        return master;
    }

    public void setMaster(ServerInfo master) {
        this.master = master;
    }

    public List<ServerInfo> getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(List<ServerInfo> serverInfo) {
        this.serverInfo = serverInfo;
    }

    public static class ServerInfo{
        private String hostAndPort;
        private String hostname;
        private Integer port;
        private String serverName;
        private Long startcode;
        private String versionedBytes;

        public String getHostAndPort() {
            return hostAndPort;
        }

        public void setHostAndPort(String hostAndPort) {
            this.hostAndPort = hostAndPort;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public Long getStartcode() {
            return startcode;
        }

        public void setStartcode(Long startcode) {
            this.startcode = startcode;
        }

        public String getVersionedBytes() {
            return versionedBytes;
        }

        public void setVersionedBytes(String versionedBytes) {
            this.versionedBytes = versionedBytes;
        }
    }

}
