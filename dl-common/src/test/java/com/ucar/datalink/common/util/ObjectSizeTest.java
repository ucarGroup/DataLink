package com.ucar.datalink.common.util;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.util.memory1.ObjectProfiler;
import com.ucar.datalink.common.util.memory2.ClassIntrospector;
import com.ucar.datalink.common.util.memory2.ObjectInfo;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sqq on 2018/3/7.
 */
public class ObjectSizeTest {

    @Test
    public void testObjectGraphMeasurer() throws Exception {
        TT t1 = new TT();
        t1.name = "aaa";
        t1.address = "bbb";
        TT t2 = new TT();
        t2.name = "xxx";
        t2.address = "yyy";
        //t1.tt = t2;
        System.out.println(ObjectProfiler.sizeof(t1));

        final ClassIntrospector ci = new ClassIntrospector();
        ObjectInfo res = ci.introspect(t1);
        System.out.println(res.getDeepSize());
    }

    @Test
    public void testObjectSize() throws Exception {
        List<RdbEventRecord> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            RdbEventRecord record = new RdbEventRecord();
            record.setTableName("t_scd_order");
            record.setSql("insert into t_scd_order(test1,test2,test3,test4,test5)values(test1,test2,test3,test4,test5)");
            record.setSchemaName("ucar_order");
            record.setEventType(EventType.INSERT);
            record.setExecuteTime(System.currentTimeMillis());
            for (int j = 0; j < 100; j++) {
                EventColumn column = new EventColumn();
                column.setColumnType(5);
                column.setIndex(1);
                column.setKey(true);
                column.setColumnValue("11234567890333");
                column.setColumnName("good_" + j);
                record.getColumns().add(column);
            }
            for (int j = 0; j < 100; j++) {
                EventColumn column = new EventColumn();
                column.setColumnType(5);
                column.setIndex(1);
                column.setKey(true);
                column.setColumnValue("11234567890333");
                column.setColumnName("good_" + j);
                record.getOldColumns().add(column);
            }
            list.add(record);
        }

        long start = System.currentTimeMillis();
        System.out.println("Size is:" + ObjectProfiler.sizeof(list));
        System.out.println("Consume Time is:" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        System.out.println("Size is:" + new ClassIntrospector().introspect(list).getDeepSize());
        System.out.println("Consume Time is:" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        long size = 0;
        for (RdbEventRecord record : list) {
            size += ObjectProfiler.sizeof(record);
        }
        System.out.println("Size is:" + size);
        System.out.println("Consume Time is:" + (System.currentTimeMillis() - start));

        List<String> sList = new ArrayList<>();
        list.stream().forEach(i -> sList.add(parseToJsonStr(i, 11111L)));
        start = System.currentTimeMillis();
        System.out.println("Size is:" + ObjectProfiler.sizeof(sList));
        System.out.println("Consume Time is:" + (System.currentTimeMillis() - start));
    }

    private static String parseToJsonStr(RdbEventRecord record, long seq) {
        HashMap<String, String> map = new HashMap<>();
        List<EventColumn> columnList = record.getColumns();
        List<EventColumn> keyList = record.getKeys();
        List<EventColumn> allColumns = new ArrayList<>();
        allColumns.addAll(columnList);
        allColumns.addAll(keyList);
        for (EventColumn column : allColumns) {
            String columnName = column.getColumnName();
            String columnValue = column.getColumnValue();
            if (column.isNull()) {
                columnValue = "null";
            }
            map.put(columnName, columnValue);
        }

        // Mysql数据库binlog的精度是毫秒
        // 需要binlog_ts和binlog_seq组合，标识先后顺序
        map.put("binlog_ts", String.valueOf(System.currentTimeMillis()));
        map.put("binlog_seq", String.valueOf(seq));
        return JSONObject.toJSONString(map);
    }


    public static class TT {
        TT tt;
        String name;
        String address;
    }
}
