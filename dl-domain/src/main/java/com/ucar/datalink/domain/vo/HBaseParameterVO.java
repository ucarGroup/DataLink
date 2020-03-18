package com.ucar.datalink.domain.vo;

import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;

/**
 * Created by user on 2017/7/2.
 */
public class HBaseParameterVO {

    private String tableName;

    private String zkAddress;

    private String port;

    private String znode;

    private int splitCount;

    private int onceFethAmount;

    private HBaseMediaSrcParameter hbaseParameter;

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getZnode() {
        return znode;
    }

    public void setZnode(String znode) {
        this.znode = znode;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }

    public int getOnceFethAmount() {
        return onceFethAmount;
    }

    public void setOnceFethAmount(int onceFethAmount) {
        this.onceFethAmount = onceFethAmount;
    }

    public HBaseMediaSrcParameter getHbaseParameter() {
        return hbaseParameter;
    }

    public void setHbaseParameter(HBaseMediaSrcParameter hbaseParameter) {
        this.hbaseParameter = hbaseParameter;
    }

    @Override
    public String toString() {
        return "HBaseParameterVO{" +
                "zkAddress='" + zkAddress + '\'' +
                ", port='" + port + '\'' +
                ", znode='" + znode + '\'' +
                '}';
    }

}
