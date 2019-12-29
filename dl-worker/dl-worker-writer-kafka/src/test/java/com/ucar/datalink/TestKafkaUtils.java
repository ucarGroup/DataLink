package com.ucar.datalink;

import com.ucar.datalink.writer.kafka.handle.util.KafkaFactory;
import com.ucar.datalink.writer.kafka.handle.util.KafkaUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.junit.Test;

import java.util.Properties;

/**
 * @author xy.li
 */
public class TestKafkaUtils {

    @Test
   public void test(){
        KafkaFactory.KafkaClientModel kafkaClientModel = get();
        KafkaUtils.verifyTopicName("kafka_lucky_iot_iot_equipment_type",kafkaClientModel);
    }



   private KafkaFactory.KafkaClientModel get(){
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
       return kafkaClientModel;
   }

}
