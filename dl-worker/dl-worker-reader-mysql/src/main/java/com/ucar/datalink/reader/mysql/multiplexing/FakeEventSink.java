package com.ucar.datalink.reader.mysql.multiplexing;

import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.sink.CanalEventSink;
import com.alibaba.otter.canal.sink.exception.CanalSinkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * <p>
 * 模拟的EventSink，实际不做任何Sinking的事情，主要当做一个占位符来使用.
 * 该Sink在启动时，会注册给GatewayInstance，在关闭时会从GatewayInstance注销.
 * </p>
 * <p>
 * Created by lubiao on 2019/5/10.
 */
public class FakeEventSink implements CanalEventSink {
    private static final Logger logger = LoggerFactory.getLogger(FakeEventSink.class);

    private final Canal canal;
    private final CanalInstance canalInstance;
    private volatile boolean running = false;

    public FakeEventSink(Canal canal, CanalInstance canalInstance) {
        this.canal = canal;
        this.canalInstance = canalInstance;
    }

    @Override
    public void start() {
        logger.info("fake event sink starting.");
        running = true;
        logger.info("fake event sink started.");
    }

    @Override
    public void stop() {
        logger.info("fake event sink stopping.");
        running = false;
        logger.info("fake event sink stopped.");
    }

    @Override
    public boolean isStart() {
        return running;
    }

    //---------------------------------------------not implement method-------------------------------------------------

    @Override
    public boolean sink(Object event, InetSocketAddress remoteAddress, String destination)
            throws CanalSinkException, InterruptedException {
        throw new UnsupportedOperationException("sink operation is not support.");
    }

    @Override
    public void interrupt() {
        throw new UnsupportedOperationException("interrupt operation is not support.");
    }
}
