package com.ucar.datalink.worker.api.util.statistic;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lubiao on 2017/8/4.
 */
public class WriterStatistic extends BaseWriterStatistic {
    public static final String KEY = "WRITER_STATISTIC";

    private String taskId;
    private PluginWriterParameter writerParameter;


    //mapping statistic
    private long timeForMapping;
    private int recordsCountBeforeMapping;
    private int recordsCountAfterMapping;

    //intercept statistic
    private long timeForIntercept;
    private int recordsCountBeforeIntercept;
    private int recordsCountAftereIntercept;

    //merge statistic
    private long timeForMerge;
    private int recordsCountBeforeMerge;
    private int recordsCountAfterMerge;

    //transform statistic
    private long timeForTransform;
    private int recordsCountBeforeTransform;
    private int recordsCountAfterTransform;

    //group statistic
    private long timeForGroup;
    private int recordsCountBeforeGroup;
    private int recordsCountAfterGroup;
    private int recordsGroupCount;

    //load statistic
    //one element per group
    private long timeForLoad;
    private Map<Long,RecordGroupLoadStatistic> groupLoadStatistics = new ConcurrentHashMap<>();


    public WriterStatistic(String taskId, PluginWriterParameter writerParameter) {
        this.taskId = taskId;
        this.writerParameter = writerParameter;
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

    public PluginWriterParameter getWriterParameter() {
        return writerParameter;
    }

    public void setWriterParameter(PluginWriterParameter writerParameter) {
        this.writerParameter = writerParameter;
    }

    public long getTimeForMapping() {
        return timeForMapping;
    }

    public void setTimeForMapping(long timeForMapping) {
        this.timeForMapping = timeForMapping;
    }

    public int getRecordsCountBeforeMapping() {
        return recordsCountBeforeMapping;
    }

    public void setRecordsCountBeforeMapping(int recordsCountBeforeMapping) {
        this.recordsCountBeforeMapping = recordsCountBeforeMapping;
    }

    public int getRecordsCountAfterMapping() {
        return recordsCountAfterMapping;
    }

    public void setRecordsCountAfterMapping(int recordsCountAfterMapping) {
        this.recordsCountAfterMapping = recordsCountAfterMapping;
    }

    public long getTimeForIntercept() {
        return timeForIntercept;
    }

    public void setTimeForIntercept(long timeForIntercept) {
        this.timeForIntercept = timeForIntercept;
    }

    public int getRecordsCountBeforeIntercept() {
        return recordsCountBeforeIntercept;
    }

    public void setRecordsCountBeforeIntercept(int recordsCountBeforeIntercept) {
        this.recordsCountBeforeIntercept = recordsCountBeforeIntercept;
    }

    public int getRecordsCountAftereIntercept() {
        return recordsCountAftereIntercept;
    }

    public void setRecordsCountAftereIntercept(int recordsCountAftereIntercept) {
        this.recordsCountAftereIntercept = recordsCountAftereIntercept;
    }

    public long getTimeForMerge() {
        return timeForMerge;
    }

    public void setTimeForMerge(long timeForMerge) {
        this.timeForMerge = timeForMerge;
    }

    public int getRecordsCountBeforeMerge() {
        return recordsCountBeforeMerge;
    }

    public void setRecordsCountBeforeMerge(int recordsCountBeforeMerge) {
        this.recordsCountBeforeMerge = recordsCountBeforeMerge;
    }

    public int getRecordsCountAfterMerge() {
        return recordsCountAfterMerge;
    }

    public void setRecordsCountAfterMerge(int recordsCountAfterMerge) {
        this.recordsCountAfterMerge = recordsCountAfterMerge;
    }

    public long getTimeForTransform() {
        return timeForTransform;
    }

    public void setTimeForTransform(long timeForTransform) {
        this.timeForTransform = timeForTransform;
    }

    public int getRecordsCountBeforeTransform() {
        return recordsCountBeforeTransform;
    }

    public void setRecordsCountBeforeTransform(int recordsCountBeforeTransform) {
        this.recordsCountBeforeTransform = recordsCountBeforeTransform;
    }

    public int getRecordsCountAfterTransform() {
        return recordsCountAfterTransform;
    }

    public void setRecordsCountAfterTransform(int recordsCountAfterTransform) {
        this.recordsCountAfterTransform = recordsCountAfterTransform;
    }

    public long getTimeForGroup() {
        return timeForGroup;
    }

    public void setTimeForGroup(long timeForGroup) {
        this.timeForGroup = timeForGroup;
    }

    public int getRecordsCountBeforeGroup() {
        return recordsCountBeforeGroup;
    }

    public void setRecordsCountBeforeGroup(int recordsCountBeforeGroup) {
        this.recordsCountBeforeGroup = recordsCountBeforeGroup;
    }

    public int getRecordsCountAfterGroup() {
        return recordsCountAfterGroup;
    }

    public void setRecordsCountAfterGroup(int recordsCountAfterGroup) {
        this.recordsCountAfterGroup = recordsCountAfterGroup;
    }

    public int getRecordsGroupCount() {
        return recordsGroupCount;
    }

    public void setRecordsGroupCount(int recordsGroupCount) {
        this.recordsGroupCount = recordsGroupCount;
    }

    public long getTimeForLoad() {
        return timeForLoad;
    }

    public void setTimeForLoad(long timeForLoad) {
        this.timeForLoad = timeForLoad;
    }

    public Map<Long, RecordGroupLoadStatistic> getGroupLoadStatistics() {
        return groupLoadStatistics;
    }

    public void setGroupLoadStatistics(Map<Long, RecordGroupLoadStatistic> groupLoadStatistics) {
        this.groupLoadStatistics = groupLoadStatistics;
    }
}
