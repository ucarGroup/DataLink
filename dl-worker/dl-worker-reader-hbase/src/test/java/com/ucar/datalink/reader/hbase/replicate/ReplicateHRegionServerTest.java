package com.ucar.datalink.reader.hbase.replicate;

import com.google.common.collect.Lists;
import com.ucar.datalink.common.zookeeper.ZkConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by lubiao on 2017/11/20.
 */
public class ReplicateHRegionServerTest {
    private static final String SOURCE_ZK_SERVERS = "10.104.3.1";
    private static final String SOURCE_ZK_PORT = "5181";
    private static final String SOURCE_ZK_ZNODE = "/hbase_0.98";

    private static final String TARGET_ZK_SERVERS = "10.104.102.208:5181,10.104.102.209:5181,10.104.102.210:5181";
    private static final String TARGET_ZK_PORT = "5181";
    private static final String TARGET_ZK_ZNODE = "/hrdl_114_hbase_0.98";

    @Test
    public void testReplicate() throws Exception {
        System.setProperty("hadoop.home.dir", "E:\\hadoop-common-2.2.0-bin-master");

        ArrayBlockingQueue<HRecordChunk> queue = new ArrayBlockingQueue<>(1);
        ReplicationConfig config = new ReplicationConfig();
        config.setHbaseName("2");
        config.setZnodeParent(TARGET_ZK_ZNODE);
        config.setZkConfig(new ZkConfig(TARGET_ZK_SERVERS, 6000, 60000));

        ReplicateHRegionServer server = new ReplicateHRegionServer(config, queue);
        server.start();

        while (true) {
            HRecordChunk chunk = queue.take();

            System.out.println("---------- HRecordChunk -----------");

            chunk.getRecords().stream().forEach(
                    r -> {
                        System.out.println("HRecord is :" + r);
                    }
            );
            if (Bytes.toString(chunk.getRecords().get(0).getRowKey()).equals("111")) {
                chunk.getCallback().onCompletion(null, null);
            } else {
                chunk.getCallback().onCompletion(null, null);
            }
        }
    }


    @Test
    public void putDataOnce() {
        try {
            //System.setProperty("hadoop.home.dir", "E:\\hadoop-common-2.2.0-bin-master");
            Configuration conf = HBaseConfiguration.create();
            conf.set(HConstants.ZOOKEEPER_QUORUM, SOURCE_ZK_SERVERS);
            conf.set(HConstants.ZOOKEEPER_CLIENT_PORT, SOURCE_ZK_PORT);
            conf.set(HConstants.ZOOKEEPER_ZNODE_PARENT, SOURCE_ZK_ZNODE);
            System.setProperty("HADOOP_USER_NAME", "hadoop");

            HTable table = new HTable(conf, Bytes.toBytes("lubiao_test"));
            for (int i = 1; i <= 100; i++) {
                Put put = new Put(Bytes.toBytes(String.valueOf(i)));// 设置rowkey
                put.add(
                        Bytes.toBytes("vvii"),
                        Bytes.toBytes("name"),
                        Bytes.toBytes("{ \"remark\" : \"{\\\"upDetail\\\":\\\"星沙大道22号\\\",\\\"downDistrict\\\":\\\"\\\",\\\"upCityId\\\":\\\"24\\\",\\\"upName\\\":\\\"长沙市第八医院\\\",\\\"upDistrict\\\":\\\"长沙县\\\",\\\"downLon\\\":\\\"112.975525\\\",\\\"downName\\\":\\\"长沙美爵酒店\\\",\\\"floatFactor\\\":\\\"1\\\",\\\"downCityId\\\":\\\"24\\\",\\\"upLon\\\":\\\"113.086754\\\",\\\"downDetail\\\":\\\"开福区五一大道868号(地铁2号线五一广场站8号出口)\\\",\\\"downCityCode\\\":\\\"\\\",\\\"estimateValue\\\":\\\"81.06\\\",\\\"upLat\\\":\\\"28.235417\\\",\\\"downLat\\\":\\\"28.195648\\\",\\\"upCityCode\\\":\\\"0731\\\"}\" }")
                );
                put.add(
                        Bytes.toBytes("vvii"),
                        Bytes.toBytes("content"),
                        Bytes.toBytes("设备root情况，未root")
                );
                table.put(put);
                table.flushCommits();

                System.out.println("put one");
                Thread.sleep(1000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void putDataBatch() {
        System.setProperty("hadoop.home.dir", "E:\\hadoop-common-2.2.0-bin-master");
        Configuration conf = HBaseConfiguration.create();
        conf.set(HConstants.ZOOKEEPER_QUORUM, SOURCE_ZK_SERVERS);
        conf.set(HConstants.ZOOKEEPER_CLIENT_PORT, SOURCE_ZK_PORT);
        conf.set(HConstants.ZOOKEEPER_ZNODE_PARENT, SOURCE_ZK_ZNODE);
        System.setProperty("HADOOP_USER_NAME", "hadoop");

        List<String> list = Lists.newArrayList("lubiao_test", "lubiao_test_201709", "lubiao_test_201710", "lubiao_test_201711", "lubiao_test_201712");

        list.stream().forEach(tableName -> {
            try {
                HTable table = new HTable(conf, Bytes.toBytes(tableName));
                for (int i = 1; i <= 1000; i++) {
                    Put put = new Put(Bytes.toBytes(String.valueOf(i)));// 设置rowkey
                    put.add(Bytes.toBytes("vvii"), Bytes.toBytes("name"), Bytes.toBytes(i + UUID.randomUUID().toString()));
                    put.add(Bytes.toBytes("vvii"), Bytes.toBytes("age"), Bytes.toBytes(i + UUID.randomUUID().toString()));
                    put.add(Bytes.toBytes("vvii"), Bytes.toBytes("address"), Bytes.toBytes(i + UUID.randomUUID().toString()));
                    table.put(put);
                }
                table.flushCommits();
                System.out.println(String.format("Table [%s] puts succeeded.", tableName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void queryData() throws Exception {
        System.setProperty("hadoop.home.dir", "E:\\hadoop-common-2.2.0-bin-master");
        Configuration conf = HBaseConfiguration.create();
        conf.set(HConstants.ZOOKEEPER_QUORUM, SOURCE_ZK_SERVERS);
        conf.set(HConstants.ZOOKEEPER_CLIENT_PORT, SOURCE_ZK_PORT);
        conf.set(HConstants.ZOOKEEPER_ZNODE_PARENT, SOURCE_ZK_ZNODE);
        System.setProperty("HADOOP_USER_NAME", "hadoop");

        Scan scan = new Scan();
        ResultScanner rs = null;
        HTable table = new HTable(conf, Bytes.toBytes("lucky_t_scd_app_monitor_event_201801"));
        try {
            rs = table.getScanner(scan);
            for (Result r : rs) {
                for (KeyValue kv : r.list()) {
                    System.out.println("row:" + Bytes.toString(kv.getRow()));
                    System.out.println("family:" + Bytes.toString(kv.getFamily()));
                    System.out.println("qualifier:" + Bytes.toString(kv.getQualifier()));
                    System.out.println("value:" + Bytes.toString(kv.getValue()));
                    System.out.println("timestamp:" + kv.getTimestamp());
                    System.out.println("-------------------------------------------");
                }
            }
        } finally {
            rs.close();
        }
    }

    @Test
    public void test(){
        System.out.println(System.currentTimeMillis());
    }
}
