package com.ucar.datalink.writer.kafka;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.plugin.writer.kafka.KafkaWriterParameter;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.TaskWriter;
import com.ucar.datalink.writer.kafka.handle.HRecordHandler;
import com.ucar.datalink.writer.kafka.handle.RdbEventRecordHandler;


public class KafkaTaskWriter extends TaskWriter<KafkaWriterParameter> {

    public static final String TOPIC_EXPERSSION_FLAG = "${dbTable}";

    @Override
    protected Handler getHandler(Class<? extends Record> clazz) {
        if (clazz.equals(RdbEventRecord.class)) {
            return new RdbEventRecordHandler();
        } else if (clazz.equals(HRecord.class)) {
            return new HRecordHandler();
        }
        return null;
    }
}
