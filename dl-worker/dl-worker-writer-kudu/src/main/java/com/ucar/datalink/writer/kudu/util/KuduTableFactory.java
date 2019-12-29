package com.ucar.datalink.writer.kudu.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.kudu.KuduMediaSrcParameter;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import org.apache.kudu.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class KuduTableFactory {

    private static final Logger logger = LoggerFactory.getLogger(KuduTableFactory.class);
    private static final LoadingCache<LoadingKey, KuduTableClient> kuduTableClient;

    static {
        kuduTableClient = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).removalListener(new RemovalListener<LoadingKey, KuduTableClient>() {
            @Override
            public void onRemoval(com.google.common.cache.RemovalNotification<LoadingKey, KuduTableClient> notification) {
                KuduTableClient table = notification.getValue();
                if (table != null) {
                    KuduClient client = table.getClient();
                    try {
                        client.close();
                        logger.info("RemovalListener close kudu succeeded.");
                    } catch (KuduException e) {
                        logger.error("RemovalListener close kudu failed, HTable is " + table, e);
                    }
                }
            }
        }).build(new CacheLoader<LoadingKey, KuduTableClient>() {
            @Override
            public KuduTableClient load(LoadingKey key) throws Exception {
                KuduMediaSrcParameter kuduMediaSrcParameter = key.mediaSourceInfo.getParameterObj();
                int batchSize = key.getPluginWriterParameter().getBatchSize();
                batchSize = batchSize < 1 ? 1 : batchSize;
                KuduClient client = KuduUtils.createClient(kuduMediaSrcParameter.getHost2Ports());
                String dataBaseAndTableName = getDataBaseAndTableName(key);
                KuduTable kuduTable;
                try {
                    kuduTable = client.openTable(dataBaseAndTableName);
                } catch (KuduException e) {
                    String msg = String.format("open table[%s] failed", dataBaseAndTableName);
                    logger.error(msg, e);
                    throw new Exception(msg, e);
                }
                KuduSession kuduSession = client.newSession();
                kuduSession.setFlushMode(SessionConfiguration.FlushMode.MANUAL_FLUSH);
                kuduSession.setMutationBufferSpace(batchSize);
                return new KuduTableClient(client, kuduTable, kuduSession, batchSize);
            }
        });
    }

    private static String getDataBaseAndTableName(LoadingKey key) {
        String tableName = key.getTableName();
        KuduMediaSrcParameter kuduMediaSrcParameter = key.getMediaSourceInfo().getParameterObj();
        String database = kuduMediaSrcParameter.getDatabase();
        return String.format("%s.%s", database, tableName);
    }

    public static void invalidate(MediaSourceInfo mediaSourceInfo) {
        kuduTableClient.invalidateAll();
    }

    public static KuduTableClient getKuduTableClient(String tableName, MediaSourceInfo mediaSourceInfo, PluginWriterParameter pluginWriterParameter) {
        return kuduTableClient.getUnchecked(new LoadingKey(tableName, mediaSourceInfo, pluginWriterParameter));
    }

    private static class LoadingKey {
        private String tableName;
        private MediaSourceInfo mediaSourceInfo;
        private PluginWriterParameter pluginWriterParameter;

        public LoadingKey(String tableName, MediaSourceInfo mediaSourceInfo, PluginWriterParameter pluginWriterParameter) {
            this.tableName = tableName;
            this.mediaSourceInfo = mediaSourceInfo;
            this.pluginWriterParameter = pluginWriterParameter;
        }

        public String getTableName() {
            return tableName;
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

            return mediaSourceInfo.equals(that.mediaSourceInfo) && tableName.equals(that.tableName);

        }

        @Override
        public int hashCode() {
            int result = tableName.hashCode();
            result = 31 * result + mediaSourceInfo.hashCode();
            return result;
        }
    }


}
