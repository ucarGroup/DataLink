package com.ucar.datalink.writer.hbase;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.plugin.writer.hbase.HBaseWriterParameter;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.TaskWriter;
import com.ucar.datalink.writer.hbase.handle.RdbEventRecordHandler;

/**
 * Created by sqq on 2017/11/29.
 */
public class HBaseTaskWriter extends TaskWriter<HBaseWriterParameter> {
    @Override
    protected Handler getHandler(Class<? extends Record> clazz) {
        return new RdbEventRecordHandler();
    }
}
