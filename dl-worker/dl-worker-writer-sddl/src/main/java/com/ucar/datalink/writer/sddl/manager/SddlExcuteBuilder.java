package com.ucar.datalink.writer.sddl.manager;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.worker.api.util.copy.RecordCopier;
import com.ucar.datalink.writer.sddl.dataSource.DataSourceCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlJdbcTemplate;
import com.ucar.datalink.writer.sddl.exception.SddlSqlException;
import com.ucar.datalink.writer.sddl.manager.generatesql.MysqlAddSQLGenerator;
import com.ucar.datalink.writer.sddl.manager.generatesql.MysqlDeleteSQLGenerator;
import com.ucar.datalink.writer.sddl.manager.generatesql.MysqlUpdateSQLGenerator;
import com.ucar.datalink.writer.sddl.manager.generatesql.SQLGenerator;
import com.ucar.datalink.writer.sddl.model.SddlExcuteData;
import com.ucar.datalink.writer.sddl.model.ShardingColumnInfo;
import com.zuche.framework.sddl.datasource.SddlCluster;
import com.zuche.framework.sddl.datasource.SddlDataSource;
import com.zuche.framework.sddl.util.DBUniqueCodeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 15/11/2017.
 */
public class SddlExcuteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(SddlExcuteBuilder.class);

    public static void buildShardingSql(RdbEventRecord record, ShardingColumnInfo shardingColumnInfo, SddlCluster sddlCluster) {
        EventType type = record.getEventType();
        String tableName = record.getTableName();

        if (sddlCluster == null) {
            throw new SddlSqlException("sddl_writer执行异常：RecordLoader.buildShardingSql获取附属纬度的ds为空, record:" + JSON.toJSONString(record));
        }

        String shardingClumnName = shardingColumnInfo.getShardingClumnName();
        EventColumn oldColumn = record.getOldColumn(shardingClumnName);
        EventColumn updateColumn = record.getColumn(shardingClumnName);

        List<SddlExcuteData> sddlExcuteDatas = new ArrayList<>();
        if (type == EventType.INSERT) {
            if (Objects.isNull(updateColumn) || StringUtils.isBlank(updateColumn.getColumnValue())) {
                LOG.info("insert without sharding id! table:{}, keys:{}", tableName, JSON.toJSONString(record.getKeys()));
            } else {
                SddlExcuteData sddlExcuteData = SddlExcuteBuilder.getSddlExcuteData(tableName, record, sddlCluster,
                        updateColumn, type);

                sddlExcuteDatas.add(sddlExcuteData);
                SddlRecordMeta.appendSddlDs(record, sddlExcuteDatas);
            }

        } else if (type == EventType.DELETE) {
            if (Objects.isNull(updateColumn) || StringUtils.isBlank(updateColumn.getColumnValue())) {
                LOG.info("delete without sharding id! table:{}, record:{}", tableName, JSON.toJSONString(record));
            } else {
                SddlExcuteData sddlExcuteData = SddlExcuteBuilder.getSddlExcuteData(tableName, record, sddlCluster,
                        updateColumn, type);

                sddlExcuteDatas.add(sddlExcuteData);
                SddlRecordMeta.appendSddlDs(record, sddlExcuteDatas);
            }
        } else if (type == EventType.UPDATE) {
            if (updateColumn.isUpdate()) {
                if (StringUtils.isEmpty(oldColumn.getColumnValue())) {
                    if (StringUtils.isEmpty(updateColumn.getColumnValue())) { // 这种情况理论上不会出现，如果都为null，则isUpdate就是false了；
                        LOG.error("update（isUpdate） without old/update sharding id! table:{}, record:{}", tableName, JSON.toJSONString(record));
                    } else {
                        SddlExcuteData sddlExcuteData = SddlExcuteBuilder.getSddlExcuteData(tableName, record, sddlCluster,
                                updateColumn, EventType.INSERT);

                        sddlExcuteDatas.add(sddlExcuteData);
                        SddlRecordMeta.appendSddlDs(record, sddlExcuteDatas);
                    }
                } else {

                    SddlExcuteData sddlDelExcuteData = SddlExcuteBuilder.getSddlExcuteData(tableName, record, sddlCluster,
                            oldColumn, EventType.DELETE);
                    sddlExcuteDatas.add(sddlDelExcuteData);

                    if (StringUtils.isNotEmpty(updateColumn.getColumnValue())) {
                        SddlExcuteData sddlAddExcuteData = SddlExcuteBuilder.getSddlExcuteData(tableName, record, sddlCluster,
                                updateColumn, EventType.INSERT);
                        sddlExcuteDatas.add(sddlAddExcuteData);
                    }

                    SddlRecordMeta.appendSddlDs(record, sddlExcuteDatas);
                }

            } else {
                if (oldColumn.getColumnValue() == null && updateColumn.getColumnValue() == null) {
                    LOG.info("update without old/update sharding id! table:{}, keys:{}", tableName, JSON.toJSONString(record.getKeys()));
                } else {
                    SddlExcuteData sddlExcuteData = SddlExcuteBuilder.getSddlExcuteData(tableName, record, sddlCluster,
                            updateColumn, EventType.UPDATE);

                    sddlExcuteDatas.add(sddlExcuteData);
                    SddlRecordMeta.appendSddlDs(record, sddlExcuteDatas);
                }
            }
        }
    }

    public static void buildMainRedundancySql(RdbEventRecord record, MediaSourceInfo sddlMediaSourceInfo, RedundancySqlMode redundancySqlMode) {

        List<SddlExcuteData> sddlExcuteDatas = SddlExcuteBuilder.getMainRedundancyExcuteData(record, sddlMediaSourceInfo, redundancySqlMode);

        SddlRecordMeta.appendSddlDs(record, sddlExcuteDatas);

    }

    public static void buildAccessoryRedundancySql(RdbEventRecord record, MediaSourceInfo sddlMediaSourceInfo) {

        List<SddlExcuteData> sddlExcuteDatas = SddlExcuteBuilder.getAccessoryRedundancyExcuteData(record, sddlMediaSourceInfo);

        SddlRecordMeta.appendSddlDs(record, sddlExcuteDatas);

    }

    public static SddlExcuteData getSddlExcuteData(String tableName, RdbEventRecord record, SddlCluster sddlCluster,
                                                   EventColumn shardingCulomn, EventType eventType) {
        // datasource
        SddlExcuteData sddlExcuteData = new SddlExcuteData(sddlCluster.getClusterName(), eventType);

        String shardingColumnValue = shardingCulomn.getColumnValue();

        int shardingNo = DBUniqueCodeUtils.createShardingNum(shardingColumnValue);

        sddlExcuteData.setSddlDs(sddlCluster.getDataSource(shardingNo));

        String shardTableSuffix = SddlTableManager.getShardingTableSuffix(sddlCluster, shardingNo);
        if (shardTableSuffix != null)
            sddlExcuteData.setTableName(tableName + "_" + shardTableSuffix);
        else
            sddlExcuteData.setTableName(tableName);

        // sql
        buildSql(sddlExcuteData, record, tableName);

        return sddlExcuteData;
    }

    public static List<SddlExcuteData> getMainRedundancyExcuteData(RdbEventRecord record, MediaSourceInfo sddlMediaSourceInfo, RedundancySqlMode redundancySqlMode) {
        // datasource
        EventType eventType = record.getEventType();
        String tableName = record.getTableName();
        List<SddlCluster> sddlClusters = DataSourceCluster.getSddlDs(sddlMediaSourceInfo).getSddlLogicCluster().getSddlClusters();

        List<SddlExcuteData> results = new ArrayList<>();
        // 添加主维度冗余表配置(是冗余表的话，主维度肯定包含)
        SddlCluster sddlClusterMain = sddlClusters.get(0);
        for (Map.Entry entry : sddlClusterMain.getMapDataSource().entrySet()) {
            if (redundancySqlMode == RedundancySqlMode.PROXY && !entry.getKey().equals(0))
                continue;
            if (redundancySqlMode == RedundancySqlMode.BROTHER && entry.getKey().equals(0))
                continue;

            SddlExcuteData sddlExcuteData = new SddlExcuteData(sddlClusterMain.getClusterName(), (SddlDataSource) entry.getValue(),
                    tableName, eventType);

            results.add(sddlExcuteData);
        }

        if (CollectionUtils.isEmpty(results)) {
            return null;
        }

        // sql
        for (SddlExcuteData sddlExcuteData : results) {
            buildSql(sddlExcuteData, record, tableName);
        }

        return results;
    }

    public static List<SddlExcuteData> getAccessoryRedundancyExcuteData(RdbEventRecord record, MediaSourceInfo sddlMediaSourceInfo) {
        EventType eventType = record.getEventType();
        String tableName = record.getTableName();
        List<SddlCluster> sddlClusters = DataSourceCluster.getSddlDs(sddlMediaSourceInfo).getSddlLogicCluster().getSddlClusters();

        List<SddlExcuteData> results = new ArrayList<>();
        // 添加附属维度冗余表配置
        SddlCluster sddlClusterAccess = sddlClusters.get(1);
        for (Map.Entry entry : sddlClusterAccess.getMapDataSource().entrySet()) {
            SddlExcuteData sddlExcuteData = new SddlExcuteData(sddlClusterAccess.getClusterName(), (SddlDataSource) entry.getValue(),
                    tableName, eventType);

            results.add(sddlExcuteData);
        }

        if (CollectionUtils.isEmpty(results)) {
            return null;
        }

        // sql
        for (SddlExcuteData sddlExcuteData : results) {
            buildSql(sddlExcuteData, record, tableName);
        }

        return results;
    }

    public static Map<SddlDataSource, List<RdbEventRecord>> groupRecords(List<RdbEventRecord> records) {
        Map<SddlDataSource, List<RdbEventRecord>> groupResults = new HashMap<>();
        for (RdbEventRecord rdbEventRecord : records) {
            List<SddlExcuteData> sddlExcuteDatas = SddlRecordMeta.getSddlDs(rdbEventRecord);

            if (CollectionUtils.isEmpty(sddlExcuteDatas)) {
                LOG.debug("sddl_writer,when groupRecord, datasource is null, record:{}", JSON.toJSONString(rdbEventRecord));
                continue;
            }

            SddlRecordMeta.removeSddlDs(rdbEventRecord);
            if (sddlExcuteDatas.size() == 1) {
                SddlExcuteData sddlExcuteData = sddlExcuteDatas.get(0);

                putGroupResults(groupResults, sddlExcuteData, rdbEventRecord);

            } else {
                sddlExcuteDatas.stream().forEach(p -> {
                    RdbEventRecord copyRecord = RecordCopier.copy(rdbEventRecord);

                    putGroupResults(groupResults, p, copyRecord);
                });
            }
        }

        return groupResults;
    }

    private static void putGroupResults(Map<SddlDataSource, List<RdbEventRecord>> groupResults,
                                        SddlExcuteData sddlExcuteData,
                                        RdbEventRecord rdbEventRecord) {
        SddlDataSource sddlDataSource = sddlExcuteData.getSddlDs();

        List<RdbEventRecord> rdbEventRecords = groupResults.get(sddlDataSource);
        SddlRecordMeta.attachExcuteSddlDs(rdbEventRecord, sddlExcuteData);
        if (null == rdbEventRecords) {
            rdbEventRecords = new ArrayList<>();

            rdbEventRecords.add(rdbEventRecord);
            groupResults.put(sddlDataSource, rdbEventRecords);
        } else {

            rdbEventRecords.add(rdbEventRecord);
        }


    }

    private static void buildSql(SddlExcuteData sddlExcuteData, RdbEventRecord record, String tableName) {
        EventType eventType = sddlExcuteData.getEventType();

        SQLGenerator sqlGenerator = null;
        if (eventType == EventType.INSERT) {
            sqlGenerator = MysqlAddSQLGenerator.getInstance();
        } else if (eventType == EventType.UPDATE) {
            sqlGenerator = MysqlUpdateSQLGenerator.getInstance();
        } else if (eventType == EventType.DELETE) {
            sqlGenerator = MysqlDeleteSQLGenerator.getInstance();
        } else {
            LOG.error("sddl_writer do not build build type<{}> sql, record = {}", eventType, JSON.toJSONString(record));
            throw new SddlSqlException("sddl_writer do not build type<" + eventType + "> sql, record = " + JSON.toJSONString(record));
        }

        SddlJdbcTemplate sddlJdbcTemplate = DataSourceCluster.getSddlJdbcTemplate(sddlExcuteData.getSddlDs().getDs());
        String fullName = getFullName(sddlJdbcTemplate.getSchemaName(), tableName);

        SddlExcuteData.PreparedSqlInfo preparedSqlInfo = sddlExcuteData.new PreparedSqlInfo();
        sqlGenerator.generate(record, fullName, preparedSqlInfo);
        sddlExcuteData.setPreparedSqlInfo(preparedSqlInfo);

    }

    private static String getFullName(String schemaName, String tableName) {
        StringBuilder sb = new StringBuilder();
        if (schemaName != null) {
            sb.append(schemaName).append("`.`");
        }
        sb.append(tableName);
        return sb.toString();
    }

    public static enum RedundancySqlMode {
        PROXY, BROTHER
    }
}
