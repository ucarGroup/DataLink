package com.ucar.datalink.worker.api.task;

import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.contract.RSI;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.plugin.PluginReaderParameter;
import com.ucar.datalink.worker.api.util.statistic.BaseReaderStatistic;
import com.ucar.datalink.worker.api.util.statistic.ReaderStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class TaskReader<T extends PluginReaderParameter, R extends Record> extends TaskLifeCycle {
    private static final Logger logger = LoggerFactory.getLogger(TaskReader.class);

    protected TaskReaderContext context;
    protected T parameter;

    /**
     * Initialize this TaskReader with the specified context object.
     */
    @SuppressWarnings("unchecked")
    public void initialize(TaskReaderContext context) {
        this.context = context;
        this.parameter = (T) context.getReaderParameter();
    }

    public void prePoll() {
        //把上一次的统计结果打印出来
        BaseReaderStatistic statistic = context.taskReaderSession().getData(ReaderStatistic.KEY);
        if (statistic != null && parameter.isPerfStatistic()) {
            logger.info(statistic.toJsonString());
        }

        //本次开始前，进行reset操作
        context.beginSession();
        context.taskReaderSession().setData(ReaderStatistic.KEY, new ReaderStatistic(context.taskId()));
    }

    /**
     * Poll this ReaderTask for new records.
     * <p>
     * 实现poll方法时，方法內可以选择两种策略:
     * 其一:当发现没有数据时，进行轮询试探，直至取到数据后再返回,对调用者来说没有数据时便会一直阻塞。
     * 采用这种策略时需要自行检测是否要stop，当发现需要stop时，抛出InterruptedException，否则会导致调用者一直阻塞无法结束。
     * 其二:内部不实现任何轮询，有数据直接返回，没数据返回null，调用者会提供一个轮询机制。
     * 采用这种策略时poll方法可以不用关心是否需要stop，它只是一个数据采集器
     * </p>
     *
     * @return a list of source records
     */
    public RecordChunk<R> poll() throws InterruptedException {
        RecordChunk<R> fetched = toFetch();

        if (parameter.isDump()) {
            dump(fetched);
        }

        return toFilter(fetched);
    }

    private RecordChunk<R> toFetch() throws InterruptedException {
        //statistic before
        ReaderStatistic readerStatistic = context.taskReaderSession().getData(ReaderStatistic.KEY);
        long startTime = System.currentTimeMillis();

        //do fetch
        RecordChunk<R> result = fetch();

        //statistic after
        readerStatistic.setDelayTimeAfterFetch(System.currentTimeMillis() - result.getFirstEntryTime());
        readerStatistic.setTimeForFetch(System.currentTimeMillis() - startTime);
        readerStatistic.setRecordsCountByFetch(result.getRecords().size());
        return result;
    }

    /**
     * Actual producing records.
     */
    protected abstract RecordChunk<R> fetch() throws InterruptedException;

    /**
     * dump the records of the chunk for troubleshooting.
     */
    protected abstract void dump(RecordChunk<R> recordChunk);

    /**
     * 过滤数据并添加元数据
     * 如果有override的需求，子类必须实现attach-meta-data的功能
     */
    protected RecordChunk<R> toFilter(RecordChunk<R> rc) {
        //statistic before
        ReaderStatistic readerStatistic = context.taskReaderSession().getData(ReaderStatistic.KEY);
        readerStatistic.setRecordsCountBeforeFilter(rc.getRecords().size());
        long startTime = System.currentTimeMillis();

        //do filter
        if (rc.getRecords().isEmpty()) {
            return rc;
        }

        MediaService mediaService = context.getService(MediaService.class);
        RecordChunk<R> result = rc.copyWithoutRecords();
        rc.getRecords().stream().forEach(r -> {
            RSI rsi = r.RSI();
            List<MediaMappingInfo> mediaMappings = mediaService.getMediaMappingsByMedia(Long.valueOf(context.taskId()),
                    rsi.getNamespace(), rsi.getName(), true);
            if (mediaMappings != null && !mediaMappings.isEmpty()) {
                result.merge(r);
            }
        });

        //statistic after
        readerStatistic.setTimeForFilter(System.currentTimeMillis() - startTime);
        readerStatistic.setRecordsCountAfterFilter(result.getRecords().size());
        return result;
    }

    /**
     * Commit the records,up to the records that have been returned by {@link #poll()}. This
     * method should block until the commit is complete.
     * <p>
     * TaskReaders are not required to implement this functionality; Datalink Framework will record the offsets of the records
     * automatically. This hook is provided for systems that store offsets internally in their own system.
     *
     * @param recordChunk the record batch returned by the poll
     * @throws InterruptedException
     */
    public void commit(RecordChunk<R> recordChunk) throws InterruptedException {

    }

    /**
     * Rollback the records,up to the records that have been returned by {@link #poll()}.This
     * method should block until the rollback is complete.
     * <p>
     *
     * @param recordChunk the record batch returned by the poll
     */
    public void rollback(RecordChunk<R> recordChunk, Throwable t) {

    }

    /**
     * close the task reader
     * 一般执行资源释放操作
     */
    public void close() {

    }
}
