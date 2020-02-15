package com.ucar.datalink.contract.log.hbase;

import java.io.Serializable;

/**
 * Created by lubiao on 2017/11/15.
 */
public class HColumn implements Serializable {
    private static final long LATEST_TIMESTAMP = 9223372036854775807L;

    private byte[] family;
    private long timestamp;
    private byte[] qualifier;
    private byte[] value;
    private byte type;//同org.apache.hadoop.hbase.KeyValue.Type

    public HColumn() {
    }

    public HColumn(byte[] family, byte[] qualifier, byte[] value, byte type) {
        this(family, qualifier, value, type, LATEST_TIMESTAMP);
    }

    public HColumn(byte[] family, byte[] qualifier, byte[] value, byte type, long timestamp) {
        this.family = family;
        this.qualifier = qualifier;
        this.value = value;
        this.type = type;
        this.timestamp = timestamp;
    }

    public byte[] getFamily() {
        return family;
    }

    public void setFamily(byte[] family) {
        this.family = family;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getQualifier() {
        return qualifier;
    }

    public void setQualifier(byte[] qualifier) {
        this.qualifier = qualifier;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "HColumn{" +
                "family=" + HUtil.toString(family) +
                ", timestamp=" + timestamp +
                ", qualifier=" + HUtil.toString(qualifier) +
                ", value=" + HUtil.toString(value) +
                ", type=" + type +
                '}';
    }
}
