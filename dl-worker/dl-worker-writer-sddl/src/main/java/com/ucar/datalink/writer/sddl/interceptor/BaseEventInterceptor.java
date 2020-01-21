package com.ucar.datalink.writer.sddl.interceptor;

import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.sddl.dataSource.DataSourceCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlDataLinkCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlTaskInfo;
import com.ucar.datalink.writer.sddl.manager.SddlTableManager;
import org.apache.commons.collections.CollectionUtils;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 07/11/2017.
 */
public class BaseEventInterceptor implements Interceptor<RdbEventRecord> {

    private SddlTaskInfo sddlTaskInfo;

    public BaseEventInterceptor(SddlTaskInfo sddlTaskInfo) {
        this.sddlTaskInfo = sddlTaskInfo;
    }

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        // ⚠️ sddl表，非分表的话，表名不应以"_数字"结尾，如：_0031

        if (record.getEventType().isDdl())
            return null; // 跳过ddl

        SddlDataLinkCluster sddlDataLinkCluster = DataSourceCluster.getSddlDs(sddlTaskInfo.getSddlMediaSourceInfo());

        // 1.如果主维度分表了的话，tableName去除后缀
        String tableName   = record.getTableName();
        boolean isMainShardingTable = CollectionUtils.isNotEmpty(sddlDataLinkCluster.getMainShardingTableSuffixs());
        if (isMainShardingTable) {
            record.setTableName(SddlTableManager.getTableNameOfRmSuffix(tableName));
        }

        // DDL功能以来DBA的DBMS系统执行，此功能废弃！
        /*if (record.getEventType().isDdl())
            return null; // 暂时延迟侧功能，跳过ddl
        // 2.
        if (record.getEventType().isDdl() && sddlTaskInfo.isProxyDb()) {
            boolean isAccessoryShardingTable = CollectionUtils.isNotEmpty(sddlDataLinkCluster.getAccessoryShardingTableSuffixs());

            List<SddlDataSource> mainSddlDataSources        = Lists.newArrayList();
            List<SddlDataSource> accessorySddlDataSources   = Lists.newArrayList();
            List<SddlCluster> sddlClusters = DataSourceCluster.getSddlDs(sddlTaskInfo.getSddlMediaSourceInfo()).getSddlLogicCluster().getSddlClusters();
            SddlCluster sddlClusterMain = sddlClusters.get(0);
            for (Map.Entry entry : sddlClusterMain.getMapDataSource().entrySet()) {
                if (entry.getKey().equals(0))
                    continue;

                mainSddlDataSources.add((SddlDataSource) entry.getValue());
            }
            SddlCluster sddlClusterAccess = sddlClusters.get(1);
            for (Map.Entry entry : sddlClusterAccess.getMapDataSource().entrySet()) {
                accessorySddlDataSources.add((SddlDataSource) entry.getValue());
            }

            // 一.sharding表：在所有非proxy库里执行，且如果是分表的话，则需要添加对应数量的带有后缀的分表ddl
            if (isMainShardingTable && isAccessoryShardingTable) {
                loadMainDdlWriter(true, mainSddlDataSources);
                loadRedundancyDdlWriter(true, accessorySddlDataSources);
            } else if (isMainShardingTable) {
                loadMainDdlWriter(true, mainSddlDataSources);
            } else if (isAccessoryShardingTable) {
                loadRedundancyDdlWriter(true, accessorySddlDataSources);
            } else {
                loadMainDdlWriter(false, mainSddlDataSources);
                loadRedundancyDdlWriter(false, accessorySddlDataSources);
            }

            // 二.字典表：如果是ddl，则在所有非proxy库里执行

        }*/


        return record;
    }

}
