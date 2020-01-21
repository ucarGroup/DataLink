package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.position.Position;
import com.alibaba.otter.canal.store.CanalEventStore;
import com.alibaba.otter.canal.store.CanalStoreException;
import com.alibaba.otter.canal.store.model.Event;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.store.model.Events;
import com.ucar.datalink.reader.mysql.extend.FixedGroupEventSink;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lubiao on 2019/1/28.
 */
public class GroupEventSinkTest {

    @Test
    public void test() throws Exception {
        FixedGroupEventSink groupEventSink = new FixedGroupEventSink(4);
        groupEventSink.setEventStore(new CanalEventStore<Event>() {
            @Override
            public void put(List<Event> data) throws InterruptedException, CanalStoreException {

            }

            @Override
            public boolean put(List<Event> data, long timeout, TimeUnit unit) throws InterruptedException, CanalStoreException {
                return false;
            }

            @Override
            public boolean tryPut(List<Event> data) throws CanalStoreException {
                synchronized (this) {
                    data.stream().forEach(i -> {
                        System.out.println(String.format("Thread %s Time %s Type %s",
                                Thread.currentThread().getName(), date2Str(new Date(i.getEntry().getHeader().getExecuteTime())), i.getEntry().getEntryType()));
                    });
                }
                return true;
            }

            @Override
            public void put(Event data) throws InterruptedException, CanalStoreException {

            }

            @Override
            public boolean put(Event data, long timeout, TimeUnit unit) throws InterruptedException, CanalStoreException {
                return false;
            }

            @Override
            public boolean tryPut(Event data) throws CanalStoreException {
                return false;
            }

            @Override
            public Events<Event> get(Position start, int batchSize) throws InterruptedException, CanalStoreException {
                return null;
            }

            @Override
            public Events<Event> get(Position start, int batchSize, long timeout, TimeUnit unit) throws InterruptedException, CanalStoreException {
                return null;
            }

            @Override
            public Events<Event> tryGet(Position start, int batchSize) throws CanalStoreException {
                return null;
            }

            @Override
            public Position getLatestPosition() throws CanalStoreException {
                return null;
            }

            @Override
            public Position getFirstPosition() throws CanalStoreException {
                return null;
            }

            @Override
            public void ack(Position position) throws CanalStoreException {

            }

            @Override
            public void rollback() throws CanalStoreException {

            }

            @Override
            public void start() {

            }

            @Override
            public void stop() {

            }

            @Override
            public boolean isStart() {
                return false;
            }

            @Override
            public void cleanUntil(Position position) throws CanalStoreException {

            }

            @Override
            public void cleanAll() throws CanalStoreException {

            }
        });
        groupEventSink.start();

        Thread t1 = new Thread(() -> {
            try {
                List<CanalEntry.Entry> list = new ArrayList<>();
                list.add(buildEvent(str2Date("2018-01-01 01:00:01"), CanalEntry.EntryType.TRANSACTIONBEGIN));
                list.add(buildEvent(str2Date("2018-01-01 01:01:00"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:02:00"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:03:00"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:04:00"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:05:00"), CanalEntry.EntryType.TRANSACTIONEND));
                groupEventSink.sink(list, null, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                List<CanalEntry.Entry> list = new ArrayList<>();
                list.add(buildEvent(str2Date("2018-01-01 01:01:02"), CanalEntry.EntryType.TRANSACTIONBEGIN));
                list.add(buildEvent(str2Date("2018-01-01 01:01:03"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:01:04"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:01:05"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:01:06"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:01:07"), CanalEntry.EntryType.TRANSACTIONEND));
                groupEventSink.sink(list, null, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                List<CanalEntry.Entry> list = new ArrayList<>();
                list.add(buildEvent(str2Date("2018-01-01 01:02:03"), CanalEntry.EntryType.TRANSACTIONBEGIN));
                list.add(buildEvent(str2Date("2018-01-01 01:02:04"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:02:05"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:02:06"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:02:07"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:02:08"), CanalEntry.EntryType.TRANSACTIONEND));
                groupEventSink.sink(list, null, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        Thread t4 = new Thread(() -> {
            try {
                List<CanalEntry.Entry> list = new ArrayList<>();
                list.add(buildEvent(str2Date("2018-01-01 01:03:04"), CanalEntry.EntryType.TRANSACTIONBEGIN));
                list.add(buildEvent(str2Date("2018-01-01 01:03:05"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:03:06"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:03:07"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:03:08"), CanalEntry.EntryType.ROWDATA));
                list.add(buildEvent(str2Date("2018-01-01 01:03:09"), CanalEntry.EntryType.TRANSACTIONEND));
                groupEventSink.sink(list, null, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        t4.start();
        t3.start();
        t2.start();
        t1.start();


        t1.join();
        t2.join();
        t3.join();
        t4.join();
    }


    private static CanalEntry.Entry buildEvent(Date date, CanalEntry.EntryType entryType) {
        Header header = Header.newBuilder().setExecuteTime(date.getTime()).build();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder().setHeader(header).setEntryType(entryType).build();
        return entry;
    }

    private static Date str2Date(String date) {
        try {
            return DateUtils.parseDate(date, new String[]{"yyyy-MM-dd hh:mm:ss"});
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String date2Str(Date date) {
        return DateFormatUtils.format(date, "yyyy-MM-dd hh:mm:ss");
    }
}
