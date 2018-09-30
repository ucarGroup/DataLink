package com.ucar.datalink.writer.rdbms.intercept;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.ddl.DdlSqlUtils;
import com.ucar.datalink.biz.utils.ddl.SQLStatementHolder;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.relationship.SqlType;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import com.ucar.datalink.writer.rdbms.utils.TableCheckUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.StatementCallback;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by lubiao on 2017/7/21.
 */
public class DdlEventInterceptor implements Interceptor<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(DdlEventInterceptor.class);

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        try {
            if (record.getEventType().isDdl()) {
                logger.info("receive a ddl record in DdlEventInterceptor.ddl sql is " + record.getSql());

                MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
                MediaInfo srcMedia = mappingInfo.getSourceMedia();

                //暂时只支持同构数据源之间的自动同步
                if (srcMedia.getMediaSource().getType() == MediaSourceType.MYSQL &&
                        !(
                                mappingInfo.getTargetMediaSource().getType() == MediaSourceType.SDDL ||
                                        mappingInfo.getTargetMediaSource().getType() == MediaSourceType.MYSQL
                        )) {
                    logger.info("ddl event is ignored,for the target media source type is not mysql or sddl, media-mapping-id is {}.", mappingInfo.getId());
                    return null;
                }

                //对于白名单和列别名的情况，暂时不做更细粒度的判断，直接忽略ddl语句
                if (mappingInfo.getColumnMappingMode() == ColumnMappingMode.INCLUDE &&
                        !mappingInfo.getColumnMappings().isEmpty()) {
                    logger.info("ddl event is ignored,for the ColumnMappingMode is INCLUDE,media-mapping-id is {}.", mappingInfo.getId());
                    return null;
                }

                //对于配置了拦截器的情况，不支持ddl自动同步
                if (mappingInfo.getInterceptorId() != null) {
                    logger.info("ddl event is ignored,for the interceptor id is not null,media-mapping-id is {}.", mappingInfo.getId());
                    return null;
                }

                //对于建表语句，如果不是全库同步，则直接忽略
                if (record.getEventType() == EventType.CREATE && !mappingInfo.getSourceMedia().getNameMode().getMode().isWildCard()) {
                    logger.info("ddl event is ignored,for the media-name-mode is not WildCard,media-mapping-id is {}.", mappingInfo.getId());
                    return null;
                }

                //进行sql解析
                List<SQLStatementHolder> holders = DdlSqlUtils.buildSQLStatement(mappingInfo.getSourceMedia().getMediaSource().getType(), record.getSql());
                if (holders.size() > 1) {
                    throw new ValidationException("The count of ddl slqs is more than one,please check,it may be a bug.");
                }
                SQLStatementHolder holder = holders.get(0);
                holder.check();

//            String sql = buildSql(record.getSql(), record, mappingInfo);
                String sql = record.getSql();
                if (SqlType.CreateTable.equals(holder.getSqlType()) && srcMedia.getNameMode().getMode().isWildCard()) {
                    execute(record, sql, mappingInfo, holder.getSqlType());
                } else if (SqlType.AlterTable.equals(holder.getSqlType())) {
                    Boolean isRowLimit = TableCheckUtils.isRowNumLimit(record, sql);
                    if (isRowLimit) {
                        logger.info("ddl event is ignored because row count in target table is over 5000000: media-mapping-id is {},sql is {}", mappingInfo.getId(), record.getSql());
                    } else {
                        execute(record, sql, mappingInfo, holder.getSqlType());
                    }
                } else {
                    logger.info("ddl event is ignored: media-mapping-id is {},sql is {}", mappingInfo.getId(), record.getSql());
                }

                return null;
            }
            return record;
        } catch (Throwable t) {
            logger.error(String.format("error occurred when do ddl sync.ddl sql is :%s", record.getSql()));
            return null;
        }
    }

    private void execute(RdbEventRecord record, String sql, MediaMappingInfo mappingInfo, SqlType sqlType) {
        MediaSourceInfo targetMediaSource = mappingInfo.getTargetMediaSource();

        if (targetMediaSource.getType() == MediaSourceType.SDDL) {
            if (sqlType == SqlType.CreateTable || sqlType == SqlType.AlterTable) {
                executeForSecondaryDbs(record, sql, mappingInfo);
                executeForPrimaryDbs(record, sql, mappingInfo);
            } else if (sqlType == SqlType.DropTable) {
                executeForPrimaryDbs(record, sql, mappingInfo);
                executeForSecondaryDbs(record, sql, mappingInfo);
            }
        } else {
            sql = buildSql(record.getSql(), record, mappingInfo, targetMediaSource, false);
            DbDialect dbDialect = DbDialectFactory.getDbDialect(targetMediaSource);
            executeSql(mappingInfo, dbDialect, sql);
        }
    }

    private String buildSql(String sql, RdbEventRecord record, MediaMappingInfo mediaMappingInfo, MediaSourceInfo targetMediaSource, Boolean isSddl) {
        sql = sql.toLowerCase();

        String srcSchemaName1 = record.getSchemaName().toLowerCase();
        String srcSchemaName2 = "`" + record.getSchemaName().toLowerCase() + "`";
        String srcTableName1 = record.getTableName().toLowerCase();
        String srcTableName2 = "`" + record.getTableName().toLowerCase() + "`";
        String srcFullTableName1 = srcSchemaName1 + "." + srcTableName1;
        String srcFullTableName2 = srcSchemaName2 + "." + srcTableName2;

        String targetSchemaName;
        if (isSddl) {
            targetSchemaName = "`" + targetMediaSource.getParameterObj().getNamespace() + "`";
        } else {
            targetSchemaName = "`" + mediaMappingInfo.getTargetMediaNamespace() + "`";
        }
        String targetTableName = "`" + (StringUtils.isNotBlank(mediaMappingInfo.getTargetMediaName()) ? mediaMappingInfo.getTargetMediaName() : srcTableName1) + "`";
        String targetFullTableName = targetSchemaName + "." + targetTableName;

        if (sql.contains(srcFullTableName2)) {//先2后1
            return StringUtils.replaceOnce(sql, srcFullTableName2, targetFullTableName);
        } else if (sql.contains(srcFullTableName1)) {
            return StringUtils.replaceOnce(sql, srcFullTableName1, targetFullTableName);
        } else if (sql.contains(srcTableName2)) {//先2后1
            return StringUtils.replaceOnce(sql, srcTableName2, targetFullTableName);
        } else if (sql.contains(srcTableName1)) {
            return StringUtils.replaceOnce(sql, srcTableName1, targetFullTableName);
        }

        return sql;
    }

    private void executeForPrimaryDbs(RdbEventRecord record, String sql, MediaMappingInfo mappingInfo) {
        SddlMediaSrcParameter mediaSrcParameter = mappingInfo.getTargetMediaSource().getParameterObj();
        mediaSrcParameter.getPrimaryDbsId().forEach(i -> {
            MediaSourceInfo subMediaSource = DataLinkFactory.getObject(MediaSourceService.class).getById(i);
            DbDialect dbDialect = DbDialectFactory.getDbDialect(subMediaSource);
            String targetSql = buildSql(sql, record, mappingInfo, subMediaSource, true);
            executeSql(mappingInfo, dbDialect, targetSql);
        });
    }

    private void executeForSecondaryDbs(RdbEventRecord record, String sql, MediaMappingInfo mappingInfo) {
        SddlMediaSrcParameter mediaSrcParameter = mappingInfo.getTargetMediaSource().getParameterObj();
        mediaSrcParameter.getSecondaryDbsId().forEach(i -> {
            MediaSourceInfo subMediaSource = DataLinkFactory.getObject(MediaSourceService.class).getById(i);
            DbDialect dbDialect = DbDialectFactory.getDbDialect(subMediaSource);
            String targetSql = buildSql(sql, record, mappingInfo, subMediaSource, true);
            executeSql(mappingInfo, dbDialect, targetSql);
        });
    }

    private void executeSql(MediaMappingInfo mappingInfo, DbDialect dbDialect, String sql) {
        try {
            Boolean result = dbDialect.getJdbcTemplate().execute(new StatementCallback<Boolean>() {

                public Boolean doInStatement(Statement stmt) throws SQLException, DataAccessException {
                    return stmt.execute(sql);
                }
            });
            logger.info("ddl is executed successfully: media-mapping-id is {}, sql is {}", mappingInfo.getId(), sql);
        } catch (Throwable t) {
            logger.error(String.format("skip exception for ddl: media-mapping-id is %s, sql is %s", mappingInfo.getId(), sql), t);
        }
    }
}
