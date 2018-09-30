package com.ucar.datalink.domain.vo;

/**
 * Created by user on 2018/4/2.
 */
public class RdbmsOperatorVO {

    private long mediaSourceId;

    private String sql;

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
        return "RdbmsOperatorVO{" +
                "mediaSourceId=" + mediaSourceId +
                ", sql='" + sql + '\'' +
                '}';
    }

}
