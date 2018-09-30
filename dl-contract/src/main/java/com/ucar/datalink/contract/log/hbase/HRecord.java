package com.ucar.datalink.contract.log.hbase;

import com.ucar.datalink.contract.RSI;
import com.ucar.datalink.contract.Record;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Hbase数据库的数据模型抽象.
 * <p>
 * Created by lubiao on 2017/11/15.
 */
public class HRecord extends Record<byte[]> implements Serializable {
    private byte[] rowKey;
    private String namespace;
    private String tableName;
    private List<HColumn> columns;
    private RSI rsi;

    public HRecord() {
    }

    public HRecord(String namespace, String tableName, byte[] rowKey, HColumn... columns) {
        this(namespace, tableName, rowKey, Arrays.asList(columns));
    }

    public HRecord(String namespace, String tableName, byte[] rowKey, List<HColumn> columns) {
        this.namespace = namespace;
        this.tableName = tableName;
        this.rowKey = rowKey;
        this.columns = columns;
        this.rsi = new RSI(namespace, tableName);
    }

    @Override
    public byte[] getId() {
        return rowKey;
    }

    @Override
    public RSI RSI() {
        if (rsi == null) {
            rsi = new RSI(namespace, tableName);
        }
        return rsi;
    }

    public byte[] getRowKey() {
        return rowKey;
    }

    public void setRowKey(byte[] rowKey) {
        this.rowKey = rowKey;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
        this.rsi = null;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        this.rsi = null;
    }

    public List<HColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<HColumn> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "HRecord{" +
                "rowKey=" + HUtil.toString(rowKey) +
                ", namespace='" + namespace + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columns=" + columns +
                '}';
    }
}
