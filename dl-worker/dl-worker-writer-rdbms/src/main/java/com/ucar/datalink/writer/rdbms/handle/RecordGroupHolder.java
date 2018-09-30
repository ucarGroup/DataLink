package com.ucar.datalink.writer.rdbms.handle;

import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import com.ucar.datalink.writer.rdbms.utils.StatisticKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * 将RdbEventRecord按表进行分组
 * 1.当启用了压缩合并时，会根据INSERT/UPDATE/DELETE进行更细粒度的分组
 * 2.当没有启用压缩合并时，只在表级别进行分组，保证单表内的局部有序
 * <p>
 * Created by lubiao on 2017/3/8.
 */
public class RecordGroupHolder {
    private static final Logger logger = LoggerFactory.getLogger(RecordGroupHolder.class);

    private List<TableLoadData> tables = new ArrayList<>();
    private TaskWriterContext context;

    public RecordGroupHolder(List<RdbEventRecord> records, TaskWriterContext context) {
        this.context = context;
        this.doGroup(records, context);
    }

    public void loadByGroup(Function<List<List<RdbEventRecord>>, Void> function) {
        if (context.getWriterParameter().isMerging()) {
            // 优先执行delete语句，针对uniqe更新，一般会进行delete + insert的处理模式，避免并发更新
            loadDeletedRecords(function);
            loadInsertAndUpdatedRecords(function);
        } else {
            boolean originalUseBatch = context.getWriterParameter().isUseBatch();
            try {
                context.getWriterParameter().setUseBatch(false);//禁用batch模式，因为按表分组并没有根据相同sql做聚合
                loadRecordsByTable(function);
            } finally {
                context.getWriterParameter().setUseBatch(originalUseBatch);//结束时把useBatch设置为之前的值,避免影响其它地方用
            }
        }
    }

    private void doGroup(List<RdbEventRecord> records, TaskWriterContext context) {
        //statistic before
        WriterStatistic writerStatistic = context.taskWriterSession().getData(WriterStatistic.KEY);
        long key = RecordMeta.mediaMapping(records.get(0)).getTargetMediaSource().getId();
        writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.TABLE_GROUP_RECORDS_COUNT, records.size());
        long startTime = System.currentTimeMillis();

        //do group
        records.stream().forEach(r -> groupForOneRecord(r));
        logger.debug("Table Size in this Batch is " + tables.size());

        //statistic after
        long timeThrough = System.currentTimeMillis() - startTime;
        writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.TABLE_GROUP_TIME_THROUGH, timeThrough);
        writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.TABLE_GROUP_TABLE_COUNT, tables.size());
        writerStatistic.getGroupLoadStatistics().get(key).getExtendStatistic().put(StatisticKey.TABLE_GROUP_TIME_PER_RECORD,
                new BigDecimal(((double) timeThrough) / records.size()).setScale(2, RoundingMode.UP).doubleValue());
    }

    private void groupForOneRecord(RdbEventRecord record) {
        TableLoadData tableData = findTableData(record);

        if (context.getWriterParameter().isMerging()) {
            EventType type = record.getEventType();
            if (type.isInsert()) {
                tableData.getInsertDatas().add(record);
            } else if (type.isUpdate()) {
                tableData.getUpdateDatas().add(record);
            } else if (type.isDelete()) {
                tableData.getDeleteDatas().add(record);
            }
        } else {
            tableData.getTableDatas().add(record);
        }
    }

    private synchronized TableLoadData findTableData(RdbEventRecord record) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        RowKey key = new RowKey(mappingInfo.getId(), record.getSchemaName(), record.getTableName());

        for (TableLoadData table : tables) {
            if (table.getRowKey().equals(key)) {
                return table;
            }
        }

        TableLoadData data = new TableLoadData(key);
        tables.add(data);
        return data;
    }

    private void loadDeletedRecords(Function<List<List<RdbEventRecord>>, Void> function) {
        List<List<RdbEventRecord>> batchRecords = new ArrayList<>();

        for (RecordGroupHolder.TableLoadData tableData : this.tables) {
            if (context.getWriterParameter().isUseBatch()) {
                batchRecords.addAll(split(tableData.getDeleteDatas(), context.getWriterParameter().getBatchSize()));
            } else {
                for (RdbEventRecord data : tableData.getDeleteDatas()) {
                    batchRecords.add(Arrays.asList(data));
                }
            }
        }
        function.apply(batchRecords);
    }

    private void loadInsertAndUpdatedRecords(Function<List<List<RdbEventRecord>>, Void> function) {
        List<List<RdbEventRecord>> batchRecords = new ArrayList<>();

        for (RecordGroupHolder.TableLoadData tableData : this.tables) {
            if (context.getWriterParameter().isUseBatch()) {
                batchRecords.addAll(split(tableData.getInsertDatas(), context.getWriterParameter().getBatchSize()));
                batchRecords.addAll(split(tableData.getUpdateDatas(), context.getWriterParameter().getBatchSize()));
            } else {
                for (RdbEventRecord data : tableData.getInsertDatas()) {
                    batchRecords.add(Arrays.asList(data));
                }
                for (RdbEventRecord data : tableData.getUpdateDatas()) {
                    batchRecords.add(Arrays.asList(data));
                }
            }
        }
        function.apply(batchRecords);
    }

    private void loadRecordsByTable(Function<List<List<RdbEventRecord>>, Void> function) {
        List<List<RdbEventRecord>> batchRecords = new ArrayList<>();
        for (RecordGroupHolder.TableLoadData tableData : this.tables) {
            batchRecords.add(tableData.getTableDatas());//一张表一个分组
        }

        function.apply(batchRecords);
    }

    /**
     * 将对应的数据按照sql相同进行batch组合
     */
    private List<List<RdbEventRecord>> split(List<RdbEventRecord> records, int batchSize) {
        List<List<RdbEventRecord>> result = new ArrayList<>();
        if (records == null || records.size() == 0) {
            return result;
        } else {
            int[] bits = new int[records.size()];// 初始化一个标记，用于标明对应的记录是否已分入某个batch
            for (int i = 0; i < bits.length; i++) {
                // 跳过已经被分入batch的Record
                while (i < bits.length && bits[i] == 1) {
                    i++;
                }

                if (i >= bits.length) { // 已处理完成，退出
                    break;
                }

                // 开始添加batch，最大只加入batchSize个数的对象
                List<RdbEventRecord> batch = new ArrayList<>();
                bits[i] = 1;
                batch.add(records.get(i));
                for (int j = i + 1; j < bits.length && batch.size() < batchSize; j++) {
                    if (bits[j] == 0 && sameSql(records.get(i), records.get(j))) {//相同的sql才能放入一个batch
                        batch.add(records.get(j));
                        bits[j] = 1;// 修改为已加入
                    }
                }
                result.add(batch);
            }

            return result;
        }
    }

    /**
     * 判断两条记录是否可以作为一个batch提交，主要判断sql是否相等.
     * 因为sqlTemplate构造sql时用了String.intern()的操作，保证相同字符串的引用是同一个
     * 所以可以直接使用==进行判断，提升效率
     */
    private boolean sameSql(RdbEventRecord source, RdbEventRecord target) {
        return source.getSql() == target.getSql();
    }

    /**
     * 按table进行分类
     */
    private static class TableLoadData {
        private RowKey rowKey;
        private List<RdbEventRecord> insertDatas = new LinkedList<>();
        private List<RdbEventRecord> updateDatas = new LinkedList<>();
        private List<RdbEventRecord> deleteDatas = new LinkedList<>();
        private List<RdbEventRecord> tableDatas = new LinkedList<>();

        public TableLoadData(RowKey rowKey) {
            this.rowKey = rowKey;
        }

        public RowKey getRowKey() {
            return rowKey;
        }

        public List<RdbEventRecord> getInsertDatas() {
            return insertDatas;
        }

        public List<RdbEventRecord> getUpdateDatas() {
            return updateDatas;
        }

        public List<RdbEventRecord> getDeleteDatas() {
            return deleteDatas;
        }

        public List<RdbEventRecord> getTableDatas() {
            return tableDatas;
        }
    }

    /**
     * Record的唯一性标识
     */
    public static class RowKey implements Serializable {
        private Long mappingId;
        private String schemaName;
        private String tableName;

        public RowKey(Long mappingId, String schemaName, String tableName) {
            this.mappingId = mappingId;
            this.schemaName = schemaName;
            this.tableName = tableName;
        }

        public Long getMappingId() {
            return mappingId;
        }

        public void setMappingId(Long mappingId) {
            this.mappingId = mappingId;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RowKey rowKey = (RowKey) o;

            if (!mappingId.equals(rowKey.mappingId)) return false;
            if (!schemaName.equals(rowKey.schemaName)) return false;
            return tableName.equals(rowKey.tableName);

        }

        @Override
        public int hashCode() {
            int result = mappingId.hashCode();
            result = 31 * result + schemaName.hashCode();
            result = 31 * result + tableName.hashCode();
            return result;
        }
    }
}
