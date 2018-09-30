package com.ucar.datalink.common.system;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by qianqian.shi on 2018/5/9.
 */
public class SystemSnapshot {
    private BigDecimal loadAverage;//1分钟平均负载
    private BigDecimal userCPUUtilization;//用户CPU使用率(%)
    private BigDecimal sysCPUUtilization;//系统CPU使用率(%)
    private Map<String, Long> incomingNetworkTrafficMap;//每个网卡分别接收的总字节数
    private Map<String, Long> outgoingNetworkTrafficMap;//每个网卡分别发送的总字节数
    private Long tcpCurrentEstab;//当前TCP连接数

    public BigDecimal getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(BigDecimal loadAverage) {
        this.loadAverage = loadAverage;
    }

    public BigDecimal getUserCPUUtilization() {
        return userCPUUtilization;
    }

    public void setUserCPUUtilization(BigDecimal userCPUUtilization) {
        this.userCPUUtilization = userCPUUtilization;
    }

    public BigDecimal getSysCPUUtilization() {
        return sysCPUUtilization;
    }

    public void setSysCPUUtilization(BigDecimal sysCPUUtilization) {
        this.sysCPUUtilization = sysCPUUtilization;
    }

    public Map<String, Long> getIncomingNetworkTrafficMap() {
        return incomingNetworkTrafficMap;
    }

    public void setIncomingNetworkTrafficMap(Map<String, Long> incomingNetworkTrafficMap) {
        this.incomingNetworkTrafficMap = incomingNetworkTrafficMap;
    }

    public Map<String, Long> getOutgoingNetworkTrafficMap() {
        return outgoingNetworkTrafficMap;
    }

    public void setOutgoingNetworkTrafficMap(Map<String, Long> outgoingNetworkTrafficMap) {
        this.outgoingNetworkTrafficMap = outgoingNetworkTrafficMap;
    }

    public Long getTcpCurrentEstab() {
        return tcpCurrentEstab;
    }

    public void setTcpCurrentEstab(Long tcpCurrentEstab) {
        this.tcpCurrentEstab = tcpCurrentEstab;
    }
}
