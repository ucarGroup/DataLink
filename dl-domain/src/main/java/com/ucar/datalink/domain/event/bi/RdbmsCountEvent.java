package com.ucar.datalink.domain.event.bi;

import com.ucar.datalink.common.event.CallbackEvent;
import com.ucar.datalink.common.utils.FutureCallback;

/**
 * Created by user on 2018/3/29.
 */
public class RdbmsCountEvent extends CallbackEvent {

    private long mediaSourceId;

    private String sql;

    public RdbmsCountEvent(FutureCallback event,long mediaSourceId,String sql) {
        super(event);
        this.mediaSourceId = mediaSourceId;
        this.sql = sql;
    }

    public long getMediaSourceId() {
        return mediaSourceId;
    }

    public void setMediaSourceId(long mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return "RdbmsCountEvent{" +
                "mediaSourceId=" + mediaSourceId +
                ", sql='" + sql + '\'' +
                '}';
    }
}
