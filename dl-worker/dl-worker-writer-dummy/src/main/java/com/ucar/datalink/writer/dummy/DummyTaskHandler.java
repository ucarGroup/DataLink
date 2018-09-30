package com.ucar.datalink.writer.dummy;

import com.ucar.datalink.contract.log.dummy.DummyRecord;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by user on 2017/3/15.
 */
public class DummyTaskHandler implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(DummyTaskHandler.class);

    @Override
    public void initialize(TaskWriterContext context) {

    }

    @Override
    public void destroy() {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeData(RecordChunk recordChunk, TaskWriterContext context) {
        if (recordChunk.getRecords().get(0) instanceof DummyRecord) {
            for (DummyRecord record : (List<DummyRecord>) recordChunk.getRecords()) {
                logger.info("Dummy Task Writer :" + record.getId());
            }
        } else if (recordChunk.getRecords().get(0) instanceof RdbEventRecord) {
            for (RdbEventRecord record : (List<RdbEventRecord>) recordChunk.getRecords()) {
                logger.info("Event type is :" + record.getEventType().toString());
            }
        }
    }
}
