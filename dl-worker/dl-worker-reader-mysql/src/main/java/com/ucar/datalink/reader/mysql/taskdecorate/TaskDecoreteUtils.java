package com.ucar.datalink.reader.mysql.taskdecorate;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.biz.service.TaskDecorateDetailService;
import com.ucar.datalink.biz.service.TaskDecorateService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.DataSourceFactory;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.decorate.TaskDecorate;
import com.ucar.datalink.domain.decorate.TaskDecorateDetail;
import com.ucar.datalink.domain.decorate.TaskDecorateStatus;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.worker.api.task.TaskReaderContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author xy.li
 * @date 2019/06/03
 */
public class TaskDecoreteUtils {

    private static final Logger logger = LoggerFactory.getLogger(TaskDecoreteUtils.class);


    /**
     * 获取要补录的数据
     */
    public static List<RdbEventRecord> getRecord(TaskReaderContext context) throws Exception {

        List<RdbEventRecord> records = new ArrayList<>();
        //取出多个需要执行的，分别查询数据，如果出现异常，将不同的异常添加到不同的记录，修改记录状态为失败，如果没有异常，将查询出来的数据主键添加到对应字段，修改状态为“补录中”
        long taskId = Long.parseLong(context.taskId());
        TaskDecorateDetailService taskDecorateDetailService = DataLinkFactory.getObject(TaskDecorateDetailService.class);
        List<TaskDecorateDetail> taskDecorateDetails = taskDecorateDetailService.queryBytaskIdAndStatus(taskId, TaskDecorateStatus.NEW_CREATED.getCode());
        if (taskDecorateDetails == null || taskDecorateDetails.size() == 0) {
            return records;
        }
        List<String> executeSql = getSQL(taskDecorateDetails);
        DataSource dataSource = getDataSource(taskId);
        ResultSet resultSet = null;
        Connection connection = null;

        Map<String, Set<String>> decorateDetailId2PrimaryKeys = new HashMap<>();

        String sql = "";
        int index = -1;
        try {

            String schameName = getSchameName(taskId);
            connection = dataSource.getConnection();
            for (int i = 0; i < executeSql.size(); i++) {
                index = i;
                sql = executeSql.get(i);
                logger.info(String.format("execute sql [%s]",sql));
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                resultSet = preparedStatement.executeQuery();
                ReusltTableSchema metaRdbEventRecord = getMetaRdbEventRecord(resultSet);
                metaRdbEventRecord.setSchema(schameName);
                logger.info(String.format("sql metadata [%s]",metaRdbEventRecord.toString()));
                List<RdbEventRecord> sqlRecords = getRecordsByResultSet(resultSet, metaRdbEventRecord);
                logger.info(String.format("records object [%s]", ArrayUtils.toString(sqlRecords)));
                close(resultSet);
                records.addAll(sqlRecords);
                Set<String> primaryKeys = getPrimaryKeys(sqlRecords);
                long id = taskDecorateDetails.get(i).getId();
                decorateDetailId2PrimaryKeys.put(String.valueOf(id), primaryKeys);
            }

        } catch (Exception e) {
            logger.error(String.format("execute sql[%s] error", sql), e);
            String log = String.format("execute sql[%s] error", sql) + e.getMessage();
            saveError(taskDecorateDetails.get(index).getId(), log);
        } finally {
            close(resultSet);
            close(connection);
        }
        savePrimaryIdsAndStatus(decorateDetailId2PrimaryKeys);
        return records;
    }


    private static String getSchameName(long taskId){
        TaskConfigService taskConfigService = DataLinkFactory.getObject(TaskConfigService.class);
        TaskInfo task = taskConfigService.getTask(taskId);
        if(task == null){
            throw new IllegalArgumentException(String.format("not find TaskInfo by taskId[%d]",taskId));
        }
        long mediaSourceId = task.getTaskReaderParameterObj().getMediaSourceId();
        MediaSourceService mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        MediaSourceInfo msi = mediaSourceService.getById(mediaSourceId);
        if(msi == null){
            throw new IllegalArgumentException(String.format("not find MediaSourceInfo by mediaSourceId[%d]",mediaSourceId));
        }

        String namespace = msi.getParameterObj().getNamespace();
        if(StringUtils.isBlank(namespace)){
            RdbMediaSrcParameter parameterObj = (RdbMediaSrcParameter) msi.getParameterObj();
            namespace = parameterObj.getName();
        }
        return namespace;
    }

    private static void saveError(Long decorateId, String errorLog) {
        try {
            TaskDecorateDetailService taskDecorateDetailService = DataLinkFactory.getObject(TaskDecorateDetailService.class);
            TaskDecorateDetail tdd = taskDecorateDetailService.findById(decorateId);
            String log = tdd.getExecutedLog() == null ? "" : tdd.getExecutedLog() + errorLog;
            tdd.setExecutedLog(log);
            tdd.setStatus(TaskDecorateStatus.FAILED.getCode());
            taskDecorateDetailService.update(tdd);
        } catch (Exception e) {
            logger.error("保存失败。", e);
        }
    }


    /**
     * 保存查询数据记录到表中，将状态置为TaskDecorateStatus.RUNNING
     *
     * @param decorateDetailId2PrimaryKeys
     */
    private static void savePrimaryIdsAndStatus(Map<String, Set<String>> decorateDetailId2PrimaryKeys) {
        try {
            TaskDecorateDetailService taskDecorateDetailService = DataLinkFactory.getObject(TaskDecorateDetailService.class);
            for (String key : decorateDetailId2PrimaryKeys.keySet()) {
                TaskDecorateDetail tdd = taskDecorateDetailService.findById(Long.parseLong(key));
                String log = tdd.getExecutedLog() == null ? "" : tdd.getExecutedLog() + ArrayUtils.toString(decorateDetailId2PrimaryKeys.get(key));
                tdd.setExecutedLog(log);
                tdd.setStatus(TaskDecorateStatus.RUNNING.getCode());
                taskDecorateDetailService.update(tdd);
            }
        } catch (Exception e) {
            logger.error("保存失败。", e);
        }
    }


    private static void close(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
            }
        }
    }

    private static Set<String> getPrimaryKeys(List<RdbEventRecord> records) {
        Set<String> ids = new HashSet<>();
        for (RdbEventRecord er : records) {
            for (EventColumn ec : er.getKeys()) {
                if ("id".equalsIgnoreCase(ec.getColumnName())) {
                    ids.add(ec.getColumnValue());
                }
            }
        }
        return ids;
    }


    private static List<RdbEventRecord> getRecordsByResultSet(ResultSet resultSet, ReusltTableSchema schame) throws Exception {
        List<RdbEventRecord> records = new ArrayList();
        int columnSize = schame.getColumnSize();
        Map<String, EventColumn> columnMetas = schame.getColumns();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        while (resultSet.next()) {
            RdbEventRecord rdbEventRecord = new RdbEventRecord();

            List<EventColumn> keys = new ArrayList<>();
            List<EventColumn> columns = new ArrayList<>();
            for (int i = 1; i <= columnSize; i++) {
                Object object = resultSet.getObject(i);
                if (object == null) {//TODO  没有数据留意是否需要创建对象
                    continue;
                }
                String value = String.valueOf(object);
                int columnType = columnMetas.get(String.valueOf(i)).getColumnType();
                if (columnType == Types.TIMESTAMP) {//TODO 时间类型，datatime 和 timestamp 都是93
                    value = String.valueOf(simpleDateFormat.parse(value).getTime()); //TODO  13位数字
                }
                EventColumn eventColumnMeta = columnMetas.get(String.valueOf(i));
                EventColumn eventColumn = new EventColumn();
                eventColumn.setColumnType(eventColumnMeta.getColumnType());
                eventColumn.setColumnName(eventColumnMeta.getColumnName());
                eventColumn.setColumnValue(value);

                if (eventColumnMeta.getColumnName().equalsIgnoreCase("id")) {
                    eventColumn.setKey(true);
                    keys.add(eventColumn);
                    continue;
                }
                columns.add(eventColumn);
            }
            rdbEventRecord.setKeys(keys);
            rdbEventRecord.setColumns(columns);
            rdbEventRecord.setEventType(EventType.INSERT);
            rdbEventRecord.setSchemaName(schame.getSchema());
            rdbEventRecord.setTableName(schame.getTableName());
            rdbEventRecord.setExecuteTime(System.currentTimeMillis());
            records.add(rdbEventRecord);
        }
        return records;
    }


    /**
     * 根据resultSet获取元数据
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private static ReusltTableSchema getMetaRdbEventRecord(ResultSet resultSet) throws SQLException {
        ReusltTableSchema reusltTableSchame = new ReusltTableSchema();
        ResultSetMetaData metaData = resultSet.getMetaData();
        String tableName = metaData.getTableName(1);
        int columnCount = metaData.getColumnCount();
        reusltTableSchame.setTableName(tableName);
        reusltTableSchame.setColumnSize(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            int columnType = metaData.getColumnType(i);
            EventColumn eventColumn = new EventColumn();
            eventColumn.setColumnName(columnName);
            eventColumn.setColumnType(columnType);
            reusltTableSchame.putColumn(String.valueOf(i), eventColumn);
        }
        return reusltTableSchame;
    }


    private static List<String> getSQL(List<TaskDecorateDetail> taskDecorateDetails) {
        List<String> sqls = new ArrayList<>();
        if (taskDecorateDetails == null) {
            return sqls;
        }
        TaskDecorateService taskDecorateService = DataLinkFactory.getObject(TaskDecorateService.class);

        for (TaskDecorateDetail tdd : taskDecorateDetails) {
            TaskDecorate taskDecorate = taskDecorateService.getById(tdd.getDecorateId());
            if (taskDecorate == null) {
                throw new IllegalArgumentException(String.format("无法根据id[%d]获取对象TaskDecorate", tdd.getDecorateId()));
            }
            String sql = taskDecorate.getSql();
            sqls.add(sql);
        }
        return sqls;
    }


    private static DataSource getDataSource(long taskId) {
        TaskConfigService taskConfigService = DataLinkFactory.getObject(TaskConfigService.class);
        TaskInfo task = taskConfigService.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException(String.format("taskId[%d] 无法查询出对应Task信息！", taskId));
        }
        Long readerMediaSourceId = task.getReaderMediaSourceId();
        MediaSourceService mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(readerMediaSourceId);
        if (mediaSourceInfo == null) {
            throw new IllegalArgumentException(String.format("taskId[%d] readerMediaSourceId[%d]无法查询出对应MediaSourceInfo信息！", taskId, readerMediaSourceId));
        }
        return DataSourceFactory.getDataSource(mediaSourceInfo);
    }


    /**
     * 这个方法被commit和rollback调用,添加执行日志
     *只修改和添加task中为“补录中状态数据”，防止新增加补录数据被修改，这个方法被commit和rollback调用
     * @param context     主要为获取taskId
     * @param fail        执行状态
     * @param executedLog 提示消息
     */
    public static void saveExecuteLog(TaskReaderContext context, boolean fail, String executedLog) {
        try {
            long taskId = Long.parseLong(context.taskId());
            TaskDecorateDetailService taskDecorateDetailService = DataLinkFactory.getObject(TaskDecorateDetailService.class);
            List<TaskDecorateDetail> taskDecorateDetails = taskDecorateDetailService.queryBytaskIdAndStatus(taskId, TaskDecorateStatus.RUNNING.getCode());
            for (TaskDecorateDetail taskDecorateDetail : taskDecorateDetails) {
                StringBuilder newExecutedLog = new StringBuilder();
                newExecutedLog.append(taskDecorateDetail.getExecutedLog());
                newExecutedLog.append("\t\n");
                newExecutedLog.append(executedLog);
                taskDecorateDetail.setExecutedLog(newExecutedLog.toString());
                taskDecorateDetail.setStatus(fail ? TaskDecorateStatus.FAILED.getCode() : TaskDecorateStatus.SUCESSED.getCode());
                taskDecorateDetailService.update(taskDecorateDetail);
            }
        } catch (Exception e) {
            logger.error("保存失败。", e);
        }
    }


}


class ReusltTableSchema {
    private String tableName;
    private String schema;
    private int columnSize;
    private Map<String, EventColumn> columns;

    public ReusltTableSchema() {
        this.columns = new HashMap();
    }

    public void putColumn(String key, EventColumn eventColumn) {
        this.columns.put(key, eventColumn);
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public Map<String, EventColumn> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, EventColumn> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }

}
