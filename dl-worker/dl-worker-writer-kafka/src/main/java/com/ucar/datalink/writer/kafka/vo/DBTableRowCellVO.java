package com.ucar.datalink.writer.kafka.vo;

import java.io.Serializable;

/**
 * @auther yifan.liu02
 * @date 2019/12/11
 */
public class DBTableRowCellVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String columnName;
    private String beforeValue;
    private String afterValue;

    public DBTableRowCellVO() {
    }

    public DBTableRowCellVO(String columnName, String beforeValue, String afterValue) {
        this.columnName = columnName;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }

    public void setAfterValue(String afterValue) {
        this.afterValue = afterValue;
    }
}
