package com.ucar.datalink.reader.mysql.multiplexing;

import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by lubiao on 2019/5/10.
 */
public class GatewayInstanceMonitor {
    private final static Logger logger = LoggerFactory.getLogger(GatewayInstanceMonitor.class);
    private final static Map<String, GatewayInstance> INSTANCES;
    private final static ScheduledExecutorService EXECUTOR_SERVICE;

    static {
        INSTANCES = new ConcurrentHashMap<>();
        EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("gateway-monitor"));
        EXECUTOR_SERVICE.scheduleWithFixedDelay(() -> {

            Iterator<Map.Entry<String, GatewayInstance>> iterator = INSTANCES.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, GatewayInstance> item = iterator.next();
                try {
                    MDC.put(Constants.MDC_TASKID, item.getKey());
                    item.getValue().refresh();
                } catch (Throwable t) {
                    logger.error("something goes wrong when checking instance status.", t);
                } finally {
                    MDC.remove(Constants.MDC_TASKID);
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    public static void register(String gwCanalName, GatewayInstance gatewayInstance) {
        INSTANCES.put(gwCanalName, gatewayInstance);
        logger.info("gate way instance {} registered to monitor.", gwCanalName);
    }
}
