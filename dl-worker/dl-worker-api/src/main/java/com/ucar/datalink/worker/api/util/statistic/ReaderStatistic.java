package com.ucar.datalink.worker.api.util.statistic;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by lubiao on 2017/8/4.
 */
public class ReaderStatistic extends BaseReaderStatistic {
    public static final String KEY = "READER_STATISTIC";

    private String taskId;
    private long delayTimeAfterFetch;
    private long timeForTotalPoll;
    private long timeForWrite;

    private long timeForFetch;
    private int recordsCountByFetch;

    private long timeForFilter;
    private int recordsCountBeforeFilter;
    private int recordsCountAfterFilter;

    private Map<Object, Object> extendStatistic = new TreeMap<>();//extend statistic,每个reader插件可以自定义

    public ReaderStatistic(String taskId) {
        this.taskId = taskId;
    }

    public String toJsonString() {
        return JSONObject.toJSONString(this, true);
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getDelayTimeAfterFetch() {
        return delayTimeAfterFetch;
    }

    public void setDelayTimeAfterFetch(long delayTimeAfterFetch) {
        this.delayTimeAfterFetch = delayTimeAfterFetch;
    }

    public long getTimeForFetch() {
        return timeForFetch;
    }

    public void setTimeForFetch(long timeForFetch) {
        this.timeForFetch = timeForFetch;
    }

    public int getRecordsCountByFetch() {
        return recordsCountByFetch;
    }

    public void setRecordsCountByFetch(int recordsCountByFetch) {
        this.recordsCountByFetch = recordsCountByFetch;
    }

    public long getTimeForFilter() {
        return timeForFilter;
    }

    public void setTimeForFilter(long timeForFilter) {
        this.timeForFilter = timeForFilter;
    }

    public int getRecordsCountBeforeFilter() {
        return recordsCountBeforeFilter;
    }

    public void setRecordsCountBeforeFilter(int recordsCountBeforeFilter) {
        this.recordsCountBeforeFilter = recordsCountBeforeFilter;
    }

    public int getRecordsCountAfterFilter() {
        return recordsCountAfterFilter;
    }

    public void setRecordsCountAfterFilter(int recordsCountAfterFilter) {
        this.recordsCountAfterFilter = recordsCountAfterFilter;
    }

    public long getTimeForTotalPoll() {
        return timeForTotalPoll;
    }

    public void setTimeForTotalPoll(long timeForTotalPoll) {
        this.timeForTotalPoll = timeForTotalPoll;
    }

    public Map<Object, Object> getExtendStatistic() {
        return extendStatistic;
    }

    public void setExtendStatistic(Map<Object, Object> extendStatistic) {
        this.extendStatistic = extendStatistic;
    }

    public long getTimeForWrite() {
        return timeForWrite;
    }

    public void setTimeForWrite(long timeForWrite) {
        this.timeForWrite = timeForWrite;
    }
}
