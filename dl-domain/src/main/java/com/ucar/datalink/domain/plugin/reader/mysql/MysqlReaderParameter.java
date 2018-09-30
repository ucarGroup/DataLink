package com.ucar.datalink.domain.plugin.reader.mysql;

import com.google.common.collect.Sets;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginReaderParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * CanalTaskReader的参数配置类.
 * <p>
 * Created by lubiao on 2017/2/15.
 */
public class MysqlReaderParameter extends PluginReaderParameter {
    /**
     * 是否需要dump records详情
     */
    private boolean dumpDetail = false;
    /**
     * 获取批量数据的超时时间,-1代表不进行超时控制，0代表永久，>0则表示按照指定的时间进行控制(单位毫秒)
     */
    private Long batchTimeout = -1L;
    /**
     * Message订阅批次大小
     * 参数调大了可以增大系统的吞吐率，调小了增加同步的实时性
     */
    private Integer messageBatchSize = 2000;
    /**
     * 内存存储的buffer大小
     */
    private Integer memoryStorageBufferSize = 32 * 1024;
    /**
     * 内存存储的buffer内存占用单位，默认为1kb
     */
    private Integer memoryStorageBufferMemUnit = 1024;
    /**
     * 心跳检测sql
     */
    private String detectingSQL = "select 1";
    /**
     * 检测频率
     */
    private Integer detectingIntervalInSeconds = 3;
    /**
     * 心跳超时时间
     */
    private Integer detectingTimeoutThresholdInSeconds = 30;
    /**
     * 心跳检查重试次数
     */
    private Integer detectingRetryTimes = 3;
    /**
     * 默认超时时间(sotimeout)
     */
    private Integer defaultConnectionTimeoutInSeconds = 30;
    /**
     * mysql连接的receiveBufferSize
     */
    private Integer receiveBufferSize = 64 * 1024;
    /**
     * mysql连接的sendBufferSize
     */
    private Integer sendBufferSize = 64 * 1024;
    /**
     * 数据库发生切换查找时回退的时间
     */
    private Integer fallbackIntervalInSeconds = 60;
    /**
     * 匹配黑名单,忽略解析
     */
    private String blackFilter = ".*\\._.*";
    /**
     * 将某些特定类型的事件过滤掉
     */
    private List<EventType> filteredEventTypes = new ArrayList<>();
    /**
     * 起始时间戳，任务第一次启动时，通过该时间戳寻找位点
     */
    private Long startTimeStamps;
    /**
     * 分组同步模式下的Event-Sink模式
     */
    private GroupSinkMode groupSinkMode = GroupSinkMode.Coordinate;

    @Override
    public String initPluginName() {
        return "reader-mysql";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.reader.mysql.MysqlTaskReader";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.reader.mysql.MysqlTaskReaderListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.MYSQL);
    }

    public boolean isDumpDetail() {
        return dumpDetail;
    }

    public void setDumpDetail(boolean dumpDetail) {
        this.dumpDetail = dumpDetail;
    }

    public Long getBatchTimeout() {
        return batchTimeout;
    }

    public void setBatchTimeout(Long batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    public Integer getMessageBatchSize() {
        return messageBatchSize;
    }

    public void setMessageBatchSize(Integer messageBatchSize) {
        this.messageBatchSize = messageBatchSize;
    }

    public Integer getMemoryStorageBufferSize() {
        return memoryStorageBufferSize;
    }

    public void setMemoryStorageBufferSize(Integer memoryStorageBufferSize) {
        this.memoryStorageBufferSize = memoryStorageBufferSize;
    }

    public Integer getMemoryStorageBufferMemUnit() {
        return memoryStorageBufferMemUnit;
    }

    public void setMemoryStorageBufferMemUnit(Integer memoryStorageBufferMemUnit) {
        this.memoryStorageBufferMemUnit = memoryStorageBufferMemUnit;
    }

    public String getDetectingSQL() {
        return detectingSQL;
    }

    public void setDetectingSQL(String detectingSQL) {
        this.detectingSQL = detectingSQL;
    }

    public Integer getDetectingIntervalInSeconds() {
        return detectingIntervalInSeconds;
    }

    public void setDetectingIntervalInSeconds(Integer detectingIntervalInSeconds) {
        this.detectingIntervalInSeconds = detectingIntervalInSeconds;
    }

    public Integer getDetectingTimeoutThresholdInSeconds() {
        return detectingTimeoutThresholdInSeconds;
    }

    public void setDetectingTimeoutThresholdInSeconds(Integer detectingTimeoutThresholdInSeconds) {
        this.detectingTimeoutThresholdInSeconds = detectingTimeoutThresholdInSeconds;
    }

    public Integer getDetectingRetryTimes() {
        return detectingRetryTimes;
    }

    public void setDetectingRetryTimes(Integer detectingRetryTimes) {
        this.detectingRetryTimes = detectingRetryTimes;
    }

    public Integer getDefaultConnectionTimeoutInSeconds() {
        return defaultConnectionTimeoutInSeconds;
    }

    public void setDefaultConnectionTimeoutInSeconds(Integer defaultConnectionTimeoutInSeconds) {
        this.defaultConnectionTimeoutInSeconds = defaultConnectionTimeoutInSeconds;
    }

    public Integer getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(Integer receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public Integer getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(Integer sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public String getBlackFilter() {
        return blackFilter;
    }

    public void setBlackFilter(String blackFilter) {
        this.blackFilter = blackFilter;
    }

    public Integer getFallbackIntervalInSeconds() {
        return fallbackIntervalInSeconds;
    }

    public void setFallbackIntervalInSeconds(Integer fallbackIntervalInSeconds) {
        this.fallbackIntervalInSeconds = fallbackIntervalInSeconds;
    }

    public List<EventType> getFilteredEventTypes() {
        return filteredEventTypes;
    }

    public void setFilteredEventTypes(List<EventType> filteredEventTypes) {
        this.filteredEventTypes = filteredEventTypes;
    }

    public Long getStartTimeStamps() {
        return startTimeStamps;
    }

    public void setStartTimeStamps(Long startTimeStamps) {
        this.startTimeStamps = startTimeStamps;
    }

    public GroupSinkMode getGroupSinkMode() {
        return groupSinkMode;
    }

    public void setGroupSinkMode(GroupSinkMode groupSinkMode) {
        this.groupSinkMode = groupSinkMode;
    }
}