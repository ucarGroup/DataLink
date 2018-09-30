package com.ucar.datalink.domain.plugin;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Plugin-Writer参数基类
 * <p>
 * Created by lubiao on 2017/2/17.
 */
public abstract class PluginWriterParameter extends PluginParameter {
    /**
     * Writer的线程池大小,根据MediaMapping的配置情况设置一个合理的值
     */
    private int poolSize = 5;
    /**
     * dryRun时不会进行实际写入操作
     */
    private boolean dryRun = false;
    /**
     * 是否开启批量写入
     * 不同的writer对批量写入会有不同的定义
     */
    private boolean useBatch = true;
    /**
     * 批量写入时每个批次的大小
     */
    private int batchSize = 50;
    /**
     * 是否可以对数据进行合并
     */
    private boolean merging = false;
    /**
     * 最大重试次数
     */
    private int maxRetryTimes = 3;
    /**
     * 重试模式
     */
    private RetryMode retryMode = RetryMode.Always;
    /**
     * 目标端缺少列时，是否自动加列
     */
    private boolean syncAutoAddColumn = true;


    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isUseBatch() {
        return useBatch;
    }

    public void setUseBatch(boolean useBatch) {
        this.useBatch = useBatch;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isMerging() {
        return merging;
    }

    public void setMerging(boolean merging) {
        this.merging = merging;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public RetryMode getRetryMode() {
        return retryMode;
    }

    public void setRetryMode(RetryMode retryMode) {
        this.retryMode = retryMode;
    }

    public boolean isSyncAutoAddColumn() {
        return syncAutoAddColumn;
    }

    public void setSyncAutoAddColumn(boolean syncAutoAddColumn) {
        this.syncAutoAddColumn = syncAutoAddColumn;
    }

    public static enum RetryMode {
        Always,//一直重试
        TimesOutDiscard,//超过重试次数后丢弃数据
        TimesOutError,//超过重试次数后抛异常，终止任务
        NoAndError;//不重试，直接抛异常,终止Task

        public static List<RetryMode> getAllModes() {
            return Lists.newArrayList(Always, TimesOutError, TimesOutDiscard, NoAndError);
        }
    }
}
