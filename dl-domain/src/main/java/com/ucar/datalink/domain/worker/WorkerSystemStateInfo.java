package com.ucar.datalink.domain.worker;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by qianqian.shi on 2018/5/23.
 */
@Alias("workerSystemState")
public class WorkerSystemStateInfo implements Serializable, Storable {

    private Long id;
    /**
     * worker机器Id
     */
    private Long workerId;
    /**
     * worker机器ip
     */
    private String host;
    /**
     * 平均负载（1分钟）
     */
    private BigDecimal loadAverage;
    /**
     * 用户平均CPU使用率
     */
    private BigDecimal userCPUUtilization;
    /**
     * 系统平均CPU使用率
     */
    private BigDecimal sysCPUUtilization;
    /**
     * 1分钟内接收的网卡流量
     */
    private Long incomingNetworkTraffic;
    /**
     * 1分钟内发送的网卡流量
     */
    private Long outgoingNetworkTraffic;
    /**
     * 当前TCP连接数
     */
    private Long tcpCurrentEstab;
    /**
     * 统计时间
     */
    private Date createTime;

    /**
     * 查询时间段
     */
    private String startTime;

    private String endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

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

    public Long getIncomingNetworkTraffic() {
        return incomingNetworkTraffic;
    }

    public void setIncomingNetworkTraffic(Long incomingNetworkTraffic) {
        this.incomingNetworkTraffic = incomingNetworkTraffic;
    }

    public Long getOutgoingNetworkTraffic() {
        return outgoingNetworkTraffic;
    }

    public void setOutgoingNetworkTraffic(Long outgoingNetworkTraffic) {
        this.outgoingNetworkTraffic = outgoingNetworkTraffic;
    }

    public Long getTcpCurrentEstab() {
        return tcpCurrentEstab;
    }

    public void setTcpCurrentEstab(Long tcpCurrentEstab) {
        this.tcpCurrentEstab = tcpCurrentEstab;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
