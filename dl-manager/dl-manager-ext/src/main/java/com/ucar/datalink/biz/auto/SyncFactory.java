package com.ucar.datalink.biz.auto;

import com.ucar.datalink.biz.auto.impl.*;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.sync.SyncApplyContent;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.domain.sync.SyncApplyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yang.wang09 on 2018-04-24 10:37.
 */
public class SyncFactory {

    private static Logger logger = LoggerFactory.getLogger(SyncFactory.class);

    private static MediaSourceService mediaSourceService;

    private static final String Full = "Full";

    private static final String Increment = "Increment";


    static {
        mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
    }

    public static AbstractSync createSync(SyncApplyInfo info) {
        SyncApplyContent syncApplyContent = info.getApplyContentObj();
        SyncApplyParameter syncApplyParameter = syncApplyContent.getApplyParameterList().get(0);

        Long srcMediaSourceId = syncApplyParameter.getSrcMediaSourceId();
        Long tarMediaSourceId = syncApplyParameter.getTargetMediaSourceId();
        MediaSourceInfo srcInfo = mediaSourceService.getById(srcMediaSourceId);
        MediaSourceInfo destInfo = mediaSourceService.getById(tarMediaSourceId);

        AbstractSync sync = null;
        if (srcInfo.getType().isRdbms() || srcInfo.getType() == MediaSourceType.HBASE ||srcInfo.getType() == MediaSourceType.HDFS ||srcInfo.getType() == MediaSourceType.ELASTICSEARCH) {
            sync = assembleFullAndIncrement(destInfo, info);
        }

        /*switch(srcInfo.getType()) {
            case HBASE:
                sync = assembleFullAndIncrement(generateSrcHBaseType(destInfo), info);
                break;
            case HDFS:
                sync = assembleFullAndIncrement(generateSrcHiveType(destInfo), info);
                break;
            case MYSQL:
                sync = assembleFullAndIncrement(generateSrcMySqlType(destInfo), info);
                break;
            case SQLSERVER:
                sync = assembleFullAndIncrement(generateSrcSqlServerType(destInfo), info);
                break;
            case POSTGRESQL:
                //暂不支持
                break;
            case ELASTICSEARCH:
                //暂不支持
                break;
            case SDDL:
                //暂不支持
                break;
            default:
                //报错
        }*/
        //如果 sync 还是为null，抛错
        //根据src id得到类型
        //根据dest id得到类型
        if(sync == null) {
            throw new RuntimeException("get sync executor failure, src media source="+srcInfo.toString()+"" +
                    "dest media soure="+destInfo.toString());
        }
        return sync;

    }

    /**
     * 将 同步类型(全量，增量)，是否初始化两个参数，传给具体的同步类
     * @param destInfo
     * @param info
     * @return
     */
    private static AbstractSync assembleFullAndIncrement(MediaSourceInfo destInfo,SyncApplyInfo info) {
        AbstractSync sync = new SyncImpl();
        if (destInfo.getType() == MediaSourceType.HDFS) {
            sync.setFullFirst(false);
        } else {
            sync.setFullFirst(true);
        }
        if(Full.equalsIgnoreCase(info.getApplyType())) {
            sync.setHasFull(true);
        }
        if(Increment.equalsIgnoreCase(info.getApplyType())) {
            sync.setHasIncrement(true);
            if(info.getIsInitialData()) {
                sync.setHasFull(true);
            }
        }
        return sync;
    }


    private static AbstractSync generateSrcHBaseType(MediaSourceInfo target) {
        try {
            switch (target.getType()) {

                case HBASE:
                    return (AbstractSync)DataLinkFactory.getObject(HBase2HBase.class);
                case HDFS:
                    return (AbstractSync)DataLinkFactory.getObject(HBase2Hive.class);
                case ELASTICSEARCH:
                case MYSQL:
                case SQLSERVER:
                default:
                    return null;
            }
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }


    private static AbstractSync generateSrcHiveType(MediaSourceInfo target) {
        try {
            switch (target.getType()) {
                case ELASTICSEARCH:
                    return (AbstractSync)DataLinkFactory.getObject(Hive2ElasticSearch.class);
                case HBASE:
                    return (AbstractSync)DataLinkFactory.getObject(Hive2HBase.class);
                case HDFS:
                    return (AbstractSync)DataLinkFactory.getObject(Hive2Hive.class);
                case MYSQL:
                    return (AbstractSync)DataLinkFactory.getObject(Hive2MySql.class);
                case SQLSERVER:
                    return (AbstractSync)DataLinkFactory.getObject(Hive2SqlServer.class);
                default:
                    return null;
            }
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private static AbstractSync generateSrcMySqlType(MediaSourceInfo target) {
        try {
            switch (target.getType()) {
                case ELASTICSEARCH:
                    return (AbstractSync)DataLinkFactory.getObject(MySql2ElasticSearch.class);
                case HBASE:
                    return (AbstractSync)DataLinkFactory.getObject(MySql2HBase.class);
                case HDFS:
                    return (AbstractSync)DataLinkFactory.getObject(MySql2Hive.class );
                case MYSQL:
                    return (AbstractSync)DataLinkFactory.getObject(MySql2MySql.class);
                case SQLSERVER:
                    return (AbstractSync)DataLinkFactory.getObject(MySql2SqlServer.class);
                default:
                    return null;
            }
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }


    private static AbstractSync generateSrcSqlServerType(MediaSourceInfo target) {
        try {
            switch (target.getType()) {
                case ELASTICSEARCH:
                    return (AbstractSync)DataLinkFactory.getObject(SqlServer2ElasticSearch.class);
                case HBASE:
                    return (AbstractSync)DataLinkFactory.getObject(SqlServer2HBase.class);
                case HDFS:
                    return (AbstractSync)DataLinkFactory.getObject(SqlServer2Hive.class);
                case MYSQL:
                    return (AbstractSync)DataLinkFactory.getObject(SqlServer2MySql.class);
                case SQLSERVER:
                    return (AbstractSync)DataLinkFactory.getObject(SqlServer2SqlServer.class);
                default:
                    return null;
            }
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }



}
