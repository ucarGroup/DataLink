package com.ucar.datalink.biz.utils.flinker.check.meta;

import java.util.Arrays;

/**
 * Created by yang.wang09 on 2018-04-23 11:33.
 */
public class SyncTableInfo {

    protected String database;

    protected String table;

    protected String dbType;

    private boolean isFull = false;

    private boolean isIncrement = false;

    private String applicant;

    private String hdfsLocation = "";

    protected ColumnInfo[] columns;



    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public boolean getIsFull() {
        return isFull;
    }

    public void setIsFull(boolean isFull) {
        this.isFull = isFull;
    }

    public boolean getIsIncrement() {
        return isIncrement;
    }

    public void setIsIncrement(boolean isIncrement) {
        this.isIncrement = isIncrement;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getHdfsLocation() {
        return hdfsLocation;
    }

    public void setHdfsLocation(String hdfsLocation) {
        this.hdfsLocation = hdfsLocation;
    }

    public ColumnInfo[] getColumns() {
        return columns;
    }

    public void setColumns(ColumnInfo[] columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "SyncTableInfo{" +
                "database='" + database + '\'' +
                ", table='" + table + '\'' +
                ", dbType='" + dbType + '\'' +
                ", isFull=" + isFull +
                ", isIncrement=" + isIncrement +
                ", applicant='" + applicant + '\'' +
                ", hdfsLocation='" + hdfsLocation + '\'' +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }
}
