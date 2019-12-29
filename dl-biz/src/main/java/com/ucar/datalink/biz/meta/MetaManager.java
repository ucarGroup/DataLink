package com.ucar.datalink.biz.meta;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 2017/6/24.
 */
public class MetaManager {

    private static final String HBASE_KEY = "rowkey";

    public static MediaSourceInfo getMediaSourceInfoById(long id) {
        MediaSourceService mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        return mediaSourceService.getById(id);
    }

    /**
     * 根据传入的MediaSourceInfo，获取对应的数据库中所有表的元信息
     *
     * @param info
     * @return
     * @throws Exception
     */
    public static List<MediaMeta> getTables(MediaSourceInfo info) throws Exception {
        MediaSrcParameter msp = info.getParameterObj();
        if (info.getType().isRdbms()) {
            //处理MySql、oracle、sql server、Postgresql类型的元数据
            List<MediaMeta> tables = RDBMSUtil.getTables(info);
            updateMediaMetaDBType(tables, info.getType());
            return tables;
        } else if (info.getType() == MediaSourceType.ELASTICSEARCH) {
            //处理ElasticSearch类型的元数据
            List<MediaMeta> tables = ElasticSearchUtil.getTables(info);
            updateMediaMetaDBType(tables, MediaSourceType.ELASTICSEARCH);
            return tables;
        } else if (info.getType() == MediaSourceType.HBASE) {
            //处理HBase类型的元数据
            List<MediaMeta> tables = HBaseUtil.getTables(info);
            updateMediaMetaDBType(tables, MediaSourceType.HBASE);
            return tables;
        } else if (info.getType() == MediaSourceType.HDFS) {
            //处理HDFS类型的元数据
            List<MediaMeta> tables = HDFSUtil.getTables(info);
            updateMediaMetaDBType(tables, MediaSourceType.HDFS);
            return tables;
        } else if (info.getType() == MediaSourceType.KUDU) {
            List<MediaMeta> tables = KuduUtil.getTables(info);
            updateMediaMetaDBType(tables, MediaSourceType.KUDU);
            return tables;
        } else if (info.getType() == MediaSourceType.SDDL) {
            //处理SDDL类型的元数据
            SddlMediaSrcParameter sddl = (SddlMediaSrcParameter) info.getParameterObj();
            MediaSourceInfo s_info = getMediaSourceInfoById(sddl.getProxyDbId());
            List<MediaMeta> tables = RDBMSUtil.getTables(s_info);
            updateMediaMetaDBType(tables, MediaSourceType.SDDL);
            return tables;
        } else {
            //暂时不支持
            throw new UnsupportedOperationException();
        }

    }


    public static List<ColumnMeta> getColumns(MediaSourceInfo info, String tableName) throws Exception {
        MediaSrcParameter msp = info.getParameterObj();
        if (info.getType().isRdbms()) {
            //处理MySql、Oracle、SqlServer、Postgresql类型的元数据的列
            List<ColumnMeta> list = RDBMSUtil.getColumns(info, tableName);
            return list;
        } else if (info.getType() == MediaSourceType.ELASTICSEARCH) {
            //处理ElasticSearch类型的元数据的列
            List<ColumnMeta> columns = ElasticSearchUtil.getColumns(info, tableName);
            return columns;
        } else if (info.getType() == MediaSourceType.HBASE) {
            //处理HBase类型的元数据的列
            List<ColumnMeta> list = HBaseUtil.getColumns(info, tableName);
            dislogeEmptyColumnName(list);
            return list;
        } else if (info.getType() == MediaSourceType.HDFS) {
            //处理HDFS类型的元数据列
            List<ColumnMeta> list = HDFSUtil.getColumns(info, tableName);
            return list;
        } else if (info.getType() == MediaSourceType.SDDL) {
            SddlMediaSrcParameter sddl = (SddlMediaSrcParameter) info.getParameterObj();
            MediaSourceInfo s_info = getMediaSourceInfoById(sddl.getProxyDbId());
            List<ColumnMeta> list = RDBMSUtil.getColumns(s_info, tableName);
            return list;
        } else if (info.getType() == MediaSourceType.KUDU) {
            List<ColumnMeta> list = KuduUtil.getColumns(info, tableName);
            return list;
        } else {
            //其他类型暂不支持
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 把列名为空的去掉
     *
     * @param list
     */
    private static void dislogeEmptyColumnName(List<ColumnMeta> list) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ColumnMeta columnMeta = (ColumnMeta) it.next();
            if (StringUtils.isEmpty(columnMeta.getName())) {
                columnMeta.setName("NULL_FLAG");
                list.set(list.indexOf(columnMeta), columnMeta);
            }
        }
    }

    /**
     * 统一设置表对应的数据库类型
     *
     * @param tables
     * @param type
     */
    public static void updateMediaMetaDBType(List<MediaMeta> tables, MediaSourceType type) {
        if (tables == null || tables.size() == 0 || type == null) {
            return;
        }
        for (MediaMeta t : tables) {
            t.setDbType(type);
        }
    }

    public static MediaMeta getMediaMeta(MediaSourceInfo mediaSourceInfo, String tableName) throws Exception {
        MediaMeta mediaMeta = new MediaMeta();
        List<ColumnMeta> columns = null;
        if (mediaSourceInfo.getType() == MediaSourceType.MYSQL) {
            columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
            mediaMeta.setDbType(MediaSourceType.MYSQL);
        } else if (mediaSourceInfo.getType() == MediaSourceType.SQLSERVER) {
            columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
            mediaMeta.setDbType(MediaSourceType.SQLSERVER);
        } else if (mediaSourceInfo.getType() == MediaSourceType.POSTGRESQL) {
            columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
            mediaMeta.setDbType(MediaSourceType.POSTGRESQL);
        } else if (mediaSourceInfo.getType() == MediaSourceType.HBASE) {
            try {
                columns = HBaseUtil.getColumns(mediaSourceInfo, tableName);
                dislogeEmptyColumnName(columns);
                mediaMeta.setDbType(MediaSourceType.HBASE);
            } catch (ErrorException e) {
                if (e.getCode().equals(CodeContext.HBASE_COLUMNMETA_ERROR_CODE)) {
                    columns = new ArrayList<>();
                    ColumnMeta columnMeta = new ColumnMeta();
                    columnMeta.setName(HBASE_KEY);
                    columnMeta.setType("Bytes");
                    columnMeta.setIsPrimaryKey(true);
                    columns.add(columnMeta);
                } else {
                    throw new ErrorException(e.getCode(), e.getMessage());
                }
            }
        } else if (mediaSourceInfo.getType() == MediaSourceType.ORACLE) {
            columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
            mediaMeta.setDbType(MediaSourceType.ORACLE);
        } else {
            return null;
        }
        mediaMeta.setColumn(columns);
        mediaMeta.setName(tableName);
        return mediaMeta;
    }

    public static List<ColumnMetaInfo> getTableInfos(MediaMeta mediaMeta, MediaSourceInfo mediaSourceInfo) {
        List<ColumnMeta> columns = mediaMeta.getColumn();
        List<ColumnMetaInfo> columnMetaInfos = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            ColumnMetaInfo info = new ColumnMetaInfo();
            if (mediaSourceInfo.getType() == MediaSourceType.HBASE) {
                info.setHbaseFamily(columns.get(i).getColumnFamily());
            }
            if (columns.get(i).isPrimaryKey()) {
                info.setIsPrimaryKey("true");
            }
            info.setName(columns.get(i).getName());
            info.setType(columns.get(i).getType());
            info.setLength(columns.get(i).getLength());
            info.setDecimalDigits(columns.get(i).getDecimalDigits());
            info.setColumnDesc(columns.get(i).getColumnDesc());
            columnMetaInfos.add(info);
        }
        return columnMetaInfos;
    }

    public static class ColumnMetaInfo {
        //列名
        private String name;
        //列类型
        private String type;
        //类长度
        private Integer length;
        //列信息
        private String columnDesc;
        //列精度
        private Integer decimalDigits;
        private String isPrimaryKey;
        //hbase的family
        private String hbaseFamily;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public String getColumnDesc() {
            return columnDesc;
        }

        public void setColumnDesc(String columnDesc) {
            this.columnDesc = columnDesc;
        }

        public Integer getDecimalDigits() {
            return decimalDigits;
        }

        public void setDecimalDigits(Integer decimalDigits) {
            this.decimalDigits = decimalDigits;
        }

        public String getIsPrimaryKey() {
            return isPrimaryKey;
        }

        public void setIsPrimaryKey(String isPrimaryKey) {
            this.isPrimaryKey = isPrimaryKey;
        }

        public String getHbaseFamily() {
            return hbaseFamily;
        }

        public void setHbaseFamily(String hbaseFamily) {
            this.hbaseFamily = hbaseFamily;
        }
    }
}
