package com.ucar.datalink.writer.hbase.handle.util;

import com.google.common.cache.*;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by sqq on 2017/11/29.
 */
public class HTableFactory {
    private static final Logger logger = LoggerFactory.getLogger(HTableFactory.class);
    private static final LoadingCache<LoadingKey, HTable> hTable;

    static {
        hTable = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).removalListener(new RemovalListener<LoadingKey, HTable>() {
            @Override
            public void onRemoval(RemovalNotification<LoadingKey, HTable> notification) {
                HTable table = notification.getValue();
                if (table != null) {
                    try {
                        table.close();
                        logger.info("RemovalListener close HTable succeeded.");
                    } catch (IOException e) {
                        logger.error("RemovalListener close HTable failed, HTable is " + table, e);
                    }
                }
            }
        }).build(new CacheLoader<LoadingKey, HTable>() {
            @Override
            public HTable load(LoadingKey key) throws Exception {
                Configuration configuration = HBaseConfigurationFactory.getConfiguration(key.mediaSourceInfo);
                HTable hTable = new HTable(configuration, key.tableName);
                hTable.setAutoFlushTo(false);
                return hTable;
            }
        });
    }

    public static HTable getHTable(String tableName, MediaSourceInfo mediaSourceInfo) {
        return hTable.getUnchecked(new LoadingKey(tableName, mediaSourceInfo));
    }

    public static void invalidate() {
        hTable.invalidateAll();
    }

    private static class LoadingKey {
        private String tableName;
        private MediaSourceInfo mediaSourceInfo;

        public LoadingKey(String tableName, MediaSourceInfo mediaSourceInfo) {
            this.tableName = tableName;
            this.mediaSourceInfo = mediaSourceInfo;
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
