package com.ucar.datalink.biz.meta;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/6/24.
 */
public class MetaManager {

    public static MediaSourceInfo getMediaSourceInfoById(long id) {
        MediaSourceService mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        return mediaSourceService.getById(id);
    }

    /**
     * 根据传入的MediaSourceInfo，获取对应的数据库中所有表的元信息
     * @param info
     * @return
     * @throws Exception
     */
    public static List<MediaMeta> getTables(MediaSourceInfo info) throws Exception {
        MediaSrcParameter msp = info.getParameterObj();
        if(info.getType().isRdbms()) {
            //处理MySql、oracle、sql server、Postgresql类型的元数据
            List<MediaMeta> tables = RDBMSUtil.getTables(info);
            updateMediaMetaDBType(tables,info.getType());
            return tables;
        }
        else if( info.getType()==MediaSourceType.ELASTICSEARCH ) {
            //处理ElasticSearch类型的元数据
            List<MediaMeta> tables = ElasticSearchUtil.getTables(info);
            updateMediaMetaDBType(tables,MediaSourceType.ELASTICSEARCH);
            return tables;
        }
        else if( info.getType()==MediaSourceType.HBASE ) {
            //处理HBase类型的元数据
            List<MediaMeta> tables = HBaseUtil.getTables(info);
            updateMediaMetaDBType(tables,MediaSourceType.HBASE);
            return tables;
        }
        else if( info.getType()==MediaSourceType.HDFS ) {
            //处理HDFS类型的元数据
//            List<MediaMeta> tables = HDFSUtil.getTables(info);
//            updateMediaMetaDBType(tables,MediaSourceType.HDFS);
//            return tables;
            return new ArrayList<>();
        }
        else if( info.getType()==MediaSourceType.SDDL ) {
            //处理SDDL类型的元数据
            SddlMediaSrcParameter sddl = (SddlMediaSrcParameter)info.getParameterObj();
            MediaSourceInfo s_info = getMediaSourceInfoById( sddl.getProxyDbId() );
            List<MediaMeta> tables = RDBMSUtil.getTables(s_info);
            updateMediaMetaDBType(tables,MediaSourceType.SDDL);
            return tables;
        }
        else {
            //暂时不支持
            throw new UnsupportedOperationException();
        }

    }



    public static List<ColumnMeta> getColumns(MediaSourceInfo info, String tableName) throws Exception {
        MediaSrcParameter msp = info.getParameterObj();
        if(info.getType().isRdbms()) {
            //处理MySql、Oracle、SqlServer、Postgresql类型的元数据的列
            List<ColumnMeta> list = RDBMSUtil.getColumns(info, tableName);
            return list;
        }
        else if( info.getType()==MediaSourceType.ELASTICSEARCH ) {
            //处理ElasticSearch类型的元数据的列
            List<ColumnMeta> columns = ElasticSearchUtil.getColumns(info, tableName);
            return columns;
        }
        else if( info.getType()==MediaSourceType.HBASE ) {
            //处理HBase类型的元数据的列
            List<ColumnMeta> list = HBaseUtil.getColumns(info, tableName);
            return list;
        }
        else if( info.getType()==MediaSourceType.HDFS ) {
            //处理HDFS类型的元数据列
//            List<ColumnMeta> list = HDFSUtil.getColumns(info, tableName);
//            return list;
            return new ArrayList<>();
        }
        else if( info.getType()==MediaSourceType.SDDL ) {
            SddlMediaSrcParameter sddl = (SddlMediaSrcParameter)info.getParameterObj();
            MediaSourceInfo s_info = getMediaSourceInfoById( sddl.getProxyDbId() );
            List<ColumnMeta> list = RDBMSUtil.getColumns(s_info, tableName);
            return list;
        }
        else {
            //其他类型暂不支持
            throw new UnsupportedOperationException();
        }

    }


    /**
     * 统一设置表对应的数据库类型
     * @param tables
     * @param type
     */
    public static void updateMediaMetaDBType(List<MediaMeta> tables, MediaSourceType type) {
        if(tables==null || tables.size()==0 || type==null) {
            return ;
        }
        for(MediaMeta t : tables) {
            t.setDbType(type);
        }
    }


}
