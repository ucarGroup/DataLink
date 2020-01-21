package com.ucar.datalink.writer.sddl.testCase;

import com.google.common.collect.Lists;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;

import java.sql.Types;
import java.util.List;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 04/12/2017.
 */
public class UpdateCase {
    public static List<RdbEventRecord> getUpdateShardDriverIdNullRecodes(List<String> oldValues, List<String> updateValues,
                                                                         List<String> oldDriverIds, List<String> updateDriverIds) {
        List<RdbEventRecord> records = Lists.newArrayList();

        for (int i = 0; i < oldValues.size(); i++) {
            RdbEventRecord record1 = null;

            if (oldDriverIds == null && updateDriverIds == null) {
                record1 = getDetail(oldValues.get(i), updateValues.get(i), null, null);
            } else if (oldDriverIds == null) {
                record1 = getDetail(oldValues.get(i), updateValues.get(i), null, updateDriverIds.get(i));
            } else if (updateDriverIds == null) {
                record1 = getDetail(oldValues.get(i), updateValues.get(i), oldDriverIds.get(i), null);
            } else {
                record1 = getDetail(oldValues.get(i), updateValues.get(i), oldDriverIds.get(i), updateDriverIds.get(i));
            }
            records.add(record1);
        }

        return records;
    }

    public static List<RdbEventRecord> getUpdateRedundancy(List<String> oldValues, List<String> updateValues) {
        List<RdbEventRecord> records = Lists.newArrayList();

        RdbEventRecord record1 = getCity(oldValues.get(0), updateValues.get(0));
        records.add(record1);

        return records;
    }

    private static RdbEventRecord getDetail(String oldValue, String updateValue, String oldDriverId, String updateDriverId) {
        RdbEventRecord record = new RdbEventRecord();
        record.setSchemaName("tcar_order_m0");
        record.setTableName("t_taxi_order_detail");
        record.setEventType(EventType.UPDATE);
        record.setExecuteTime(10l);

        List<EventColumn> keys = getKeys(oldValue);
        record.setKeys(keys);
        record.setOldKeys(keys);

            EventColumn orderIdColumn               = getColumn("order_id", oldValue, Types.BIGINT, false);
            EventColumn createEmpColumn             = getColumn("create_emp", oldValue, Types.INTEGER, false);


            List<EventColumn> columns = Lists.newArrayList();
            EventColumn virtualMobileStatusColumn   = getColumn("virtual_mobile_status", "1", Types.INTEGER, false);
            EventColumn modifyEmpColumn             = getColumn("modify_emp", oldValue, Types.INTEGER, false);
            columns.add(orderIdColumn);
            columns.add(virtualMobileStatusColumn);
            columns.add(createEmpColumn);
            columns.add(modifyEmpColumn);
            EventColumn driverIdColumn              = getColumn("driver_id", oldDriverId, Types.BIGINT, false);
            columns.add(driverIdColumn);
        record.setOldColumns(columns);

            List<EventColumn> columnsUpdate = Lists.newArrayList();
            EventColumn virtualMobileStatusUpdateColumn   = getColumn("virtual_mobile_status", "2", Types.INTEGER, true);
            EventColumn modifyEmpUpdateColumn             = getColumn("modify_emp", updateValue, Types.INTEGER, true);
            columnsUpdate.add(orderIdColumn);
            EventColumn driverIdUpdateColumn              = getColumn("driver_id", updateDriverId, Types.BIGINT, oldDriverId != updateDriverId);
            columnsUpdate.add(driverIdUpdateColumn);
            columnsUpdate.add(virtualMobileStatusUpdateColumn);
            columnsUpdate.add(createEmpColumn);
            columnsUpdate.add(modifyEmpUpdateColumn);
        record.setColumns(columnsUpdate);

        record.RSI();

        return record;
    }

    private static RdbEventRecord getCity(String oldValue, String updateValue) {
        RdbEventRecord record = new RdbEventRecord();
        record.setSchemaName("tcar_order_m0");
        record.setTableName("t_b_city");
        record.setEventType(EventType.UPDATE);
        record.setExecuteTime(10l);


        List<EventColumn> keys = getKeys(oldValue);
        record.setKeys(keys);
        record.setOldKeys(keys);

            List<EventColumn> oldColumns = Lists.newArrayList();
            String currenttime = "2017-12-05 16:55:00.000";
            EventColumn nameColumn                  = getColumn("name", oldValue, Types.VARCHAR, false);
            EventColumn isBusinessColumn            = getColumn("is_business", oldValue, Types.INTEGER, false);
            EventColumn createEmpColumn             = getColumn("create_emp", oldValue, Types.INTEGER, false);
            EventColumn createTimeColumn            = getColumn("create_time", currenttime, Types.TIMESTAMP, false);
            EventColumn modifyEmpColumn             = getColumn("modify_emp", oldValue, Types.INTEGER, false);
            EventColumn modifyTimeColumn            = getColumn("modify_time", currenttime, Types.TIMESTAMP, false);

            EventColumn administrativeNameColumn    = getColumn("administrative_name", oldValue, Types.VARCHAR, false);
            EventColumn selfSupportColumn           = getColumn("self_support", oldValue, Types.INTEGER, false);
            EventColumn leagueColumn                = getColumn("league", oldValue, Types.INTEGER, false);
            EventColumn partTimeColumn              = getColumn("part_time", oldValue, Types.INTEGER, false);
            EventColumn isTaxiColumn                = getColumn("is_taxi", oldValue, Types.INTEGER, false);

            oldColumns.add(nameColumn);
            oldColumns.add(isBusinessColumn);
            oldColumns.add(createEmpColumn);
            oldColumns.add(createTimeColumn);
            oldColumns.add(modifyEmpColumn);
            oldColumns.add(modifyTimeColumn);
            oldColumns.add(administrativeNameColumn);
            oldColumns.add(selfSupportColumn);
            oldColumns.add(leagueColumn);
            oldColumns.add(partTimeColumn);
            oldColumns.add(isTaxiColumn);
        record.setOldColumns(oldColumns);

            List<EventColumn> updatecolumns = Lists.newArrayList();
            EventColumn updatenameColumn           = getColumn("name", updateValue, Types.VARCHAR, true);
            EventColumn updatemodifyEmpColumn      = getColumn("modify_emp", currenttime, Types.INTEGER, true);

            updatecolumns.add(updatenameColumn);
            updatecolumns.add(updatemodifyEmpColumn);
        record.setColumns(updatecolumns);

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

    private static EventColumn getColumn(String name, String value, int type , boolean isUpdate) {
        EventColumn column = new EventColumn();
        column.setIndex(1);
        column.setKey(false);
        column.setNull(false);
        column.setColumnName(name);
        column.setColumnValue(value);
        column.setUpdate(isUpdate);
        column.setColumnType(type);

        return column;
    }

}
