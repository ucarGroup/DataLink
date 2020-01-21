import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.domain.meta.HbaseStatus;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ClusterStatusInHBaseTest {

    private static final long MAX_FETCH_NUM = 1000;

    @Test
    public void status() throws IOException {
        System.setProperty("hadoop.home.dir", "E:\\hadoop");
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "10.104.105.90");
        conf.set("hbase.zookeeper.property.clientPort", "5181");
        conf.set("zookeeper.znode.parent", "/hbase_0.98");
/*        conf.set("hbase.zookeeper.quorum", "10.204.94.61,10.204.94.62,10.204.94.63,10.214.3.158,10.214.3.159,10.214.5.108");
        conf.set("hbase.zookeeper.property.clientPort", "5181");
        conf.set("zookeeper.znode.parent", "/hrdl_107_hbase_0.98");*/
/*        conf.set("hbase.zookeeper.quorum", "10.101.22.31,10.104.108.87,10.104.108.88,10.104.111.157,10.104.111.158,10.104.111.159");
        conf.set("hbase.zookeeper.property.clientPort", "5181");
        conf.set("zookeeper.znode.parent", "/hbase_0.98");*/
        HBaseAdmin admin = new HBaseAdmin(conf);
        ClusterStatus status = admin.getClusterStatus();
        String result = JSONObject.toJSONString(status);
        HbaseStatus hbaseStatus =  JSONObject.parseObject(result, HbaseStatus.class);
        System.out.println("测试："+hbaseStatus.getServersSize());

        System.out.println("集群状态:\n--------------");
        System.out.println("HBase Version: " + status.getHBaseVersion());
        System.out.println("Version: " + status.getVersion());
        System.out.println("Cluster ID: " + status.getClusterId());
        System.out.println("Master: " + status.getMaster());
        System.out.println("No. Backup Masters: " + status.getBackupMastersSize());
        System.out.println("Backup Masters: " + status.getBackupMasters());
        System.out.println("No. Live Servers: " + status.getServersSize());
        System.out.println("Servers: " + status.getServers());
        System.out.println("No. Dead Servers: " + status.getDeadServers());
        System.out.println("Dead Servers: " + status.getDeadServerNames());
        System.out.println("No. Regions: " + status.getRegionsCount());
        System.out.println("Regions in Transition: " + status.getRegionsInTransition());
        System.out.println("No. Requests: " + status.getRequestsCount());
        System.out.println("Avg Load: " + status.getAverageLoad());
        System.out.println("Balancer On: " + status.getBalancerOn());
        System.out.println("Is Balancer On: " + status.isBalancerOn());
        System.out.println("Master Coprocessors: " + Arrays.asList(status.getMasterCoprocessors()));
        System.out.println("\nServer Info:\n--------------");
  /*      for (ServerName server : status.getServers()) {
            System.out.println("Hostname: " + server.getHostname());
            System.out.println("Host and Port: " + server.getHostAndPort());
            System.out.println("Server Name: " + server.getServerName());
            System.out.println("RPC Port: " + server.getPort());
            System.out.println("Start Code: " + server.getStartcode());
            ServerLoad load = status.getLoad(server);
            System.out.println("\nServer Load:\n--------------");
            System.out.println("Info Port: " + load.getInfoServerPort());
            System.out.println("Load: " + load.getLoad());
            System.out.println("Max Heap (MB): " + load.getMaxHeapMB());
            System.out.println("Used Heap (MB): " + load.getUsedHeapMB());
            System.out.println("Memstore Size (MB): " + load.getMemstoreSizeInMB());
            System.out.println("No. Regions: " + load.getNumberOfRegions());
            System.out.println("No. Requests: " + load.getNumberOfRequests());
            System.out.println("Total No. Requests: " + load.getTotalNumberOfRequests());
            System.out.println("No. Requests per Sec: " + load.getRequestsPerSecond());
            System.out.println("No. Read Requests: " + load.getReadRequestsCount());
            System.out.println("No. Write Requests: " + load.getWriteRequestsCount());
            System.out.println("No. Stores: " + load.getStores());
            System.out.println("Store Size Uncompressed (MB): " + load.getStoreUncompressedSizeMB());
            System.out.println("No. Storefiles: " + load.getStorefiles());
            System.out.println("Storefile Size (MB): " + load.getStorefileSizeInMB());
            System.out.println("Storefile Index Size (MB): " + load.getStorefileIndexSizeInMB());
            System.out.println("Root Index Size: " + load.getRootIndexSizeKB());
            System.out.println("Total Bloom Size: " + load.getTotalStaticBloomSizeKB());
            System.out.println("Total Index Size: " + load.getTotalStaticIndexSizeKB());
            System.out.println("Current Compacted Cells: " + load.getCurrentCompactedKVs());
            System.out.println("Total Compacting Cells: " + load.getTotalCompactingKVs());
            System.out.println("Coprocessors1: " + Arrays.asList(load.getRegionServerCoprocessors()));
            System.out.println("Coprocessors2: " + Arrays.asList(load.getRsCoprocessors()));
            System.out.println("Replication Load Sink: " + load.getReplicationLoadSink());
            System.out.println("Replication Load Source: " + load.getReplicationLoadSourceList());
            System.out.println("\nRegion Load:\n--------------");
            for (Map.Entry<byte[], RegionLoad> entry : load.getRegionsLoad().entrySet()) {
                System.out.println("Region: " + Bytes.toStringBinary(entry.getKey()));
                RegionLoad regionLoad = entry.getValue();
                System.out.println("Name: " + Bytes.toStringBinary(regionLoad.getName()));
                System.out.println("Name (as String): " + regionLoad.getNameAsString());
                System.out.println("No. Requests: " + regionLoad.getRequestsCount());
                System.out.println("No. Read Requests: " + regionLoad.getReadRequestsCount());
                System.out.println("No. Write Requests: " + regionLoad.getWriteRequestsCount());
                System.out.println("No. Stores: " + regionLoad.getStores());
                System.out.println("No. Storefiles: " + regionLoad.getStorefiles());
                System.out.println("Data Locality: " + regionLoad.getDataLocality());
                System.out.println("Storefile Size (MB): " + regionLoad.getStorefileSizeMB());
                System.out.println("Storefile Index Size (MB): " + regionLoad.getStorefileIndexSizeMB());
                System.out.println("Memstore Size (MB): " + regionLoad.getMemStoreSizeMB());
                System.out.println("Root Index Size: " + regionLoad.getRootIndexSizeKB());
                System.out.println("Total Bloom Size: " + regionLoad.getTotalStaticBloomSizeKB());
                System.out.println("Total Index Size: " + regionLoad.getTotalStaticIndexSizeKB());
                System.out.println("Current Compacted Cells: " + regionLoad.getCurrentCompactedKVs());
                System.out.println("Total Compacting Cells: " + regionLoad.getTotalCompactingKVs());
                System.out.println();

            }
        }*/
    }


   // @Test
    public static List<String> testGetHbaseTable() throws IOException {
        List<String> columnFamiliesList = new ArrayList<String>();
        System.setProperty("hadoop.home.dir", "E:\\hadoop");
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "10.104.105.90");
        conf.set("hbase.zookeeper.property.clientPort", "5181");
        conf.set("zookeeper.znode.parent", "/hbase_0.98");
        System.out.println("getHbaseColumnFamilies hbase admin begin connection.");
        HBaseAdmin admin = new HBaseAdmin(conf);
        System.out.println("getHbaseColumnFamilies hbase admin execute success "+admin.toString());
        HTableDescriptor s = admin.getTableDescriptor("data_router_add_to_shopping_cart_201902".getBytes());
        HColumnDescriptor[] columns = s.getColumnFamilies();
        System.out.println(JSONArray.toJSON(columns).toString());
        for (HColumnDescriptor c : columns) {
            columnFamiliesList.add(c.getNameAsString());
        }
        return columnFamiliesList;
    }

    @Test
    public void testGetColumnInfo() throws IOException {
        System.setProperty("hadoop.home.dir", "E:\\hadoop");
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "10.104.105.90");
        conf.set("hbase.zookeeper.property.clientPort", "5181");
        conf.set("zookeeper.znode.parent", "/hbase_0.98");
        System.out.println("getHbaseQualifier hbase admin begin connection.");
        HBaseAdmin admin = new HBaseAdmin(conf);
        System.out.println("getHbaseQualifier hbase admin execute success "+admin.toString());
        HTable hTable = new HTable(conf, "data_router_add_to_shopping_cart_201902");
        System.out.println("getHbaseQualifier hbase table execute success "+admin.toString());
        Scan scan = new Scan();

        List<String> list = testGetHbaseTable();

        for (String columnFamily:list){
            scan.addFamily(columnFamily.getBytes());
            scan.setCaching((int)1000);
            int fetchAmount = 5000;
            if(fetchAmount <= 0) {
                scan.setFilter(new PageFilter(MAX_FETCH_NUM));
            } else {
                scan.setFilter(new PageFilter(fetchAmount));
            }
            ResultScanner rs = hTable.getScanner(scan);
            Result r = rs.next();
            if (r == null) {
                System.err.println("query data is empty Result:" + r);

            }

            for (Cell cell : r.rawCells()) {
                System.out.println(columnFamily+":"+Bytes.toString(cell.getQualifier()));
            }
        }
    }

   /* @Test
    public void sendDataToHbase() throws IOException {
        System.setProperty("hadoop.home.dir", "E:\\hadoop");
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        Configuration conf = HBaseConfiguration.create();

        conf.set("hbase.zookeeper.quorum", "10.104.105.90");
        conf.set("hbase.zookeeper.property.clientPort", "5181");
        conf.set("zookeeper.znode.parent", "/hbase_0.98");
        HTable table = new HTable(conf,"t_hmonitor_track_event_201908");
        Put put = new Put(Bytes.toBytes(String.valueOf(107)));// 设置rowkey
        put.add(Bytes.toBytes("device_family"),Bytes.toBytes("bssid"),Bytes.toBytes("8341"));
        put.add(Bytes.toBytes("device_family"),Bytes.toBytes("wx_version"),Bytes.toBytes("123"));
        put.add(Bytes.toBytes("device_family"),Bytes.toBytes("gyro_y"),Bytes.toBytes("30"));


*//*        HTable table = new HTable(conf,"lubiao_text_2017");
        Put put = new Put(Bytes.toBytes(String.valueOf(105)));// 设置rowkey
        put.add(Bytes.toBytes("vvii"),Bytes.toBytes("name"),Bytes.toBytes("和光"));
        put.add(Bytes.toBytes("vvii"),Bytes.toBytes("address"),Bytes.toBytes("山西"));
        put.add(Bytes.toBytes("vvii"),Bytes.toBytes("age"),Bytes.toBytes("30"));*//*

*//*        Put put = new Put(Bytes.toBytes(String.valueOf(105)));// 设置rowkey
        conf.set("hbase.zookeeper.quorum", "10.104.108.87");
        conf.set("hbase.zookeeper.property.clientPort", "5181");
        conf.set("zookeeper.znode.parent", "/hbase_0.98");
        HTable table = new HTable(conf,"t_dl_test_source");
        put.add(Bytes.toBytes("default"),Bytes.toBytes("name"),Bytes.toBytes("和光"));
        put.add(Bytes.toBytes("default"),Bytes.toBytes("address"),Bytes.toBytes("山西"));
        put.add(Bytes.toBytes("default"),Bytes.toBytes("age"),Bytes.toBytes("30"));*//*

        table.put(put);
        table.flushCommits();
        System.out.println("put one");
    }*/

    @Test
    public void sendDataToHbase() throws IOException {
        System.setProperty("hadoop.home.dir", "E:\\hadoop");
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        Configuration conf = HBaseConfiguration.create();

        conf.set("hbase.zookeeper.quorum", "10.104.105.90");
        conf.set("hbase.zookeeper.property.clientPort", "5181");
        conf.set("zookeeper.znode.parent", "/hbase_0.98");
        HTable table = new HTable(conf,"t_hmonitor_track_event_201908");

        for(int i = 0; i < 10000000; i ++){
            for(int j = 0; j < 100; j++){
                Put put = new Put(Bytes.toBytes(String.valueOf(107)));// 设置rowkey
                put.add(Bytes.toBytes("device_family"),Bytes.toBytes("bssid"),Bytes.toBytes("8341"));
                put.add(Bytes.toBytes("device_family"),Bytes.toBytes("wx_version"),Bytes.toBytes("123"));
                put.add(Bytes.toBytes("device_family"),Bytes.toBytes("gyro_y"),Bytes.toBytes("30"));
                table.put(put);
                table.flushCommits();
                System.out.println("put one");
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
}
