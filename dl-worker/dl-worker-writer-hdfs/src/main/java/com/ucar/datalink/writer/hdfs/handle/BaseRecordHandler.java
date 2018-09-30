package com.ucar.datalink.writer.hdfs.handle;

import com.ucar.datalink.common.errors.DataLoadException;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.plugin.writer.hdfs.CommitMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.BatchSplitter;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import com.ucar.datalink.writer.hdfs.HdfsTaskWriter;
import com.ucar.datalink.writer.hdfs.handle.group.RecordGroup;
import com.ucar.datalink.writer.hdfs.handle.util.HdfsFilePathGenerator;
import com.ucar.datalink.writer.hdfs.handle.stream.FileStreamHolder;
import com.ucar.datalink.writer.hdfs.handle.stream.FileStreamToken;
import com.ucar.datalink.writer.hdfs.handle.stream.RemoteUtil;
import com.ucar.datalink.writer.hdfs.handle.util.*;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.hdfs.DFSOutputStream;
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


/**
 * Created by lubiao on 2017/12/15.
 */
public abstract class BaseRecordHandler<T extends Record> extends AbstractHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(RdbEventRecordHandler.class);

    protected final HdfsTaskWriter hdfsTaskWriter;
    protected final HdfsWriterParameter hdfsWriterParameter;
    protected final HdfsFilePathGenerator hdfsFilePathGenerator;
    protected final FileStreamHolder fileStreamHolder;

    public BaseRecordHandler(HdfsTaskWriter hdfsTaskWriter, TaskWriterContext taskWriterContext, HdfsWriterParameter hdfsWriterParameter) {
        this.hdfsTaskWriter = hdfsTaskWriter;
        this.hdfsWriterParameter = hdfsWriterParameter;
        this.hdfsFilePathGenerator = new HdfsFilePathGenerator(taskWriterContext, hdfsWriterParameter);
        this.fileStreamHolder = new FileStreamHolder(taskWriterContext.taskId(), hdfsWriterParameter);
    }

    @Override
    protected void doWrite(List<T> records, TaskWriterContext context) {
        if (records.size() > 0) {
            //statistic before
            WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
            long key = RecordMeta.mediaMapping(records.get(0)).getTargetMediaSource().getId();
            long tableGroupStartTime = System.currentTimeMillis();

            RecordGroup<T> recordGroup = new RecordGroup<>(records, context);//先按库名、表名进行分组，不同分组并发写入

            //statistic after
            long tableGroupTimeThrough = System.currentTimeMillis() - tableGroupStartTime;
            writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.TABLE_GROUP_TIME_THROUGH, tableGroupTimeThrough);
            writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.TABLE_GROUP_TABLE_COUNT, recordGroup.getGroupDatas().size());
            writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.TABLE_GROUP_TIME_PER_RECORD,
                    new BigDecimal(((double) tableGroupTimeThrough) / records.size()).setScale(2, RoundingMode.UP).doubleValue());

            long startTime = System.currentTimeMillis();
            List<Future<Long>> results = new ArrayList<>();
            recordGroup.getGroupDatas().stream().forEach(g ->
                    results.add(executorService.submit(
                            () -> {
                                List<T> tableRecords = g.getRecords();

                                Long tableWriteTime = 0L;
                                List<Long> writeTimeList = new ArrayList<>();
                                //根据mapping-id再次进行聚合，多表合一的场景下，会存在同一个TableGroup下mappingId不相同的情况
                                tableRecords
                                        .stream()
                                        .collect(Collectors.groupingBy(i -> RecordMeta.mediaMapping(i).getId()))
                                        .entrySet()
                                        .stream()
                                        .forEach(mr -> {
                                            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(mr.getValue().get(0));
                                            List<List<T>> list = BatchSplitter.splitForBatch(mr.getValue(),
                                                    context.getWriterParameter().getBatchSize());
                                            for (List<T> item : list) {
                                                Map<String, List<String>> datas = buildData(item, context);
                                                datas.entrySet().stream().forEach(
                                                        d -> {
                                                            LoadResult result = writeToHdfs(d.getKey(), d.getValue(), mappingInfo);
                                                            writeTimeList.add(result.getTotalWriteTime());
                                                        }
                                                );
                                            }
                                        });
                                for (Long time : writeTimeList) {
                                    tableWriteTime += time;
                                }
                                return tableWriteTime;
                            }
                    )));

            Throwable ex = null;
            for (int i = 0; i < results.size(); i++) {
                Future result = results.get(i);
                try {
                    Object obj = result.get();
                    if (obj instanceof Throwable) {
                        ex = (Throwable) obj;
                    }
                } catch (Throwable e) {
                    ex = e;
                }
            }
            if (ex != null) {
                throw new DatalinkException("something goes wrong when do hdfs write.", ex);
            } else {
                long timeThrough = System.currentTimeMillis() - startTime;
                int totalRecords = records.size();
                long totalWriteTime = 0L;
                for (Future<Long> result : results) {
                    try {
                        long tableWriteTime = result.get();
                        totalWriteTime += tableWriteTime;
                    } catch (Exception e) {
                        // do nothing
                    }
                }
                if (totalRecords != 0) {
                    writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.WRITE_RECORDS_COUNT, totalRecords);
                    writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.WRITE_TIME_THROUGH, timeThrough);
                    writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.WRITE_TIME_TOTAL, totalWriteTime);
                    writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.WRITE_TIME_PER_RECORD,
                            new BigDecimal(((double) totalWriteTime) / totalRecords).setScale(2, RoundingMode.UP).doubleValue());
                    if (timeThrough == 0) {
                        writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.WRITE_TPS, 0);
                    } else {
                        writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.WRITE_TPS,
                                new BigDecimal(totalRecords / (((double) timeThrough) / 1000)).longValue());
                    }
                }
            }
        }
    }

    @Override
    public void initialize(TaskWriterContext context) {
        super.initialize(context);
        fileStreamHolder.start();
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.info("prepare to close all output streams.");
        fileStreamHolder.close();
        logger.info("all output streams have been closed.");
    }

    //records中所有Record的namespace和name都相同
    //records中所有Record对应的MediaMappingInfo对象都相同
    //返回的Map对象，key为Hdfs文件路径，value为构造好的待写入数据
    protected abstract Map<String, List<String>> buildData(List<T> records, TaskWriterContext context);

    private LoadResult writeToHdfs(String hdfsFilePath, List<String> hdfsTransferData, MediaMappingInfo mappingInfo) {
        ReentrantLock lock = FileLockUtils.getLock(hdfsFilePath);
        FileStreamToken fileStreamToken = null;
        try {
            lock.lock();
            fileStreamToken = fileStreamHolder.getStreamToken(hdfsFilePath, mappingInfo.getTargetMediaSource());
            return writeData(fileStreamToken, hdfsTransferData);
        } catch (Exception e) {
            if (fileStreamToken != null) {
                fileStreamHolder.closeStreamToken(hdfsFilePath);
            } else {
                RemoteUtil.tryRemoteClose(hdfsFilePath, e);
            }
            throw new DataLoadException(MessageFormat.format("Data Append failed for file - {0}.", hdfsFilePath), e);
        } finally {
            if (fileStreamToken != null) {
                fileStreamToken.setLastUpdateTime(System.currentTimeMillis());
            }
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    private LoadResult writeData(FileStreamToken fileStreamToken, List<String> transferData) throws Exception {
        //statistic before
        LoadResult loadResult = new LoadResult();
        long startTime = System.currentTimeMillis();

        StringBuffer sb = new StringBuffer();
        for (String row : transferData) {
            if (row == null || "null".equalsIgnoreCase(row)) {
                logger.warn("find null row data");
            }
            sb.append(row);
            sb.append("\n");
        }

        // 写入数据的大小不能超过packetSize的大小，否则极端情况下，会存在这样一种情况：
        // 数据被分拆成多个packet，部分packet已经发送到了datanode，后续数据未来得及flush之前，程序异常退出了，那么会有数据被截断的风险
        byte[] bytes = sb.toString().getBytes("UTF-8");
        if (bytes.length > hdfsWriterParameter.getHdfsPacketSize()) {
            throw new RuntimeException("the size of transfered data can not be greater than " + hdfsWriterParameter.getHdfsPacketSize() + "(bytes)");
        }

        FSDataOutputStream fsOut = fileStreamToken.getFileStream();
        fsOut.write(bytes);

        CommitMode mode = hdfsWriterParameter.getCommitMode();
        logger.debug("CommitMode is " + mode);

        if (mode.equals(CommitMode.Hflush)) {
            hflush(fsOut);
            if (System.currentTimeMillis() - fileStreamToken.getLastHSyncTime() > hdfsWriterParameter.getHsyncInterval()) {
                //我们必须定时进行hsync操作，否则在文件流未关闭的情况下，RDD或者MR任务读不到写入的数据
                hsync(fsOut);
                fileStreamToken.setLastHSyncTime(System.currentTimeMillis());
                logger.info("A Interval HSync Triggered for file : " + fileStreamToken.getPathString());
            }
        } else if (mode.equals(CommitMode.Hsync)) {
            hsync(fsOut);
        } else {
            throw new RuntimeException("invalid commit mode config");
        }

        //statistic after
        loadResult.setTotalWriteTime(System.currentTimeMillis() - startTime);
        return loadResult;
    }

    private void hflush(FSDataOutputStream fsOut) throws Exception {
        fsOut.hflush();
    }

    private void hsync(FSDataOutputStream fsOut) throws Exception {
        // 调用hsync时，必须设置SyncFlag.UPDATE_LENGTH，否则RDD或者MR任务读取不到写入的数据
        // 参见：
        // https://issues.cloudera.org/browse/DISTRO-696;
        // http://www.hypertable.com/documentation/administrator_guide/hdfs_and_durability
        // https://blog.csdn.net/leen0304/article/details/77854052?locationNum=10&fps=1
        // https://issues.apache.org/jira/browse/HDFS-11915
        if (fsOut instanceof HdfsDataOutputStream) {
            ((HdfsDataOutputStream) fsOut).hsync(EnumSet.of(HdfsDataOutputStream.SyncFlag.UPDATE_LENGTH));
        } else if (fsOut.getWrappedStream() instanceof DFSOutputStream) {
            ((DFSOutputStream) fsOut.getWrappedStream()).hsync(EnumSet.of(HdfsDataOutputStream.SyncFlag.UPDATE_LENGTH));
        } else {
            fsOut.hsync();
        }
    }
}
