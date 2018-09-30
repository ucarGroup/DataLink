package com.ucar.datalink.writer.rdbms.load;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobCreator;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * 数据加载器，数据写入的最后一个环节，执行数据持久化操作
 * 非线程安全
 * <p>
 * Created by lubiao on 2017/3/14.
 */
public abstract class RecordLoader<T extends Record> {

    private static final Logger logger = LoggerFactory.getLogger(RecordLoader.class);

    /**
     * 执行最终的数据写入
     *
     * @param records 如果WriterParameter的useBatch为true,则调用者需保证传入的records已经是按batchSize分组后的数据,
     *                Loader just do load.
     */
    public LoadResult load(TaskWriterContext context, List<T> records) {
        try {
            return doCall(
                    context,
                    records,
                    DbDialectFactory.getDbDialect(RecordMeta.mediaMapping(records.get(0)).getTargetMediaSource())
            );
        } catch (RecordLoadException re) {
            return new LoadResult(re.getCause(), re.getRecord());
        } catch (Throwable t) {
            return new LoadResult(t, null);
        }
    }

    private LoadResult doCall(TaskWriterContext context, List<T> records, DbDialect dbDialect) {
        if (context.getWriterParameter().isUseBatch()) {
            try {
                return loadInBatch(context, records, dbDialect);
            } catch (Throwable t) {
                //批量模式出错，转为single模式，保证：
                //1.尽可能把不报错的数据写入到目标库
                //2.改为single模式，才能知道具体出错的record是哪一条
                logger.error("something goes wrong when load records with batch, now try to load with single.", t);
                return loadInSingle(context, records, dbDialect);
            }
        } else {
            return loadInSingle(context, records, dbDialect);
        }
    }

    protected abstract String getSql(T record);

    protected abstract void transactionBegin();

    protected abstract void transactionEnd();

    protected abstract void fillPreparedStatement(PreparedStatement ps, LobCreator lobCreator, T record, DbDialect dbDialect, boolean isSyncAutoAddColumn) throws SQLException;

    private LoadResult loadInBatch(TaskWriterContext context, List<T> records, DbDialect dbDialect) {
        //statistic before
        LoadResult loadResult = new LoadResult();
        long startTime = System.currentTimeMillis();

        //do load
        final String sql = getSql(records.get(0));//一个batch内的sql都是相同的,取第一个即可
        dbDialect.getTransactionTemplate().execute((transactionStatus -> {
            final LobCreator lobCreator = dbDialect.getLobHandler().getLobCreator();
            try {
                transactionBegin();
                JdbcTemplate template = dbDialect.getJdbcTemplate();
                int[] result = template.batchUpdate(sql, new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int idx) throws SQLException {
                        fillPreparedStatement(ps, lobCreator, records.get(idx), dbDialect, context.getWriterParameter().isSyncAutoAddColumn());
                    }

                    public int getBatchSize() {
                        return records.size();
                    }
                });
                transactionEnd();
                return result;
            } finally {
                lobCreator.close();
            }
        }));

        //statistic after
        long totalTime = System.currentTimeMillis() - startTime;
        loadResult.setTotalRecords(records.size());
        loadResult.setTotalSqlTime(totalTime);
        loadResult.setAvgSqlTime(totalTime / records.size());

        return loadResult;
    }

    private LoadResult loadInSingle(TaskWriterContext context, List<T> records, DbDialect dbDialect) {
        //statistic before
        LoadResult loadResult = new LoadResult();
        long startTime = System.currentTimeMillis();

        //do load
        LobCreator lobCreator = dbDialect.getLobHandler().getLobCreator();
        try {
            for (T record : records) {
                try {
                    dbDialect.getTransactionTemplate().execute(transactionStatus -> {
                        transactionBegin();
                        JdbcTemplate template = dbDialect.getJdbcTemplate();
                        loadOne(context, record, dbDialect, lobCreator, template);
                        transactionEnd();
                        return 0;
                    });
                } catch (Throwable e) {
                    if (e instanceof DuplicateKeyException) {
                        logger.warn("DuplicateKeyException for record load :" + e.getMessage());
                    } else {
                        throw new RecordLoadException(record, e);
                    }
                }
            }
        } finally {
            lobCreator.close();
        }

        //statistic after
        long totalTime = System.currentTimeMillis() - startTime;
        loadResult.setTotalRecords(records.size());
        loadResult.setTotalSqlTime(totalTime);
        loadResult.setAvgSqlTime(totalTime / records.size());

        return loadResult;
    }

    private int loadOne(TaskWriterContext context, T record, DbDialect dbDialect, LobCreator lobCreator, JdbcTemplate template) {
        return template.update(getSql(record), ps -> {
            fillPreparedStatement(ps, lobCreator, record, dbDialect, context.getWriterParameter().isSyncAutoAddColumn());
        });
    }

    static class RecordLoadException extends RuntimeException {
        private Record record;

        RecordLoadException(Record record, Throwable cause) {
            super(cause);
            this.record = record;
        }

        public Record getRecord() {
            return record;
        }
    }
}