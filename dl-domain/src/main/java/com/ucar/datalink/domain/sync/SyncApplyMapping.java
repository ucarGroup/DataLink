package com.ucar.datalink.domain.sync;

import com.ucar.datalink.domain.media.ColumnMappingMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sqq on 2017/9/22.
 */
public class SyncApplyMapping {

    private String sourceTableName;
    private String targetTableName;
    private ColumnMappingMode columnMappingMode;
    private List<String> sourceColumn;
    private List<String> targetColumn;
    private Map<String,String> otherMappingRelation;

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }

    public ColumnMappingMode getColumnMappingMode() {
        return columnMappingMode;
    }

    public void setColumnMappingMode(ColumnMappingMode columnMappingMode) {
        this.columnMappingMode = columnMappingMode;
    }

    public List<String> getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(List<String> sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public List<String> getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(List<String> targetColumn) {
        this.targetColumn = targetColumn;
    }

    public Map<String, String> getOtherMappingRelation() {
        return otherMappingRelation;
    }

    public void setOtherMappingRelation(Map<String, String> otherMappingRelation) {
        this.otherMappingRelation = otherMappingRelation;
    }

    @Override
    public String toString() {
        return "SyncApplyMapping{" +
                "sourceTableName='" + sourceTableName + '\'' +
                ", targetTableName='" + targetTableName + '\'' +
                ", columnMappingMode=" + columnMappingMode +
                ", sourceColumn=" + sourceColumn +
                ", targetColumn=" + targetColumn +
                ", otherMappingRelation=" + otherMappingRelation +
                '}';
    }
}
