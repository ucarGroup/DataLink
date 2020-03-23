package com.ucar.datalink.biz.utils.flinker.module;

/**
 * Created by user on 2017/7/26.
 */
public class HBaseJobExtendProperty extends AbstractJobExtendProperty {

    private String regionNum;

    private String columnFamily;

    private String hbaseSpecifiedNum;

    public String getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(String columnFamily) {
        this.columnFamily = columnFamily;
    }

    public String getRegionNum() {
        return regionNum;
    }

    public void setRegionNum(String regionNum) {
        this.regionNum = regionNum;
    }

    public String getHbaseSpecifiedNum() {
        return hbaseSpecifiedNum;
    }

    public void setHbaseSpecifiedNum(String hbaseSpecifiedNum) {
        this.hbaseSpecifiedNum = hbaseSpecifiedNum;
    }
}
