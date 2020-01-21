package com.ucar.datalink.writer.dove.handle;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.dove.DoveMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.dove.DoveWriterParameter;
import com.ucar.datalink.domain.plugin.writer.dove.PartitionMode;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.dove.vo.DBEventType;
import com.ucar.datalink.writer.dove.vo.DBTableRowCellVO;
import com.ucar.datalink.writer.dove.vo.DBTableRowVO;
import com.ucarinc.dove.common.message.Message;
import com.ucarinc.framework.dove.DoveConstants;
import com.ucarinc.framework.dove.producer.ProducerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by liuyifan.
 */
public class RdbEventRecordHandler extends AbstractHandler<RdbEventRecord> {

    static {
        System.setProperty("project.name", "datalink");
    }

    @Override
    public void initialize(TaskWriterContext context) {
        super.initialize(context);
    }

    @Override
    protected void doWrite(List<RdbEventRecord> records, TaskWriterContext context) {
        if (records.size() > 0) {
            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(records.get(0));
            MediaSourceInfo sourceInfo = mappingInfo.getTargetMediaSource();
            DoveMediaSrcParameter dove = sourceInfo.getParameterObj();
            MediaSourceInfo zkSourceInfo = DataLinkFactory.getObject(MediaSourceService.class).getById(dove.getZkMediaSourceId());
            ZkMediaSrcParameter zk = zkSourceInfo.getParameterObj();
            DoveWriterParameter doveWriterParameter = (DoveWriterParameter) context.getWriterParameter();

            Map<String, List<RdbEventRecord>> rdbEventRecordByTables = new HashMap<>();
            for (RdbEventRecord record : records) {
                String key = record.getSchemaName() + record.getTableName();
                if (!rdbEventRecordByTables.containsKey(key)) {
                    rdbEventRecordByTables.put(key, new ArrayList<>());
                }
                rdbEventRecordByTables.get(key).add(record);
            }

            List<Future> results = new ArrayList<>();
            for (Map.Entry<String, List<RdbEventRecord>> rdbEventRecordByTable : rdbEventRecordByTables.entrySet()) {
                List<RdbEventRecord> rdbEventRecords = rdbEventRecordByTable.getValue();
                results.add(this.executorService.submit(() -> {
                    rdbEventRecords.forEach(record -> {
                        DBTableRowVO rowVO = new DBTableRowVO();
                        rowVO.setDatabaseName(record.getSchemaName());
                        rowVO.setTableName(record.getTableName());
                        rowVO.setEventType(DBEventType.getDBEventTypeFromCode(record.getEventType().toString()));
                        if (record.getKeys().size() > 1) {
                            throw new RuntimeException("不支持联合主键 : [" + record.getKeys() + "]");
                        } else if (record.getKeys().size() == 1) {
                            rowVO.setId(record.getKeys().get(0).getColumnValue());
                        }
                        rowVO.setDbTableRowCellVOList(initSendData(record));
                        submitToMQ(rowVO, dove, zk, doveWriterParameter);
                    });

                }));
            }

            this.checkFutures(results, "something goes wrong when do writing to elasticsearch.");
        }
    }

    private List<DBTableRowCellVO> initSendData(RdbEventRecord record) {
        List<DBTableRowCellVO> list = new ArrayList<>();
        List<EventColumn> columns = record.getColumns();
        List<EventColumn> oldColumns = record.getOldColumns();
        List<EventColumn> keys = record.getKeys();
        List<EventColumn> oldKeys = record.getOldKeys();
        columns.forEach(column -> {
            DBTableRowCellVO dbTableRowCellVO = new DBTableRowCellVO();
            dbTableRowCellVO.setColumnName(column.getColumnName());
            dbTableRowCellVO.setAfterValue(column.getColumnValue());
            for (EventColumn oldColumn : oldColumns) {
                if (oldColumn.getColumnName().equals(column.getColumnName())) {
                    dbTableRowCellVO.setBeforeValue(oldColumn.getColumnValue());
                    break;
                }
            }
            list.add(dbTableRowCellVO);
        });
        keys.forEach(key -> {
            DBTableRowCellVO dbTableRowCellVO = new DBTableRowCellVO();
            dbTableRowCellVO.setColumnName(key.getColumnName());
            dbTableRowCellVO.setAfterValue(key.getColumnValue());
            for (EventColumn oldKey : oldKeys) {
                if (oldKey.getColumnName().equals(key.getColumnName())) {
                    dbTableRowCellVO.setBeforeValue(oldKey.getColumnValue());
                    break;
                }
            }
            list.add(dbTableRowCellVO);
        });
        return list;
    }

    void submitToMQ(DBTableRowVO rowVO, DoveMediaSrcParameter dove, ZkMediaSrcParameter zk, DoveWriterParameter doveWriterParameter) {
        System.setProperty(DoveConstants.SPRING_DOVE_META_SERVER, zk.getServers());
        String topic = dove.getTopic();
        //如果dove的topic像dove_meta_topic_${dbTable}这样的模式，则替换为dove_meta_topic_databaseName_tableName
        if (topic.indexOf("${dbTable}") > -1) {
            topic = topic.substring(0, topic.indexOf("${dbTable}")) + rowVO.getDatabaseName() + "_" + rowVO.getTableName();
        }
        String haseKey = rowVO.getDatabaseName() + "_" + rowVO.getTableName() + "_" + rowVO.getId();
        if (doveWriterParameter.getPartitionMode() == PartitionMode.TABLE) {
            haseKey = rowVO.getDatabaseName() + "_" + rowVO.getTableName();
        }

        try {
            ProducerManager.send(new Message<Object, Object>(topic, haseKey, JSON.toJSONString(rowVO)));
        } catch (InterruptedException e) {
            throw new DatalinkException(e);
        }

    }

}
