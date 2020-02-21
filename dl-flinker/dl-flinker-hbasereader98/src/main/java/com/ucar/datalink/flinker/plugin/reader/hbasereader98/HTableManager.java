package com.ucar.datalink.flinker.plugin.reader.hbasereader98;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;

import java.io.IOException;

public final class HTableManager {

    public static HTable createHTable(Configuration config, String tableName)
            throws IOException {

        return new HTable(config, tableName);
    }

    public static HBaseAdmin createHBaseAdmin(Configuration config)
            throws IOException {
        return new HBaseAdmin(config);
    }

    public static void closeHTable(HTable hTable) throws IOException {
        if (hTable != null) {
            hTable.close();
        }
    }
}
