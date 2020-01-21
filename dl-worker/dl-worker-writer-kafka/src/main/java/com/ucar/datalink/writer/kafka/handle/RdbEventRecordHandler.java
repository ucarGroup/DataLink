package com.ucar.datalink.writer.kafka.handle;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.kafka.KafkaMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.kafka.KafkaWriterParameter;
import com.ucar.datalink.domain.plugin.writer.kafka.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.kafka.SerializeMode;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.kafka.handle.util.KafkaFactory;
import com.ucar.datalink.writer.kafka.handle.util.KafkaUtils;
import com.zuche.framework.cache.customize.DBEventType;
import com.zuche.framework.cache.customize.DBTableRowCellVO;
import com.zuche.framework.cache.customize.DBTableRowVO;
import com.zuche.framework.remote.nio.codec.HessianSerializerUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Future;

public class RdbEventRecordHandler extends AbstractHandler<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(RdbEventRecordHandler.class);

    @Override
    public void initialize(TaskWriterContext context) {
        super.initialize(context);
    }

    @Override
    protected void doWrite(List<RdbEventRecord> records, TaskWriterContext context) {

        if (records.size() > 0) {
            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(records.get(0));
            MediaSourceInfo sourceInfo = mappingInfo.getTargetMediaSource();
            KafkaWriterParameter kafkaWriterParameter = (KafkaWriterParameter) context.getWriterParameter();
            KafkaFactory.KafkaClientModel kafkaClientModel;
            try {
                kafkaClientModel = KafkaFactory.getKafkaProducer(sourceInfo,kafkaWriterParameter);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            if (kafkaClientModel == null) {
                throw new RuntimeException("创建KafkaProducer失败,具体原因请查看worker执行日志!");
            }
            Map<String, List<RdbEventRecord>> rdbEventRecordByTables = new HashMap<>();
            for (RdbEventRecord record : records) {
                String key = KafkaUtils.getDBTable(record.getSchemaName(),record.getTableName());
                if (!rdbEventRecordByTables.containsKey(key)) {
                    rdbEventRecordByTables.put(key, new ArrayList<>());
                }
                rdbEventRecordByTables.get(key).add(record);
            }

            KafkaMediaSrcParameter kafkaMediaSrcParameter = sourceInfo.getParameterObj();
            String expressionTopic = kafkaMediaSrcParameter.getTopic();
            if(!KafkaUtils.hasExpression(expressionTopic)){
                KafkaUtils.verifyTopicName(expressionTopic,kafkaClientModel);
            }else if(KafkaUtils.hasExpression(expressionTopic)){
                Set<String> dbTables = rdbEventRecordByTables.keySet();
                Set<String> topics = KafkaUtils.getTopics(expressionTopic, dbTables);
                KafkaUtils.verifyTopicName(topics,kafkaClientModel);
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
                        submitToKafka(rowVO, expressionTopic, kafkaWriterParameter, kafkaClientModel);
                    });

                }));
            }
            this.checkFutures(results, "something goes wrong when do writing to kafka.");
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


    private static void submitToKafka(DBTableRowVO rowVO, String topic, KafkaWriterParameter kafkaWriterParameter, KafkaFactory.KafkaClientModel kafkaClientModel) {

        topic = KafkaUtils.getTopic(topic, rowVO.getDatabaseName(),rowVO.getTableName());
        byte[] data;
        if (kafkaWriterParameter.getSerializeMode() == SerializeMode.Hessian) {
            data = HessianSerializerUtils.serialize(rowVO);
        } else if (kafkaWriterParameter.getSerializeMode() == SerializeMode.Json) {
            data = JSON.toJSONBytes(rowVO);
        } else {
            throw new UnsupportedOperationException("Invalid SerializeMode " + kafkaWriterParameter.getSerializeMode());
        }

        //兼容老数据，为空时为id方式
        String haseKey = rowVO.getDatabaseName() + rowVO.getTableName() + rowVO.getId();
        if (kafkaWriterParameter.getPartitionMode() == PartitionMode.TABLE) {
            haseKey = rowVO.getDatabaseName() + rowVO.getTableName();
        }

        try {
            kafkaClientModel.getKafkaProducer().send(new ProducerRecord(topic, haseKey, data));
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.104.156.83:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required username='kafka' password='kafka';");
        KafkaProducer<String, Byte[]> producer = new KafkaProducer<>(props);
        AdminClient client = AdminClient.create(props);

        KafkaFactory.KafkaClientModel kafkaClientModel = new KafkaFactory.KafkaClientModel(producer, client);

        KafkaWriterParameter kafkaWriterParameter = new KafkaWriterParameter();
        kafkaWriterParameter.setSerializeMode(SerializeMode.Json);
        DBTableRowVO dbTableRowVO = new DBTableRowVO();
        dbTableRowVO.setDatabaseName("kafka_${dbTable}");
        dbTableRowVO.setTableName("lucky_iot");

        int i = 10;
        while(i > 0){
            i--;
            dbTableRowVO.setId(System.currentTimeMillis()+"");
            submitToKafka(dbTableRowVO,"iot_equipment_type",kafkaWriterParameter,kafkaClientModel);
        }




    }

}
