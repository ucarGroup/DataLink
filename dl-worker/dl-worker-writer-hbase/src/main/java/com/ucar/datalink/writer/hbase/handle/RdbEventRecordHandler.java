package com.ucar.datalink.writer.hbase.handle;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.writer.hbase.HBaseSyncParameter;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.BatchSplitter;
import com.ucar.datalink.writer.hbase.handle.util.HTableFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by sqq on 2017/11/29.
 */
public class RdbEventRecordHandler extends AbstractHandler<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(RdbEventRecordHandler.class);

    @Override
    protected void doWrite(List<RdbEventRecord> records, TaskWriterContext context) {
        if (records.size() > 0) {
            RecordGroup recordGroup = new RecordGroup(records, context);
            List<Future> results = new ArrayList<>();
            recordGroup.getTables().stream().forEach(t ->
                    results.add(executorService.submit(
                            () -> {
                                List<RdbEventRecord> tableRecords = t.getTableDatas();

                                tableRecords.stream()
                                        .collect(Collectors.groupingBy(i -> RecordMeta.mediaMapping(i).getId()))
                                        .entrySet()
                                        .stream()
                                        .forEach(mr -> {
                                            RdbEventRecord firstRecord = mr.getValue().get(0);
                                            String tableName = firstRecord.getTableName();
                                            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(firstRecord);
                                            HBaseSyncParameter hBaseSyncParameter = mappingInfo.getParameterObj();
                                            MediaSourceInfo targetMediaSourceInfo = mappingInfo.getTargetMediaSource();
                                            HTable hTable = HTableFactory.getHTable(tableName, targetMediaSourceInfo);
                                            List<List<RdbEventRecord>> batchRecords = BatchSplitter.splitForBatch(mr.getValue(), context.getWriterParameter().getBatchSize());
                                            for (List<RdbEventRecord> recordList : batchRecords) {
                                                writeToHBase(recordList, hTable, hBaseSyncParameter != null && hBaseSyncParameter.isSyncDelete());
                                            }
                                        });
                            }
                    )));
            Throwable ex = null;
            for (Future result : results) {
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
                throw new DatalinkException("something goes wrong when do hbase write.", ex);
            }
        }
    }

    void writeToHBase(List<RdbEventRecord> recordList, HTable hTable, boolean syncDelete) {
        try {
            List<Put> puts = new LinkedList<>();
            List<Delete> deletes = new LinkedList<>();
            for (RdbEventRecord record : recordList) {
                List<EventColumn> keys = record.getKeys();
                if (keys.size() > 1) {
                    throw new RuntimeException("不支持联合主键 : [" + record.getKeys() + "]");
                }
                String keyName = keys.get(0).getColumnName();
                String keyValue = keys.get(0).getColumnValue();
                if (record.getEventType() == EventType.INSERT || record.getEventType() == EventType.UPDATE) {
                    Put put = new Put(Bytes.toBytes(keyValue));
                    put.add(Bytes.toBytes("default"), Bytes.toBytes(keyName), Bytes.toBytes(keyValue));

                    List<EventColumn> columns = record.getColumns();
                    for (EventColumn column : columns) {
                        String columnName = column.getColumnName();
                        String columnValue = column.getColumnValue();
                        put.add(Bytes.toBytes("default"), Bytes.toBytes(columnName), columnValue == null ? null : Bytes.toBytes(columnValue));
                    }
                    puts.add(put);
                } else if (record.getEventType() == EventType.DELETE && syncDelete) {
                    Delete del = new Delete(Bytes.toBytes(keyValue));
                    deletes.add(del);
                }
            }
            if (puts.size() > 0) {
                hTable.put(puts);
                hTable.flushCommits();
            }
            if (deletes.size() > 0) {
                hTable.delete(deletes);
                hTable.flushCommits();
            }
        } catch (Exception e) {
            logger.info("write to HBase failed.", e);
            throw new DatalinkException("write to HBase failed.", e);
        }

    }
}
