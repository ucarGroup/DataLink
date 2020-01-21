package com.ucar.datalink.domain.plugin.reader.fq;

import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginReaderParameter;

import java.util.Set;

/**
 * Created by sqq on 2017/6/13.
 */
public class FqReaderParameter extends PluginReaderParameter {

    private String group;
    private Long offset = 9223372036854775807L;
    private Integer maxFetchRetries = 2147483647;
    private Long commitOffsetPeriodInMills = 500L;
    private Long maxDelayFetchTimeInMills = 200L;
    private Long originalMediaSourceId;
    private Integer bufferSize = 100;

    @Override
    public String initPluginName() {
        return "reader-fq";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.reader.fq.FqTaskReader";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.reader.fq.FqTaskReaderListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.FLEXIBLEQ);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Integer getMaxFetchRetries() {
        return maxFetchRetries;
    }

    public void setMaxFetchRetries(Integer maxFetchRetries) {
        this.maxFetchRetries = maxFetchRetries;
    }

    public Long getCommitOffsetPeriodInMills() {
        return commitOffsetPeriodInMills;
    }

    public void setCommitOffsetPeriodInMills(Long commitOffsetPeriodInMills) {
        this.commitOffsetPeriodInMills = commitOffsetPeriodInMills;
    }

    public Long getMaxDelayFetchTimeInMills() {
        return maxDelayFetchTimeInMills;
    }

    public void setMaxDelayFetchTimeInMills(Long maxDelayFetchTimeInMills) {
        this.maxDelayFetchTimeInMills = maxDelayFetchTimeInMills;
    }

    public Long getOriginalMediaSourceId() {
        return originalMediaSourceId;
    }

    public void setOriginalMediaSourceId(Long originalMediaSourceId) {
        this.originalMediaSourceId = originalMediaSourceId;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }
}
