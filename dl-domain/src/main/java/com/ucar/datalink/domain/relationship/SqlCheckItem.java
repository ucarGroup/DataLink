package com.ucar.datalink.domain.relationship;

import java.util.List;

/**
 * Created by lubiao on 2017/7/14.
 */
public class SqlCheckItem {
    private String sqlString;
    private String tableName;
    private SqlType sqlType;
    private boolean containsTableRename;
    private boolean containsColumnRename;
    private boolean containsColumnDrop;
    private boolean containsColumnAdd;
    private boolean containsColumnAddAfter;
    private boolean containsColumnModify;
    private boolean containsColumnModifyAfter;
    private boolean containsUniqueKeysDrop;
    private boolean containsIndexesAdd;
    private boolean alterAffectColumn;
    private List<SqlCheckColumnInfo> columnsAddInfo;
    private List<String> uniqueKeysDropInfo;
    private List<String> indexesAddInfo;

    public String getSqlString() {
        return sqlString;
    }

    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }

    public boolean isContainsTableRename() {
        return containsTableRename;
    }

    public void setContainsTableRename(boolean containsTableRename) {
        this.containsTableRename = containsTableRename;
    }

    public boolean isContainsColumnRename() {
        return containsColumnRename;
    }

    public void setContainsColumnRename(boolean containsColumnRename) {
        this.containsColumnRename = containsColumnRename;
    }

    public boolean isContainsColumnDrop() {
        return containsColumnDrop;
    }

    public void setContainsColumnDrop(boolean containsColumnDrop) {
        this.containsColumnDrop = containsColumnDrop;
    }

    public boolean isContainsColumnAdd() {
        return containsColumnAdd;
    }

    public void setContainsColumnAdd(boolean containsColumnAdd) {
        this.containsColumnAdd = containsColumnAdd;
    }

    public boolean isContainsColumnAddAfter() {
        return containsColumnAddAfter;
    }

    public void setContainsColumnAddAfter(boolean containsColumnAddAfter) {
        this.containsColumnAddAfter = containsColumnAddAfter;
    }

    public boolean isContainsColumnModify() {
        return containsColumnModify;
    }

    public void setContainsColumnModify(boolean containsColumnModify) {
        this.containsColumnModify = containsColumnModify;
    }

    public boolean isContainsColumnModifyAfter() {
        return containsColumnModifyAfter;
    }

    public void setContainsColumnModifyAfter(boolean containsColumnModifyAfter) {
        this.containsColumnModifyAfter = containsColumnModifyAfter;
    }

    public boolean isContainsUniqueKeysDrop() {
        return containsUniqueKeysDrop;
    }

    public void setContainsUniqueKeysDrop(boolean containsUniqueKeysDrop) {
        this.containsUniqueKeysDrop = containsUniqueKeysDrop;
    }

    public boolean isContainsIndexesAdd() {
        return containsIndexesAdd;
    }

    public void setContainsIndexesAdd(boolean containsIndexesAdd) {
        this.containsIndexesAdd = containsIndexesAdd;
    }

    public boolean isAlterAffectColumn() {
        return alterAffectColumn;
    }

    public void setAlterAffectColumn(boolean alterAffectColumn) {
        this.alterAffectColumn = alterAffectColumn;
    }

    public List<SqlCheckColumnInfo> getColumnsAddInfo() {
        return columnsAddInfo;
    }

    public void setColumnsAddInfo(List<SqlCheckColumnInfo> columnsAddInfo) {
        this.columnsAddInfo = columnsAddInfo;
    }

    public List<String> getUniqueKeysDropInfo() {
        return uniqueKeysDropInfo;
    }

    public void setUniqueKeysDropInfo(List<String> uniqueKeysDropInfo) {
        this.uniqueKeysDropInfo = uniqueKeysDropInfo;
    }

    public List<String> getIndexesAddInfo() {
        return indexesAddInfo;
    }

    public void setIndexesAddInfo(List<String> indexesAddInfo) {
        this.indexesAddInfo = indexesAddInfo;
    }

    public boolean containsForbidden() {
        return containsColumnAddAfter || containsColumnDrop || containsColumnModifyAfter || containsColumnRename || containsTableRename;
    }
}
