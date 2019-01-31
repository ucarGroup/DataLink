package com.ucar.datalink.manager.core.server;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ucar.datalink.common.utils.IPUtils;
import com.ucar.datalink.common.zookeeper.ManagerMetaData;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by lubiao on 2016/12/9.
 */
public class ServerStatusMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ServerStatusMonitor.class);

    private final DLinkZkUtils zkUtils;
    private final IZkDataListener zkDataListener;
    private final IZkStateListener zkStateListener;
    private final ManagerMetaData thisManagerMetaData;
    private volatile ManagerMetaData activeManagerMetaData;
    private final ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);
    private final int delayTime = 2;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private ServerStatusListener listener;

    ServerStatusMonitor(ManagerConfig config, DLinkZkUtils zkUtils) {
        this.zkUtils = zkUtils;
        this.thisManagerMetaData = new ManagerMetaData(
                StringUtils.isEmpty(config.getHostName()) ? IPUtils.getHostIp() : config.getHostName(),
                config.getPort(),
                config.getHttpPort());
        this.zkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                //暂时不支持动态显示切换，so do nothing
                logger.info("received manager active node data change event.");
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                logger.info("received manager active node delete event.");
                setActive(null);
                // 等待delayTime，避免因网络瞬端或者zk异常，导致出现频繁的切换操作
                delayExector.schedule(() -> initRunning(), delayTime, TimeUnit.SECONDS);
            }
        };
        this.zkStateListener = new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                logger.info("received handle state change event , state is :" + state);
            }

            @Override
            public void handleNewSession() throws Exception {
                logger.info("received handle new session event.");
                setActive(null);
                initCluster();
                initRunning();
            }
        };
    }

    public synchronized boolean activeIsMine() {
        return thisManagerMetaData.equals(this.activeManagerMetaData);
    }

    public void startup() {
        isStarted.set(true);
        zkUtils.zkClient().subscribeDataChanges(DLinkZkPathDef.ManagerActiveNode, zkDataListener);
        initCluster();
        initRunning();
        zkUtils.zkClient().subscribeStateChanges(zkStateListener);

        logger.info(" ##ServerStatusMonitor is started!");
    }

    public void shutdown() {
        if (!isStarted.get()) {
            return;
        }
        zkUtils.zkClient().unsubscribeDataChanges(DLinkZkPathDef.ManagerActiveNode, zkDataListener);
        zkUtils.zkClient().unsubscribeStateChanges(zkStateListener);
        releaseRunning();
        releaseCluster();
        isStarted.set(false);

        logger.info(" ##ServerStatusMonitor is shutdown!");
    }

    private synchronized void initCluster() {
        String path = DLinkZkPathDef.getManagerClusterNode(thisManagerMetaData.getAddress() + "@" + thisManagerMetaData.getPort());
        try {
            zkUtils.zkClient().create(path, JSON.toJSONBytes(thisManagerMetaData), CreateMode.EPHEMERAL);
        } catch (ZkNoNodeException e) {
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            zkUtils.zkClient().createPersistent(parentDir, true);
            zkUtils.zkClient().create(path, JSON.toJSONBytes(thisManagerMetaData), CreateMode.EPHEMERAL);
        }
    }

    private synchronized void releaseCluster() {
        String path = DLinkZkPathDef.getManagerClusterNode(thisManagerMetaData.getAddress() + "@" + thisManagerMetaData.getPort());
        zkUtils.zkClient().delete(path);
    }

    private synchronized void initRunning() {
        if (!isStarted.get()) {
            return;
        }

        String path = DLinkZkPathDef.ManagerActiveNode;
        // 序列化
        byte[] bytes = JSON.toJSONBytes(thisManagerMetaData);
        try {
            setActive(null);
            zkUtils.zkClient().create(path, bytes, CreateMode.EPHEMERAL);
            setActive(thisManagerMetaData);
        } catch (ZkNodeExistsException e) {
            bytes = zkUtils.zkClient().readData(path, true);
            if (bytes == null) {
                initRunning();
            } else {
                setActive(JSON.parseObject(bytes, ManagerMetaData.class));
            }
        } catch (ZkNoNodeException e) {
            zkUtils.zkClient().createPersistent(DLinkZkPathDef.ManagerRoot, true); // 尝试创建父节点
            initRunning();
        }
    }

    private synchronized void releaseRunning() {
        if (activeIsMine()) {
            zkUtils.zkClient().delete(DLinkZkPathDef.ManagerActiveNode);
        }
    }

    private void setActive(ManagerMetaData active) {
        try {
            if ((this.activeManagerMetaData == null && active != null) ||
                    (this.activeManagerMetaData != null && !this.activeManagerMetaData.equals(active))) {
                this.activeManagerMetaData = active;
                listener.onActiveChange();
            }
        } catch (Exception e) {
            logger.error("process active change failed.", e);
        }
    }

    public void setListener(ServerStatusListener listener) {
        this.listener = listener;
    }

    public ManagerMetaData getActiveManagerMetaData() {
        return activeManagerMetaData;
    }

    public List<ManagerMetaData> getAllAliveManagers() {
        List<ManagerMetaData> result = Lists.newArrayList();
        List<String> childrenPath = zkUtils.zkClient().getChildren(DLinkZkPathDef.ManagerClusterRoot);
        if (childrenPath != null) {
            for (String path : childrenPath) {
                String fullPath = DLinkZkPathDef.getManagerClusterNode(path);
                result.add(JSON.parseObject((byte[]) zkUtils.zkClient().readData(fullPath, true), ManagerMetaData.class));
            }
        }
        return result;
    }
}
