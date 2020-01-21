package com.ucar.datalink.writer.sddl.testCase;

import com.google.common.collect.Lists;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 04/12/2017.
 */
public class DeleteCase {
    public static List<RdbEventRecord> getDeleteShardingDetail(List<String> updateValues) {
        List<RdbEventRecord> records = Lists.newArrayList();

        for (String defaultValue : updateValues) {
            RdbEventRecord record1 = getDetail(defaultValue);
            records.add(record1);
        }

        return records;
    }

    public static List<RdbEventRecord> getDeleteRedundancyCity(List<String> updateValues) {

        List<RdbEventRecord> records = Lists.newArrayList();

        RdbEventRecord record1 = getCity(updateValues.get(0));
        records.add(record1);

        return records;
    }

    private static RdbEventRecord getDetail(String defaultValue) {
        RdbEventRecord record = new RdbEventRecord();
        record.setTableName("t_taxi_order_detail");
        record.setEventType(EventType.DELETE);
        record.setExecuteTime(10l);


        List<EventColumn> keys = getKeys(defaultValue);
        record.setOldKeys(keys);

        List<EventColumn> columns = Lists.newArrayList();
        EventColumn driverIdColumn              = getColumn("driver_id", defaultValue, Types.BIGINT);
        columns.add(driverIdColumn);

        record.setOldColumns(columns);

        record.setKeys(new ArrayList<>());
        record.setColumns(new ArrayList<>());
        record.RSI();

        return record;
    }

    private static RdbEventRecord getCity(String defaultValue) {
        RdbEventRecord record = new RdbEventRecord();
        record.setTableName("t_b_city");
        record.setEventType(EventType.DELETE);
        record.setExecuteTime(10l);


        List<EventColumn> keys = getKeys(defaultValue);
        record.setKeys(keys);
        record.setOldKeys(keys);

        record.setColumns(new ArrayList<>());
        record.setOldColumns(new ArrayList<>());
        record.RSI();

        return record;
    }

    private static List<EventColumn> getKeys(String value) {
        List<EventColumn> keys = Lists.newArrayList();
        EventColumn key = new EventColumn();
        key.setIndex(1);
        key.setKey(true);
        key.setNull(false);
        key.setColumnName("id");
        key.setColumnValue(value);
        key.setUpdate(true);
        key.setColumnType(Types.BIGINT);
        keys.add(key);

        return keys;
    }

    private static EventColumn getColumn(String name, String value, int type) {
        EventColumn column = new EventColumn();
        column.setIndex(1);
        column.setKey(false);
        column.setNull(false);
        column.setColumnName(name);
        column.setColumnValue(value);
        column.setUpdate(false);
        column.setColumnType(type);

        return column;
    }
}
