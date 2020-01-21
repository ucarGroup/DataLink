package com.ucar.datalink.worker.api.merge;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2017/10/26.
 */
public class BuiltInRdbEventRecordMergerTest {

    private static final int COLUMN_TYPE = 1;

    private static final long MAPPING_ID = 10;

    private static final String SCHEMA_NAME = "test";

    private static final String TABLE_NAME = "test";

    private static final String KEY_NAME = "id";

    private static final String KEY_VALUE = "100";

    private static final String KEY_VALUE_NEW1 = "1001";
    private static final String KEY_VALUE_NEW2 = "1002";
    private static final String KEY_VALUE_NEW3 = "1003";
    private static final String KEY_VALUE_NEW4 = "1004";

    private static final String[] COLUMN_NAMES1 = {"name", "password"};

    private static final String[] COLUMN_NAMES2 = {"name", "age"};

    private static final BuiltInRdbEventRecordMerger merger = new BuiltInRdbEventRecordMerger();

    @Test
    public void testRowKey() {
        HashMap<BuiltInRdbEventRecordMerger.RowKey, String> map = new HashMap<>();

        EventColumn e1 = makeEventColumn("c1", "v1", true);
        e1.setUpdate(true);
        EventColumn e2 = makeEventColumn("c2", "v2", true);
        e2.setUpdate(true);
        List<EventColumn> list1 = Lists.newArrayList(e1, e2);
        BuiltInRdbEventRecordMerger.RowKey key1 = new BuiltInRdbEventRecordMerger.RowKey(1L, "ss", "tt", list1);

        EventColumn e3 = makeEventColumn("c1", "v1", true);
        e3.setUpdate(false);
        EventColumn e4 = makeEventColumn("c2", "v2", true);
        e4.setUpdate(false);
        List<EventColumn> list2 = Lists.newArrayList(e3, e4);
        BuiltInRdbEventRecordMerger.RowKey key2 = new BuiltInRdbEventRecordMerger.RowKey(1L, "ss", "tt", list2);

        Assert.assertEquals(key1, key2);
        Assert.assertEquals(key1.hashCode(), key2.hashCode());

        map.put(key1, "888");
        map.put(key2, "999");
        Assert.assertEquals(map.size(), 1);
    }

    /**
     * 测试insert+update
     */
    @Test
    public void testMergeWithSameKeyOfIU() {
        Map<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> mergeMap = new MapMaker().makeMap();
        merger.merge(makeInsertEventData(), mergeMap);
        merger.merge(makeUpdateEventData(), mergeMap);

        for (Map.Entry<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> entry : mergeMap.entrySet()) {
            BuiltInRdbEventRecordMerger.RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeyEvents().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            RdbEventRecord rdbEventRecord = entry.getValue();

            Assert.assertEquals(SCHEMA_NAME, rdbEventRecord.getSchemaName());
            Assert.assertEquals(TABLE_NAME, rdbEventRecord.getTableName());
            Assert.assertEquals(MAPPING_ID, RecordMeta.mediaMapping(rdbEventRecord).getId().longValue());
            Assert.assertEquals(EventType.INSERT, rdbEventRecord.getEventType());
            Assert.assertEquals(rdbEventRecord.getOldKeys().size(), 0); // 不存在oldKeys

            List<EventColumn> columns = rdbEventRecord.getColumns();
            Assert.assertEquals(3, columns.size());
        }
    }

    /**
     * 测试insert+update+delete
     */
    @Test
    public void testMergeWithSameKeyOfIUD() {
        Map<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> mergeMap = new MapMaker().makeMap();
        merger.merge(makeInsertEventData(), mergeMap);
        merger.merge(makeUpdateEventData(), mergeMap);
        merger.merge(makeDeleteEventData(), mergeMap);

        for (Map.Entry<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> entry : mergeMap.entrySet()) {
            BuiltInRdbEventRecordMerger.RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeyEvents().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            RdbEventRecord rdbEventRecord = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, rdbEventRecord.getSchemaName());
            Assert.assertEquals(TABLE_NAME, rdbEventRecord.getTableName());
            Assert.assertEquals(MAPPING_ID, RecordMeta.mediaMapping(rdbEventRecord).getId().longValue());
            Assert.assertEquals(EventType.DELETE, rdbEventRecord.getEventType());
            Assert.assertEquals(rdbEventRecord.getOldKeys().size(), 0); // 不存在oldKeys

            List<EventColumn> columns = rdbEventRecord.getColumns();
            Assert.assertEquals(0, columns.size());
        }
    }

    /**
     * 测试insert+update+delete+insert
     */
    @Test
    public void testMergeWithSameKeyOfIUDI() {
        Map<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> mergeMap = new MapMaker().makeMap();
        merger.merge(makeInsertEventData(), mergeMap);
        merger.merge(makeUpdateEventData(), mergeMap);
        merger.merge(makeDeleteEventData(), mergeMap);
        merger.merge(makeInsertEventData(), mergeMap);

        for (Map.Entry<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> entry : mergeMap.entrySet()) {
            BuiltInRdbEventRecordMerger.RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeyEvents().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            RdbEventRecord rdbEventRecord = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, rdbEventRecord.getSchemaName());
            Assert.assertEquals(TABLE_NAME, rdbEventRecord.getTableName());
            Assert.assertEquals(MAPPING_ID, RecordMeta.mediaMapping(rdbEventRecord).getId().longValue());
            Assert.assertEquals(EventType.INSERT, rdbEventRecord.getEventType());
            Assert.assertEquals(rdbEventRecord.getOldKeys().size(), 0); // 不存在oldKeys

            List<EventColumn> columns = rdbEventRecord.getColumns();
            Assert.assertEquals(2, columns.size());
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Update/Update
     */
    @Test
    public void testMergeWithUpdateKeyOfUU() {
        Map<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> mergeMap = new MapMaker().makeMap();
        merger.merge(makeUpdateEventData(KEY_VALUE, KEY_VALUE_NEW1), mergeMap);
        merger.merge(makeUpdateEventData(KEY_VALUE_NEW1, KEY_VALUE_NEW2), mergeMap);

        for (Map.Entry<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> entry : mergeMap.entrySet()) {
            BuiltInRdbEventRecordMerger.RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeyEvents().get(0);
            Assert.assertEquals(KEY_VALUE_NEW2, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            RdbEventRecord rdbEventRecord = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, rdbEventRecord.getSchemaName());
            Assert.assertEquals(TABLE_NAME, rdbEventRecord.getTableName());
            Assert.assertEquals(MAPPING_ID, RecordMeta.mediaMapping(rdbEventRecord).getId().longValue());
            Assert.assertEquals(EventType.UPDATE, rdbEventRecord.getEventType());

            List<EventColumn> oldKeys = rdbEventRecord.getOldKeys();
            List<EventColumn> keys = rdbEventRecord.getKeys();
            Assert.assertNotSame(oldKeys, keys);
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Update/Update/delete
     */
    @Test
    public void testMergeWithUpdateKeyOfUUD() {
        Map<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> mergeMap = new MapMaker().makeMap();
        merger.merge(makeUpdateEventData(KEY_VALUE_NEW1, KEY_VALUE_NEW2), mergeMap);
        merger.merge(makeUpdateEventData(KEY_VALUE_NEW2, KEY_VALUE), mergeMap);
        merger.merge(makeDeleteEventData(), mergeMap);

        for (Map.Entry<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> entry : mergeMap.entrySet()) {
            BuiltInRdbEventRecordMerger.RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeyEvents().get(0);
            Assert.assertEquals(KEY_VALUE_NEW1, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            RdbEventRecord rdbEventRecord = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, rdbEventRecord.getSchemaName());
            Assert.assertEquals(TABLE_NAME, rdbEventRecord.getTableName());
            Assert.assertEquals(MAPPING_ID, RecordMeta.mediaMapping(rdbEventRecord).getId().longValue());
            Assert.assertEquals(EventType.DELETE, rdbEventRecord.getEventType());
            Assert.assertEquals(rdbEventRecord.getOldKeys().size(), 0); // 不存在oldKeys
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Insert/Update/Update/Update/Update
     */
    @Test
    public void testMergeWithUpdateKeyOfIUUUU() {
        Map<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> mergeMap = new MapMaker().makeMap();
        merger.merge(makeInsertEventData(), mergeMap);
        merger.merge(makeUpdateEventData(KEY_VALUE, KEY_VALUE_NEW1), mergeMap);
        merger.merge(makeUpdateEventData(KEY_VALUE_NEW1, KEY_VALUE_NEW2), mergeMap);
        merger.merge(makeUpdateEventData(KEY_VALUE_NEW2, KEY_VALUE_NEW3), mergeMap);
        merger.merge(makeUpdateEventData(KEY_VALUE_NEW3, KEY_VALUE_NEW4), mergeMap);

        for (Map.Entry<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> entry : mergeMap.entrySet()) {
            BuiltInRdbEventRecordMerger.RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeyEvents().get(0);
            Assert.assertEquals(KEY_VALUE_NEW4, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            RdbEventRecord rdbEventRecord = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, rdbEventRecord.getSchemaName());
            Assert.assertEquals(TABLE_NAME, rdbEventRecord.getTableName());
            Assert.assertEquals(MAPPING_ID, RecordMeta.mediaMapping(rdbEventRecord).getId().longValue());
            Assert.assertEquals(EventType.INSERT, rdbEventRecord.getEventType());
            Assert.assertEquals(rdbEventRecord.getOldKeys().size(), 0); // 不存在oldKeys
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Update/Update/Insert
     */
    @Test
    public void testMergeWithUpdateKeyOfUI() {
        Map<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> mergeMap = new MapMaker().makeMap();
        merger.merge(makeUpdateEventData(KEY_VALUE_NEW1, KEY_VALUE_NEW2), mergeMap);
        merger.merge(makeUpdateEventData(KEY_VALUE_NEW2, KEY_VALUE), mergeMap);
        merger.merge(makeInsertEventData(), mergeMap);

        for (Map.Entry<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> entry : mergeMap.entrySet()) {
            BuiltInRdbEventRecordMerger.RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeyEvents().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            RdbEventRecord rdbEventRecord = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, rdbEventRecord.getSchemaName());
            Assert.assertEquals(TABLE_NAME, rdbEventRecord.getTableName());
            Assert.assertEquals(MAPPING_ID, RecordMeta.mediaMapping(rdbEventRecord).getId().longValue());
            Assert.assertEquals(EventType.INSERT, rdbEventRecord.getEventType());

            List<EventColumn> oldKeys = rdbEventRecord.getOldKeys();
            List<EventColumn> keys = rdbEventRecord.getKeys();

            Assert.assertNotSame(oldKeys, keys);
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Insert/Insert
     */
    @Test
    public void testMergeWithUpdateKeyOfII() {
        Map<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> mergeMap = new MapMaker().makeMap();
        merger.merge(makeInsertEventData(), mergeMap);
        merger.merge(makeInsertEventData(), mergeMap);

        for (Map.Entry<BuiltInRdbEventRecordMerger.RowKey, RdbEventRecord> entry : mergeMap.entrySet()) {
            BuiltInRdbEventRecordMerger.RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeyEvents().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            RdbEventRecord rdbEventRecord = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, rdbEventRecord.getSchemaName());
            Assert.assertEquals(TABLE_NAME, rdbEventRecord.getTableName());
            Assert.assertEquals(MAPPING_ID, RecordMeta.mediaMapping(rdbEventRecord).getId().longValue());
            Assert.assertEquals(EventType.INSERT, rdbEventRecord.getEventType());

            List<EventColumn> oldKeys = rdbEventRecord.getOldKeys();
            List<EventColumn> keys = rdbEventRecord.getKeys();

            Assert.assertNotSame(oldKeys, keys);
        }
    }

    private RdbEventRecord makeInsertEventData() {
        RdbEventRecord rdbEventRecord = new RdbEventRecord();
        rdbEventRecord.setEventType(EventType.INSERT);
        rdbEventRecord.setSchemaName(SCHEMA_NAME);
        rdbEventRecord.setTableName(TABLE_NAME);
        rdbEventRecord.RSI();

        MediaMappingInfo mappingInfo = new MediaMappingInfo();
        mappingInfo.setId(MAPPING_ID);
        RecordMeta.attachMediaMapping(rdbEventRecord, mappingInfo);

        List<EventColumn> keys = new ArrayList<EventColumn>();
        keys.add(makeEventColumn(KEY_NAME, KEY_VALUE, true));
        keys.stream().forEach(k -> k.setUpdate(true));
        rdbEventRecord.setKeys(keys);

        List<EventColumn> columns = new ArrayList<EventColumn>();
        int i = 0;
        for (String columnName : COLUMN_NAMES1) {
            columns.add(makeEventColumn(columnName, columnName + i, false));
        }
        rdbEventRecord.setColumns(columns);
        return rdbEventRecord;
    }

    private RdbEventRecord makeUpdateEventData() {
        RdbEventRecord rdbEventRecord = new RdbEventRecord();
        rdbEventRecord.setEventType(EventType.UPDATE);
        rdbEventRecord.setSchemaName(SCHEMA_NAME);
        rdbEventRecord.setTableName(TABLE_NAME);
        rdbEventRecord.RSI();

        MediaMappingInfo mappingInfo = new MediaMappingInfo();
        mappingInfo.setId(MAPPING_ID);
        RecordMeta.attachMediaMapping(rdbEventRecord, mappingInfo);

        List<EventColumn> keys = new ArrayList<EventColumn>();
        keys.add(makeEventColumn(KEY_NAME, KEY_VALUE, true));
        keys.stream().forEach(k -> k.setUpdate(false));
        rdbEventRecord.setKeys(keys);

        List<EventColumn> columns = new ArrayList<EventColumn>();
        int i = 0;
        for (String columnName : COLUMN_NAMES2) {
            columns.add(makeEventColumn(columnName, columnName + i, false));
            i++;
        }
        rdbEventRecord.setColumns(columns);
        return rdbEventRecord;
    }

    private RdbEventRecord makeUpdateEventData(String oldKeyValue, String newKeyValue) {
        RdbEventRecord rdbEventRecord = new RdbEventRecord();
        rdbEventRecord.setEventType(EventType.UPDATE);
        rdbEventRecord.setSchemaName(SCHEMA_NAME);
        rdbEventRecord.setTableName(TABLE_NAME);
        rdbEventRecord.RSI();

        MediaMappingInfo mappingInfo = new MediaMappingInfo();
        mappingInfo.setId(MAPPING_ID);
        RecordMeta.attachMediaMapping(rdbEventRecord, mappingInfo);

        List<EventColumn> oldKeys = new ArrayList<EventColumn>();
        oldKeys.add(makeEventColumn(KEY_NAME, oldKeyValue, true));
        List<EventColumn> newKeys = new ArrayList<EventColumn>();
        newKeys.add(makeEventColumn(KEY_NAME, newKeyValue, true));
        rdbEventRecord.setKeys(newKeys);
        rdbEventRecord.setOldKeys(oldKeys);
        return rdbEventRecord;
    }

    private RdbEventRecord makeDeleteEventData() {
        RdbEventRecord rdbEventRecord = new RdbEventRecord();
        rdbEventRecord.setEventType(EventType.DELETE);
        rdbEventRecord.setSchemaName(SCHEMA_NAME);
        rdbEventRecord.setTableName(TABLE_NAME);
        rdbEventRecord.RSI();

        MediaMappingInfo mappingInfo = new MediaMappingInfo();
        mappingInfo.setId(MAPPING_ID);
        RecordMeta.attachMediaMapping(rdbEventRecord, mappingInfo);

        List<EventColumn> keys = new ArrayList<EventColumn>();
        keys.add(makeEventColumn(KEY_NAME, KEY_VALUE, true));
        rdbEventRecord.setKeys(keys);
        return rdbEventRecord;
    }

    private EventColumn makeEventColumn(String columnName, String columnValue, boolean key) {
        EventColumn eventColumn = new EventColumn();
        eventColumn.setColumnName(columnName);
        eventColumn.setColumnType(COLUMN_TYPE);
        eventColumn.setColumnValue(columnValue);
        eventColumn.setKey(key);
        return eventColumn;
    }
}
