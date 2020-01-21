package com.ucar.datalink.writer.kafka.handle.util;

import com.ucar.datalink.writer.kafka.KafkaTaskWriter;
import com.ucar.datalink.writer.kafka.handle.RdbEventRecordHandler;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author xy.li
 */
public class KafkaUtils {

    private static final Logger logger = LoggerFactory.getLogger(KafkaUtils.class);

    public static void verifyTopicName(Set<String> topics, KafkaFactory.KafkaClientModel kafkaClientModel){
        try {
            Set<String> realTopics = kafkaClientModel.getAdminClient().listTopics().names().get();
            if(!realTopics.containsAll(topics)){
                topics.removeAll(realTopics);
                logger.error(String.format("topics[%s] does not exist!", ArrayUtils.toString(topics)));
                throw new RuntimeException(String.format("topics[%s] does not exist!", ArrayUtils.toString(topics)));
            }
        } catch (Exception e) {
            logger.error(String.format("topics[%s] does not exist!", e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    public static void verifyTopicName(String topic, KafkaFactory.KafkaClientModel kafkaClientModel){
        Set<String> topics = new HashSet();
        topics.add(topic);
        verifyTopicName(topics,kafkaClientModel);
    }

    public static String getTopic(String expressionTopic, String database, String table) {
        String newTopic = expressionTopic;
        if (hasExpression(expressionTopic)) {
            ////如果kafka的topic像kafka_meta_topic_${dbTable}这样的模式，则替换为kafka_meta_topic_databaseName_tableName
            newTopic = expressionTopic.substring(0, expressionTopic.indexOf(KafkaTaskWriter.TOPIC_EXPERSSION_FLAG)) + getDBTable(database,table);
        }
        return newTopic;
    }

    public static String getTopic(String expressionTopic, String dbTable) {
        String newTopic = expressionTopic;
        if (hasExpression(expressionTopic)) {
            ////如果kafka的topic像kafka_meta_topic_${dbTable}这样的模式，则替换为kafka_meta_topic_databaseName_tableName
            newTopic = expressionTopic.substring(0, expressionTopic.indexOf(KafkaTaskWriter.TOPIC_EXPERSSION_FLAG)) + dbTable;
        }
        return newTopic;
    }

    public static String getDBTable(String database, String table) {
       return database + "_" + table;
    }


    public static Set<String> getTopics(String expressionTopic, Set<String> dbTables){
        HashSet<String> newTopics = new HashSet<>();
        if(!hasExpression(expressionTopic)){
            newTopics.add(expressionTopic);
            return newTopics;
        }

        for(String dbTable : dbTables){
            newTopics.add(getTopic(expressionTopic,dbTable));
        }
        return newTopics;
    }


    public static boolean hasExpression(String expressionTopic){
        if(expressionTopic.indexOf(KafkaTaskWriter.TOPIC_EXPERSSION_FLAG) > -1){
            return true;
        }
        return false;
    }

}
