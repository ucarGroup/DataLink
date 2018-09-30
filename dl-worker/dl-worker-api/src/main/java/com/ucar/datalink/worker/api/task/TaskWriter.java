package com.ucar.datalink.worker.api.task;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.common.errors.RecordNotSupportException;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.util.statistic.BaseWriterStatistic;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The write part of the task.
 * <p>
 * Created by lubiao on 2017/2/15.
 */
public abstract class TaskWriter<T extends PluginWriterParameter> extends TaskLifeCycle {
    private static final Logger logger = LoggerFactory.getLogger(TaskWriter.class);

    protected TaskWriterContext context;
    protected T parameter;
    protected LoadingCache<Class<? extends Record>, Handler> handlers;

    /**
     * Initialize this TaskWriter with the specified context object.
     */
    @SuppressWarnings("unchecked")
    public void initialize(TaskWriterContext context) {
        this.context = context;
        this.parameter = (T) context.getWriterParameter();
        this.handlers = CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends Record>, Handler>() {
            @Override
            public Handler load(Class<? extends Record> clazz) throws Exception {
                Handler handler = getHandler(clazz);
                if (handler == null) {
                    throw new RecordNotSupportException(clazz);
                }
                handler.initialize(context);
                return handler;
            }
        });
    }

    /**
     * close the task writer
     * 一般执行资源释放操作
     */
    public void close(){
        handlers.asMap().values().stream().forEach(Handler::destroy);
    }

    public void prePush() {
        context.beginSession();
        context.taskWriterSession().setData(WriterStatistic.KEY, new WriterStatistic(context.taskId(), parameter));
    }

    public void push(RecordChunk recordChunk) {
        doPush(recordChunk);
    }

    public void postPush() {
        BaseWriterStatistic statistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        if (parameter.isPerfStatistic()) {
            logger.info(statistic.toJsonString());
        }
    }

    @SuppressWarnings("unchecked")
    protected void doPush(RecordChunk recordChunk) {
        List<? extends Record> records = recordChunk.getRecords();
        if (records != null && !records.isEmpty()) {
            Handler handler = handlers.getUnchecked(records.get(0).getClass());//一个chunk里面的数据类型都是一样的
            handler.writeData(recordChunk, context);
        }
    }

    protected abstract Handler getHandler(Class<? extends Record> clazz);
}
