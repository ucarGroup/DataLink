package com.ucar.datalink.writer.hbase.handle.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceRelationService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceRelationInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sqq on 2017/11/29.
 */
public class HBaseConfigurationFactory {
    private static final Logger logger = LoggerFactory.getLogger(HBaseConfigurationFactory.class);
    private static final LoadingCache<MediaSourceInfo, Configuration> configuration;

    static {
        configuration = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(new CacheLoader<MediaSourceInfo, Configuration>() {
            @Override
            public Configuration load(MediaSourceInfo mediaSourceInfo) throws Exception {

                HBaseMediaSrcParameter hbaseParameter = mediaSourceInfo.getParameterObj();
                MediaSourceInfo zkMediaSource = DataLinkFactory.getObject(MediaService.class).getMediaSourceById(hbaseParameter.getZkMediaSourceId());
                ZkMediaSrcParameter zkParameter = zkMediaSource.getParameterObj();
                String address = zkParameter.parseServersToString();
                String prot = zkParameter.parsePort() + "";
                String znode = hbaseParameter.getZnodeParent();
                System.getProperties().setProperty("HADOOP_USER_NAME", "tomcat");
                Configuration conf = HBaseConfiguration.create();
                conf.set("hbase.zookeeper.quorum", address);
                conf.set("hbase.zookeeper.property.clientPort", prot);
                conf.set("zookeeper.znode.parent", znode);
                return conf;
            }
        });
    }

    public static Configuration getConfiguration(MediaSourceInfo mediaSourceInfo) {
        //只有HTableFactory引用，传过来的已经是真实数据源，若要在其他地方引用，需要兼容虚拟数据源
        return configuration.getUnchecked(mediaSourceInfo);
    }

    public static void invalidate(MediaSourceInfo mediaSourceInfo) {
        if (mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)) {
            //清理虚拟数据源对应的真实数据源缓存
            List<MediaSourceInfo> list = DataLinkFactory.getObject(MediaSourceService.class).findRealListByVirtualMsId(mediaSourceInfo.getId());
            for (MediaSourceInfo info : list) {
                Configuration config = configuration.getIfPresent(info);
                if (config != null) {
                    configuration.invalidate(info);
                    HTableFactory.invalidate();
                    logger.info("HBase config invalidate successfully with mediaSoruceId = " + mediaSourceInfo.getId() + " and labName = " + mediaSourceInfo.getLabName());
                }
            }

            //清理虚拟和真实数据源的对应关系
            DataLinkFactory.getObject(MediaSourceService.class).clearRealMediaSourceListCache(mediaSourceInfo.getId());
            logger.info("HBase realListCache invalidate successfully with virtualMediaSoruceId = " + mediaSourceInfo.getId());
        } else {
            Configuration config = configuration.getIfPresent(mediaSourceInfo);
            if (config != null) {
                configuration.invalidate(mediaSourceInfo);
                HTableFactory.invalidate();
                logger.info("HBase config invalidate successfully with mediaSoruceId = " + mediaSourceInfo.getId() + " and labName = " + mediaSourceInfo.getLabName());
            }
            //如果有对应的虚拟数据源，则清理虚拟数据源对应的真实数据源的MediaSourceInfo缓存
            MediaSourceRelationInfo relationInfo = DataLinkFactory.getObject(MediaSourceRelationService.class).getOneByRealMsId(mediaSourceInfo.getId());
            if (relationInfo != null) {
                DataLinkFactory.getObject(MediaSourceService.class).clearRealMediaSourceListCache(relationInfo.getVirtualMsId());
                logger.info("HBase realListCache invalidate successfully with virtualMediaSoruceId = " + relationInfo.getVirtualMsId());
            }
        }
    }

}
