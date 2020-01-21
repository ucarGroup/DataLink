package com.ucar.datalink.writer.kudu.handle;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.kudu.util.*;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.OperationResponse;
import org.apache.kudu.client.PartialRow;
import org.apache.kudu.client.Upsert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


public class RdbEventRecordHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(RdbEventRecordHandler.class);

    private RdbEventRecordTransformer transformer = new RdbEventRecordTransformer();

    private static final String NOT_APPEND_PERFIX_LAG = "##";
    @Override
    protected void doWrite(List records, TaskWriterContext context) {
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
                                            MediaSourceInfo targetMediaSourceInfo = mappingInfo.getTargetMediaSource();
                                            KuduTableClient kuduTableClient = KuduTableFactory.getKuduTableClient(tableName, targetMediaSourceInfo, context.getWriterParameter());
                                            List<RdbEventRecord> rdbEventRecords = mr.getValue();
                                            writeToKudu(rdbEventRecords, kuduTableClient);
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
                throw new DatalinkException("something goes wrong when do kudu write.", ex);
            }
        }
    }


    @Override
    protected RecordChunk transform(RecordChunk recordChunk, TaskWriterContext context) {
        return this.transformer.transform(recordChunk, context);
    }

    void writeToKudu(List<RdbEventRecord> recordList, KuduTableClient kuduTableClient) {
        Upsert upsert;
        PartialRow row = null;
        try {
            MetaTable metaTable = kuduTableClient.getMetaTable();

            int halfBatchSize = kuduTableClient.getHalfBatchSize();
            int applyCount = 0;
            for (RdbEventRecord record : recordList) {
                upsert = kuduTableClient.getKuduTable().newUpsert();
                if (record.getEventType() == EventType.INSERT || record.getEventType() == EventType.UPDATE) {
                    row = upsert.getRow();
                    List<EventColumn> kuduEventColumns = getKuduEventColumns(record);

                    for (EventColumn column : kuduEventColumns) {
                        String columnName = column.getColumnName().toLowerCase();
                        String columnValue = column.getColumnValue();
                        ColumnSchema columnSchema = metaTable.getColumnSchema(columnName);
                        Assert.notNull(columnSchema, String.format("column[%s] in tables[%s] do not exist!"
                                , columnName, kuduTableClient.getKuduTable().getName()));
                        KuduRowUtils.setValue(row, columnValue, columnSchema);
                    }
                    kuduTableClient.getKuduSession().apply(upsert);
                    applyCount++;
                    if(applyCount < halfBatchSize){
                        continue;
                    }
                    List<OperationResponse> responses = kuduTableClient.getKuduSession().flush();
                    checkError(responses);
                    applyCount = 0;
                }
            }
            List<OperationResponse> responses = kuduTableClient.getKuduSession().flush();
            checkError(responses);
        } catch (Exception e) {
            if(row != null){
                logger.error("upsert data:" + row.toString());
            }
            logger.error("write to Kudu failed.", e);
            throw new DatalinkException("write to Kudu failed.", e);
        }
    }

    private List<EventColumn> getKuduEventColumns(RdbEventRecord record){
        ArrayList<EventColumn> resultEventColumns = new ArrayList<>();
        resultEventColumns.addAll(record.getKeys());
        resultEventColumns.addAll(record.getColumns());
        return resultEventColumns;
    }


    private String getKuduFiedldPrefix(String fieldNamePrefix){
        if(fieldNamePrefix == null || "".equals(fieldNamePrefix.trim())){
            return "";
        }
        return fieldNamePrefix;
    }

    private void checkError(List<OperationResponse> responses) {
        for (OperationResponse response : responses) {
            boolean error = response.hasRowError();
            if (error) {
                throw new DatalinkException(response.getRowError().toString());
            }
        }
    }




}
