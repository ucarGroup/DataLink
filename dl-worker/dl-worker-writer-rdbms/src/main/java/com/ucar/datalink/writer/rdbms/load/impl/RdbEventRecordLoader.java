package com.ucar.datalink.writer.rdbms.load.impl;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import com.ucar.datalink.worker.api.util.dialect.mysql.MysqlDialect;

import com.ucar.datalink.worker.api.util.dialect.sqlserver.SqlServerDialect;
import com.ucar.datalink.writer.rdbms.handle.SqlBuilder;
import com.ucar.datalink.writer.rdbms.load.RecordLoader;
import com.ucar.datalink.worker.api.util.dialect.SqlUtils;
import com.ucar.datalink.writer.rdbms.utils.TableCheckUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecordLoader for RdbEventRecord.
 * <p>
 * Created by lubiao on 2017/3/14.
 */
public class RdbEventRecordLoader extends RecordLoader<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(RdbEventRecordLoader.class);

    @Override
    protected String getSql(RdbEventRecord record) {
        if (StringUtils.isBlank(record.getSql())) {
            //如果外面没有生成过sql,则及时生成一下
            SqlBuilder.buildSql(record);
        }
        return record.getSql();
    }

    @Override
    protected void transactionBegin() {

    }

    @Override
    protected void transactionEnd() {

    }

    @Override
    protected void fillPreparedStatement(PreparedStatement ps, LobCreator lobCreator, RdbEventRecord record, DbDialect dbDialect, boolean isSyncAutoAddColumn) throws SQLException {
        EventType type = record.getEventType();
        // 注意insert/update语句对应的字段数序都是将主键排在后面
        List<EventColumn> columns = new ArrayList<EventColumn>();
        if (type.isInsert()) {
            columns.addAll(record.getColumns()); // insert为所有字段
            columns.addAll(record.getKeys());
        } else if (type.isDelete()) {
            columns.addAll(record.getKeys());
        } else if (type.isUpdate()) {
            boolean existOldKeys = !CollectionUtils.isEmpty(record.getOldKeys());
            columns.addAll(record.getUpdatedColumns());// 只更新带有isUpdate=true的字段
            columns.addAll(record.getKeys());
            if (existOldKeys) {
                columns.addAll(record.getOldKeys());
            }
        }

        // 获取一下当前字段名的数据是否必填

        Map<String, Boolean> isRequiredMap = buildRequiredMap(dbDialect, record, columns, isSyncAutoAddColumn);
        Table table = dbDialect.findTable(record.getSchemaName(), record.getTableName());

        for (int i = 0; i < columns.size(); i++) {
            int paramIndex = i + 1;
            EventColumn column = columns.get(i);
            Boolean isRequired = isRequiredMap.get(StringUtils.lowerCase(column.getColumnName()));

            int sqlType = getSqlType(column, table, record);
            Object param = null;
            if (dbDialect instanceof MysqlDialect
                    && (sqlType == Types.TIME || sqlType == Types.TIMESTAMP || sqlType == Types.DATE)) {
                // 解决mysql的0000-00-00 00:00:00问题，直接依赖mysql
                // driver进行处理，如果转化为Timestamp会出错
                param = column.getColumnValue();
            } else {
                param = SqlUtils.stringToSqlValue(column.getColumnValue(),
                        sqlType,
                        isRequired,
                        dbDialect.isEmptyStringNulled());
            }

            try {
                switch (sqlType) {
                    case Types.CLOB:
                        lobCreator.setClobAsString(ps, paramIndex, (String) param);
                        break;

                    case Types.BLOB:
                        lobCreator.setBlobAsBytes(ps, paramIndex, param instanceof String ? ((String) param).getBytes() : (byte[]) param);
                        break;
                    case Types.TIME:
                    case Types.TIMESTAMP:
                    case Types.DATE:
                        // 只处理mysql的时间类型，oracle的进行转化处理
                        if (dbDialect instanceof MysqlDialect) {
                            // 解决mysql的0000-00-00 00:00:00问题，直接依赖mysql
                            // driver进行处理，如果转化为Timestamp会出错
                            ps.setObject(paramIndex, param);
                        } else {
                            StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                        }
                        break;
                    case Types.BIT:
                        // 只处理mysql的bit类型，bit最多存储64位，所以需要使用BigInteger进行处理才能不丢精度
                        // mysql driver将bit按照setInt进行处理，会导致数据越界
                        if (dbDialect instanceof MysqlDialect) {
                            StatementCreatorUtils.setParameterValue(ps, paramIndex, Types.DECIMAL, null, param);
                        } else if (dbDialect instanceof SqlServerDialect) {
                            StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param.toString());
                        } else {
                            StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                        }
                        break;
                    default:
                        StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                        break;
                }
            } catch (SQLException ex) {
                logger.error("SetParam error , [mappingId={}, sqltype={}, value={}]",
                        RecordMeta.mediaMapping(record).getId(), sqlType, param);
                throw ex;
            }
        }
    }

    private Map<String, Boolean> buildRequiredMap(DbDialect dbDialect, RdbEventRecord record, List<EventColumn> columns, boolean isSyncAutoAddColumn) {
        String errorMsg = "Build RequiredMap Failed.";

        //最多试3次
        for (int i = 0; i < 3; i++) {
            Map<String, Boolean> isRequiredMap = new HashMap<>();
            Table table = dbDialect.findTable(record.getSchemaName(), record.getTableName());
            for (Column tableColumn : table.getColumns()) {
                isRequiredMap.put(StringUtils.lowerCase(tableColumn.getName()), tableColumn.isRequired());
            }

            boolean lackColumns = false;
            for (int j = 0; j < columns.size(); j++) {
                EventColumn column = columns.get(j);
                Boolean isRequired = isRequiredMap.get(StringUtils.lowerCase(column.getColumnName()));
                if (isRequired == null) {
                    lackColumns = true;
                    if (isSyncAutoAddColumn) {
                        autoAddColumn(record, dbDialect, record.getSchemaName(), record.getTableName(), column.getColumnName());
                    }
                    errorMsg = String.format("column name %s is not found in Table[%s]",
                            column.getColumnName(),
                            table.toString());
                }
            }

            if (lackColumns) {
                //缺字段，可能是因为内存中保存的元数据信息还未及时更新，实时reload一下，然后重试
                dbDialect.reloadTable(record.getSchemaName(), record.getTableName());
            } else {
                return isRequiredMap;
            }
        }

        throw new DatalinkException(errorMsg);
    }

    private boolean autoAddColumn(RdbEventRecord record, DbDialect targetDbDialect, String targetSchemaName, String targetTableName, String columnName) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        if (!mappingInfo.getSourceMedia().getMediaSource().getType().equals(MediaSourceType.MYSQL) &&
                !mappingInfo.getSourceMedia().getMediaSource().getType().equals(MediaSourceType.SDDL)) {
            return false;//暂时只支持源为mysql和sddl的同步
        }

        if (mappingInfo.getTargetMediaSource().getType() == MediaSourceType.MYSQL || mappingInfo.getTargetMediaSource().getType() == MediaSourceType.SDDL) {
            Boolean isRowLimit = TableCheckUtils.isRowNumLimit(record, columnName);
            if (isRowLimit) {
                logger.info("column name {} is not found in Table {} because row count is over 5000000.", columnName, targetTableName);
                return false;
            }
        }

        //获取源DbDialect
        DbDialect srcDbDialect = DbDialectFactory.getDbDialect(mappingInfo.getSourceMedia().getMediaSource());

        //获取源表名称
        String srcTableName;
        if (mappingInfo.getSourceMedia().getNameMode().getMode().isSingle()) {
            srcTableName = "`" + mappingInfo.getSourceMedia().getName() + "`";
        } else {
            srcTableName = "`" + targetTableName + "`";
        }

        //构造加字段sql并执行
        Map<String, Object> tableResult = srcDbDialect.getJdbcTemplate().queryForMap("show create table " + srcTableName);
        String createSql = tableResult.get("Create Table").toString();
        logger.info("createSql is : " + createSql);

        createSql = StringUtils.substringAfter(createSql, "(");
        createSql = StringUtils.substringBeforeLast(createSql, ")");


        String[] columnSqls = StringUtils.split(createSql, "\n");
        for (String columnSql : columnSqls) {
            if (columnSql.contains("`" + columnName + "`")) {
                columnSql = StringUtils.substringBeforeLast(columnSql, ",");
                String addSql = "";

                if (mappingInfo.getTargetMediaSource().getType().equals(MediaSourceType.SQLSERVER)) {
                    columnSql = columnSql.replace("`", "");//目标端不是mysql库则把 "`" 去掉
                    columnSql = StringUtils.substringBefore(columnSql, "COMMENT");//目标端不是mysql，则把注释去掉
                    String tableSql = targetSchemaName + ".dbo." + targetTableName;
                    addSql = "Alter table " + tableSql + " add " + columnSql;
                } else if (mappingInfo.getTargetMediaSource().getType().equals(MediaSourceType.POSTGRESQL)) {
                    columnSql = columnSql.replace("`", "");//目标端不是mysql库则把 "`" 去掉
                    columnSql = StringUtils.substringBefore(columnSql, "COMMENT");//目标端不是mysql，则把注释去掉
                    String tableSql = targetSchemaName + "." + targetTableName;
                    addSql = "Alter table " + tableSql + " add column " + columnSql;
                } else if (mappingInfo.getTargetMediaSource().getType().equals(MediaSourceType.MYSQL)) {
                    String tableSql = "`" + targetSchemaName + "`.`" + targetTableName + "`";
                    addSql = "Alter table " + tableSql + " add column " + columnSql;
                } else if (mappingInfo.getTargetMediaSource().getType().equals(MediaSourceType.SDDL)) {
                    //do nothing
                } else {
                    logger.info("Column Auto-Add is not supported for type " + mappingInfo.getTargetMediaSource().getType());
                    return false;
                }

                if (mappingInfo.getTargetMediaSource().getType().equals(MediaSourceType.SDDL)) {
                    boolean result = true;
                    result &= executeForSecondaryDbs(columnSql, mappingInfo, targetTableName);
                    result &= executeForPrimaryDbs(columnSql, mappingInfo, targetTableName);
                    return result;
                } else {
                    return executeSql(targetDbDialect, addSql);
                }
            }
        }

        logger.info("There is no column named {} in the source table {}.", columnName, srcTableName);
        return false;
    }

    private boolean executeForPrimaryDbs(String columnSql, MediaMappingInfo mappingInfo, String targetTableName) {
        boolean result = true;
        SddlMediaSrcParameter mediaSrcParameter = mappingInfo.getTargetMediaSource().getParameterObj();
        for (Long id : mediaSrcParameter.getPrimaryDbsId()) {
            MediaSourceInfo subMediaSource = DataLinkFactory.getObject(MediaSourceService.class).getById(id);

            String tableSql = "`" + subMediaSource.getParameterObj().getNamespace() + "`.`" + targetTableName + "`";
            String addSql = "Alter table " + tableSql + " add column " + columnSql;

            DbDialect dbDialect = DbDialectFactory.getDbDialect(subMediaSource);
            result &= executeSql(dbDialect, addSql);
        }
        return result;
    }

    private boolean executeForSecondaryDbs(String columnSql, MediaMappingInfo mappingInfo, String targetTableName) {
        boolean result = true;
        SddlMediaSrcParameter mediaSrcParameter = mappingInfo.getTargetMediaSource().getParameterObj();
        for (Long id : mediaSrcParameter.getSecondaryDbsId()) {
            MediaSourceInfo subMediaSource = DataLinkFactory.getObject(MediaSourceService.class).getById(id);

            String tableSql = "`" + subMediaSource.getParameterObj().getNamespace() + "`.`" + targetTableName + "`";
            String addSql = "Alter table " + tableSql + " add column " + columnSql;

            DbDialect dbDialect = DbDialectFactory.getDbDialect(subMediaSource);
            result &= executeSql(dbDialect, addSql);
        }
        return result;
    }

    private boolean executeSql(DbDialect dbDialect, String sql) {
        TransactionTemplate transactionTemplate = dbDialect.getTransactionTemplate();
        int originalBehavior = transactionTemplate.getPropagationBehavior();

        try {
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
            return transactionTemplate.execute(transactionStatus -> {
                try {
                    dbDialect.getJdbcTemplate().execute(new StatementCallback<Boolean>() {

                        public Boolean doInStatement(Statement stmt) throws SQLException, DataAccessException {
                            return stmt.execute(sql);
                        }
                    });
                    logger.info("Sql for adding-column is executed successfully : sql is {}", sql);
                    return true;
                } catch (Throwable t) {
                    logger.error(String.format("skip exception for adding-column-sql : sql is %s", sql), t);
                }
                return false;
            });
        } finally {
            transactionTemplate.setPropagationBehavior(originalBehavior);
        }
    }

    private int getSqlType(EventColumn column, Table table, RdbEventRecord record) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);

        MediaSourceType srcMediaSourceType = mappingInfo.getSourceMedia().getMediaSource().getType();
        if (srcMediaSourceType == MediaSourceType.SDDL) {
            srcMediaSourceType = MediaSourceType.MYSQL;
        }
        MediaSourceType targetMediaSourceType = mappingInfo.getTargetMediaSource().getType();

        //源库和目标库字段类型可能不一致,需要采用目标端的数据类型
        //只对异构数据库进行判断，同构数据库约定两边结构肯定是一致的
        if (srcMediaSourceType != targetMediaSourceType) {
            String columnName = column.getColumnName();
            int srcType = column.getColumnType();

            for (Column c : table.getColumns()) {
                if (c.getName().equalsIgnoreCase(columnName)) {
                    column.setColumnType(c.getTypeCode());
                    break;
                }
            }
            logger.debug(
                    String.format("Type for column [%s] in Table [%s].[%s] is [%s] , and corresponding source type is [%s] , and ColumnValue is [%s].",
                            columnName,
                            mappingInfo.getTargetMediaSource().getName(),
                            table.getName(),
                            column.getColumnType(),
                            srcType,
                            column.getColumnValue()));
        }
        return column.getColumnType();

    }
}
