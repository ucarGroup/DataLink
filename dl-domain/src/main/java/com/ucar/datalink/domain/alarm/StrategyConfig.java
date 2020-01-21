package com.ucar.datalink.domain.alarm;

/**
 * @author wenbin.song
 * @date 2019/10/16
 * 报警参数配置
 */
public class StrategyConfig {
    /**
     * 名称
     */
    private String name;
    /**
     * 延迟阈值
     */
    private Integer threshold = 5000;
    /**
     * 报警间隔阈值
     */
    private Long intervalTime = 600L;
    /**
     * 监控时间范围
     */
    private String timeRange = "00:00-23:59";
    /**
     * 是否发送电话报警
     */
    private Boolean isPhone = false;
    /**
     * 是否发送短信报警
     */
    private Boolean isSMS = false;
    /**
     * 是否发送钉钉报警
     */
    private Boolean isDingD =false;
    /**
     * 是否错误阈值触发电话报警
     */
    private Boolean isThreshold = false;
    /**
     * 电话报警阈值
     */
    private Integer phoneThreshold;
    /**
     * 其他配置 , json格式
     */
    private String otherConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Long getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(Long intervalTime) {
        this.intervalTime = intervalTime;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public Boolean isPhone() {
        return isPhone;
    }

    public void setPhone(Boolean phone) {
        isPhone = phone;
    }

    public Boolean isSMS() {
        return isSMS;
    }

    public void setSMS(Boolean SMS) {
        isSMS = SMS;
    }

    public String getOtherConfig() {
        return otherConfig;
    }

    public void setOtherConfig(String otherConfig) {
        this.otherConfig = otherConfig;
    }

    public Boolean isDingD() {
        return isDingD;
    }

    public void setDingD(Boolean dingD) {
        isDingD = dingD;
    }

    public Boolean isThreshold() {
        return isThreshold;
    }

    public void setThreshold(Boolean threshold) {
        isThreshold = threshold;
    }

    public Integer getPhoneThreshold() {
        return phoneThreshold;
    }

    public void setPhoneThreshold(Integer phoneThreshold) {
        this.phoneThreshold = phoneThreshold;
    }
}
