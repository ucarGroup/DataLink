package com.ucar.datalink.writer.hbase.handle.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;

/**
 * Created by sqq on 2017/11/29.
 */
public class HTableFactory {

    private static final LoadingCache<LoadingKey, HTable> hTable;

    static {
        hTable = CacheBuilder.newBuilder().build(new CacheLoader<LoadingKey, HTable>() {
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
