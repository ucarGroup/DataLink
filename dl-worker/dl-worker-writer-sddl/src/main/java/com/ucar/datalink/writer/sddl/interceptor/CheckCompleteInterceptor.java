package com.ucar.datalink.writer.sddl.interceptor;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.sddl.dataSource.DataSourceCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlDataLinkCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlTaskInfo;
import com.ucar.datalink.writer.sddl.manager.checkData.SddlCheckData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: just do the data complete validation, do not update record;
 *
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 07/11/2017.
 */
public class CheckCompleteInterceptor implements Interceptor<RdbEventRecord> {
    private static final Logger LOG = LoggerFactory.getLogger(CheckCompleteInterceptor.class);
    private static final int hashSet_MaxCapacity = 100;

    private SddlTaskInfo sddlTaskInfo;
    private String taskId;

    public CheckCompleteInterceptor(SddlTaskInfo sddlTaskInfo) {
        this.sddlTaskInfo = sddlTaskInfo;
        this.taskId = sddlTaskInfo.getTaskInfo().getId().toString();
    }

    public static Map<String, CheckCompleteMode> sddlSyncDatas = new ConcurrentHashMap<>();

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        try {

            if (SddlCheckData.checkDataSwitch.get()) {
                LOG.info("#### CheckCompleteInterceptor<count:{}>, taskId:{}, start!", SddlCheckData.checkDataCount.get(), taskId);

                SddlDataLinkCluster sddlDataLinkCluster = DataSourceCluster.getSddlDs(sddlTaskInfo.getSddlMediaSourceInfo());

                String tableName   = record.getTableName();
                Map<String, String> accessoryShardingTableInfos = sddlDataLinkCluster.getAccessoryShardingTableInfos();  // T : <tableName, tableName.hashColumnName>;

                if (accessoryShardingTableInfos != null && accessoryShardingTableInfos.containsKey(tableName)) {
                    CheckCompleteMode checkCompleteMode;
                    if (sddlSyncDatas.containsKey(taskId)) {
                        checkCompleteMode = sddlSyncDatas.get(taskId);

                    } else {
                        checkCompleteMode = new CheckCompleteMode(sddlTaskInfo.getSddlMediaSourceInfo());
                        sddlSyncDatas.put(taskId, checkCompleteMode);
                    }


                    String  accessoryShardingColumn = accessoryShardingTableInfos.get(tableName).split("\\.")[1];
                    EventColumn updateColumn  = record.getColumn(accessoryShardingColumn);

                    EventColumn keyColumn     = record.getKeys().get(0);

                    Map<String, Map<String, HashSet<String>>> sddlSyncData = checkCompleteMode.getSddlSyncDataSet();
                    if (Objects.isNull(updateColumn) || StringUtils.isBlank(updateColumn.getColumnValue())) {
                        // do nothing
                    } else {
                        if (sddlSyncData.containsKey(tableName)) {
                            if (sddlSyncData.get(tableName).size() < hashSet_MaxCapacity) {

                                if (sddlSyncData.get(tableName).containsKey(updateColumn.getColumnValue())) {

                                    if (sddlSyncData.get(tableName).get(updateColumn.getColumnValue()).size() < hashSet_MaxCapacity) {

                                        sddlSyncData.get(tableName).get(updateColumn.getColumnValue()).add(keyColumn.getColumnValue());
                                    } else {
                                        LOG.info("CheckCompleteInterceptor`s the sddlSyncData({})-accessoryHashColumn({}) of sddlSyncDatas is full!", tableName, updateColumn.getColumnValue());
                                    }

                                } else {
                                    HashSet<String> accessoryId = new HashSet<>(hashSet_MaxCapacity);
                                    accessoryId.add(keyColumn.getColumnValue());
                                    sddlSyncData.get(tableName).put(updateColumn.getColumnValue(), accessoryId);
                                }

                            } else {
                                LOG.info("CheckCompleteInterceptor`s the sddlSyncData({}) of sddlSyncDatas is full!", tableName);
                            }
                        } else {
                            HashSet<String> accessoryId = new HashSet<>(hashSet_MaxCapacity);
                            accessoryId.add(keyColumn.getColumnValue());

                            Map<String, HashSet<String>> accessoryHashColumn = new HashMap<>(hashSet_MaxCapacity);
                            accessoryHashColumn.put(updateColumn.getColumnValue(), accessoryId);

                            sddlSyncData.put(tableName, accessoryHashColumn);
                        }

                        LOG.info("#### CheckCompleteInterceptor<count:{}>, taskId:{}, tableName:{}, endInfo:HashValue={},id={} !",
                                SddlCheckData.checkDataCount.get(), taskId, tableName, Objects.isNull(updateColumn)?"nullHash":updateColumn.getColumnValue(), keyColumn.getColumnValue());
                    }

                }
            } else {
                sddlSyncDatas.clear();
            }
        } catch (Exception e) {
            LOG.error("CheckCompleteInterceptor is error！", e);
        }

        return record;
    }

    public class CheckCompleteMode implements Serializable{
        MediaSourceInfo sddlMediaSourceInfo;

        // <tableName, Map<accessoryHashColumn, HashSet<id>>>, must set the max capacity of queue is 100;
        private Map<String, Map<String, HashSet<String>>> sddlSyncDataSet = new ConcurrentHashMap<>();

        public CheckCompleteMode(MediaSourceInfo sddlMediaSourceInfo) {
            this.sddlMediaSourceInfo = sddlMediaSourceInfo;
        }

        public MediaSourceInfo getSddlMediaSourceInfo() {
            return sddlMediaSourceInfo;
        }

        public void setSddlMediaSourceInfo(MediaSourceInfo sddlMediaSourceInfo) {
            this.sddlMediaSourceInfo = sddlMediaSourceInfo;
        }

        public Map<String, Map<String, HashSet<String>>> getSddlSyncDataSet() {
            return sddlSyncDataSet;
        }

        public void setSddlSyncDataSet(Map<String, Map<String, HashSet<String>>> sddlSyncDataSet) {
            this.sddlSyncDataSet = sddlSyncDataSet;
        }
    }

}
