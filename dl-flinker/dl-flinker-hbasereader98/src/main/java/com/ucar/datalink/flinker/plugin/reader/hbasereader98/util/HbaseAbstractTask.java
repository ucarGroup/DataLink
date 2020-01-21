package com.ucar.datalink.flinker.plugin.reader.hbasereader98.util;

import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.Constant;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.HTableManager;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.Key;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class HbaseAbstractTask {
    private final static Logger LOG = LoggerFactory.getLogger(HbaseAbstractTask.class);

    private int scanCache;
    private byte[] startKey = null;
    private byte[] endKey = null;

    protected HTable htable;
    protected String encoding;
    protected Result lastResult = null;
    protected Scan scan;
    protected ResultScanner resultScanner;

    public HbaseAbstractTask(Configuration configuration) {
        this.htable = HbaseUtil.initHtable(configuration);

        this.encoding = configuration.getString(Key.ENCODING);

        this.scanCache = configuration.getInt(Key.SCAN_CACHE, Constant.DEFAULT_SCAN_CACHE);

        this.startKey = HbaseUtil.convertInnerStartRowkey(configuration);
        this.endKey = HbaseUtil.convertInnerEndRowkey(configuration);
    }

    public abstract boolean fetchLine(Record record) throws Exception;

    public abstract void initScan(Scan scan);

    public void prepare() throws Exception {
        this.scan = new Scan();
        scan.setCacheBlocks(false);

        this.scan.setStartRow(startKey);
        this.scan.setStopRow(endKey);

        LOG.info("The task set startRowkey=[{}], endRowkey=[{}].", Bytes.toStringBinary(this.startKey), Bytes.toStringBinary(this.endKey));

        initScan(this.scan);

        this.scan.setCaching(this.scanCache);
        this.resultScanner = this.htable.getScanner(this.scan);
    }


    public void close() throws IOException {
        if (this.resultScanner != null) {
            this.resultScanner.close();
        }
        HTableManager.closeHTable(this.htable);
    }

    protected Result getNextHbaseRow() throws IOException {
        Result result;
        try {
            result = resultScanner.next();
        } catch (IOException e) {
            if (lastResult != null) {
                scan.setStartRow(lastResult.getRow());
            }
            resultScanner = this.htable.getScanner(scan);
            result = resultScanner.next();
            if (lastResult != null && Bytes.equals(lastResult.getRow(), result.getRow())) {
                result = resultScanner.next();
            }
        }

        lastResult = result;

        // may be null
        return result;
    }
}
