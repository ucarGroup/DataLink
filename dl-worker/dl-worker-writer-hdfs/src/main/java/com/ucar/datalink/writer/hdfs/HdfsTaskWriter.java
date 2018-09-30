package com.ucar.datalink.writer.hdfs;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.TaskWriter;
import com.ucar.datalink.writer.hdfs.handle.HRecordHandler;
import com.ucar.datalink.writer.hdfs.handle.RdbEventRecordHandler;

/**
 * Created by sqq on 2017/7/12.
 */
public class HdfsTaskWriter extends TaskWriter<HdfsWriterParameter> {

    @Override
    protected Handler getHandler(Class<? extends Record> clazz) {
        if (clazz.equals(RdbEventRecord.class)) {
            return new RdbEventRecordHandler(this, context, parameter);
        } else if (clazz.equals(HRecord.class)) {
            return new HRecordHandler(this, context, parameter);
        }

        return null;
    }
}
