package com.ucar.datalink.reader.mysql.multiplexing;

import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.protocol.position.Position;
import com.alibaba.otter.canal.store.CanalEventStore;
import com.alibaba.otter.canal.store.CanalStoreException;
import com.alibaba.otter.canal.store.model.Events;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * GatewayEventStore:
 * 负责组装多个Eventstore，并把数据库事件发送给这些store，
 * 被组装的这些Store是各个MysqlTaskReader管理的CanalInstance的EventSotre,
 * 这些store沿着FakeEventSink -> GatewayInstance -> GatewayEventStore这一路径，注册到这里
 * <p>
 * Created by lubiao on 2019/5/9.
 */
public class GatewayEventStore implements CanalEventStore {
    private final static Logger logger = LoggerFactory.getLogger(GatewayEventStore.class);

    private final Canal gwCanal;
    private final Map<String, CanalEventStore> attachedEventStores;
    private final Map<String, CanalEventStore> succeededStores;
    private volatile long startTime;
    private volatile boolean isSuspend = false;
    private volatile boolean interrupted = false;
    private volatile boolean running = false;

    public GatewayEventStore(Canal gwCanal) {
        this.gwCanal = gwCanal;
        this.attachedEventStores = new ConcurrentHashMap<>();
        this.succeededStores = new HashMap<>();
    }

    @Override
    public void start() {
        logger.info("gateway event store starting.");
        running = true;
        logger.info("gateway event store started.");
    }

    @Override
    public void stop() {
        logger.info("gateway event store stopping.");
        attachedEventStores.clear();
        running = false;
        logger.info("gateway event store stopped.");
    }

    @Override
    public boolean isStart() {
        return running;
    }


    @Override
    public boolean tryPut(List data) throws CanalStoreException {
        /*
         * 对类加一个全局锁，以避免上一轮的instanc和本轮的instance，同时执行tryput方法，而出现线程安全问题
         * 为什么会出现此种情况呢？因为上一轮的instance关闭时，只是把running的状态置为了false，tryput方法不一定立刻退出，而下一轮的
         * instance此时可能已经启动起来了，这样就可能出现同一个EnpointInstance的CanalEventStore被两个GatewayEventStore持有的情况。
         */
        synchronized (GatewayEventStore.class) {
            return doPut(data);
        }
    }

    public void registerEventStore(String destination, CanalEventStore eventStore) {
        attachedEventStores.put(destination, eventStore);
        logger.info("registered an event store from endpoint instance {}.", destination);
    }

    public void unRegisterEventStore(String destination) {
        attachedEventStores.remove(destination);
        logger.info("unregistered an event store from endpoint instance {}.", destination);
    }

    public Set<String> checkKickout() {
        if (isSuspend) {
            Set<String> set = new HashSet<>();
            for (String s : attachedEventStores.keySet()) {
                if (!succeededStores.containsKey(s)) {
                    set.add(s);
                }
            }

            //如果全部都挂起了，就没必要kickout了，因为重启之后很可能还是挂起，直接就地继续重试就行了
            if (set.size() == attachedEventStores.size()) {
                logger.info("all stores are put failed , no need to kickout.");
                return new HashSet<>();
            }

            isSuspend = false;
            return set;
        }

        return new HashSet<>();
    }

    /**
     * GatewayEventStore的中断判断，不能使用Thread.interrupted()方法，而是靠自己设定的中断开关进行控制
     * 原因：
     *      EntryEventSink中已经使用了Thread.interrupted()方法，此处如果也使用该方法，会导致store退出之后，sink无法退出，
     *      因为store中调用完Thread.interrupted()之后，线程中断状态会自动重置为正常，sink中的while循环就无法正常退出了
     *      为什么要靠中断来退出while循环呢？不是已经有running开关了吗？根本原因是canal的instance在关闭的时候，内部的关闭
     *      流程为先关闭parser，再关闭sink和store，其为了保证parser线程能彻底关闭，在触发parser的stop之后会执行线程的join
     *      等待，即：还没有机会触发sink和sotre的stop方法，只能靠interrupt进行中断操作。其实canal的设计不太合理，应该是为
     *      sink、store等增加一个awaitStop方法，不应该在stop的时候进行阻塞操作。
     */
    public void interrupt() {
        this.interrupted = true;
    }

    public boolean isSuspend() {
        return isSuspend;
    }

    /**
     * 必须所有store都put成功之后再返回，避免外部重试导致单个store一直重复put
     */
    @SuppressWarnings("unchecked")
    private boolean doPut(List data) throws CanalStoreException {
        succeededStores.clear();
        isSuspend = false;
        startTime = System.currentTimeMillis();

        while (running && !interrupted) {
            if (!isSuspend) {
                Map<String, CanalEventStore> stores = Maps.newHashMap(attachedEventStores);
                if (stores.isEmpty()) {
                    return false;
                }

                boolean result = true;
                for (String key : stores.keySet()) {
                    if (!succeededStores.containsKey(key)) {
                        boolean putResult = stores.get(key).tryPut(data);
                        if (putResult) {
                            succeededStores.put(key, stores.get(key));
                        }
                        result &= putResult;
                    }
                }
                if (result) {
                    return true;
                }
                if (System.currentTimeMillis() - startTime > 1000 * 60 * 1) {
                    isSuspend = true;
                }
            }

            //100ms
            LockSupport.parkNanos(1000 * 1000L * 100);
        }

        return false;
    }

    //------------------------------------------------not supported methods---------------------------------------------

    @Override
    public void put(List data) throws InterruptedException, CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public boolean put(List data, long timeout, TimeUnit unit) throws InterruptedException, CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public void put(Object data) throws InterruptedException, CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public boolean put(Object data, long timeout, TimeUnit unit) throws InterruptedException, CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public boolean tryPut(Object data) throws CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public Events get(Position start, int batchSize) throws InterruptedException, CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public Events get(Position start, int batchSize, long timeout, TimeUnit unit) throws InterruptedException, CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public Events tryGet(Position start, int batchSize) throws CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public Position getLatestPosition() throws CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public Position getFirstPosition() throws CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public void ack(Position position) throws CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public void rollback() throws CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public void cleanUntil(Position position) throws CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }

    @Override
    public void cleanAll() throws CanalStoreException {
        throw new UnsupportedOperationException("this method is not support.");
    }
}
