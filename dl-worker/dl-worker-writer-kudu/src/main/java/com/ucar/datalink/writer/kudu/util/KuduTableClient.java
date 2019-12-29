package com.ucar.datalink.writer.kudu.util;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduSession;
import org.apache.kudu.client.KuduTable;

import java.util.List;

public class KuduTableClient {

    private KuduClient client;
    private KuduTable kuduTable;
    private MetaTable metaTable;
    private KuduSession kuduSession;
    private int batchSize;


    public KuduTableClient(KuduClient client, KuduTable kuduTable, KuduSession kuduSession,int batchSize) {
        this.client = client;
        this.kuduTable = kuduTable;
        this.metaTable = getMetaTable(kuduTable);
        this.kuduSession = kuduSession;
        this.batchSize = batchSize;
    }

    public KuduClient getClient() {
        return client;
    }

    public KuduTable getKuduTable() {
        return kuduTable;
    }

    public int getHalfBatchSize(){
        return batchSize < 1 ? 1 : batchSize / 2;
    }

    public MetaTable getMetaTable() {
        return metaTable;
    }

    public KuduSession getKuduSession() {
        return kuduSession;
    }

    private MetaTable getMetaTable(KuduTable kuduTable){
        List<ColumnSchema> columns = kuduTable.getSchema().getColumns();
        return new  MetaTable(columns);
    }

}
