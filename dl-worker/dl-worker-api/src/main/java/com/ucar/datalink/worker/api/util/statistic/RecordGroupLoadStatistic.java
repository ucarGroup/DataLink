package com.ucar.datalink.worker.api.util.statistic;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by lubiao on 2017/8/4.
 */
public class RecordGroupLoadStatistic {
    private long mediaSourceId;
    private long groupRecordsCount;
    private long groupLoadTime;
    private long groupTps;
    private Map<Object, Object> extendStatistic = new TreeMap<>();//extend statistic,每个writer插件可以自定义

    public long getMediaSourceId() {
        return mediaSourceId;
    }

    public void setMediaSourceId(long mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
    }

    public long getGroupRecordsCount() {
        return groupRecordsCount;
    }

    public void setGroupRecordsCount(long groupRecordsCount) {
        this.groupRecordsCount = groupRecordsCount;
    }

    public long getGroupLoadTime() {
        return groupLoadTime;
    }

    public void setGroupLoadTime(long groupLoadTime) {
        this.groupLoadTime = groupLoadTime;
    }

    public long getGroupTps() {
        return groupTps;
    }

    public void setGroupTps(long groupTps) {
        this.groupTps = groupTps;
    }

    public Map<Object, Object> getExtendStatistic() {
        return extendStatistic;
    }

    public void setExtendStatistic(Map<Object, Object> extendStatistic) {
        this.extendStatistic = extendStatistic;
    }
}
