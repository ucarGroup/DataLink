package com.ucar.datalink.writer.fq;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.plugin.writer.fq.FqWriterParameter;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.TaskWriter;
import com.ucar.datalink.writer.fq.handle.HRecordHandler;
import com.ucar.datalink.writer.fq.handle.RdbEventRecordHandler;

/**
 * Created by liuyifan
 */
public class FqTaskWriter extends TaskWriter<FqWriterParameter> {
    @Override
    protected Handler getHandler(Class<? extends Record> clazz) {
        if(clazz.equals(RdbEventRecord.class)){
            return new RdbEventRecordHandler();
        }else if(clazz.equals(HRecord.class)){
            return new HRecordHandler();
        }
        return null;
    }
}
