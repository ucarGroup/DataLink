package com.ucar.datalink.writer.rdbms.extend;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import com.ucar.datalink.worker.api.util.dialect.SqlTemplate;
import com.ucar.datalink.worker.api.util.dialect.SqlUtils;
import com.ucar.datalink.worker.api.util.dialect.mysql.MysqlDialect;
import com.ucar.datalink.worker.api.util.dialect.sqlserver.SqlServerDialect;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.lob.LobCreator;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 出现主键冲突时改成自增插入
 * <p>
 * Created by djj on 2018/7/30.
 */
public class DuplicateKeyInterceptor implements Interceptor<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(DuplicateKeyInterceptor.class);

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        String tableName = record.getTableName();

        if ("t_scd_order_detail".equalsIgnoreCase(tableName) ||
                "t_scd_order_finance".equalsIgnoreCase(tableName) ||
                "t_scd_member_recharge_detail".equalsIgnoreCase(tableName)) {
            if (record.getEventType().equals(EventType.INSERT)) {
                doInsert(record);
                return null;
            } else if (record.getEventType().equals(EventType.UPDATE)) {
                doUpdate(record);
                return null;
            } else if (record.getEventType().equals(EventType.DELETE)) {
                doDelete(record);
                return null;
            }
        }

        return record;
    }

    private void doInsert(RdbEventRecord record) {
        //只处理insert
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        DbDialect dbDialect = DbDialectFactory.getDbDialect(mappingInfo.getTargetMediaSource());
        SqlTemplate sqlTemplate = dbDialect.getSqlTemplate();
        LobCreator lobCreator = dbDialect.getLobHandler().getLobCreator();

        try {
            //正常插入
            String sql = sqlTemplate.getInsertSql(mappingInfo.getTargetMediaNamespace(), mappingInfo.getTargetMediaName(),
                    buildColumnNames(record.getKeys()), buildColumnNames(record.getColumns()));
            //只是记录日志
            List<Object> args = new ArrayList<>();
            for (EventColumn column : record.getColumns()) {
                args.add(column.getColumnValue());
            }
            for (EventColumn column : record.getKeys()) {
                args.add(column.getColumnValue());
            }
            logger.debug("DuplicateKeyInterceptor，正常执行中,sql语句是：{}，参数是：{}", sql, args.toArray());

            dbDialect.getJdbcTemplate().update(sql, new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {
                    fillPreparedStatement(ps, record, lobCreator, dbDialect, buildInsertColumns(record, true));
                }
            });

            logger.debug("DuplicateKeyInterceptor，正常插入成功");
        } catch (DuplicateKeyException e) {
            try {
                //主键自曾插入
                String sql = sqlTemplate.getInsertSql(mappingInfo.getTargetMediaNamespace(), mappingInfo.getTargetMediaName(),
                        new String[]{}, buildColumnNames(record.getColumns()));
                //只是记录日志
                List<Object> args = new ArrayList<>();
                for (EventColumn column : record.getColumns()) {
                    args.add(column.getColumnValue());
                }
                logger.info("DuplicateKeyInterceptor，自增执行中,sql语句是：{}，参数是：{}", sql, args.toArray());

                dbDialect.getJdbcTemplate().update(sql, new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        fillPreparedStatement(ps, record, lobCreator, dbDialect, buildInsertColumns(record, false));
                    }
                });

                logger.info("DuplicateKeyInterceptor，自增插入成功");
            } catch (DuplicateKeyException de) {
                logger.info("自增插入仍然出现唯一性约束冲突，忽略，被忽略的数据为:TableName{},PrimaryKey{}", record.getTableName(), record.getKeys().get(0).getColumnValue());
            }
        } catch (Exception e) {
            logger.info("DuplicateKeyInterceptor出现未知异常,异常原因:{}", e);
            throw new RuntimeException("DuplicateKeyInterceptor出现未知", e);
        } finally {
            lobCreator.close();
        }
    }

    private void doUpdate(RdbEventRecord record) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        DbDialect dbDialect = DbDialectFactory.getDbDialect(mappingInfo.getTargetMediaSource());
        SqlTemplate sqlTemplate = dbDialect.getSqlTemplate();
        LobCreator lobCreator = dbDialect.getLobHandler().getLobCreator();

        try {
            EventColumn updateColumn = getColumnName4UpdateOrDelete(record);
            if (StringUtils.isBlank(updateColumn.getColumnValue())) {
                logger.info("Update Column value is null , skip update." + updateColumn);
                return;
            }

            String sql = sqlTemplate.getUpdateSql(
                    mappingInfo.getTargetMediaNamespace(),
                    mappingInfo.getTargetMediaName(),
                    new String[]{updateColumn.getColumnName()},
                    buildColumnNames(record.getUpdatedColumns()));
            dbDialect.getJdbcTemplate().update(sql, new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {
                    fillPreparedStatement(ps, record, lobCreator, dbDialect, buildUpdateColumns(record, updateColumn));
                }
            });
        } finally {
            lobCreator.close();
        }
    }

    private void doDelete(RdbEventRecord record) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        DbDialect dbDialect = DbDialectFactory.getDbDialect(mappingInfo.getTargetMediaSource());
        SqlTemplate sqlTemplate = dbDialect.getSqlTemplate();
        LobCreator lobCreator = dbDialect.getLobHandler().getLobCreator();

        try {
            EventColumn deleteColumn = getColumnName4UpdateOrDelete(record);
            if (StringUtils.isBlank(deleteColumn.getColumnValue())) {
                logger.info("Delete Column value is null , skip delete." + deleteColumn);
                return;
            }

            String sql = sqlTemplate.getDeleteSql(
                    mappingInfo.getTargetMediaNamespace(),
                    mappingInfo.getTargetMediaName(),
                    new String[]{deleteColumn.getColumnName()});
            dbDialect.getJdbcTemplate().update(sql, new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {
                    fillPreparedStatement(ps, record, lobCreator, dbDialect, buildDeleteColumns(record, deleteColumn));
                }
            });
        } finally {
            lobCreator.close();
        }
    }

    private EventColumn getColumnName4UpdateOrDelete(RdbEventRecord record) {
        String tableName = record.getTableName();
        String columnName = null;
        if ("t_scd_order_detail".equalsIgnoreCase(tableName)) {
            columnName = "order_id";
        }
        if ("t_scd_order_finance".equalsIgnoreCase(tableName)) {
            columnName = "order_id";
        }
        if ("t_scd_member_recharge_detail".equalsIgnoreCase(tableName)) {
            columnName = "relate_bill_id";
        }

        return record.getColumn(columnName);
    }

    private String[] buildColumnNames(List<EventColumn> columns) {
        String[] result = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            EventColumn column = columns.get(i);
            result[i] = column.getColumnName();
        }
        return result;
    }

    private List<EventColumn> buildInsertColumns(RdbEventRecord record, Boolean isNeedPrimaryKey) {
        List<EventColumn> columns = new ArrayList<EventColumn>();
        columns.addAll(record.getColumns());
        if (isNeedPrimaryKey) {
            columns.addAll(record.getKeys());
        }
        return columns;
    }

    private List<EventColumn> buildDeleteColumns(RdbEventRecord record, EventColumn deleteKey) {
        List<EventColumn> columns = new ArrayList<EventColumn>();
        columns.add(deleteKey);
        return columns;
    }

    private List<EventColumn> buildUpdateColumns(RdbEventRecord record, EventColumn updateKey) {
        List<EventColumn> columns = new ArrayList<EventColumn>();
        columns.addAll(record.getUpdatedColumns());// 只更新带有isUpdate=true的字段
        columns.add(updateKey);
        return columns;
    }

    //填充PreparedStatement
    private void fillPreparedStatement(PreparedStatement ps, RdbEventRecord record, LobCreator lobCreator,
                                       DbDialect dbDialect, List<EventColumn> columns) throws SQLException {
        //获取一下当前字段名的数据是否必填
        Map<String, Boolean> isRequiredMap = new HashMap<String, Boolean>();
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        Table table = dbDialect.findTable(mappingInfo.getTargetMediaNamespace(), mappingInfo.getTargetMediaName());
        for (Column tableColumn : table.getColumns()) {
            isRequiredMap.put(StringUtils.lowerCase(tableColumn.getName()), tableColumn.isRequired());
        }

        //给字段赋值
        for (int i = 0; i < columns.size(); i++) {
            int paramIndex = i + 1;
            EventColumn column = columns.get(i);
            Boolean isRequired = isRequiredMap.get(StringUtils.lowerCase(column.getColumnName()));

            int sqlType = column.getColumnType();//djj
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
                        lobCreator.setBlobAsBytes(ps, paramIndex, (byte[]) param);
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
                logger.info("SetParam error , [mappingId={}, sqltype={}, value={}]",
                        RecordMeta.mediaMapping(record).getId(), sqlType, param);
                throw ex;
            }
        }
    }

}
