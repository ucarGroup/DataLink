package com.ucar.datalink.reader.hbase;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.zookeeper.ZkConfig;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.plugin.reader.hbase.HBaseReaderParameter;
import com.ucar.datalink.reader.hbase.replicate.HRecordChunk;
import com.ucar.datalink.reader.hbase.replicate.ReplicateHRegionServer;
import com.ucar.datalink.reader.hbase.replicate.ReplicationConfig;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by user on 2017/6/29.
 */
public class HBaseTaskReader extends TaskReader<HBaseReaderParameter, HRecord> {

    private static final Logger logger = LoggerFactory.getLogger(HBaseTaskReader.class);

    private ArrayBlockingQueue<HRecordChunk> queue;
    private HRecordChunk hRecordChunk;
    private ReplicateHRegionServer replicateHRegionServer;

    @Override
    public void start() {
        super.start();
        try {
            this.queue = new ArrayBlockingQueue<>(1);
            this.replicateHRegionServer = new ReplicateHRegionServer(buidReplicationConfig(this.parameter), queue);
            this.replicateHRegionServer.start();
        } catch (Exception e) {
            throw new DatalinkException("something goes wrong when start HBaseTaskReader", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void close() {
        this.replicateHRegionServer.stop(null);
    }

    @Override
    protected RecordChunk<HRecord> fetch() throws InterruptedException {
        hRecordChunk = queue.take();
        long firstEntryTime = hRecordChunk.getRecords().get(0).getColumns().get(0).getTimestamp();
        return new RecordChunk<>(hRecordChunk.getRecords(), firstEntryTime, hRecordChunk.getPayloadSize());
    }

    @Override
    protected void dump(RecordChunk<HRecord> recordChunk) {
        for (HRecord record : recordChunk.getRecords()) {
            logger.info("HRecord Info is :" + record);
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void commit(RecordChunk<HRecord> recordChunk) throws InterruptedException {
        hRecordChunk.getCallback().onCompletion(null, null);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void rollback(RecordChunk<HRecord> recordChunk, Throwable t) {
        hRecordChunk.getCallback().onCompletion(t, null);
    }

    private ReplicationConfig buidReplicationConfig(HBaseReaderParameter readerParameter) {
        ReplicationConfig config = new ReplicationConfig();
        config.setHbaseName("hbase-4-datalink");
        config.setZnodeParent(readerParameter.getReplZnodeParent());
        ZkMediaSrcParameter zkMediaSrcParameter = DataLinkFactory.getObject(MediaSourceService.class).
                getById(readerParameter.getReplZkMediaSourceId()).getParameterObj();
        config.setZkConfig(new ZkConfig(zkMediaSrcParameter.getServers(), 6000, 60000));
        return config;
    }
}
