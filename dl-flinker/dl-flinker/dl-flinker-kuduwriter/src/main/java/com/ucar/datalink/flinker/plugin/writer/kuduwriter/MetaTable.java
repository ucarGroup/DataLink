package com.ucar.datalink.flinker.plugin.writer.kuduwriter;

import org.apache.kudu.ColumnSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaTable {

    private Map<String, ColumnSchema> metaColumns;

    public MetaTable() {
        this.metaColumns = new HashMap<String, ColumnSchema>();
    }

    public MetaTable(List<ColumnSchema> columnSchemas) {
        this();
        putColumnSchema(columnSchemas);
    }

    public ColumnSchema getColumnSchema(String columnName) {
        return metaColumns.get(columnName);
    }

    public int getColumnSize() {
        return metaColumns.size();
    }

    public void putColumnSchema(ColumnSchema columnSchema) {
        if (columnSchema == null) {
            return;
        }
        metaColumns.put(columnSchema.getName(), columnSchema);
    }

    private void putColumnSchema(List<ColumnSchema> columnSchemas) {
        if (columnSchemas == null) {
            return;
        }

        for (ColumnSchema c : columnSchemas) {
            this.putColumnSchema(c);
        }
    }


}


