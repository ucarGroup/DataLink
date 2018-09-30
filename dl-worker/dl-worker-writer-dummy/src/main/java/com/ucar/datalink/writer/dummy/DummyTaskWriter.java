package com.ucar.datalink.writer.dummy;

import com.google.common.collect.Lists;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.plugin.writer.dummy.DummyWriterParameter;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.TaskWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by lubiao on 2017/2/17.
 */
public class DummyTaskWriter extends TaskWriter<DummyWriterParameter> {
    private static Logger logger = LoggerFactory.getLogger(DummyWriterParameter.class);

    @Override
    public void start() {
        super.start();
        Lists.newArrayList();
    }

    @Override
    public void stop() {
        super.stop();
        logger.info("DummyTaskWriter stopped.");
    }

    @Override
    protected Handler getHandler(Class<? extends Record> clazz) {
        return new DummyTaskHandler();
    }
}
