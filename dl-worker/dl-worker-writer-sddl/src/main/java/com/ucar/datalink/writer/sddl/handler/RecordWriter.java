package com.ucar.datalink.writer.sddl.handler;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.dialect.SqlUtils;
import com.ucar.datalink.worker.api.util.statistic.RecordGroupLoadStatistic;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import com.ucar.datalink.writer.sddl.dataSource.DataSourceCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlJdbcTemplate;
import com.ucar.datalink.writer.sddl.manager.SddlRecordMeta;
import com.ucar.datalink.writer.sddl.model.SddlExcuteData;
import com.ucar.datalink.writer.sddl.util.statistic.WriterSddlStatistic;
import com.zuche.framework.sddl.datasource.SddlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.lob.LobCreator;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2018/10/17.
 */
public class RecordWriter {

    private static final Logger logger = LoggerFactory.getLogger(RecordWriter.class);

    public static void doWrite(SddlDataSource dataSource, List<RdbEventRecord> records, TaskWriterContext context) {

        //statistic before
        WriterSddlStatistic sddlStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        RecordGroupLoadStatistic loadStatistic = new RecordGroupLoadStatistic();
        sddlStatistic.getGroupLoadStatistics().put(Long.valueOf(dataSource.getClusterNum()), loadStatistic);
        loadStatistic.setMediaSourceId(Long.valueOf(dataSource.getClusterNum()));
        loadStatistic.setGroupRecordsCount(records.size());
        long startTime = System.currentTimeMillis();

        // doWrite
        if (records == null || records.size() == 0) {
            return;
        }

        LobCreator lobCreator = DataSourceCluster.defaultLobHandler.getLobCreator();
        int maxTry = context.getWriterParameter().getMaxRetryTimes();
        try {
            SddlJdbcTemplate sddlJdbcTemplate = DataSourceCluster.getSddlJdbcTemplate(dataSource.getDs());

            for (RdbEventRecord record : records) {
                SddlExcuteData excuteData = SddlRecordMeta.getExcuteSddlDs(record);
                String sql = excuteData.getPreparedSqlInfo().getSql();
                List<EventColumn> preparedColumns = excuteData.getPreparedSqlInfo().getPreparedColumns();

                doWriteRecord(sddlJdbcTemplate, sql, lobCreator, record, preparedColumns, maxTry);

                //statistic detail
                String tableName = record.getTableName();
                String id = record.getKeys().get(0).getColumnValue();
                Map<Object, Object> extendStatistic = loadStatistic.getExtendStatistic();
                if (extendStatistic.containsKey(tableName)) {
                    ((List) extendStatistic.get(tableName)).add(id);
                } else {
                    List<String> idList = new ArrayList();
                    idList.add(id);
                    extendStatistic.put(tableName, idList);
                }

            }
        } finally {
            lobCreator.close();
        }

        //load after
        long timeThrough = System.currentTimeMillis() - startTime;
        loadStatistic.setGroupLoadTime(timeThrough);
        if (timeThrough != 0L) {
            loadStatistic.setGroupTps(new BigDecimal(records.size() / (((double) timeThrough) / 1000)).longValue());
        }
    }

    private static void doWriteRecord(SddlJdbcTemplate sddlJdbcTemplate, String sql, LobCreator lobCreator,
                                      RdbEventRecord record, List<EventColumn> preparedColumns, int maxTry) {
        int retryCount = 0;
        boolean executed = false;

        while (!executed && retryCount < maxTry) {
            try {
                sddlJdbcTemplate.getJdbcTemplate().update(sql, ps -> {
                    fillPreparedStatement(ps, lobCreator, record, preparedColumns);
                });
                executed = true;
            } catch (Throwable e) {
                if (e instanceof DuplicateKeyException) {
                    logger.warn("sddl_write,忽略主键或唯一性约束冲突, sql:{}, paramColumns:{}, ExceptionInfo:{};",
                            sql, JSON.toJSONString(preparedColumns), e.getMessage());
                    return;
                }

                retryCount++;
                if (retryCount == maxTry) {
                    logger.error("sddl_write, doWriteRecord is error, retry {} times, sql:{}, paramColumns:{};",
                            retryCount, sql, JSON.toJSONString(preparedColumns));
                    throw new SddlRecordLoadException(record, e);
                }

            }
        }
    }


    private static void fillPreparedStatement(PreparedStatement ps, LobCreator lobCreator, RdbEventRecord record,
                                              List<EventColumn> columns) throws SQLException {

        for (int i = 0; i < columns.size(); i++) {
            int paramIndex = i + 1;
            EventColumn column = columns.get(i);

            int sqlType = column.getColumnType();
            Object param = null;
            if (sqlType == Types.TIME || sqlType == Types.TIMESTAMP || sqlType == Types.DATE) {
                // 解决mysql的0000-00-00 00:00:00问题，直接依赖mysql
                // driver进行处理，如果转化为Timestamp会出错
                param = column.getColumnValue();
            } else {
                param = SqlUtils.stringToSqlValue(column.getColumnValue(),
                        sqlType,
                        false,
                        false);
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
                        // 解决mysql的0000-00-00 00:00:00问题，直接依赖mysql
                        // driver进行处理，如果转化为Timestamp会出错
                        ps.setObject(paramIndex, param);

                        break;
                    case Types.BIT:
                        // 只处理mysql的bit类型，bit最多存储64位，所以需要使用BigInteger进行处理才能不丢精度
                        // mysql driver将bit按照setInt进行处理，会导致数据越界
                        StatementCreatorUtils.setParameterValue(ps, paramIndex, Types.DECIMAL, null, param);
                        break;
                    default:
                        StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                        break;
                }
            } catch (SQLException ex) {
                logger.error("SetParam error , [record={}, sqltype={}, value={}]",
                        JSON.toJSONString(record), sqlType, param);
                throw ex;
            }
        }

    }


    static class SddlRecordLoadException extends RuntimeException {
        private Record record;

        SddlRecordLoadException(Record record, Throwable cause) {
            super(cause);
            this.record = record;
        }

        public Record getRecord() {
            return record;
        }
    }
}
