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
public class InsertCase {

    public static List<RdbEventRecord> getInsertShardingDetail(List<String> defaultValues) {
        List<RdbEventRecord> records = Lists.newArrayList();

        for (String defaultValue : defaultValues) {
            RdbEventRecord record1 = getDetail(defaultValue);
            records.add(record1);
        }

        return records;
    }

    public static List<RdbEventRecord> getInsertRedundancyCity(List<String> defaultValues) {
        List<RdbEventRecord> records = Lists.newArrayList();

        RdbEventRecord record1 = getCity(defaultValues.get(0));
        records.add(record1);

        return records;
    }

    private static RdbEventRecord getCity(String defaultValue) {
        RdbEventRecord record = new RdbEventRecord();
        record.setSchemaName("tcar_order_m0");
        record.setTableName("t_b_city");
        record.setEventType(EventType.INSERT);
        record.setExecuteTime(10l);


        List<EventColumn> keys = getKeys(defaultValue);
        record.setKeys(keys);

        List<EventColumn> columns = Lists.newArrayList();
        String currenttime = "2017-12-05 16:55:00.000";
        EventColumn nameColumn                  = getColumn("name", defaultValue, Types.VARCHAR);
        EventColumn isBusinessColumn            = getColumn("is_business", defaultValue, Types.INTEGER);
        EventColumn createEmpColumn             = getColumn("create_emp", defaultValue, Types.INTEGER);
        EventColumn createTimeColumn            = getColumn("create_time", currenttime, Types.TIMESTAMP);
        EventColumn modifyEmpColumn             = getColumn("modify_emp", defaultValue, Types.INTEGER);
        EventColumn modifyTimeColumn            = getColumn("modify_time", currenttime, Types.TIMESTAMP);

        EventColumn administrativeNameColumn    = getColumn("administrative_name", defaultValue, Types.VARCHAR);
        EventColumn selfSupportColumn           = getColumn("self_support", defaultValue, Types.INTEGER);
        EventColumn leagueColumn                = getColumn("league", defaultValue, Types.INTEGER);
        EventColumn partTimeColumn              = getColumn("part_time", defaultValue, Types.INTEGER);
        EventColumn isTaxiColumn                = getColumn("is_taxi", defaultValue, Types.INTEGER);

        columns.add(nameColumn);
        columns.add(isBusinessColumn);
        columns.add(createEmpColumn);
        columns.add(createTimeColumn);
        columns.add(modifyEmpColumn);
        columns.add(modifyTimeColumn);
        columns.add(administrativeNameColumn);
        columns.add(selfSupportColumn);
        columns.add(leagueColumn);
        columns.add(partTimeColumn);
        columns.add(isTaxiColumn);

        record.setColumns(columns);

        record.setOldKeys(new ArrayList<>());
        record.setOldColumns(new ArrayList<>());
        record.RSI();

        return record;
    }

    private static RdbEventRecord getDetail(String defaultValue) {
        RdbEventRecord record = new RdbEventRecord();
        record.setSchemaName("tcar_order_m0");
        record.setTableName("t_taxi_order_detail");
        record.setEventType(EventType.INSERT);
        record.setExecuteTime(10l);


        List<EventColumn> keys = getKeys(defaultValue);
        record.setKeys(keys);

        List<EventColumn> columns = Lists.newArrayList();
        EventColumn orderIdColumn               = getColumn("order_id", defaultValue, Types.BIGINT);
        // EventColumn driverIdColumn              = getColumn("driver_id", defaultValue, Types.BIGINT);
        EventColumn virtualMobileStatusColumn   = getColumn("virtual_mobile_status", "1", Types.INTEGER);
        EventColumn createEmpColumn             = getColumn("create_emp", defaultValue, Types.INTEGER);
        EventColumn modifyEmpColumn             = getColumn("modify_emp", defaultValue, Types.INTEGER);
        columns.add(orderIdColumn);
        // columns.add(driverIdColumn);
        columns.add(virtualMobileStatusColumn);
        columns.add(createEmpColumn);
        columns.add(modifyEmpColumn);

        record.setColumns(columns);

        record.setOldKeys(new ArrayList<>());
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
        column.setUpdate(true);
        column.setColumnType(type);

        return column;
    }
}
