package com.ucar.datalink.writer.hbase.handle;

import com.google.common.cache.*;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.writer.hbase.handle.util.HTableFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sqq on 2017/12/5.
 */
public class HBaseWriterTest {

    static {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
    }

    @Test
    public void testHBaseWriter() {
        List<RdbEventRecord> records = new ArrayList<>();

        List<EventColumn> keys = new ArrayList<>();
        EventColumn key = new EventColumn();
        key.setColumnName("id");
        key.setColumnValue("333");
        keys.add(key);
        List<EventColumn> columns = new ArrayList<>();
        EventColumn column1 = new EventColumn();
        column1.setColumnName("id");
        column1.setColumnValue("333");
        EventColumn column2 = new EventColumn();
        column2.setColumnName("name");
        column2.setColumnValue("David");
        columns.add(column1);
        columns.add(column2);
        RdbEventRecord record = new RdbEventRecord();
        record.setTableName("tsqq");
        record.setKeys(keys);
        record.setColumns(columns);
        record.setEventType(EventType.INSERT);
        RdbEventRecord record1 = new RdbEventRecord();
        record1.setTableName("tsqq");
        record1.setKeys(keys);
        record1.setColumns(columns);
        record1.setEventType(EventType.DELETE);
        records.add(record);
        records.add(record1);
        MediaSourceInfo targetMediaSourceInfo = new MediaSourceInfo();
        HBaseMediaSrcParameter hbaseParameter = new HBaseMediaSrcParameter();
        hbaseParameter.setZkMediaSourceId(45L);
        hbaseParameter.setZnodeParent("/hbase_0.98");
        hbaseParameter.setKeyvalueMaxsize(1048576000);
        hbaseParameter.setMediaSourceType(MediaSourceType.HBASE);
        targetMediaSourceInfo.setParameter(hbaseParameter.toJsonString());
        targetMediaSourceInfo.setId(128L);
        targetMediaSourceInfo.setParameter(hbaseParameter.toJsonString());
        targetMediaSourceInfo.setType(MediaSourceType.HBASE);
        targetMediaSourceInfo.setDesc("HBase集群");
        targetMediaSourceInfo.setName("hbase_ucar");
        HTable hTable = HTableFactory.getHTable("tsqq", targetMediaSourceInfo);

        RdbEventRecordHandler handler = new RdbEventRecordHandler();
        handler.writeToHBase(records, hTable, true);
    }

    @Test
    public void removalListenerTest() {
        // 测试手动清除是否会触发removalListener————会
        LoadingCache<Integer,Integer> cache= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).removalListener(new RemovalListener<Integer,Integer>() {
            @Override
            public void onRemoval(RemovalNotification<Integer,Integer> notification) {
                System.out.println("removalListener triggered!");
            }
        }).build(new CacheLoader<Integer, Integer>() {
            @Override
            public Integer load(Integer key) throws Exception {
                return null;
            }
        });
        cache.put(1,1);
        // 手动清除  
        cache.invalidate(1);
        System.out.println(cache.getIfPresent(1));// null
    }

}
