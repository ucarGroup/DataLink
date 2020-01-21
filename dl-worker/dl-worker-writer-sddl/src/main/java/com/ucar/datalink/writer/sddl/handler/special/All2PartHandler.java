package com.ucar.datalink.writer.sddl.handler.special;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DataLoadException;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.sddl.SddlWriterParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import com.ucar.datalink.writer.sddl.ConfCenter.ConfCenterApiSingleton;
import com.ucar.datalink.writer.sddl.ConfCenter.SddlCFContext;
import com.ucar.datalink.writer.sddl.dataSource.DataSourceCluster;
import com.ucar.datalink.writer.sddl.exception.SddlInitException;
import com.ucar.datalink.writer.sddl.exception.SddlSqlException;
import com.ucar.datalink.writer.sddl.handler.RecordWriter;
import com.ucar.datalink.writer.sddl.manager.SddlExcuteBuilder;
import com.ucar.datalink.writer.sddl.manager.SddlTableManager;
import com.ucar.datalink.writer.sddl.model.ShardingColumnInfo;
import com.ucar.datalink.writer.sddl.util.statistic.WriterSddlStatistic;
import com.zuche.framework.sddl.datasource.SddlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by lubiao on 2018/10/17.
 */
public class All2PartHandler implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(All2PartHandler.class);

    private TaskInfo sddlTaskInfo;
    private MediaSourceInfo sddlMediaSourceInfo;
    private ExecutorService executorService;

    @Override
    public void initialize(TaskWriterContext context) {
        initTaskAndMeiaSourceInfo(context);

        initCfCent();

        initThreadPool(context);
    }

    @Override
    public void writeData(RecordChunk recordChunk, TaskWriterContext context) {
        // cover statistic
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        context.taskWriterSession().setData(WriterStatistic.KEY, new WriterSddlStatistic(context.taskId(), writerStatistic.getWriterParameter()));

        // 二期：build sql (group:可以考虑按"库+表"group来提升性能)
        Map<SddlDataSource, List<RdbEventRecord>> records = buildSql(recordChunk);

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

    @Override
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void initTaskAndMeiaSourceInfo(TaskWriterContext context) {
        this.sddlTaskInfo = context.getService(TaskConfigService.class).getTask(Long.valueOf(context.taskId()));
        List<MediaMappingInfo> mappingInfos = context.getService(MediaService.class).getMediaMappingsByTask(sddlTaskInfo.getId(), false);
        if (mappingInfos.size() != 1) {
            throw new DatalinkException("MappingInfo个数不为1");
        }

        this.sddlMediaSourceInfo = mappingInfos.get(0).getTargetMediaSource();
        //虚拟数据源
        if (sddlMediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)) {
            sddlMediaSourceInfo = DataLinkFactory.getObject(MediaService.class).getRealDataSource(sddlMediaSourceInfo);
        }
        if (null == sddlMediaSourceInfo || sddlMediaSourceInfo.getType() != MediaSourceType.SDDL) {
            throw new SddlInitException("RdbEventRecordHandler.initTaskSddlInfo init sddlMediaSourceInfo is null, context:" + JSON.toJSONString(context));
        }
    }

    private void initCfCent() {
        SddlMediaSrcParameter sddlMediaSrcParameter = sddlMediaSourceInfo.getParameterObj();

        SddlCFContext cfContext = new SddlCFContext(sddlMediaSrcParameter.getProjectName(),
                sddlMediaSrcParameter.getCfKey(),
                sddlMediaSrcParameter.getServerDomain(),
                sddlMediaSrcParameter.getBusinessName());
        ConfCenterApiSingleton.getInstance(cfContext);
    }

    private void initThreadPool(TaskWriterContext context) {
        SddlWriterParameter parameter = (SddlWriterParameter) context.getWriterParameter();
        int dbSize = ((SddlMediaSrcParameter) sddlMediaSourceInfo.getParameterObj()).getPrimaryDbsId().size();
        int corePoolSize = dbSize;
        int maxPoolSize = dbSize;

        executorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(parameter.getPoolSize() * 4),
                new NamedThreadFactory(
                        MessageFormat.format("Task-{0}-Writer-{1}-load", context.taskId(), parameter.getPluginName())
                ),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private Map<SddlDataSource, List<RdbEventRecord>> buildSql(RecordChunk recordChunk) {
        //do build
        List<RdbEventRecord> records = recordChunk.getRecords();
        List<Future> results = new ArrayList<>();
        for (RdbEventRecord record : records) {
            results.add(executorService.submit(
                    () -> {
                        buildSql(record);
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

        return dsBatchRecords;
    }

    private void buildSql(RdbEventRecord record) {
        // 填充数据源，并匹配分库规则
        // Record.metaData(map里，如果多个（冗余表）就有多个数据源元素)
        String tableName = record.getTableName();

        ShardingColumnInfo mainShardingClumnInfo = SddlTableManager.getMainShardingClumn(tableName, sddlMediaSourceInfo);
        if (mainShardingClumnInfo != null) {
            SddlExcuteBuilder.buildShardingSql(record, mainShardingClumnInfo, (DataSourceCluster.getSddlDs(sddlMediaSourceInfo))
                    .getSddlLogicCluster().getSddlClusters().get(0));
        } else if (SddlTableManager.isMainRedundancyTable(tableName, sddlMediaSourceInfo)) {
            SddlExcuteBuilder.buildMainRedundancySql(record, sddlMediaSourceInfo, SddlExcuteBuilder.RedundancySqlMode.PROXY);
        } else {
            logger.info("record is not of sharding and not of redundancy table.table is {},key is {}", record.getTableName(), record.getId());
        }
    }
}
