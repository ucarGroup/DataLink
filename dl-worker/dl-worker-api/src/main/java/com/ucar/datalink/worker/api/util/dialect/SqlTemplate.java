package com.ucar.datalink.worker.api.util.dialect;

/**
 * sql构造模板操作类.
 * Created by lubiao on 2017/3/8.
 */
public interface SqlTemplate {

    String getSelectSql(String schemaName, String tableName, String[] pkNames, String[] columnNames);

    String getUpdateSql(String schemaName, String tableName, String[] pkNames, String[] columnNames);

    String getDeleteSql(String schemaName, String tableName, String[] pkNames);

    String getInsertSql(String schemaName, String tableName, String[] pkNames, String[] columnNames);

    /**
     * 获取对应的mergeSql
     */
    String getMergeSql(String schemaName, String tableName, String[] pkNames, String[] columnNames,
                       String[] viewColumnNames);
}
