package com.ucar.datalink.writer.fq.handle;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.fq.FqMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.fq.FqWriterParameter;
import com.ucar.datalink.domain.plugin.writer.fq.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.fq.SerializeMode;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.zuche.confcenter.client.manager.DefaultConfigCenterManager;
import com.zuche.framework.cache.customize.DBEventType;
import com.zuche.framework.cache.customize.DBTableRowCellVO;
import com.zuche.framework.cache.customize.DBTableRowVO;
import com.zuche.framework.metaq.manager.ConfigCenterManager;
import com.zuche.framework.metaq.send.CommonMessageSend;
import com.zuche.framework.metaq.send.MqSendParVo;
import com.zuche.framework.remote.nio.codec.HessianSerializerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by sqq on 2017/5/11.
 */
public class RdbEventRecordHandler extends AbstractHandler<RdbEventRecord> {

    static {
        System.setProperty("sz.framework.projectName", "datalink");
    }

    @Override
    public void initialize(TaskWriterContext context) {

        //初始化gaea,fq不关心业务线
        DefaultConfigCenterManager.getInstance();

        //fq要求必须要初始化，且调用多次也没有关系，只生效第一次,参数值也没有关系
        ConfigCenterManager.initDefaultPrefixOnce("ucar");

        super.initialize(context);
    }

    @Override
    protected void doWrite(List<RdbEventRecord> records, TaskWriterContext context) {
        if (records.size() > 0) {
            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(records.get(0));
            MediaSourceInfo sourceInfo = mappingInfo.getTargetMediaSource();
            FqMediaSrcParameter fq = sourceInfo.getParameterObj();
            MediaSourceInfo zkSourceInfo = DataLinkFactory.getObject(MediaSourceService.class).getById(fq.getZkMediaSourceId());
            ZkMediaSrcParameter zk = zkSourceInfo.getParameterObj();
            FqWriterParameter fqWriterParameter = (FqWriterParameter) context.getWriterParameter();



            Map<String,List<RdbEventRecord>> rdbEventRecordByTables = new HashMap<>();
            for(RdbEventRecord record  : records){
                String key = record.getSchemaName() + record.getTableName();
                if(!rdbEventRecordByTables.containsKey(key)){
                    rdbEventRecordByTables.put(key,new ArrayList<>());
                }
                rdbEventRecordByTables.get(key).add(record);
            }

            List<Future> results = new ArrayList<>();
            for(Map.Entry<String, List<RdbEventRecord>> rdbEventRecordByTable :  rdbEventRecordByTables.entrySet()){
                List<RdbEventRecord> rdbEventRecords = rdbEventRecordByTable.getValue();
                results.add(this.executorService.submit(() ->{
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
                        submitToMQ(rowVO, fq, zk, fqWriterParameter);
                    });

                }));
            }

            this.checkFutures(results,"something goes wrong when do writing to elasticsearch.");
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

    void submitToMQ(DBTableRowVO rowVO, FqMediaSrcParameter fq, ZkMediaSrcParameter zk, FqWriterParameter fqWriterParameter) {
        MqSendParVo mqSendParVo = new MqSendParVo();
        mqSendParVo.setServers(zk.getServers());
        mqSendParVo.setPrefix(fq.getClusterPrefix());
        String topic = fq.getTopic();
        //如果fq的topic像fq_meta_topic_${dbTable}这样的模式，则替换为fq_meta_topic_databaseName_tableName
        if (topic.indexOf("${dbTable}") > -1) {
            topic = topic.substring(0, topic.indexOf("${dbTable}")) + rowVO.getDatabaseName() + "_" + rowVO.getTableName();
        }
        mqSendParVo.setTopic(topic);
        if (fqWriterParameter.getSerializeMode() == SerializeMode.Hessian) {
            mqSendParVo.setData(HessianSerializerUtils.serialize(rowVO));
        } else if (fqWriterParameter.getSerializeMode() == SerializeMode.Json) {
            mqSendParVo.setData(JSON.toJSONBytes(rowVO));
        } else {
            throw new UnsupportedOperationException("Invalid SerializeMode " + fqWriterParameter.getSerializeMode());
        }
        mqSendParVo.setS(1);
        mqSendParVo.setTimeout("30000");

        //兼容老数据，为空时为id方式
        String haseKey = rowVO.getDatabaseName() + rowVO.getTableName() + rowVO.getId();
        if (fqWriterParameter.getPartitionMode() == PartitionMode.TABLE) {
            haseKey = rowVO.getDatabaseName() + rowVO.getTableName();
        }

        CommonMessageSend.sendMessage(haseKey, mqSendParVo);
    }

}
