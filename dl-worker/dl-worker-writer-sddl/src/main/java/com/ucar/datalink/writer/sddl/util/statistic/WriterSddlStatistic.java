package com.ucar.datalink.writer.sddl.util.statistic;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.worker.api.util.statistic.BaseWriterStatistic;
import com.ucar.datalink.worker.api.util.statistic.RecordGroupLoadStatistic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 *
 * @Author : yongwang.chen@ucarinc.com
 * @Date   : 1:44 PM 08/12/2017
 */
public class WriterSddlStatistic extends BaseWriterStatistic {

    private String taskId;
    private PluginWriterParameter writerParameter;

    //intercept statistic
    private long timeForIntercept;
    private int recordsCountBeforeIntercept;
    private int recordsCountAftereIntercept;

    //buildSql statistic
    private long timeForBuild;
    private int recordsCountBeforeBuild;
    private int recordsCountAfterBuild;
    private int recordsBuildDsCount;

    //load statistic
    private long timeForLoad;
    private Map<Long,RecordGroupLoadStatistic> groupLoadStatistics = new ConcurrentHashMap<>();


    public WriterSddlStatistic(String taskId, PluginWriterParameter writerParameter) {
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

    public long getTimeForBuild() {
        return timeForBuild;
    }

    public void setTimeForBuild(long timeForBuild) {
        this.timeForBuild = timeForBuild;
    }

    public int getRecordsCountBeforeBuild() {
        return recordsCountBeforeBuild;
    }

    public void setRecordsCountBeforeBuild(int recordsCountBeforeBuild) {
        this.recordsCountBeforeBuild = recordsCountBeforeBuild;
    }

    public int getRecordsCountAfterBuild() {
        return recordsCountAfterBuild;
    }

    public void setRecordsCountAfterBuild(int recordsCountAfterBuild) {
        this.recordsCountAfterBuild = recordsCountAfterBuild;
    }

    public int getRecordsBuildDsCount() {
        return recordsBuildDsCount;
    }

    public void setRecordsBuildDsCount(int recordsBuildDsCount) {
        this.recordsBuildDsCount = recordsBuildDsCount;
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
