package com.ucar.datalink.writer.es;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.plugin.writer.es.EsWriterParameter;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.TaskWriter;
import com.ucar.datalink.writer.es.handle.RdbEventRecordHandler;

/**
 * Created by lubiao on 2017/6/15.
 */
public class EsTaskWriter extends TaskWriter<EsWriterParameter>{

    @Override
    protected Handler getHandler(Class<? extends Record> clazz) {
        return new RdbEventRecordHandler();
    }
}
