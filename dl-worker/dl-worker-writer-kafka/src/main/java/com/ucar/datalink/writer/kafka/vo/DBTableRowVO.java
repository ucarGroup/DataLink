package com.ucar.datalink.writer.kafka.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @auther yifan.liu02
 * @date 2019/12/11
 */
public class DBTableRowVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String databaseName;
    private String tableName;
    private DBEventType eventType;
    private String id;
    private List<DBTableRowCellVO> dbTableRowCellVOList;

    public DBTableRowVO() {
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public DBEventType getEventType() {
        return eventType;
    }

    public void setEventType(DBEventType eventType) {
        this.eventType = eventType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DBTableRowCellVO> getDbTableRowCellVOList() {
        return dbTableRowCellVOList;
    }

    public void setDbTableRowCellVOList(List<DBTableRowCellVO> dbTableRowCellVOList) {
        this.dbTableRowCellVOList = dbTableRowCellVOList;
    }
}
