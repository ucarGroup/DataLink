package com.ucar.datalink.worker.api.handle;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.common.errors.DataLoadException;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.worker.api.intercept.BuiltInDdlEventInterceptor;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.intercept.InterceptorFactory;
import com.ucar.datalink.worker.api.intercept.SkipIdsInterceptor;
import com.ucar.datalink.worker.api.merge.Merger;
import com.ucar.datalink.worker.api.merge.MergerFactory;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.transform.Transformer;
import com.ucar.datalink.worker.api.transform.TransformerFactory;
import com.ucar.datalink.worker.api.util.copy.RecordCopier;
import com.ucar.datalink.worker.api.util.statistic.RecordGroupLoadStatistic;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import com.ucar.datalink.worker.api.util.priority.PriorityTask;
import com.ucar.datalink.worker.api.util.priority.PriorityTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Record处理器，不同类型的Record，对应不同类型的Handler.
 * <p>
 * Created by lubiao on 2017/3/3.
 */
public abstract class AbstractHandler<T extends Record> implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

    private LinkedList<Interceptor<T>> builtinInterceptors;

    private ExecutorService recordChunkExecutorService;

    protected ExecutorService executorService;

    protected AbstractHandler() {
        this.builtinInterceptors = Lists.newLinkedList();
        this.builtinInterceptors.addLast(new BuiltInDdlEventInterceptor());//必须保证BuiltInDdlEventInterceptor最先执行，将其置于拦截器List的第一个位置
        this.builtinInterceptors.addLast(new SkipIdsInterceptor());
    }

    @Override
    public void initialize(TaskWriterContext context) {
        PluginWriterParameter parameter = context.getWriterParameter();

        List<MediaMappingInfo> taskMappings = context.getService(MediaService.class)
                .getMediaMappingsByTask(Long.valueOf(context.taskId()), false)
                .stream()
                .filter(i -> parameter.getSupportedSourceTypes().contains(i.getTargetMediaSource().getType()))
                .collect(Collectors.toList());

        int corePoolSize = 1;
        if (!taskMappings.isEmpty()) {
            corePoolSize = taskMappings
                    .stream()
                    .collect(Collectors.groupingBy(MediaMappingInfo::getSourceMediaId, Collectors.counting()))
                    .values()
                    .stream()
                    .mapToInt(Long::intValue)
                    .summaryStatistics()
                    .getMin();
        }

        //必须保证 maximumPoolSize >= targetMediaSourceCount,保证所有线程都有机会执行PriorityTaskExecutor的execute方法，否则可能导致活锁现象
        //见loadData方法
        int maxPoolSize = 1;
        if (!taskMappings.isEmpty()) {
            maxPoolSize = taskMappings
                    .stream()
                    .collect(Collectors.groupingBy(MediaMappingInfo::getTargetMediaSourceId))
                    .size();
        }

        logger.info(String.format("CorePoolSize for recordChunkExecutor is %s.", corePoolSize));
        logger.info(String.format("MaxPoolSize  for recordChunkExecutor is %s.", maxPoolSize));

        recordChunkExecutorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new NamedThreadFactory(
                        MessageFormat.format("Task-{0}-Writer-{1}-chunk", context.taskId(), parameter.getPluginName())
                ),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );


        // 对于该线程池来说,不能使用CallerRunsPolicy，会导致Future.get方法一直阻塞，进而导致Task始终无法关闭
        // 相关原因可参考该链接：https://blog.csdn.net/zero__007/article/details/78915354
        // 针对我们的场景，使用固定线程数+无界队列即可，所以把之前的CallerRunsPolicy去掉
        executorService = new ThreadPoolExecutor(
                parameter.getPoolSize(),
                parameter.getPoolSize(),
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory(
                        MessageFormat.format("Task-{0}-Writer-{1}-load", context.taskId(), parameter.getPluginName())
                ));
    }

    @Override
    public void destroy() {
        if (executorService != null) {
            List<Runnable> runnables = executorService.shutdownNow();
            runnables.forEach(r -> {//如果是RunnableFuture类型，进行cancel操作，防止等待线程一直阻塞
                if (r instanceof RunnableFuture) {
                    RunnableFuture runnableFuture = (RunnableFuture) r;
                    runnableFuture.cancel(true);
                }
            });
        }

        //一直等待，即使出现卡住的情况，必须保证Task的互斥
        try {
            if (executorService != null) {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            //do nothing
        }

        if (recordChunkExecutorService != null) {
            List<Runnable> runnables = recordChunkExecutorService.shutdownNow();
            runnables.forEach(r -> {//如果是RunnableFuture类型，进行cancel操作，防止等待线程一直阻塞
                if (r instanceof RunnableFuture) {
                    RunnableFuture runnableFuture = (RunnableFuture) r;
                    runnableFuture.cancel(true);
                }
            });
        }

        try {
            if (recordChunkExecutorService != null) {
                recordChunkExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            //do nothing
        }
    }


    /**
     * 执行具体的写入操作
     */
    protected abstract void doWrite(List<T> records, TaskWriterContext context);

    /**
     * 执行最终的数据处理和写入逻辑
     * <p>
     * 经历如下的几个阶段:
     * 1.mapping阶段，根据MediaMapping的配置,将一个Record记录Mapping成多个
     * 2.intercept阶段，Mapping后的每个record对象已经是和MediaMapping绑定的了，intercept阶段可以根据MediaMapping的interceptorId的配置进行对应的拦截处理
     * 3.transform阶段，根据TaskParameter，WriterParametr和MeidaMapping的配置，对从reader取到的原生Record进行转换
     * 4.group阶段，根据MediaMapping的配置，按照目标数据源的不同，对Records进行分组，每个组对应一个targetMediaSource
     * 5.load阶段，将分完组的数据并行提交，然后等待所有Task执行完毕
     */
    @Override
    @SuppressWarnings("unchecked")
    public void writeData(RecordChunk recordChunk, TaskWriterContext context) {
        RecordChunk<T> mappedChunk = mapping(recordChunk, context);
        if (!shouldContinue(mappedChunk)) {
            return;
        }

        RecordChunk<T> interceptedChunk = intercept(mappedChunk, context);
        if (!shouldContinue(interceptedChunk)) {
            return;
        }

        RecordChunk<T> mergedChunk = merge(interceptedChunk, context);
        if (!shouldContinue(mergedChunk)) {
            return;
        }

        RecordChunk<T> transformedChunk = transform(mergedChunk, context);
        if (!shouldContinue(transformedChunk)) {
            return;
        }

        List<RecordChunk<T>> groupedChunkList = group(transformedChunk, context);

        load(groupedChunkList, context);
    }

    /**
     * 根据MediaMapping的配置，对Record进行mapping映射,
     */
    @SuppressWarnings("unchecked")
    protected RecordChunk<T> mapping(RecordChunk<T> recordChunk, TaskWriterContext context) {
        //statistic before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        writerStatistic.setRecordsCountBeforeMapping(recordChunk.getRecords().size());
        Long startTime = System.currentTimeMillis();

        //do mapping
        Long taskId = Long.valueOf(context.taskId());
        RecordChunk<T> newChunk = recordChunk.copyWithoutRecords();

        for (T record : recordChunk.getRecords()) {
            List<MediaMappingInfo> list = context.getService(MediaService.class).
                    getMediaMappingsByMediaAndTarget(taskId, record.RSI().getNamespace(), record.RSI().getName(),
                            context.getWriterParameter().getSupportedSourceTypes(), true);

            for (int i = 0; i < list.size(); i++) {
                T copyRecord;
                if (i == 0) {
                    copyRecord = record;
                } else {
                    logger.debug("RecordChunk Copy Happened in " + getClass().getSimpleName());
                    copyRecord = RecordCopier.copy(record);
                }
                RecordMeta.attachMediaMapping(copyRecord, list.get(i));//attach操作会用新值替换老的值
                newChunk.merge(copyRecord);
            }

        }

        //statistic after
        writerStatistic.setRecordsCountAfterMapping(newChunk.getRecords().size());
        writerStatistic.setTimeForMapping(System.currentTimeMillis() - startTime);

        return newChunk;
    }

    /**
     * 对record进行拦截
     */
    protected RecordChunk<T> intercept(RecordChunk<T> recordChunk, TaskWriterContext context) {
        //statistic before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        writerStatistic.setRecordsCountBeforeIntercept(recordChunk.getRecords().size());
        Long startTime = System.currentTimeMillis();

        //do intercept
        RecordChunk<T> newChunk = recordChunk.copyWithoutRecords();
        for (T record : recordChunk.getRecords()) {
            boolean shouldContinue = true;
            for (Interceptor<T> interceptor : builtinInterceptors) {
                record = interceptor.intercept(record, context);
                if (record == null) {
                    shouldContinue = false;
                    break;
                }
            }

            if (shouldContinue) {
                MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
                if (mappingInfo.getInterceptorId() != null) {
                    Interceptor<T> interceptor = InterceptorFactory.getInterceptor(mappingInfo.getInterceptorId());
                    record = interceptor.intercept(record, context);
                }

                if (record != null) {
                    newChunk.merge(record);
                }
            }
        }

        //statistic after
        writerStatistic.setRecordsCountAftereIntercept(newChunk.getRecords().size());
        writerStatistic.setTimeForIntercept(System.currentTimeMillis() - startTime);

        return newChunk;
    }

    /**
     * 需要的话，可以对数据进行merge操作
     */
    protected RecordChunk<T> merge(RecordChunk<T> recordChunk, TaskWriterContext context) {
        //statistic before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        writerStatistic.setRecordsCountBeforeMerge(recordChunk.getRecords().size());
        Long startTime = System.currentTimeMillis();

        //do merge
        if (context.getWriterParameter().isMerging()) {
            Merger<T> merger = MergerFactory.
                    getMerger(recordChunk.getRecords().get(0).getClass());
            RecordChunk<T> newChunk = merger.merge(recordChunk);

            //statistic after
            writerStatistic.setTimeForMerge(System.currentTimeMillis() - startTime);
            writerStatistic.setRecordsCountAfterMerge(newChunk.getRecords().size());

            return newChunk;
        } else {
            //statistic after
            writerStatistic.setTimeForMerge(System.currentTimeMillis() - startTime);
            writerStatistic.setRecordsCountAfterMerge(recordChunk.getRecords().size());

            return recordChunk;
        }
    }

    /**
     * 按照MediaMapping的配置，重建数据，并对数据进行清洗和转换
     */
    protected RecordChunk<T> transform(RecordChunk<T> recordChunk, TaskWriterContext context) {
        //statistic before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        writerStatistic.setRecordsCountBeforeTransform(recordChunk.getRecords().size());
        long startTime = System.currentTimeMillis();

        //do transform
        Transformer<T> transformer = TransformerFactory.
                getTransformer(recordChunk.getRecords().get(0).getClass());
        RecordChunk<T> newChunk = transformer.transform(recordChunk, context);

        //statistic after
        writerStatistic.setRecordsCountAfterTransform(newChunk.getRecords().size());
        writerStatistic.setTimeForTransform(System.currentTimeMillis() - startTime);

        return newChunk;
    }

    /**
     * 按照载入的目标数据源对Record分组
     *
     * @return a list of RecordChunk，每个chunk对应一个目标数据源
     */
    protected List<RecordChunk<T>> group(RecordChunk<T> recordChunk, TaskWriterContext context) {
        //statistic before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        writerStatistic.setRecordsCountBeforeGroup(recordChunk.getRecords().size());
        long startTime = System.currentTimeMillis();

        //do group
        LoadingCache<MediaSourceInfo, RecordChunk<T>> cache = CacheBuilder.newBuilder().build(
                new CacheLoader<MediaSourceInfo, RecordChunk<T>>() {
                    @Override
                    public RecordChunk<T> load(MediaSourceInfo key) throws Exception {
                        return recordChunk.copyWithoutRecords();
                    }
                }
        );
        for (T record : recordChunk.getRecords()) {
            MediaSourceInfo targetMediaSource = RecordMeta.mediaMapping(record).getTargetMediaSource();
            try {
                cache.get(targetMediaSource).merge(record);
            } catch (ExecutionException e) {
                throw new DatalinkException("something goes wrong when grouping record chunks.", e);
            }
        }
        List<RecordChunk<T>> result = new ArrayList<>(cache.asMap().values());

        //statistic after
        writerStatistic.setTimeForGroup(System.currentTimeMillis() - startTime);
        writerStatistic.setRecordsGroupCount(result.size());
        writerStatistic.setRecordsCountAfterGroup((int) result.stream().mapToInt(i -> i.getRecords().size()).summaryStatistics().getSum());

        return result;
    }

    /**
     * 对分组后的数据进行最终的load操作
     */
    protected void load(List<RecordChunk<T>> recordChunkList, final TaskWriterContext context) {
        //statistic before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        long startTime = System.currentTimeMillis();

        //do load
        submitAndWait(recordChunkList, context);

        //statistic after
        writerStatistic.setTimeForLoad(System.currentTimeMillis() - startTime);
    }

    /**
     * 将RecordChunkList提交到线程池，所有Chunk并行执行
     */
    protected void submitAndWait(List<RecordChunk<T>> recordChunkList, final TaskWriterContext context) {
        final List<Future> futures = new ArrayList<>();
        final PriorityTaskExecutor priorityTaskExecutor = new PriorityTaskExecutor(recordChunkList.size());
        final ExecutorCompletionService completionService = new ExecutorCompletionService(recordChunkExecutorService);

        for (final RecordChunk<T> recordChunk : recordChunkList) {
            futures.add(completionService.submit(() -> {
                loadData(recordChunk, context, priorityTaskExecutor);
                return null;
            }));
        }

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

    /**
     * 在基础拦截器的前面增加自定义拦截器
     */
    protected void addInterceptorBefore(Interceptor interceptor) {
        this.builtinInterceptors.addFirst(interceptor);
    }

    /**
     * 在基础拦截器的后面增加自定义拦截器
     */
    protected void addInterceptorAfter(Interceptor interceptor) {
        this.builtinInterceptors.addLast(interceptor);
    }

    /**
     * 对每一个数据源通道执行数据同步
     */
    private void loadData(RecordChunk<T> recordChunk, TaskWriterContext context, PriorityTaskExecutor priorityTaskExecutor) throws InterruptedException {
        List<T> records = recordChunk.getRecords();
        if (records == null || records.size() == 0) {
            return;
        }

        //load before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        MediaSourceInfo targetMediaSource = RecordMeta.mediaMapping(records.get(0)).getTargetMediaSource();
        RecordGroupLoadStatistic loadStatistic = new RecordGroupLoadStatistic();
        writerStatistic.getGroupLoadStatistics().put(targetMediaSource.getId(), loadStatistic);
        loadStatistic.setMediaSourceId(targetMediaSource.getId());
        loadStatistic.setGroupRecordsCount(recordChunk.getRecords().size());
        long startTime = System.currentTimeMillis();

        //do load
        PriorityTask<T> priorityTask = buildPriorityTask(records, context);
        priorityTaskExecutor.execute(priorityTask);

        //load after
        long timeThrough = System.currentTimeMillis() - startTime;
        loadStatistic.setGroupLoadTime(timeThrough);
        if (timeThrough != 0L) {
            loadStatistic.setGroupTps(new BigDecimal(records.size() / (((double) timeThrough) / 1000)).longValue());
        }
    }

    /**
     * 构建基于优先级的PriorityTask
     */
    private PriorityTask<T> buildPriorityTask(final List<T> records, final TaskWriterContext context) {
        PriorityTask<T> task = new PriorityTask<>(bucket -> doWrite(bucket.getItems(), context));

        for (T record : records) {
            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
            task.addItem(mappingInfo.getWritePriority(), record);
        }

        return task;
    }

    /**
     * 判断是否还有必要继续
     */
    private boolean shouldContinue(RecordChunk<T> recordChunk) {
        List<T> records = recordChunk.getRecords();
        return !(records == null || records.isEmpty());
    }
}
