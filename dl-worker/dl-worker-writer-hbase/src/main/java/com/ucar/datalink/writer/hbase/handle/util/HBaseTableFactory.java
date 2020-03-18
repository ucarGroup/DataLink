package com.ucar.datalink.writer.hbase.handle.util;

import com.google.common.cache.*;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP
 *
 * @author: jhy
 * @date: 2020/3/10
 */
public class HBaseTableFactory {
    private static final Logger logger = LoggerFactory.getLogger(HBaseTableFactory.class);
    private static final LoadingCache<LoadingKey, Table> hbaseConnections;

    static {
        hbaseConnections = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).removalListener(new RemovalListener<LoadingKey, Table>() {
            @Override
            public void onRemoval(RemovalNotification<LoadingKey, Table> notification) {
                Table table = notification.getValue();
                if (table != null) {
                    try {
                        table.close();
                        logger.info("RemovalListener close HTable succeeded.");
                    } catch (IOException e) {
                        logger.error("RemovalListener close HTable failed, HTable is " + table, e);
                    }
                }
            }
        }).build(new CacheLoader<LoadingKey, Table>() {
            @Override
            public Table load(LoadingKey key) throws Exception {
                Configuration configuration = HBaseConfigurationFactory.getConfiguration(key.mediaSourceInfo);
                HBaseMediaSrcParameter hBaseMediaSrcParameter = key.mediaSourceInfo.getParameterObj();
                // kerberos 认证
                UserGroupInformation.setConfiguration(configuration);
                UserGroupInformation.loginUserFromKeytab(hBaseMediaSrcParameter.getLoginPrincipal(), hBaseMediaSrcParameter.getLoginKeytabPath());
                Connection connection = ConnectionFactory.createConnection(configuration);
                Table hTable = connection.getTable(TableName.valueOf(key.tableName));
                if (hTable == null) {
                    throw new DatalinkException("hbase 表加载失败");
                }
                return hTable;
            }
        });
    }

    public static Table getTable(String tableName, MediaSourceInfo mediaSourceInfo) {
        return hbaseConnections.getUnchecked(new LoadingKey(tableName, mediaSourceInfo));
    }

    public static void invalidate() {
        hbaseConnections.invalidateAll();
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
            if (this == o) {
                return true;
            }
            if (!(o instanceof LoadingKey)) {
                return false;
            }

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
