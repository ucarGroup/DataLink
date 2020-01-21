package com.ucar.datalink.writer.sddl.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.sddl.SddlWriterParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import com.ucar.datalink.writer.sddl.ConfCenter.ConfCenterApiSingleton;
import com.ucar.datalink.writer.sddl.ConfCenter.SddlCFContext;
import com.ucar.datalink.writer.sddl.dataSource.SddlTaskInfo;
import com.ucar.datalink.writer.sddl.exception.SddlInitException;
import com.ucar.datalink.writer.sddl.interceptor.BaseEventInterceptor;
import com.ucar.datalink.writer.sddl.interceptor.CheckCompleteInterceptor;
import com.ucar.datalink.writer.sddl.util.statistic.WriterSddlStatistic;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: sddl_plugin handler
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 30/10/2017.
 */
public class RdbEventRecordHandler implements Handler {

    private static final Logger LOG = LoggerFactory.getLogger(RdbEventRecordHandler.class);

    private LinkedList<Interceptor> builtinInterceptors;
    private ExecutorService executorService;

    private SddlTaskInfo sddlTaskInfo = new SddlTaskInfo();

    public RdbEventRecordHandler() {
        super();
    }

    @Override
    public void initialize(TaskWriterContext context) {

        initTaskSddlInfo(context); //baseInfo，so put first

        initInterceptor();

        initCfCent();

        SddlWriterParameter parameter = (SddlWriterParameter) context.getWriterParameter();

        int dbSize = ((SddlMediaSrcParameter) sddlTaskInfo.getSddlMediaSourceInfo().getParameterObj()).getPrimaryDbsId().size();
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

    @Override
    public void destroy() {

        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public void writeData(RecordChunk recordChunk, TaskWriterContext context) {
        // cover statistic
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        context.taskWriterSession().setData(WriterStatistic.KEY, new WriterSddlStatistic(context.taskId(), writerStatistic.getWriterParameter()));

        RecordChunk interceptedChunk = intercept(recordChunk, context);
        if (!shouldContinue(interceptedChunk)) {
            return;
        }

        load(interceptedChunk, context);

    }

    private void initInterceptor() {
        this.builtinInterceptors = Lists.newLinkedList();
        BaseEventInterceptor baseEventInterceptor = new BaseEventInterceptor(sddlTaskInfo);
        CheckCompleteInterceptor checkCompleteInterceptor = new CheckCompleteInterceptor(sddlTaskInfo);

        this.addInterceptorLast(baseEventInterceptor); // ⚠️此拦截器需要放到第一位（有处理分表表名功能）！！
        this.addInterceptorLast(checkCompleteInterceptor);
    }

    private void initCfCent() {
        SddlMediaSrcParameter sddlMediaSrcParameter = sddlTaskInfo.getSddlMediaSourceInfo().getParameterObj();

        SddlCFContext cfContext = new SddlCFContext(sddlMediaSrcParameter.getProjectName(),
                sddlMediaSrcParameter.getCfKey(),
                sddlMediaSrcParameter.getServerDomain(),
                sddlMediaSrcParameter.getBusinessName());
        ConfCenterApiSingleton.getInstance(cfContext);
    }

    private void initTaskSddlInfo(TaskWriterContext context) {
        TaskInfo taskInfo = context.getService(TaskConfigService.class).getTask(Long.valueOf(context.taskId()));
        if (taskInfo == null || taskInfo.getReaderMediaSourceId() == null || taskInfo.getReaderMediaSourceId() < 1)
            throw new SddlInitException("DataSourceCluster.initMediaSource init TaskInfo is error, context:" + JSON.toJSONString(context));

        //如果是虚拟数据源，取出真实数据源id
        Long readerMediaSourceId = taskInfo.getReaderMediaSourceId();
        MediaSourceInfo mediaSourceInfo = context.getService(MediaSourceService.class).getById(readerMediaSourceId);
        if(mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
            MediaSourceInfo realMediaSourceInfo = context.getService(MediaService.class).getRealDataSource(mediaSourceInfo);
            readerMediaSourceId = realMediaSourceInfo.getId();
        }
        MediaSourceInfo sddlMediaSourceInfo = getSddlMediaSourceList(context, readerMediaSourceId);
        if (null == sddlMediaSourceInfo) {
            throw new SddlInitException("RdbEventRecordHandler.initTaskSddlInfo init sddlMediaSourceInfo is null, context:" + JSON.toJSONString(context));
        }

        List<MediaMappingInfo> mappingInfos = context.getService(MediaService.class).getMediaMappingsByTask(Long.valueOf(context.taskId()), false);
        if (CollectionUtils.isEmpty(mappingInfos)) {
            throw new SddlInitException("sddl media mappings can not be null");
        }

        MediaSourceInfo targetMediaSource = mappingInfos.get(0).getTargetMediaSource();
        MediaSourceInfo mappingMediaSourceInfo;
        if(targetMediaSource.getType().equals(MediaSourceType.VIRTUAL)){
            MediaSourceInfo realMediaSourceInfo = context.getService(MediaService.class).getRealDataSource(targetMediaSource);
            mappingMediaSourceInfo = realMediaSourceInfo;
        }else {
            mappingMediaSourceInfo = targetMediaSource;
        }
        if (!mappingMediaSourceInfo.equals(sddlMediaSourceInfo)) {
            throw new SddlInitException("SddlMediaSource for reader mismatch with SddlMediaSource for mediamapping`s target mediasource");
        }

        MediaSourceInfo readerMediaSourceInfo = context.getService(MediaSourceService.class).getById(readerMediaSourceId);

        sddlTaskInfo.setTaskInfo(taskInfo);
        sddlTaskInfo.setSddlMediaSourceInfo(sddlMediaSourceInfo);
        sddlTaskInfo.setReaderMediaSourceInfo(readerMediaSourceInfo);
        sddlTaskInfo.setProxyDb(readerMediaSourceId.equals(((SddlMediaSrcParameter) sddlMediaSourceInfo.getParameterObj()).getProxyDbId()));
    }

    private MediaSourceInfo getSddlMediaSourceList(TaskWriterContext context, Long readerMediaSourceId) {
        List<MediaSourceInfo> sddlMediaSources = context.getService(MediaSourceService.class).getListByType(Sets.newHashSet(MediaSourceType.SDDL));

        if (CollectionUtils.isEmpty(sddlMediaSources))
            return null;

        List<MediaSourceInfo> resultInfos = new ArrayList<>();
        for (MediaSourceInfo mediaSource : sddlMediaSources) {
            SddlMediaSrcParameter parameter = mediaSource.getParameterObj();

            if (parameter.getPrimaryDbsId().contains(readerMediaSourceId))
                resultInfos.add(mediaSource);

        }

        if (resultInfos.size() != 1)
            throw new SddlInitException("sddl_writer初始化获取SDDL_MediaSourceInfo异常：RdbEventRecordHandler.getSddlMediaSourceList, context:"
                    + JSON.toJSONString(context) + "; readerMediaSourceId =" + readerMediaSourceId);

        return resultInfos.get(0);
    }

    protected RecordChunk intercept(RecordChunk recordChunk, TaskWriterContext context) {
        //statistic before
        WriterSddlStatistic sddlStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        sddlStatistic.setRecordsCountBeforeIntercept(recordChunk.getRecords().size());
        Long startTime = System.currentTimeMillis();

        //do intercept
        // check sddl
        RecordChunk newChunk = recordChunk.copyWithoutRecords();
        for (Object record : recordChunk.getRecords()) {
            boolean shouldContinue = true;
            for (Interceptor interceptor : builtinInterceptors) {
                record = interceptor.intercept((RdbEventRecord) record, context);
                if (record == null) {
                    shouldContinue = false;
                    break;
                }
            }

            if (shouldContinue) {
                newChunk.merge((RdbEventRecord) record);
            }
        }

        //statistic after
        sddlStatistic.setRecordsCountAftereIntercept(newChunk.getRecords().size());
        sddlStatistic.setTimeForIntercept(System.currentTimeMillis() - startTime);

        return newChunk;
    }


    protected void load(RecordChunk recordChunk, final TaskWriterContext context) {
        //statistic before
        WriterSddlStatistic sddlStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        long startTime = System.currentTimeMillis();

        new RecordLoader().submitData(recordChunk, context, sddlTaskInfo, executorService);

        //statistic after
        sddlStatistic.setTimeForLoad(System.currentTimeMillis() - startTime);
    }


    /**
     * 判断是否还有必要继续
     */
    private boolean shouldContinue(RecordChunk recordChunk) {
        List records = recordChunk.getRecords();
        return !(records == null || records.isEmpty());
    }

    /**
     * 在基础拦截器的前面增加自定义拦截器
     */
    protected void addInterceptorLast(Interceptor interceptor) {
        this.builtinInterceptors.addLast(interceptor);
    }


}
