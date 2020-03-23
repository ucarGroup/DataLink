package com.ucar.datalink.flinker.core.taskgroup.runner;

import com.ucar.datalink.flinker.api.plugin.AbstractTaskPlugin;
import com.ucar.datalink.flinker.api.plugin.RecordSender;
import com.ucar.datalink.flinker.api.spi.Reader;
import com.ucar.datalink.flinker.api.statistics.PerfRecord;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import com.ucar.datalink.flinker.core.statistics.communication.CommunicationTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jingxing on 14-9-1.
 * <p/>
 * 单个slice的reader执行调用
 */
public class ReaderRunner extends AbstractRunner implements Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(ReaderRunner.class);

    private RecordSender recordSender;

    public void setRecordSender(RecordSender recordSender) {
        this.recordSender = recordSender;
    }

    public ReaderRunner(AbstractTaskPlugin abstractTaskPlugin) {
        super(abstractTaskPlugin);
    }

    @Override
    public void run() {
        assert null != this.recordSender;

        Reader.Task taskReader = (Reader.Task) this.getPlugin();

        //统计waitWriterTime，并且在finally才end。
        PerfRecord channelWaitWrite = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.WAIT_WRITE_TIME);
        try {
            channelWaitWrite.start();

            LOG.debug("task reader starts to do init ...");
            PerfRecord initPerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.READ_TASK_INIT);
            initPerfRecord.start();
            taskReader.init();
            initPerfRecord.end();

            LOG.debug("task reader starts to do prepare ...");
            PerfRecord preparePerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.READ_TASK_PREPARE);
            preparePerfRecord.start();
            taskReader.prepare();
            preparePerfRecord.end();

            LOG.debug("task reader starts to read ...");
            PerfRecord dataPerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.READ_TASK_DATA);
            dataPerfRecord.start();
            taskReader.startRead(recordSender);
            recordSender.terminate();

            dataPerfRecord.addCount(CommunicationTool.getTotalReadRecords(super.getRunnerCommunication()));
            dataPerfRecord.addSize(CommunicationTool.getTotalReadBytes(super.getRunnerCommunication()));
            dataPerfRecord.end();

            LOG.debug("task reader starts to do post ...");
            PerfRecord postPerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.READ_TASK_POST);
            postPerfRecord.start();
            taskReader.post();
            postPerfRecord.end();
            // automatic flush
            // super.markSuccess(); 这里不能标记为成功，成功的标志由 writerRunner 来标志（否则可能导致 reader 先结束，而 writer 还没有结束的严重 bug）
        } catch (Throwable e) {
            LOG.error("Reader runner Received Exceptions:", e);
            ErrorRecord.addError("Reader runner Received Exceptions:"+e.getMessage());
            super.markFail(e);
        } finally {
            LOG.debug("task reader starts to do destroy ...");
            PerfRecord desPerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.READ_TASK_DESTROY);
            desPerfRecord.start();
            super.destroy();
            desPerfRecord.end();

            channelWaitWrite.end(super.getRunnerCommunication().getLongCounter(CommunicationTool.WAIT_WRITER_TIME));
        }
    }

    public void shutdown(){
        recordSender.shutdown();
    }
}
