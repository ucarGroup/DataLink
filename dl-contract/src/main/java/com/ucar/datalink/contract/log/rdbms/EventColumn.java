package com.ucar.datalink.contract.log.rdbms;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author lubiao
 */
public class EventColumn implements Serializable {

    private static final long serialVersionUID = 8881024631437131042L;

    private int index;

    /**
     * 列类型值
     */
    private int columnType;

    /**
     * 列名称
     */
    private String columnName;

    /**
     * 列的值，timestamp,Datetime是一个long型的数字.
     */
    private String columnValue;

    /**
     * 是否是空值
     */
    private boolean isNull;

    /**
     * 是否是主键
     */
    private boolean isKey;

    /**
     * 在更新事件中，该列是否是发生变更的列
     */
    private boolean isUpdate = true;

    public int getColumnType() {
        return columnType;
    }

    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnValue() {
        if (isNull) {
            columnValue = null;
            return null;
        } else {
            return columnValue;
        }
    }

    public void setColumnValue(String columnValue) {
        this.columnValue = columnValue;
    }

    public boolean isNull() {
        return isNull;
    }

    public void setNull(boolean isNull) {
        this.isNull = isNull;
    }

    public boolean isKey() {
        return isKey;
    }

    public void setKey(boolean isKey) {
        this.isKey = isKey;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public EventColumn clone() {
        EventColumn column = new EventColumn();
        column.setIndex(index);
        column.setColumnName(columnName);
        column.setColumnType(columnType);
        column.setColumnValue(columnValue);
        column.setKey(isKey);
        column.setNull(isNull);
        column.setUpdate(isUpdate);
        return column;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
        result = prime * result + columnType;
        result = prime * result + ((columnValue == null) ? 0 : columnValue.hashCode());
        result = prime * result + index;
        result = prime * result + (isKey ? 1231 : 1237);
        result = prime * result + (isNull ? 1231 : 1237);
        result = prime * result + (isUpdate ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        EventColumn other = (EventColumn) obj;
        if (columnName == null) {
            if (other.columnName != null) return false;
        } else if (!columnName.equals(other.columnName)) return false;
        if (columnType != other.columnType) return false;
        if (columnValue == null) {
            if (other.columnValue != null) return false;
        } else if (!columnValue.equals(other.columnValue)) return false;
        if (index != other.index) return false;
        if (isKey != other.isKey) return false;
        if (isNull != other.isNull) return false;
        if (isUpdate != other.isUpdate) return false;
        return true;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }

}
