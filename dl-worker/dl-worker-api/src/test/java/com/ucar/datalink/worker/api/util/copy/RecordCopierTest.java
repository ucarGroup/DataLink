package com.ucar.datalink.worker.api.util.copy;

import com.google.common.collect.Lists;
import com.ucar.datalink.contract.RSI;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lubiao on 2017/3/22.
 */
@SuppressWarnings("unchecked")
public class RecordCopierTest {

    @Test
    public void testList() {
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new LinkedList<>();

        long start1 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            list1.add(i);
        }
        System.out.println(System.currentTimeMillis() - start1);

        long start2 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            list2.add(i);
        }
        System.out.println(System.currentTimeMillis() - start2);
    }

    @Test
    public void testCopy() {
        TestRecord tr = new TestRecord();
        tr.metaData().put("key", "000");

        TestRecord tr2 = RecordCopier.copy(tr);
        tr.metaData().put("key", "111");
        tr.age = 88;
        tr.str = "999";

        Assert.assertEquals(tr.age, 88);
        Assert.assertEquals(tr.str, "999");
        Assert.assertEquals(tr.metaData().get("key"), "111");

        Assert.assertEquals(tr2.age, 45);
        Assert.assertEquals(tr2.str, "124");
        Assert.assertEquals(tr2.metaData().get("key"), "000");
        Assert.assertEquals(tr2.rsi.getName(), "name");
    }

    @Test
    public void testCopyList() {
        TestRecord tr = new TestRecord();
        tr.metaData().put("key", "000");
        List<TestRecord> list1 = Lists.newArrayList(tr);

        List<TestRecord> list2 = RecordCopier.copyList(list1);

        list1.get(0).metaData().put("key", "111");
        list1.get(0).age = 88;
        list1.get(0).str = "999";

        Assert.assertEquals(list1.get(0).age, 88);
        Assert.assertEquals(list1.get(0).str, "999");
        Assert.assertEquals(list1.get(0).metaData().get("key"), "111");

        Assert.assertEquals(list2.get(0).age, 45);
        Assert.assertEquals(list2.get(0).str, "124");
        Assert.assertEquals(list2.get(0).metaData().get("key"), "000");
        Assert.assertEquals(list2.get(0).rsi.getName(), "name");

    }

    @Test
    public void testCopyPerf() {
        List<TestRecord> list = new ArrayList<>();
        for (int i = 0; i < 15000; i++) {
            list.add(new TestRecord());
        }

        long start = System.currentTimeMillis();
        list.forEach(i -> RecordCopier.copy(i));
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void testCopyListPerf() {
        List<RdbEventRecord> list = new ArrayList<>();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 15000; i++) {
            RdbEventRecord r = new RdbEventRecord();
            r.setDdlSchemaName("vvv");
            r.setEventType(EventType.ALTER);
            r.setExecuteTime(23452152343256L);
            r.setHint("/** dfadfsgdg/");
            r.setSchemaName("ttt");
            r.setSql("insert into t_v_model(id,name)values(1,'hello kitty')");
            r.setTableName("t_v_model");

            EventColumn keyColumn = new EventColumn();
            keyColumn.setColumnType(1);
            keyColumn.setColumnName("id");
            keyColumn.setColumnValue("1");
            keyColumn.setKey(true);
            keyColumn.setUpdate(true);
            r.setKeys(Lists.newArrayList(keyColumn));

            List<EventColumn> columns = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                EventColumn column = new EventColumn();
                keyColumn.setColumnType(1);
                keyColumn.setColumnName("name");
                keyColumn.setColumnValue("1");
                keyColumn.setUpdate(true);
                columns.add(column);
            }
            r.setColumns(columns);

            list.add(r);
        }
        System.out.println(System.currentTimeMillis() - start);

        long start1 = System.currentTimeMillis();
        RecordCopier.copyList(list);
        System.out.println(System.currentTimeMillis() - start1);

        long start2 = System.currentTimeMillis();
        RecordCopier.copyList(list);
        System.out.println(System.currentTimeMillis() - start2);
    }

    static class TestRecord extends Record<String> {
        private RSI rsi = new RSI("na", "name");
        private String str = "124";
        private int age = 45;

        @Override
        public String getId() {
            return null;
        }

        @Override
        public RSI RSI() {
            return rsi;
        }
    }
}
