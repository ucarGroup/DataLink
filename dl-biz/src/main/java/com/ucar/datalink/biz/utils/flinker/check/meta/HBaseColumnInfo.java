package com.ucar.datalink.biz.utils.flinker.check.meta;

/**
 * Created by yang.wang09 on 2018-04-23 18:47.
 */
public class HBaseColumnInfo extends ColumnInfo {

    private String colName = "";

    private String familyName;


    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    @Override
    public String toString() {
        return "HBaseColumnInfo{" +
                "colName='" + colName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", comment='" + comment + '\'' +
                ", hiveType='" + hiveType + '\'' +
                '}';
    }
}
