package com.ucar.datalink.reader.mysql.multiplexing;

import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.sink.CanalEventSink;
import com.alibaba.otter.canal.sink.exception.CanalSinkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GatewayEventSink：
 * 主要对CanalInstanceWithManager产生的EventSink进行一下wrap操作，方便封装自定义逻辑。
 * <p>
 * <p>
 * Created by lubiao on 2019/5/9.
 */
public class GatewayEventSink implements CanalEventSink<List<CanalEntry.Entry>> {
    private final static Logger logger = LoggerFactory.getLogger(GatewayEventSink.class);

    private final Canal gwCanal;
    private final CanalEventSink eventSink;
    private final GatewayEventStore gatewayEventStore;
    private final Map<String, CanalEventSink> attachedEventSinks;

    public GatewayEventSink(Canal gwCanal, CanalEventSink eventSink, GatewayEventStore gatewayEventStore) {
        this.gwCanal = gwCanal;
        this.eventSink = eventSink;
        this.gatewayEventStore = gatewayEventStore;
        this.attachedEventSinks = new ConcurrentHashMap<>();
    }

    public void registerEventSink(String destination, CanalEventSink eventSink) {
        this.attachedEventSinks.put(destination, eventSink);
        logger.info("registered an event sink from endpoint instance {}.", destination);
    }

    public void unRegisterEventSink(String destination) {
        this.attachedEventSinks.remove(destination);
        logger.info("unregistered an event sink from endpoint instance {}.", destination);
    }

    @Override
    public void start() {
        logger.info("gateway event sink starting.");
        this.eventSink.start();
        logger.info("gateway event sink started.");
    }

    @Override
    public void stop() {
        logger.info("gateway event sink stopping.");
        this.attachedEventSinks.clear();
        this.eventSink.stop();
        logger.info("gateway event sink stopped.");
    }

    @Override
    public boolean isStart() {
        return this.eventSink.isStart();
    }

    @Override
    public void interrupt() {
        this.eventSink.interrupt();
        this.gatewayEventStore.interrupt();
    }

    @Override
    public boolean sink(List<CanalEntry.Entry> event, InetSocketAddress remoteAddress, String destination) throws CanalSinkException, InterruptedException {
        return eventSink.sink(event, remoteAddress, destination);
    }
}
