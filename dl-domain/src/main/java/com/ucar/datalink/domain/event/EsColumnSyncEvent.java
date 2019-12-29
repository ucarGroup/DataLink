package com.ucar.datalink.domain.event;

import com.ucar.datalink.common.event.CallbackEvent;
import com.ucar.datalink.common.utils.FutureCallback;

public class EsColumnSyncEvent extends CallbackEvent {
    private Long mediaSourceId;
    private String sql;
    private Long mappingId;

    public EsColumnSyncEvent(FutureCallback callback, Long mediaSourceId,Long mappingId, String sql) {
        super(callback);
        this.mediaSourceId = mediaSourceId;
        this.sql = sql;
        this.mappingId = mappingId;
    }

    public Long getMediaSourceId() {
        return mediaSourceId;
    }

    public void setMediaSourceId(Long mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Long getMappingId() {
        return mappingId;
    }

    public void setMappingId(Long mappingId) {
        this.mappingId = mappingId;
    }
}
