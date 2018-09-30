package com.ucar.datalink.manager.core.monitor;

import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.manager.core.server.ServerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by csf on 17/5/2.
 */
public class MonitorManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorManager.class);

    private Map<String, Monitor> monitorMap;
    private ScheduledExecutorService executor;

    public void startup() {
        this.monitorMap = DataLinkFactory.getBeansOfType(Monitor.class);
        this.executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Monitor-Manager"));
        this.executor.scheduleAtFixedRate(() -> {
                    check();
                },
                30,
                30,
                TimeUnit.SECONDS);

        LOGGER.info("MonitorManager is started.");
    }

    public void shutdown() {
        this.monitorMap.clear();
        this.executor.shutdownNow();

        LOGGER.info("MonitorManager is shutdown.");
    }

    private void check() {
        try {
            if (ServerContainer.getInstance().getServerStatusMonitor().activeIsMine()) {
                if (monitorMap == null || monitorMap.size() == 0) {
                    return;
                }

                monitorMap.entrySet().stream().forEach(i -> i.getValue().doMonitor());
            }
        } catch (Throwable t) {
            LOGGER.error("Something goes wrong when do monitor.", t);
        }
    }
}
