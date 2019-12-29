package com.ucar.datalink.writer.kafka.handle;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.contract.log.hbase.HUtil;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.kafka.KafkaMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.kafka.KafkaWriterParameter;
import com.ucar.datalink.domain.plugin.writer.kafka.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.kafka.SerializeMode;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.kafka.handle.util.HessianUtil;
import com.ucar.datalink.writer.kafka.handle.util.KafkaFactory;
import com.ucar.datalink.writer.kafka.handle.util.KafkaUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Future;

public class HRecordHandler extends AbstractHandler<HRecord> {

    private static final Logger logger = LoggerFactory.getLogger(HRecordHandler.class);

    @Override
    public void initialize(TaskWriterContext context) {
        super.initialize(context);
    }

    @Override
    protected void doWrite(List<HRecord> records, TaskWriterContext context) {
        if (records.size() > 0) {
            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(records.get(0));
            MediaSourceInfo sourceInfo = mappingInfo.getTargetMediaSource();
            KafkaWriterParameter kafkaWriterParameter = (KafkaWriterParameter) context.getWriterParameter();
            KafkaFactory.KafkaClientModel kafkaClientModel;
            try {
                kafkaClientModel = KafkaFactory.getKafkaProducer(sourceInfo, kafkaWriterParameter);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            if (kafkaClientModel == null) {
                throw new RuntimeException("创建KafkaProducer失败,具体原因请查看worker执行日志!");
            }

            Map<String, List<HRecord>> hrecordByTables = new HashMap<>();
            for (HRecord record : records) {
                String key = KafkaUtils.getDBTable(record.getNamespace(), record.getTableName());
                if (!hrecordByTables.containsKey(key)) {
                    hrecordByTables.put(key, new ArrayList<>());
                }
                hrecordByTables.get(key).add(record);
            }

            KafkaMediaSrcParameter kafkaMediaSrcParameter = sourceInfo.getParameterObj();
            String expressionTopic = kafkaMediaSrcParameter.getTopic();
            if (!KafkaUtils.hasExpression(expressionTopic)) {
                KafkaUtils.verifyTopicName(expressionTopic, kafkaClientModel);
            } else if (KafkaUtils.hasExpression(expressionTopic)) {
                Set<String> dbTables = hrecordByTables.keySet();
                Set<String> topics = KafkaUtils.getTopics(expressionTopic, dbTables);
                KafkaUtils.verifyTopicName(topics, kafkaClientModel);
            }

            List<Future> results = new ArrayList<>();
            for (Map.Entry<String, List<HRecord>> hrecordByTable : hrecordByTables.entrySet()) {
                List<HRecord> hRecords = hrecordByTable.getValue();
                results.add(this.executorService.submit(() -> {
                    hRecords.stream().forEach(r -> {
                        Map<String, Object> data = Maps.newHashMap();
                        data.put("rowkey", HUtil.toString(r.getRowKey()));
                        data.put("namespace", r.getNamespace());
                        data.put("tableName", r.getTableName());
                        List<Map<String, String>> columns = Lists.newArrayList();
                        r.getColumns().stream().forEach(c -> {
                            Map<String, String> nameValue = Maps.newHashMap();
                            String columnName = HUtil.toString(c.getFamily()) + "_" + HUtil.toString(c.getQualifier());
                            String columnValue = HUtil.toString(c.getValue());
                            nameValue.put(columnName, columnValue);
                            columns.add(nameValue);
                        });
                        data.put("columns", columns);
                        submitToKafka(data, expressionTopic, kafkaWriterParameter, kafkaClientModel);
                    });
                }));
            }
            this.checkFutures(results, "something goes wrong when do writing to kafka.");
        }
    }


    void submitToKafka(Map<String, Object> map, String topic, KafkaWriterParameter kafkaWriterParameter, KafkaFactory.KafkaClientModel kafkaClientModel) {
        String namespace = map.get("namespace").toString();
        String tableName = map.get("tableName").toString();

        topic = KafkaUtils.getTopic(namespace, tableName, topic);

        //兼容老数据，为空时为id方式
        String haseKey = namespace + tableName + map.get("rowkey");
        if (kafkaWriterParameter.getPartitionMode() == PartitionMode.TABLE) {
            haseKey = namespace + tableName;
        }

        byte[] data;
        if (kafkaWriterParameter.getSerializeMode() == SerializeMode.Hessian) {
            data = HessianUtil.serialize(map);
        } else if (kafkaWriterParameter.getSerializeMode() == SerializeMode.Json) {
            data = JSON.toJSONBytes(map);
        } else {
            throw new UnsupportedOperationException("Invalid SerializeMode " + kafkaWriterParameter.getSerializeMode());
        }

        try {
            kafkaClientModel.getKafkaProducer().send(new ProducerRecord(topic, haseKey, data));
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
