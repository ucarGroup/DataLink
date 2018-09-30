package com.ucar.datalink.reader.hbase.replicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.common.zookeeper.ZkClientX;
import com.ucar.datalink.contract.log.hbase.HRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.ipc.FifoRpcScheduler;
import org.apache.hadoop.hbase.ipc.PayloadCarryingRpcController;
import org.apache.hadoop.hbase.ipc.RpcServer;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 模拟HRegionServer，接收WAL日志，并解析成HRecord对象.
 * <p>
 * Created by lubiao on 2017/11/16.
 */
public class ReplicateHRegionServer extends BaseHRegionServer {

    private static final Logger logger = LoggerFactory.getLogger(ReplicateHRegionServer.class);

    private final ReplicationConfig replicationConfig;
    private final ArrayBlockingQueue<HRecordChunk> queue;

    private final ZkClientX zkClient;
    private final Configuration hbaseConf;
    private final RpcServer rpcServer;
    private final ZooKeeperWatcher zkWatcher;

    private ServerName serverName;
    private String rsServerPath;
    private volatile boolean running = false;

    public ReplicateHRegionServer(ReplicationConfig replicationConfig, ArrayBlockingQueue<HRecordChunk> queue)
            throws IOException, InterruptedException {
        this.replicationConfig = replicationConfig;
        this.queue = queue;
        this.zkClient = initZkClient();
        this.hbaseConf = initHbaseConf();
        this.rpcServer = initRpcServer();
        this.zkWatcher = new ZooKeeperWatcher(hbaseConf, this.serverName.toString(), null);
    }

    public void start() {
        rpcServer.start();
        initZnodes();
        running = true;
    }

    @Override
    public void stop(String s) {
        this.zkWatcher.close();
        if (running) {
            running = false;
            rpcServer.stop();
            try {
                zkClient.delete(rsServerPath);
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public AdminProtos.ReplicateWALEntryResponse replicateWALEntry(final RpcController controller,
                                                                   final AdminProtos.ReplicateWALEntryRequest request) throws ServiceException {
        try {
            final List<HRecord> hRecords = new LinkedList<>();
            final List<AdminProtos.WALEntry> walEntries = request.getEntryList();
            final CellScanner cellScanner = ((PayloadCarryingRpcController) controller).cellScanner();

            for (final AdminProtos.WALEntry walEntry : walEntries) {
                TableName tableName = TableName.valueOf(walEntry.getKey().getTableName().toByteArray());
                if (tableName == null) {
                    continue;
                }
                Multimap<ByteBuffer, Cell> cellsForRowKey = ArrayListMultimap.create();
                int count = walEntry.getAssociatedCellCount();

                for (int i = 0; i < count; i++) {
                    if (!cellScanner.advance()) {
                        throw new ArrayIndexOutOfBoundsException("Expected=" + count + ", index=" + i);
                    }
                    Cell cell = cellScanner.current();
                    ByteBuffer rowKey = ByteBuffer.wrap(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                    cellsForRowKey.put(rowKey, cell);
                }

                // process each row.
                cellsForRowKey.asMap().entrySet().forEach(row -> {
                    final List<Cell> cellsForRow = (List<Cell>) row.getValue();
                    final HRecord hRecord = HRecordGenerator.generate(tableName, cellsForRow);
                    hRecords.add(hRecord);

                    // for debugging purpose
                    if (logger.isDebugEnabled()) {
                        logger.debug(" The event is for table {}", tableName.getNameAsString());
                        for (Cell cell : cellsForRow) {
                            logger.debug(" the row key is {} and the cell is {}", Bytes.toStringBinary(row.getKey()), cell.toString());
                        }
                    }
                });
            }

            HRecordChunk chunk = new HRecordChunk(hRecords, new FutureCallback(), request.getSerializedSize());
            queue.put(chunk);
            chunk.getCallback().get();

            return AdminProtos.ReplicateWALEntryResponse.newBuilder().build();
        } catch (Exception ie) {
            logger.error("something goes wrong when process WALEntrys.", ie);
            throw new ServiceException(ie);
        }
    }

    private ZkClientX initZkClient() {
        return ZkClientX.getZkClient(replicationConfig.getZkConfig());
    }

    private Configuration initHbaseConf() {
        Configuration config = new Configuration();
        config.set(HConstants.ZOOKEEPER_QUORUM, replicationConfig.getZkConfig().parseServersToString());
        config.setInt(HConstants.ZOOKEEPER_CLIENT_PORT, replicationConfig.getZkConfig().parsePort());
        config.set(HConstants.ZOOKEEPER_ZNODE_PARENT, replicationConfig.getZnodeParent());
        config.setInt(HConstants.REGION_SERVER_HANDLER_COUNT, replicationConfig.getHandlers());
        config.setInt("hbase.ipc.server.read.threadpool.size", 2);
        return config;
    }

    private RpcServer initRpcServer() throws IOException {
        //hostname
        HostnameSupplier hostnameSupplier = new HostnameSupplier(hbaseConf);
        String hostName = hostnameSupplier.get();

        //socketAddress
        InetSocketAddress socketAddress = null;
        for (int i = 1; i <= 10; i++) {
            try {
                socketAddress = new InetSocketAddress(hostName, randomPort());
                break;
            } catch (Exception e) {
                logger.error("InetSocketAddress create failed in " + i + " times.", e);
            }
        }
        if (socketAddress == null || socketAddress.getAddress() == null) {
            throw new IllegalArgumentException("Failed to create " + socketAddress);
        }

        //rpcServer
        String name = "regionserver/" + socketAddress.toString();
        this.serverName = ServerName.valueOf(hostName, socketAddress.getPort(), System.currentTimeMillis());

        //handler-count设置为2，设置大了也没用，因为TaskReader<->TaskWriter是单线程模型
        //并且源端的RegionServer推送Log的时候是单线程推送的，我们创建Task的时候，会比源端集群的RegionServer数目多，
        // 同一时刻，打到一个Task上的请求数不会太多
        return new RpcServer(this, name, getServices(),
                socketAddress,
                hbaseConf,
                new FifoRpcScheduler(hbaseConf, hbaseConf.getInt(HConstants.REGION_SERVER_HANDLER_COUNT, 2)));

    }

    private void initZnodes() {
        final String basePath = replicationConfig.getZnodeParent();
        final String name = replicationConfig.getHbaseName();  // name of this hbase server id.
        final UUID uuid = UUID.nameUUIDFromBytes(Bytes.toBytes(name));
        final String hbaseIdPath = String.join("/", basePath, "hbaseid");
        final String rsPath = String.join("/", basePath, "rs");

        // create base path
        boolean exists = this.zkClient.exists(basePath, false);
        if (!exists) {
            // base path doesn't exists.
            try {
                this.zkClient.createPersistent(hbaseIdPath, true);
                //不能直接调用writeData方法，因为其内部默认的序列化方式，hbase-master无法识别
                this.zkClient.retryUntilConnected(() -> {
                    zkClient.getConnection().writeData(hbaseIdPath, Bytes.toBytes(uuid.toString()), -1);
                    return null;
                });
            } catch (Exception e) {
                logger.info("The zkPath {} already exists", basePath);
            }
        }

        try {
            this.zkClient.createPersistent(rsPath, true);
        } catch (Exception e) {
            logger.info("The zkPath {} already exists", rsPath);
        }

        try {
            rsServerPath = basePath + "/rs/" + serverName.getServerName();
            zkClient.createEphemeral(rsServerPath);
        } catch (Exception ex) {
            logger.error("The rsServerPath {} already exists", rsServerPath);
        }
    }

    private List<RpcServer.BlockingServiceAndInterface> getServices() {
        List<RpcServer.BlockingServiceAndInterface> bssi = new ArrayList<>(1);
        bssi.add(new RpcServer.BlockingServiceAndInterface(
                AdminProtos.AdminService.newReflectiveBlockingService(this),
                AdminProtos.AdminService.BlockingInterface.class));
        return bssi;
    }

    private int randomPort() {
        int min = 49152;
        int max = 65535;
        return new Random().nextInt(max - min + 1) + min;
    }
}
