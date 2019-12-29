package com.ucar.datalink.reader.mysql.multiplexing;

import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.meta.CanalMetaManager;
import com.alibaba.otter.canal.meta.exception.CanalMetaManagerException;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.alibaba.otter.canal.protocol.position.Position;
import com.alibaba.otter.canal.protocol.position.PositionRange;
import com.alibaba.otter.canal.store.helper.CanalEventUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GatewayMetaManager:
 * 负责组装多个MetaManager，EventParser启动时从这些MetaManager中取最小的LogPosition进行初始化和启动。
 * <p>
 * <p>
 * Created by lubiao on 2019/5/10.
 */
public class GatewayMetaManager implements CanalMetaManager {
    private final static Logger logger = LoggerFactory.getLogger(GatewayMetaManager.class);

    private final Canal gwCanal;
    private final Map<String, CanalMetaManager> attachedMetaManagers;
    private volatile boolean running = false;

    public GatewayMetaManager(Canal gwCanal) {
        this.gwCanal = gwCanal;
        this.attachedMetaManagers = new ConcurrentHashMap<>();
    }

    @Override
    public void start() {
        logger.info("gateway meta manager starting.");
        running = true;
        logger.info("gateway meta manager started.");
    }

    @Override
    public void stop() {
        logger.info("gateway meta manager stopping.");
        attachedMetaManagers.clear();
        running = false;
        logger.info("gateway meta manager stopped.");
    }

    @Override
    public boolean isStart() {
        return running;
    }

    @Override
    public synchronized List<ClientIdentity> listAllSubscribeInfo(String s) throws CanalMetaManagerException {
        // 入参s没有什么作用，因为该方法需要返回所有"Sub Meta Manager"的 "SubscribeInfo"
        List<ClientIdentity> list = new ArrayList<>();
        Iterator<Map.Entry<String, CanalMetaManager>> iterator = attachedMetaManagers.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, CanalMetaManager> entry = iterator.next();
            list.addAll(entry.getValue().listAllSubscribeInfo(entry.getKey()));
        }
        return list;
    }

    /**
     * 需要加同步锁，保证在获取最小Position的时候，metaManager是不可变的
     *
     * @throws CanalMetaManagerException
     */
    @Override
    public synchronized Position getCursor(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        // 入参clientIdentity没有什么作用，因为该方法需要返回所有"Sub Meta Manager"中最小的Position
        List<ClientIdentity> clientIdentities = listAllSubscribeInfo("");
        LogPosition result = null;
        if (!CollectionUtils.isEmpty(clientIdentities)) {
            // 尝试找到一个最小的logPosition
            for (ClientIdentity item : clientIdentities) {
                LogPosition position = (LogPosition) attachedMetaManagers.get(item.getDestination()).getCursor(item);
                if (position == null) {
                    continue;
                }

                if (result == null) {
                    result = position;
                } else {
                    result = CanalEventUtils.min(result, position);
                }
            }
        }

        return result;
    }

    public synchronized void registerMetaManager(String destination, CanalMetaManager metaManager) {
        attachedMetaManagers.put(destination, metaManager);
        logger.info("registered a meta manager from endpoint instance {}.", destination);
    }

    public synchronized void unRegisterMetaManager(String destination) {
        attachedMetaManagers.remove(destination);
        logger.info("unregistered a meta manager from endpoint instance {}.", destination);
    }

    public synchronized void unRegisterMetaManager() {
        Set<String> keys = Sets.newHashSet(attachedMetaManagers.keySet());
        attachedMetaManagers.clear();
        logger.info("clear registered meta managers from endpoint instances {}.", keys);
    }

    public List<String> getRegisteredMetaManagerNames() {
        return Lists.newArrayList(attachedMetaManagers.keySet());
    }

    //-------------------------------------------------not supported methods--------------------------------------------

    @Override
    public void subscribe(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public boolean hasSubscribe(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public void unsubscribe(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public void updateCursor(ClientIdentity clientIdentity, Position position) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public PositionRange getFirstBatch(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public PositionRange getLastestBatch(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public Long addBatch(ClientIdentity clientIdentity, PositionRange positionRange) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public void addBatch(ClientIdentity clientIdentity, PositionRange positionRange, Long aLong) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public PositionRange getBatch(ClientIdentity clientIdentity, Long aLong) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public PositionRange removeBatch(ClientIdentity clientIdentity, Long aLong) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public Map<Long, PositionRange> listAllBatchs(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public void clearAllBatchs(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        throw new UnsupportedOperationException("not implemented!");
    }
}
