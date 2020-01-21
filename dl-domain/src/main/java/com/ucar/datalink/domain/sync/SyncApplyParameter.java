package com.ucar.datalink.domain.sync;

import java.util.List;

/**
 * Created by sqq on 2017/9/20.
 */
public class SyncApplyParameter {

    private Long srcMediaSourceId;
    private Long targetMediaSourceId;
    List<SyncApplyMapping> syncApplyMappings;

    public Long getSrcMediaSourceId() {
        return srcMediaSourceId;
    }

    public void setSrcMediaSourceId(Long srcMediaSourceId) {
        this.srcMediaSourceId = srcMediaSourceId;
    }

    public Long getTargetMediaSourceId() {
        return targetMediaSourceId;
    }

    public void setTargetMediaSourceId(Long targetMediaSourceId) {
        this.targetMediaSourceId = targetMediaSourceId;
    }

    public List<SyncApplyMapping> getSyncApplyMappings() {
        return syncApplyMappings;
    }

    public void setSyncApplyMappings(List<SyncApplyMapping> syncApplyMappings) {
        this.syncApplyMappings = syncApplyMappings;
    }
}
