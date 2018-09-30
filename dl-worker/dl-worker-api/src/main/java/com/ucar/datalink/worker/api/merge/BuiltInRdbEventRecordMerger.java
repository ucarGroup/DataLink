package com.ucar.datalink.worker.api.merge;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventColumnIndexComparable;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.task.RecordChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * 合并相同tableId的变更记录.
 * pk相同的多条变更数据合并后的结果是：
 * 1, I
 * 2, U
 * 3, D
 * 如果有一条I，多条U，merge成I;
 * 如果有多条U，取最晚的那条;
 * </pre>
 *
 * @author lubiao
 *         参考自Alibaba-Otter的：com.alibaba.otter.node.etl.load.loader.db.DbLoadMerger
 */
public class BuiltInRdbEventRecordMerger implements Merger<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(BuiltInRdbEventRecordMerger.class);

    @Override
    public RecordChunk<RdbEventRecord> merge(RecordChunk<RdbEventRecord> recordChunk) {
        RecordChunk<RdbEventRecord> newChunk = recordChunk.copyWithoutRecords();
        newChunk.setRecords(merge(recordChunk.getRecords()));
        return newChunk;
    }

    /**
     * 将一批数据进行根据table+主键信息进行合并，保证一个表的一个pk记录只有一条结果
     */
    public List<RdbEventRecord> merge(List<RdbEventRecord> records) {
        Map<RowKey, RdbEventRecord> result = new LinkedHashMap<>();
        for (RdbEventRecord eventData : records) {
            merge(eventData, result);
        }
        return new LinkedList<>(result.values());
    }

    void merge(RdbEventRecord record, Map<RowKey, RdbEventRecord> result) {
        EventType eventType = record.getEventType();
        switch (eventType) {
            case INSERT:
                mergeInsert(record, result);
                break;
            case UPDATE:
                mergeUpdate(record, result);
                break;
            case DELETE:
                mergeDelete(record, result);
                break;
            default:
                break;
        }
    }

    private void mergeInsert(RdbEventRecord record, Map<RowKey, RdbEventRecord> result) {
        // insert无主键变更的处理
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        RowKey rowKey = new RowKey(mappingInfo.getId(), record.getSchemaName(), record.getTableName(),
                record.getKeys());
        if (!result.containsKey(rowKey)) {
            result.put(rowKey, record);
        } else {
            RdbEventRecord oldEventData = result.get(rowKey);
            // 如果上一条变更是delete的，就直接用insert替换
            if (oldEventData.getEventType() == EventType.DELETE) {
                result.put(rowKey, record);
            } else if (oldEventData.getEventType() == EventType.UPDATE
                    || oldEventData.getEventType() == EventType.INSERT) {
                // insert之前出现了update逻辑上不可能，唯一的可能性是分布式DB场景下，冗余表在每个库中都有记录
                // sql,不存在即插入
                logger.debug("update-insert/insert-insert happend. before[{}] , after[{}]", oldEventData, record);
                // 如果上一条变更是update的，就用insert替换，并且把上一条存在而这一条不存在的字段值拷贝到这一条中
                RdbEventRecord mergeEventData = replaceColumnValue(record, oldEventData);
                mergeEventData.getOldKeys().clear();// 清空oldkeys，insert记录不需要
                result.put(rowKey, mergeEventData);
            }
        }
    }

    private void mergeUpdate(RdbEventRecord record, Map<RowKey, RdbEventRecord> result) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        RowKey rowKey = new RowKey(mappingInfo.getId(), record.getSchemaName(), record.getTableName(),
                record.getKeys());
        if (!CollectionUtils.isEmpty(record.getOldKeys())) {// 存在主键变更
            // 需要解决(1->2 , 2->3)级联主键变更的问题
            RowKey oldKey = new RowKey(mappingInfo.getId(), record.getSchemaName(), record.getTableName(),
                    record.getOldKeys());
            if (!result.containsKey(oldKey)) {// 不需要级联
                result.put(rowKey, record);
            } else {
                RdbEventRecord oldEventData = result.get(oldKey);
                // 如果上一条变更是insert的，就把这一条的eventType改成insert，并且把上一条存在而这一条不存在的字段值拷贝到这一条中
                if (oldEventData.getEventType() == EventType.INSERT) {
                    record.setEventType(EventType.INSERT);
                    // 删除当前变更数据老主键的记录.
                    result.remove(oldKey);

                    RdbEventRecord mergeEventData = replaceColumnValue(record, oldEventData);
                    mergeEventData.getOldKeys().clear();// 清空oldkeys，insert记录不需要
                    result.put(rowKey, mergeEventData);
                } else if (oldEventData.getEventType() == EventType.UPDATE) {
                    // 删除当前变更数据老主键的记录.
                    result.remove(oldKey);

                    // 如果上一条变更是update的，把上一条存在而这一条不存在的数据拷贝到这一条中
                    RdbEventRecord mergeEventData = replaceColumnValue(record, oldEventData);
                    result.put(rowKey, mergeEventData);
                } else {
                    throw new DatalinkException("delete(has old pks) + update impossible happed!");
                }
            }
        } else {
            if (!result.containsKey(rowKey)) {// 没有主键变更
                result.put(rowKey, record);
            } else {
                RdbEventRecord oldEventData = result.get(rowKey);
                // 如果上一条变更是insert的，就把这一条的eventType改成insert，并且把上一条存在而这一条不存在的字段值拷贝到这一条中
                if (oldEventData.getEventType() == EventType.INSERT) {
                    record.setEventType(EventType.INSERT);

                    RdbEventRecord mergeEventData = replaceColumnValue(record, oldEventData);
                    result.put(rowKey, mergeEventData);
                } else if (oldEventData.getEventType() == EventType.UPDATE) {// 可能存在
                    // 1->2
                    // ,
                    // 2update的问题

                    // 如果上一条变更是update的，把上一条存在而这一条不存在的数据拷贝到这一条中
                    RdbEventRecord mergeEventData = replaceColumnValue(record, oldEventData);
                    result.put(rowKey, mergeEventData);
                } else if (oldEventData.getEventType() == EventType.DELETE) {
                    //异常情况，出现 delete + update，那就直接更新为update
                    result.put(rowKey, record);
                }
            }
        }
    }

    private void mergeDelete(RdbEventRecord record, Map<RowKey, RdbEventRecord> result) {
        // 只保留pks，把columns去掉. 以后针对数据仓库可以开放delete columns记录
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        RowKey rowKey = new RowKey(mappingInfo.getId(), record.getSchemaName(), record.getTableName(),
                record.getKeys());
        if (!result.containsKey(rowKey)) {
            result.put(rowKey, record);
        } else {
            RdbEventRecord oldEventData = result.get(rowKey);
            if (!CollectionUtils.isEmpty(oldEventData.getOldKeys())) {// 存在主键变更
                // insert/update -> delete记录组合时，delete的对应的pk为上一条记录的pk
                record.setKeys(oldEventData.getOldKeys());
                record.getOldKeys().clear();// 清除oldKeys

                result.remove(rowKey);// 删除老的对象
                result.put(new RowKey(mappingInfo.getId(), record.getSchemaName(), record.getTableName(),
                        record.getKeys()), record); // key发生变化，需要重新构造一个RowKey
            } else {
                record.getOldKeys().clear();// 清除oldKeys
                result.put(rowKey, record);
            }

        }
    }

    /**
     * 把old中的值存在而new中不存在的值合并到new中,并且把old中的变更前的主键保存到new中的变更前的主键.
     */
    private RdbEventRecord replaceColumnValue(RdbEventRecord newRecord, RdbEventRecord oldRecord) {
        List<EventColumn> newColumns = newRecord.getColumns();
        List<EventColumn> oldColumns = oldRecord.getColumns();
        List<EventColumn> temp = new ArrayList<EventColumn>();
        for (EventColumn oldColumn : oldColumns) {
            boolean contain = false;
            for (EventColumn newColumn : newColumns) {
                if (oldColumn.getColumnName().equalsIgnoreCase(newColumn.getColumnName())) {
                    newColumn.setUpdate(newColumn.isUpdate() || oldColumn.isUpdate());// 合并isUpdate字段
                    contain = true;
                }
            }

            if (!contain) {
                temp.add(oldColumn);
            }
        }
        newColumns.addAll(temp);
        Collections.sort(newColumns, new EventColumnIndexComparable()); // 排序
        // 把上一次变更的旧主键传递到这次变更的旧主键.
        newRecord.setOldKeys(oldRecord.getOldKeys());

        return newRecord;
    }

    public static class RowKey implements Serializable {

        private static final long serialVersionUID = -7369951798499581038L;

        private Long mappingId;
        private String schemaName;
        private String tableName;
        private List<EventColumn> keyEvents = new ArrayList<>();
        private List<ColumnValue> keyValues = new ArrayList<>();

        //EventColumn代表的是变更事件，不是列本身，比如isUpdate方法在不同的事件场景下可能为true，也可能为false
        //所以，我们在进行merge操作，判断RdbEventRecord是否是相同记录的变更事件时，不能用主键的EventColumn对象做比较，而应该直接用主键值做比较
        //如果我们用EventColumn对象做对比，那么对于同一条表数据，插入时主键列的EventColumn的isUpdate为true，非主键更新时主键列的EventColumn的isUpdate为false
        //此时通过EventColumn的equals方法做判断，是不相等的，那么结果就是merge失败（相同记录的事件没有合并到一起）
        public RowKey(Long mappingId, String schemaName, String tableName, List<EventColumn> keyEvents) {
            this.mappingId = mappingId;
            this.schemaName = schemaName;
            this.tableName = tableName;
            this.keyEvents = keyEvents;
            this.keyValues = keyEvents.stream().map(c -> new ColumnValue(c.getColumnName(), c.getColumnValue())).collect(Collectors.toList());
        }

        public List<EventColumn> getKeyEvents() {
            return keyEvents;
        }

        public void setKeyEvents(List<EventColumn> keyEvents) {
            this.keyEvents = keyEvents;
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

        public Long getMappingId() {
            return mappingId;
        }

        public void setMappingId(Long mappingId) {
            this.mappingId = mappingId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RowKey rowKey = (RowKey) o;

            if (!mappingId.equals(rowKey.mappingId)) return false;
            if (!schemaName.equals(rowKey.schemaName)) return false;
            if (!tableName.equals(rowKey.tableName)) return false;
            return keyValues.equals(rowKey.keyValues);

        }

        @Override
        public int hashCode() {
            int result = mappingId.hashCode();
            result = 31 * result + schemaName.hashCode();
            result = 31 * result + tableName.hashCode();
            result = 31 * result + keyValues.hashCode();
            return result;
        }
    }

    public static class ColumnValue implements Serializable {
        private String columnName;
        private String columnValue;

        public ColumnValue(String columnName, String columnValue) {
            this.columnName = columnName;
            this.columnValue = columnValue;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnValue() {
            return columnValue;
        }

        public void setColumnValue(String columnValue) {
            this.columnValue = columnValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ColumnValue that = (ColumnValue) o;

            if (!columnName.equals(that.columnName)) return false;
            return columnValue.equals(that.columnValue);

        }

        @Override
        public int hashCode() {
            int result = columnName.hashCode();
            result = 31 * result + columnValue.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ColumnValue{" +
                    "columnName='" + columnName + '\'' +
                    ", columnValue='" + columnValue + '\'' +
                    '}';
        }
    }
}
