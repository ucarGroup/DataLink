package com.ucar.datalink.writer.rdbms.handle;

import com.ucar.datalink.common.errors.DataLoadException;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.BatchSplitter;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import com.ucar.datalink.writer.rdbms.intercept.DdlEventInterceptor;
import com.ucar.datalink.writer.rdbms.load.LoadResult;
import com.ucar.datalink.writer.rdbms.load.RecordLoaderFactory;
import com.ucar.datalink.writer.rdbms.utils.StatisticKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * RdbEventRecordHandler for RdbmsTaskWriter.
 * <p>
 * Created by lubiao on 2017/3/3.
 */
public class RdbEventRecordHandler extends AbstractHandler<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(RdbEventRecordHandler.class);

    public RdbEventRecordHandler() {
        super();
        this.addInterceptorBefore(new DdlEventInterceptor());
    }

    @Override
    public void doWrite(List<RdbEventRecord> records, TaskWriterContext context) {
        buildSql(records, context);
        RdbmsWriterParameter parameter = (RdbmsWriterParameter) context.getWriterParameter();
        if (parameter.getSyncMode() == RdbmsWriterParameter.SyncMode.TablePartlyOrdered) {
            loadInTableMode(records, context);
        } else if (parameter.getSyncMode() == RdbmsWriterParameter.SyncMode.DbGlobalOrdered) {
            loadInGlobalMode(records, context);
        } else {
            throw new UnsupportedOperationException("Invalid SyncMode " + parameter.getSyncMode());
        }
    }

    //为每个record设置sql
    private void buildSql(List<RdbEventRecord> records, TaskWriterContext context) {
        //statistic before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        long key = RecordMeta.mediaMapping(records.get(0)).getTargetMediaSource().getId();
        writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.SQL_BUILD_RECORDS_COUNT, records.size());
        long startTime = System.currentTimeMillis();

        //do build
        List<List<RdbEventRecord>> batchRecords = BatchSplitter.splitForBatch(records, 100);
        List<Future> results = new ArrayList<>();
        for (List<RdbEventRecord> oneBatch : batchRecords) {
            results.add(executorService.submit(
                    () -> {
                        for (RdbEventRecord record : oneBatch) {
                            SqlBuilder.buildSql(record);
                        }
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
            throw new DatalinkException("something goes wrong when do sql build.", ex);
        }

        //statistic after
        long timeThrough = System.currentTimeMillis() - startTime;
        writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.SQL_BUILD_TIME_THROUGH, timeThrough);
        writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.SQL_BUILD_TIME_PER_RECORD,
                new BigDecimal(((double) timeThrough) / records.size()).setScale(2, RoundingMode.UP).doubleValue());
    }

    private void loadInGlobalMode(List<RdbEventRecord> records, TaskWriterContext context) {
        boolean originalUseBatch = context.getWriterParameter().isUseBatch();
        try {
            context.getWriterParameter().setUseBatch(false);//禁用batch模式，因为按表分组并没有根据相同sql做聚合
            List<List<RdbEventRecord>> list = new ArrayList<>();
            list.add(records);
            doHandle(context, list);
        } finally {
            context.getWriterParameter().setUseBatch(originalUseBatch);//结束时把useBatch设置为之前的值,避免影响其它地方用
        }
    }

    private void loadInTableMode(List<RdbEventRecord> records, TaskWriterContext context) {
        RecordGroupHolder recordGroupHolder = new RecordGroupHolder(records, context);
        recordGroupHolder.loadByGroup(i -> {
            doHandle(context, i);
            return null;
        });
    }

    private void doHandle(TaskWriterContext context, List<List<RdbEventRecord>> records) {
        if (context.getWriterParameter().isDryRun()) {
            doDryRun(context, records);
        } else {
            doLoad(context, records);
        }
    }

    private void doDryRun(TaskWriterContext context, List<List<RdbEventRecord>> records) {
        for (List<RdbEventRecord> item : records) {
            if (CollectionUtils.isEmpty(item)) {
                continue; // 过滤空记录
            }

            for (RdbEventRecord record : item) {
                logger.info("record is :" + record);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doLoad(TaskWriterContext context, List<List<RdbEventRecord>> records) {
        //statistic before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        long startTime = System.currentTimeMillis();

        //do execute
        List<Future<LoadResult>> results = new ArrayList<>();
        for (List<RdbEventRecord> rows : records) {
            if (CollectionUtils.isEmpty(rows)) {
                continue;
            }
            results.add(executorService.submit(
                    () -> RecordLoaderFactory.getLoader(rows.get(0).getClass()).load(context, rows)
            ));
        }

        Throwable ex = null;
        LoadResult errorResult = null;
        for (int i = 0; i < results.size(); i++) {
            Future<LoadResult> result = results.get(i);
            try {
                LoadResult lr = result.get();
                if (lr.getThrowable() != null) {
                    ex = lr.getThrowable();
                    errorResult = lr;
                }
            } catch (Exception e) {
                ex = e;
            }
        }

        if (ex != null) {
            String message = null;
            if (errorResult != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Data Load Failed.");
                sb.append("\r\n");
                sb.append("Media-Mapping-Id is:");
                sb.append("\r\n");
                sb.append(errorResult.getRecord() == null ? "Unknown" : RecordMeta.mediaMapping(errorResult.getRecord()).getId());
                sb.append("\r\n");
                sb.append("Record Content is:");
                sb.append("\r\n");
                sb.append(errorResult.getRecord());
                message = sb.toString();
            } else {
                message = "Data Load Failed.";
            }
            throw new DataLoadException(message, ex);
        } else {
            long timeThrough = System.currentTimeMillis() - startTime;
            long totalRecords = 0;
            long totalSqlTime = 0;
            for (Future<LoadResult> result : results) {
                try {
                    totalRecords += result.get().getTotalRecords();
                    totalSqlTime += result.get().getTotalSqlTime();
                } catch (Exception e) {
                    // do nothing
                }
            }

            if (totalRecords != 0) {
                MediaSourceInfo targetMediaSource = RecordMeta.mediaMapping(records.get(0).get(0)).getTargetMediaSource();
                writerStatistic.getGroupLoadStatistics().get(targetMediaSource.getId()).getExtendStatistic().put(StatisticKey.SQL_EXE_RECORDS_BLOCK_COUNT, records.size());
                writerStatistic.getGroupLoadStatistics().get(targetMediaSource.getId()).getExtendStatistic().put(StatisticKey.SQL_EXE_RECORDS_COUNT, totalRecords);
                writerStatistic.getGroupLoadStatistics().get(targetMediaSource.getId()).getExtendStatistic().put(StatisticKey.SQL_EXE_TIME_THROUGH, timeThrough);
                writerStatistic.getGroupLoadStatistics().get(targetMediaSource.getId()).getExtendStatistic().put(StatisticKey.SQL_EXE_TIME_TOTAL, totalSqlTime);
                writerStatistic.getGroupLoadStatistics().get(targetMediaSource.getId()).getExtendStatistic().put(StatisticKey.SQL_EXE_TIME_PER_RECORD,
                        new BigDecimal(((double) totalSqlTime) / totalRecords).setScale(2, RoundingMode.UP).doubleValue());
                if (timeThrough == 0) {
                    writerStatistic.getGroupLoadStatistics().get(targetMediaSource.getId()).getExtendStatistic().put(StatisticKey.SQL_EXE_TPS, 0);
                } else {
                    writerStatistic.getGroupLoadStatistics().get(targetMediaSource.getId()).getExtendStatistic().put(StatisticKey.SQL_EXE_TPS,
                            new BigDecimal(totalRecords / (((double) timeThrough) / 1000)).longValue());
                }

            }
        }
    }
}
