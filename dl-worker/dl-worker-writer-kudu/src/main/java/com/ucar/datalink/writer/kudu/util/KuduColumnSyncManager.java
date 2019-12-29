package com.ucar.datalink.writer.kudu.util;

import com.alibaba.druid.sql.parser.ParserException;
import com.google.common.collect.Maps;
import com.ucar.datalink.biz.mapping.RDBMSMapping;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.ddl.DdlSqlUtils;
import com.ucar.datalink.biz.utils.ddl.SQLStatementHolder;
import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.domain.event.KuduColumnSyncEvent;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.kudu.KuduMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.relationship.SqlCheckColumnInfo;
import com.ucar.datalink.domain.relationship.SqlCheckItem;
import com.ucar.datalink.domain.relationship.SqlType;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * kudu 同步修改字段
 */
public class KuduColumnSyncManager {

    private static final Logger logger = LoggerFactory.getLogger(KuduColumnSyncManager.class);

    public static void syncColumnDefinition(KuduColumnSyncEvent event) throws Exception {
        String sql = event.getSql();
        Long mappingId = event.getMappingId();

        MediaMappingInfo mediaMappingInfo = DataLinkFactory.getObject(MediaService.class).findMediaMappingsById(mappingId);
        if (mediaMappingInfo == null) {
            logger.info(CodeContext.getErrorDesc(CodeContext.NOTFIND_MAPPING_ERROR_CODE), String.format("mappingId[%s]", mappingId));
            throw new ErrorException(CodeContext.NOTFIND_MAPPING_ERROR_CODE, CodeContext.getErrorDesc(CodeContext.NOTFIND_MAPPING_ERROR_CODE));
        }

        KuduMediaSrcParameter kuduMediaSrcParameter = getKuduMediaSrcParameter(mediaMappingInfo.getTargetMediaSourceId());
        //获取需要更改的列和类型
        Map<String, String> columnNameAndType = getColumnNameAndType(mediaMappingInfo, sql);
        if (columnNameAndType != null) {
            mysqlTypeConvertKuduType(columnNameAndType);
            String database = kuduMediaSrcParameter.getDatabase();
            List<KuduMediaSrcParameter.ImpalaCconfig> impalaCconfig = kuduMediaSrcParameter.getImpalaCconfigs();
            String tableName = mediaMappingInfo.getTargetMediaName();
            for (Map.Entry<String, String> entry : columnNameAndType.entrySet()) {
                KuduTableDDLUtils.KuduColumnDDL kuduColumnDDL = new KuduTableDDLUtils.KuduColumnDDL(tableName, entry.getKey(), entry.getValue());
                KuduTableDDLUtils.addColumn(impalaCconfig, database, kuduColumnDDL);
            }
        }
    }


    private static void mysqlTypeConvertKuduType(Map<String, String> columnNameAndType) {
        RDBMSMapping rdbmsMapping = new RDBMSMapping();
        for (String column : columnNameAndType.keySet()) {
            ColumnMeta columnMeta = new ColumnMeta();
            columnMeta.setType(columnNameAndType.get(column));
            ColumnMeta columnMeta1 = rdbmsMapping.toKudu(columnMeta);
            columnNameAndType.put(column, columnMeta1.getType());
        }
    }


    private static KuduMediaSrcParameter getKuduMediaSrcParameter(Long mediaSourceId) throws Exception {
        MediaSourceInfo targetMediaSource = DataLinkFactory.getObject(MediaSourceService.class).getById(mediaSourceId);
        Assert.notNull(targetMediaSource, String.format("can not found mediaSource by mediaSourceId[%d]", mediaSourceId));

        MediaSrcParameter parameterObj = targetMediaSource.getParameterObj();
        if (!(parameterObj instanceof KuduMediaSrcParameter)) {
            throw new Exception(String.format("mediaSourceId[%d] is not Kudu MediaSource!", mediaSourceId));
        }
        return (KuduMediaSrcParameter) parameterObj;
    }


    private static Map<String, String> getColumnNameAndType(MediaMappingInfo mediaMappingInfo, String sql) throws ErrorException {
        MediaSourceType mediaSourceType = getMediaSourceType(mediaMappingInfo);
        SQLStatementHolder holder = getSQLStatementHolder(mediaSourceType, sql);
        //获取添加的字段及类型
        Map<String, String> datasourceAddColumns = getAddColumnMap(holder);
        return datasourceAddColumns;
    }


    private static KuduTable getKuduTable(KuduClient client, KuduMediaSrcParameter kuduMediaSrcParameter, MediaMappingInfo mediaMappingInfo) throws KuduException {
        String database = kuduMediaSrcParameter.getDatabase();
        String tableName = mediaMappingInfo.getTargetMediaName();
        KuduTable kuduTable = client.openTable(String.format("%s.%s", database, tableName));
        return kuduTable;
    }

    private static KuduClient getKuduClient(KuduMediaSrcParameter kuduMediaSrcParameter) {
        List<String> host2Ports = kuduMediaSrcParameter.getHost2Ports();
        return KuduUtils.createClient(host2Ports);
    }

    private static Map<String, String> getAddColumnMap(SQLStatementHolder holder) throws ErrorException {
        List<SqlCheckItem> items = holder.getSqlCheckItems();
        if (items.size() != 1) {
            throw new ErrorException(CodeContext.SQL_COUNT_ERROR_CODE, CodeContext.getErrorDesc(CodeContext.SQL_COUNT_ERROR_CODE));
        }
        SqlCheckItem sqlItem = items.get(0);
        if (sqlItem.isContainsColumnAdd() && SqlType.AlterTable.equals(holder.getSqlType())) {
            List<SqlCheckColumnInfo> columnsInfo = sqlItem.getColumnsAddInfo();
            Map<String, String> map = Maps.newHashMap();
            columnsInfo.forEach(e -> {
                map.put(e.getName(), e.getDataType());
            });
            return map;
        } else {
            return null;
        }
    }


    public static SQLStatementHolder getSQLStatementHolder(MediaSourceType mediaSourceType, String sqls) throws ErrorException {
        try {
            List<SQLStatementHolder> holders = DdlSqlUtils.buildSQLStatement(mediaSourceType, sqls);
            if (holders.isEmpty() || holders.size() > 1) {
                throw new ErrorException(CodeContext.SQL_COUNT_ERROR_CODE, CodeContext.getErrorDesc(CodeContext.SQL_COUNT_ERROR_CODE));
            }
            SQLStatementHolder holder = holders.get(0);
            holder.check();
            return holder;
        } catch (ParserException e) {
            throw new ErrorException(CodeContext.SQL_SYNTAX_ERROR_CODE, CodeContext.getErrorDesc(CodeContext.SQL_SYNTAX_ERROR_CODE));
        }
    }

    public static MediaSourceType getMediaSourceType(MediaMappingInfo mappingInfo) {
        //返回操作类型以及列和类型映射关系
        MediaSourceType mediaSourceType;
        mediaSourceType = mappingInfo.getSourceMedia().getMediaSource().getType();
        return mediaSourceType;
    }
}
