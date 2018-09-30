package com.ucar.datalink.writer.hdfs.handle;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.hdfs.HdfsTaskWriter;
import com.ucar.datalink.writer.hdfs.handle.config.HdfsConfig;
import com.ucar.datalink.writer.hdfs.handle.config.HdfsConfigManager;
import com.ucar.datalink.writer.hdfs.handle.util.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sqq on 2017/7/12.
 */
public class RdbEventRecordHandler extends BaseRecordHandler<RdbEventRecord> {

    private AtomicLong seq = new AtomicLong(0);

    public RdbEventRecordHandler(HdfsTaskWriter hdfsTaskWriter, TaskWriterContext taskWriterContext, HdfsWriterParameter hdfsWriterParameter) {
        super(hdfsTaskWriter, taskWriterContext, hdfsWriterParameter);
    }

    @Override
    protected Map<String, List<String>> buildData(List<RdbEventRecord> records, TaskWriterContext context) {
        Map<String, List<String>> result = new HashMap<>();

        RdbEventRecord firstRecord = records.get(0);
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(firstRecord);
        String schemaName = firstRecord.getSchemaName();
        String tableName = firstRecord.getTableName();
        HdfsConfig hdfsConfig = HdfsConfigManager.getHdfsConfig(mappingInfo.getTargetMediaSource(), this.hdfsWriterParameter);

        SendData sendData = buildSendData(records);
        String hdfsDbType = Dict.HDFS_DB_TYPE_BINLOG;
        if (!sendData.normalData.isEmpty()) {
            String hdfsTransferDataType = Dict.HDFS_TRANSFER_DATA_TYPE_NORMAL;
            String hdfsFilePath = hdfsConfig.getHdfsAddress() + hdfsFilePathGenerator.getHdfsFilePath(schemaName, tableName, mappingInfo, hdfsDbType, hdfsTransferDataType);
            result.put(hdfsFilePath, sendData.normalData);
        }
        if (!sendData.deletedData.isEmpty()) {
            String hdfsTransferDataType = Dict.HDFS_TRANSFER_DATA_TYPE_DELETE;
            String hdfsFilePath = hdfsConfig.getHdfsAddress() + hdfsFilePathGenerator.getHdfsFilePath(schemaName, tableName, mappingInfo, hdfsDbType, hdfsTransferDataType);
            result.put(hdfsFilePath, sendData.deletedData);
        }

        return result;
    }


    private SendData buildSendData(List<RdbEventRecord> recordList) {
        List<String> normalData = new ArrayList<>();
        List<String> deletedData = new ArrayList<>();

        for (RdbEventRecord record : recordList) {
            EventType eventType = record.getEventType();
            if (eventType == EventType.DELETE) {
                deletedData.add(parseToJsonStr(record, seq.incrementAndGet()));
            } else {
                normalData.add(parseToJsonStr(record, seq.incrementAndGet()));
            }
        }
        return new SendData(normalData, deletedData);
    }

    private String parseToJsonStr(RdbEventRecord record, long seq) {
        HashMap<String, String> map = new HashMap<>();
        List<EventColumn> columnList = record.getColumns();
        List<EventColumn> keyList = record.getKeys();
        List<EventColumn> allColumns = new ArrayList<>();
        allColumns.addAll(columnList);
        allColumns.addAll(keyList);
        for (EventColumn column : allColumns) {
            String columnName = column.getColumnName();
            String columnValue = column.getColumnValue();
            if (column.isNull()) {
                columnValue = "null";
            }
            map.put(columnName, columnValue);
        }

        // Mysql数据库binlog的精度是毫秒
        // 需要binlog_ts和binlog_seq组合，标识先后顺序
        map.put("binlog_ts", String.valueOf(System.currentTimeMillis()));
        map.put("binlog_seq", String.valueOf(seq));
        map.put("binlog_eventtime",String.valueOf(record.getExecuteTime()));
        return JSONObject.toJSONString(map);
    }

    static class SendData {
        List<String> normalData;
        List<String> deletedData;

        public SendData(List<String> normalData, List<String> deletedData) {
            super();
            this.normalData = normalData;
            this.deletedData = deletedData;
        }
    }
}
