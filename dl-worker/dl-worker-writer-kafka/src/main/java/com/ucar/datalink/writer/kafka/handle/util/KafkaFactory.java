package com.ucar.datalink.writer.kafka.handle.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.kafka.KafkaMediaSrcParameter;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.writer.kafka.KafkaWriterParameter;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author xy.li
 * @date 2019/08/06
 */
public class KafkaFactory {

    private static final LoadingCache<LoadingKey, KafkaClientModel> kuduTableClient;


    static {
        kuduTableClient = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).removalListener(new RemovalListener<LoadingKey, KafkaClientModel>() {
            @Override
            public void onRemoval(com.google.common.cache.RemovalNotification<LoadingKey, KafkaClientModel> notification) {
                KafkaClientModel kafkaClientModel = notification.getValue();
                if (kafkaClientModel != null) {
                    try {
                        kafkaClientModel.close();
                    } catch (IOException e) {
                    }
                }
            }
        }).build(new CacheLoader<LoadingKey, KafkaClientModel>() {
            @Override
            public KafkaClientModel load(LoadingKey key) throws Exception {
                KafkaMediaSrcParameter kafkaMediaSrcParameter = key.mediaSourceInfo.getParameterObj();
                PluginWriterParameter pluginWriterParameter = key.getPluginWriterParameter();
                Map<String, String> mapparamters = kafkaMediaSrcParameter.getMapparamters();
                String bootstrapServers = kafkaMediaSrcParameter.getBootstrapServers();

                Map<String, Object> conf = new HashMap<>(8);
                conf.put("bootstrap.servers", bootstrapServers);
                conf.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                conf.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
                conf.put("acks", "all");
                conf.put("batch.size", pluginWriterParameter.getBatchSize());
                conf.putAll(mapparamters);
                KafkaProducer producer = new KafkaProducer<>(conf);
                AdminClient client = AdminClient.create(conf);
                return new KafkaFactory.KafkaClientModel(producer, client);
            }
        });
    }

    public static KafkaFactory.KafkaClientModel getKafkaProducer(MediaSourceInfo mediaSourceInfo, KafkaWriterParameter kafkaWriterParameter) throws ExecutionException {
        return kuduTableClient.get(new LoadingKey(mediaSourceInfo, kafkaWriterParameter));
    }


    private static class LoadingKey {

        private MediaSourceInfo mediaSourceInfo;
        private PluginWriterParameter pluginWriterParameter;

        public LoadingKey(MediaSourceInfo mediaSourceInfo, PluginWriterParameter pluginWriterParameter) {
            this.mediaSourceInfo = mediaSourceInfo;
            this.pluginWriterParameter = pluginWriterParameter;
        }

        public MediaSourceInfo getMediaSourceInfo() {
            return mediaSourceInfo;
        }

        public PluginWriterParameter getPluginWriterParameter() {
            return pluginWriterParameter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LoadingKey)) return false;

            LoadingKey that = (LoadingKey) o;

            return mediaSourceInfo.getId().equals(that.mediaSourceInfo.getId());

        }

        @Override
        public int hashCode() {
            int result = mediaSourceInfo.getId().hashCode();
            result = 31 * result + mediaSourceInfo.getId().hashCode();
            return result;
        }
    }

    public static class KafkaClientModel implements Closeable {
        private KafkaProducer kafkaProducer;
        private AdminClient adminClient;

        public KafkaClientModel(KafkaProducer kafkaProducer, AdminClient adminClient) {
            this.kafkaProducer = kafkaProducer;
            this.adminClient = adminClient;
        }

        public KafkaProducer getKafkaProducer() {
            return kafkaProducer;
        }

        public void setKafkaProducer(KafkaProducer kafkaProducer) {
            this.kafkaProducer = kafkaProducer;
        }

        public AdminClient getAdminClient() {
            return adminClient;
        }

        public void setAdminClient(AdminClient adminClient) {
            this.adminClient = adminClient;
        }


        @Override
        public void close() throws IOException {
            if (kafkaProducer != null) {
                try {
                    kafkaProducer.close();
                } catch (Exception e) {
                }
            }
            if (adminClient != null) {
                try {
                    adminClient.close();
                } catch (Exception e) {
                }
            }

        }
    }

}


