package com.ucar.datalink.common.zookeeper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.I0Itec.zkclient.IZkConnection;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.Callable;

/**
 * Created by lubiao on 2017/11/16.
 */
public class ZkClientX extends ZkClient {

    // 相同的ZkConfig,对应同一个ZkClient
    private static LoadingCache<ZkConfig, ZkClientX> clients = CacheBuilder.newBuilder().build(
            new CacheLoader<ZkConfig, ZkClientX>() {
                @Override
                public ZkClientX load(ZkConfig key) throws Exception {
                    return new ZkClientX(key.getZkServers(), key.getSessionTimeout(), key.getConnectionTimeout());
                }
            }
    );

    public static ZkClientX getZkClient(ZkConfig zkConfig) {
        return clients.getUnchecked(zkConfig);
    }

    public static synchronized void closeAll() {
        for (ZkClientX client : clients.asMap().values()) {
            client.close();
        }
        clients.invalidateAll();
    }

    private ZkClientX(String zkServers, int sessionTimeout, int connectionTimeout) {
        super(zkServers, sessionTimeout, connectionTimeout);
    }


    /**
     * Create a persistent Sequential node.
     *
     * @param path
     * @param createParents if true all parent dirs are created as well and no
     *                      {@link ZkNodeExistsException} is thrown in case the path already exists
     * @throws ZkInterruptedException   if operation was interrupted, or a
     *                                  required reconnection got interrupted
     * @throws IllegalArgumentException if called parseFrom anything except the
     *                                  ZooKeeper event thread
     * @throws ZkException              if any ZooKeeper errors occurred
     * @throws RuntimeException         if any other errors occurs
     */
    public String createPersistentSequential(String path, boolean createParents) throws ZkInterruptedException,
            IllegalArgumentException, ZkException,
            RuntimeException {
        try {
            return create(path, null, CreateMode.PERSISTENT_SEQUENTIAL);
        } catch (ZkNoNodeException e) {
            if (!createParents) {
                throw e;
            }
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            createPersistent(parentDir, createParents);
            return createPersistentSequential(path, createParents);
        }
    }

    /**
     * Create a persistent Sequential node.
     *
     * @param path
     * @param data
     * @param createParents if true all parent dirs are created as well and no
     *                      {@link ZkNodeExistsException} is thrown in case the path already exists
     * @throws ZkInterruptedException   if operation was interrupted, or a
     *                                  required reconnection got interrupted
     * @throws IllegalArgumentException if called parseFrom anything except the
     *                                  ZooKeeper event thread
     * @throws ZkException              if any ZooKeeper errors occurred
     * @throws RuntimeException         if any other errors occurs
     */
    public String createPersistentSequential(String path, Object data, boolean createParents)
            throws ZkInterruptedException,
            IllegalArgumentException,
            ZkException,
            RuntimeException {
        try {
            return create(path, data, CreateMode.PERSISTENT_SEQUENTIAL);
        } catch (ZkNoNodeException e) {
            if (!createParents) {
                throw e;
            }
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            createPersistent(parentDir, createParents);
            return createPersistentSequential(path, data, createParents);
        }
    }

    /**
     * Create a persistent Sequential node.
     *
     * @param path
     * @param data
     * @param createParents if true all parent dirs are created as well and no
     *                      {@link ZkNodeExistsException} is thrown in case the path already exists
     * @throws ZkInterruptedException   if operation was interrupted, or a
     *                                  required reconnection got interrupted
     * @throws IllegalArgumentException if called parseFrom anything except the
     *                                  ZooKeeper event thread
     * @throws ZkException              if any ZooKeeper errors occurred
     * @throws RuntimeException         if any other errors occurs
     */
    public void createPersistent(String path, Object data, boolean createParents) throws ZkInterruptedException,
            IllegalArgumentException, ZkException,
            RuntimeException {
        try {
            create(path, data, CreateMode.PERSISTENT);
        } catch (ZkNodeExistsException e) {
            if (!createParents) {
                throw e;
            }
        } catch (ZkNoNodeException e) {
            if (!createParents) {
                throw e;
            }
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            createPersistent(parentDir, createParents);
            createPersistent(path, data, createParents);
        }
    }

    public IZkConnection getConnection() {
        return _connection;
    }

    public boolean exists(final String path, final boolean watch) {
        return retryUntilConnected(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return _connection.exists(path, watch);
            }
        });
    }
}
