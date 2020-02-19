package com.ucar.datalink.flinker.plugin.reader.hbasereader98.util;

import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.Key;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.List;

public class MultiVersionFixedColumnTask extends MultiVersionTask {
    private List<String> column = null;

    public MultiVersionFixedColumnTask(Configuration configuration) {
        super(configuration);

        this.column = configuration.getList(Key.COLUMN, String.class);
    }

    @Override
    public void initScan(Scan scan) {
        for (String aColumn : this.column) {
            String[] cfAndQualifier = aColumn.split(":");
            scan.addColumn(Bytes.toBytes(cfAndQualifier[0].trim()), Bytes.toBytes(cfAndQualifier[1].trim()));
        }
        super.setMaxVersions(scan);
    }
}
