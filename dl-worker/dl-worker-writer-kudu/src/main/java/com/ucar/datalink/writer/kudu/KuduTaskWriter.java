package com.ucar.datalink.writer.kudu;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.plugin.writer.kudu.KuduWriterParameter;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.TaskWriter;
import com.ucar.datalink.writer.kudu.handle.RdbEventRecordHandler;

public class KuduTaskWriter extends TaskWriter<KuduWriterParameter> {

    @Override
    protected Handler getHandler(Class<? extends Record> clazz) {
        if(clazz.equals(RdbEventRecord.class)){
            return new RdbEventRecordHandler();
        }
        return null;
    }
}
