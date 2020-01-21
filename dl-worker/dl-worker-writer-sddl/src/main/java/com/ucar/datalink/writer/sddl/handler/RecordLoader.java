package com.ucar.datalink.writer.sddl.handler;

import com.ucar.datalink.common.errors.DataLoadException;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.writer.sddl.SddlWriterParameter;
import com.ucar.datalink.worker.api.probe.ProbeManager;
import com.ucar.datalink.worker.api.probe.index.TaskExceptionProbeIndex;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import com.ucar.datalink.writer.sddl.dataSource.DataSourceCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlDataLinkCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlTaskInfo;
import com.ucar.datalink.writer.sddl.exception.SddlSqlException;
import com.ucar.datalink.writer.sddl.manager.SddlExcuteBuilder;
import com.ucar.datalink.writer.sddl.manager.SddlTableManager;
import com.ucar.datalink.writer.sddl.model.ShardingColumnInfo;
import com.ucar.datalink.writer.sddl.util.statistic.WriterSddlStatistic;
import com.zuche.framework.sddl.datasource.SddlDataSource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 14/11/2017.
 */
public class RecordLoader {
    private static final Logger LOG = LoggerFactory.getLogger(RecordLoader.class);

    public void submitData(RecordChunk recordChunk, TaskWriterContext context, SddlTaskInfo sddlTaskInfo, ExecutorService executorService) {
        // 二期：build sql (group:可以考虑按"库+表"group来提升性能)
        Map<SddlDataSource, List<RdbEventRecord>> records = buildSql(recordChunk, context, sddlTaskInfo, executorService);

        // write data by ds
        List<Future> futures = new ArrayList<>();
        ExecutorCompletionService completionService = new ExecutorCompletionService(executorService);

        for (Map.Entry<SddlDataSource, List<RdbEventRecord>> entry : records.entrySet()) {
            futures.add(completionService.submit(() -> {
                RecordWriter.doWrite(entry.getKey(), entry.getValue(), context);
                return null;
            }));
        }

        // 保留一下逻辑
        int index = 0;
        Exception exception = null;
        while (index < futures.size()) {
            try {
                Future future = completionService.take();
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                exception = e;
                break;
            }

            index++;
        }

        // 任何一个线程返回，出现了异常，就退出整个调度
        if (index < futures.size()) {
            for (int errorIndex = 0; errorIndex < futures.size(); errorIndex++) {
                Future future = futures.get(errorIndex);
                if (future.isDone()) {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        exception = e;
                    }
                } else {
                    future.cancel(true); // 对未完成的进行取消
                }
            }
        } else {
            for (int i = 0; i < futures.size(); i++) {// 收集一下正确处理完成的结果
                Future future = futures.get(i);
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    exception = e;
                }
            }
        }

        if (exception != null) {
            throw new DataLoadException("Record-writing-failure occurred in adapt.", exception);
        }

    }

    private Map<SddlDataSource, List<RdbEventRecord>> buildSql(RecordChunk recordChunk, TaskWriterContext context,
                                                               SddlTaskInfo sddlTaskInfo, ExecutorService executorService) {
        //statistic before
        WriterSddlStatistic sddlStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        sddlStatistic.setRecordsCountBeforeBuild(recordChunk.getRecords().size());
        long startTime = System.currentTimeMillis();

        //do build
        List<RdbEventRecord> records = recordChunk.getRecords();
        List<Future> results = new ArrayList<>();
        for (RdbEventRecord record : records) {
            results.add(executorService.submit(
                    () -> {
                        buildSql(record, sddlTaskInfo);
                    }
            ));
        }

        Throwable ex = null;
        for (int i = 0; i < results.size(); i++) {
            Future result = results.get(i);
            try {
                Object obj = result.get();
                if (obj instanceof Throwable) {
                    ex = (Throwable) obj;
                }
            } catch (Throwable e) {
                ex = e;
            }
        }
        if (ex != null) {
            throw new SddlSqlException("something goes wrong when do sddl_sql build.", ex);
        }

        // group ds
        Map<SddlDataSource, List<RdbEventRecord>> dsBatchRecords = SddlExcuteBuilder.groupRecords(records);

        //statistic after
        sddlStatistic.setTimeForBuild(System.currentTimeMillis() - startTime);
        sddlStatistic.setRecordsBuildDsCount(dsBatchRecords.size());
        sddlStatistic.setRecordsCountAfterBuild((int) dsBatchRecords.values().stream().mapToInt(p -> p.size()).summaryStatistics().getSum());

        return dsBatchRecords;
    }

    /**
     * @Description: 在两个维度间同步数据的类型有三种：sharing(S)、redundancy(R)、无（未配置），那么就会存在9种场景，如下：
     * 1. S -> S: 对应的同步方式：sharing同步；
     * 2. S -> R: 对应的同步方式：附属维度redundancy同步；
     * 3. R -> S: 对应的同步方式：由于源是冗余表，则先在主维度执行redundancy同步，再在附属维度执行Sharding同步；
     * 4. R -> R: 对应的同步方式：由于源是冗余表，则在主维度和附属维度都要执行redundancy同步；
     * 5. R -> 无: 对应的同步方式：先同步主维度冗余同步，再报警；
     * 且源端是冗余表的，只同步proxyDB的数据；
     * 以下四种情况仅需要报警
     * 6. 无 -> R: 对应的同步方式：报警；
     * 7. S -> 无: 对应的同步方式：报警；
     * 8. 无 -> S: 对应的同步方式：报警；
     * 9. 无 -> 无: 对应的同步方式：报警；
     * @Author : yongwang.chen@ucarinc.com
     * @Date : 10:33 AM 27/02/2018
     */
    private void buildSql(RdbEventRecord record, SddlTaskInfo sddlTaskInfo) {
        SddlDataLinkCluster dataLinkCluster = DataSourceCluster.getSddlDs(sddlTaskInfo.getSddlMediaSourceInfo());
        if (dataLinkCluster.getSddlLogicCluster().getSddlClusters().size() == 1) {
            //等于1说明没有附属维度，直接同步冗余表即可
            String tableName = record.getTableName();

            if (!sddlTaskInfo.isProxyDb()) {
                return;
            }

            if (SddlTableManager.isMainRedundancyTable(tableName, sddlTaskInfo.getSddlMediaSourceInfo())) {
                SddlExcuteBuilder.buildMainRedundancySql(record, sddlTaskInfo.getSddlMediaSourceInfo(), SddlExcuteBuilder.RedundancySqlMode.BROTHER);
            }
        } else {
            // 填充数据源，并匹配分库规则
            // Record.metaData(map里，如果多个（冗余表）就有多个数据源元素)
            String tableName = record.getTableName();

            ShardingColumnInfo mainShardingClumnInfo = SddlTableManager.getMainShardingClumn(tableName, sddlTaskInfo.getSddlMediaSourceInfo());
            ShardingColumnInfo accssoryShardingClumnInfo = SddlTableManager.getAccssoryShardingClumn(tableName, sddlTaskInfo);
            if (mainShardingClumnInfo != null
                    && accssoryShardingClumnInfo != null) {

                SddlExcuteBuilder.buildShardingSql(record, accssoryShardingClumnInfo, (DataSourceCluster.getSddlDs(sddlTaskInfo.getSddlMediaSourceInfo()))
                        .getSddlLogicCluster().getSddlClusters().get(1));
            } else if (mainShardingClumnInfo != null
                    && SddlTableManager.isAccessoryRedundancyTable(tableName, sddlTaskInfo.getSddlMediaSourceInfo())) {

                SddlExcuteBuilder.buildAccessoryRedundancySql(record, sddlTaskInfo.getSddlMediaSourceInfo());
            } else if (SddlTableManager.isMainRedundancyTable(tableName, sddlTaskInfo.getSddlMediaSourceInfo())
                    && accssoryShardingClumnInfo != null) {

                if (!sddlTaskInfo.isProxyDb()) {
                    return;
                }

                SddlExcuteBuilder.buildMainRedundancySql(record, sddlTaskInfo.getSddlMediaSourceInfo(), SddlExcuteBuilder.RedundancySqlMode.BROTHER);

                SddlExcuteBuilder.buildShardingSql(record, accssoryShardingClumnInfo, (DataSourceCluster.getSddlDs(sddlTaskInfo.getSddlMediaSourceInfo()))
                        .getSddlLogicCluster().getSddlClusters().get(1));
            } else if (SddlTableManager.isMainRedundancyTable(tableName, sddlTaskInfo.getSddlMediaSourceInfo())
                    && SddlTableManager.isAccessoryRedundancyTable(tableName, sddlTaskInfo.getSddlMediaSourceInfo())) {

                if (!sddlTaskInfo.isProxyDb()) {
                    return;
                }

                SddlExcuteBuilder.buildMainRedundancySql(record, sddlTaskInfo.getSddlMediaSourceInfo(), SddlExcuteBuilder.RedundancySqlMode.BROTHER);

                SddlExcuteBuilder.buildAccessoryRedundancySql(record, sddlTaskInfo.getSddlMediaSourceInfo());
            } else if (SddlTableManager.isMainRedundancyTable(tableName, sddlTaskInfo.getSddlMediaSourceInfo())) {

                if (!sddlTaskInfo.isProxyDb()) {
                    return;
                }

                SddlExcuteBuilder.buildMainRedundancySql(record, sddlTaskInfo.getSddlMediaSourceInfo(), SddlExcuteBuilder.RedundancySqlMode.BROTHER);

                alarmForBuildSql(sddlTaskInfo, tableName);
            } else {
                alarmForBuildSql(sddlTaskInfo, tableName);
            }

        }
    }

    private void alarmForBuildSql(SddlTaskInfo sddlTaskInfo, String tableName) {
        // 有这样几种情况：1、漏配sharding表；2、漏配冗余表； 3、dba在主维度建了临时表，解决临时问题； 4、sharding表就是在附属维度不用(无需同步)，所以正常不配；
        // 结论：非sharding+非冗余的情况，或适配"3和4"情况，则在添加"过滤白名单missMatchSkipTable"功能，否则报警（两分钟报一次）；
        List<String> missMatchSkipTableList = null;

        List<PluginWriterParameter> sddlWriteParams = sddlTaskInfo.getTaskInfo().getTaskWriterParameterObjs();
        for (PluginWriterParameter sddlWriterParam : sddlWriteParams) {
            if (sddlWriterParam instanceof SddlWriterParameter)
                missMatchSkipTableList = ((SddlWriterParameter) sddlWriterParam).getMissMatchSkipTableList();
        }

        if (CollectionUtils.isEmpty(missMatchSkipTableList) || !missMatchSkipTableList.contains(tableName)) {
            LOG.info("######严重异常：sddl_writer同步表不存在，表名为{}", tableName);

            monitorException(sddlTaskInfo.getTaskInfo().getId(), tableName);
        }

    }

    private void monitorException(Long taskId, String tableName) {
        Exception e = new IllegalTableException("sddl_writer table not exist, taskId:" + taskId + ", tableName:" + tableName);
        TaskExceptionProbeIndex index = new TaskExceptionProbeIndex(taskId, e);
        ProbeManager.getInstance().getTaskExceptionProbe().record(index);
    }

    static class IllegalTableException extends DatalinkException {

        public IllegalTableException(String message, Throwable cause) {
            super(message, cause);
        }

        public IllegalTableException(String message) {
            super(message);
        }

        public IllegalTableException(Throwable cause) {
            super(cause);
        }

        public IllegalTableException() {
            super();
        }
    }
}
