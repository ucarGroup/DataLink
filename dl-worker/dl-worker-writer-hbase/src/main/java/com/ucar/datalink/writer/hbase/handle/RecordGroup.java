package com.ucar.datalink.writer.hbase.handle;

import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.task.TaskWriterContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lubiao on 2017/10/10.
 */
public class RecordGroup {

    private List<TableLoadData> tables = new ArrayList<>();

    public RecordGroup(List<RdbEventRecord> records, TaskWriterContext context) {
        records.stream().forEach(r -> {
            TableLoadData tableData = findTableData(r);
            tableData.getTableDatas().add(r);
        });
    }

    private synchronized TableLoadData findTableData(RdbEventRecord record) {
        TableKey key = new TableKey(record.getSchemaName(), record.getTableName());

        for (TableLoadData table : tables) {
            if (table.getTableKey().equals(key)) {
                return table;
            }
        }

        TableLoadData data = new TableLoadData(key);
        tables.add(data);
        return data;
    }

    public List<TableLoadData> getTables() {
        return tables;
    }

    /**
     * 按table进行分类
     */
    public static class TableLoadData {
        private TableKey tableKey;
        private List<RdbEventRecord> tableDatas = new LinkedList<>();

        public TableLoadData(TableKey tableKey) {
            this.tableKey = tableKey;
        }

        public TableKey getTableKey() {
            return tableKey;
        }

        public List<RdbEventRecord> getTableDatas() {
            return tableDatas;
        }
    }

    /**
     * Table的唯一性标识
     */
    public static class TableKey implements Serializable {
        private String schemaName;
        private String tableName;

        public TableKey(String schemaName, String tableName) {
            this.schemaName = schemaName;
            this.tableName = tableName;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TableKey tableKey = (TableKey) o;

            if (!schemaName.equals(tableKey.schemaName)) return false;
            return tableName.equals(tableKey.tableName);

        }

        @Override
        public int hashCode() {
            int result = schemaName.hashCode();
            result = 31 * result + tableName.hashCode();
            return result;
        }
    }
}
