package com.ucar.datalink.reader.mysql.multiplexing;

import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.parse.CanalEventParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 模拟的EventParser，实际不做任何parsing的事情，主要用来当做一个占位符使用,
 * 该Parser在启动时，会注册给GatewayInstance，在关闭时会从GatewayInstance注销.
 * </p>
 * <p>
 * Created by lubiao on 2019/5/8.
 */
public class FakeEventParser implements CanalEventParser {
    private static final Logger logger = LoggerFactory.getLogger(FakeEventParser.class);

    private final Canal canal;
    private final CanalInstance canalInstance;
    private final KickoutListener kickoutListener;
    private final String filter;
    private volatile boolean running = false;

    public FakeEventParser(Canal canal, CanalInstance canalInstance, KickoutListener kickoutListener, String filter) {
        this.canal = canal;
        this.canalInstance = canalInstance;
        this.kickoutListener = kickoutListener;
        this.filter = filter;
    }

    @Override
    public void start() {
        logger.info("fake event parser starting.");

        GatewayInstance gwInstance = GatewayUtil.getGatewayInstance(canal);
        gwInstance.registerEndpointInstance(canal.getName(), canalInstance, kickoutListener, filter);
        logger.info("registered to gateway instance [{}].", gwInstance.getInstanceName());

        running = true;
        logger.info("fake event parser started.");
    }

    @Override
    public void stop() {
        logger.info("fake event parser stopping.");

        GatewayInstance gwInstance = GatewayUtil.getGatewayInstance(canal);
        gwInstance.unRegisterEndpointInstance(canal.getName());
        logger.info("unregistered to gateway instance [{}].", gwInstance.getInstanceName());

        running = false;
        logger.info("fake event parser stopped.");
    }

    @Override
    public boolean isStart() {
        return running;
    }
}
