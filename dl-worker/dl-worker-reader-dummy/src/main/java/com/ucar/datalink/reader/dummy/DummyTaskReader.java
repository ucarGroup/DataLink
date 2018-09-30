package com.ucar.datalink.reader.dummy;

import com.google.common.collect.Lists;
import com.ucar.datalink.domain.plugin.reader.dummy.DummyReaderParameter;
import com.ucar.datalink.domain.plugin.reader.dummy.DummyReaderPosition;
import com.ucar.datalink.contract.log.dummy.DummyRecord;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskReader;
import com.ucar.datalink.worker.api.task.TaskReaderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * DummyTaskReader-开发测试用
 * <p>
 * Created by lubiao on 2017/2/17.
 */
public class DummyTaskReader extends TaskReader<DummyReaderParameter, DummyRecord> {
    private static final Logger logger = LoggerFactory.getLogger(DummyTaskReader.class);

    private Long count = 0L;

    @Override
    public void initialize(TaskReaderContext context) {
        super.initialize(context);
        if (context.positionManager().getPosition(context.taskId()) != null) {
            this.count = ((DummyReaderPosition) context.positionManager().getPosition(context.taskId())).getCount();
        }
    }


    @Override
    public void start() {
        super.start();
    }

    @Override
    public RecordChunk<DummyRecord> fetch() throws InterruptedException {
        Thread.sleep(1000L);
        count++;
        DummyRecord r = new DummyRecord();
        return new RecordChunk<>(Lists.newArrayList(r), System.currentTimeMillis(), 1);
    }

    @Override
    public void stop() {
        super.stop();
        logger.info("DummyTaskReader stopped.");
    }

    @Override
    public void commit(RecordChunk<DummyRecord> recordChunk) throws InterruptedException {
        super.commit(recordChunk);
    }

    @Override
    protected void dump(RecordChunk<DummyRecord> recordChunk) {

    }
}
