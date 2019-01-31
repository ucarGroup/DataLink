package com.ucar.datalink.writer.hbase.handle.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sqq on 2017/11/29.
 */
public class HBaseConfigurationFactory {

    private static final Logger logger = LoggerFactory.getLogger(HBaseConfigurationFactory.class);
    private static final LoadingCache<MediaSourceInfo, Configuration> configuration;

    static {
        configuration = CacheBuilder.newBuilder().build(new CacheLoader<MediaSourceInfo, Configuration>() {
            @Override
            public Configuration load(MediaSourceInfo mediaSourceInfo) throws Exception {
                HBaseMediaSrcParameter hbaseParameter = mediaSourceInfo.getParameterObj();
                MediaSourceInfo zkMediaSource = DataLinkFactory.getObject(MediaService.class).getMediaSourceById(hbaseParameter.getZkMediaSourceId());
                ZkMediaSrcParameter zkParameter = zkMediaSource.getParameterObj();
                String address = zkParameter.parseServersToString();
                String prot = zkParameter.parsePort() + "";
                String znode = hbaseParameter.getZnodeParent();
                Configuration conf = HBaseConfiguration.create();
                conf.set("hbase.zookeeper.quorum", address);
                conf.set("hbase.zookeeper.property.clientPort", prot);
                conf.set("zookeeper.znode.parent", znode);
                return conf;
            }
        });
    }

    public static Configuration getConfiguration(MediaSourceInfo mediaSourceInfo) {
        return configuration.getUnchecked(mediaSourceInfo);
    }

    public static void invalidate(MediaSourceInfo mediaSourceInfo) {
        Configuration config = configuration.getIfPresent(mediaSourceInfo);
        if (config != null) {
            configuration.invalidate(mediaSourceInfo);
            HTableFactory.invalidate();
            logger.info("HBase config invalidate successfully with mediaSoruceId = " + mediaSourceInfo.getId());
        }
    }

}
