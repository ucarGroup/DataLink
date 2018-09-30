package com.ucar.datalink.worker.core.probe;

import com.ucar.datalink.biz.service.SysPropertiesService;
import com.ucar.datalink.biz.service.WorkerService;
import com.ucar.datalink.biz.service.WorkerSystemStateService;
import com.ucar.datalink.common.system.SystemSnapshot;
import com.ucar.datalink.common.system.SystemUtils;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.domain.worker.WorkerSystemStateInfo;
import com.ucar.datalink.worker.api.probe.WorkerSystemStateProbe;
import com.ucar.datalink.worker.core.runtime.WorkerConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by qianqian.shi on 2018/5/10.
 */
@Service
public class WorkerSystemStateProbeImpl implements WorkerSystemStateProbe {

    private static final Logger logger = LoggerFactory.getLogger(WorkerSystemStateProbeImpl.class);

    private volatile boolean running = false;

    private long lastIncomingNetworkTraffic;//上一时刻接收的总字节数

    private long lastOutgoingNetworkTraffic;//上一时刻发送的总字节数

    private ScheduledExecutorService workerSystemStateExecutor;

    @Autowired
    WorkerService workerService;

    @Autowired
    WorkerSystemStateService workerSystemStateService;

    @Autowired
    SysPropertiesService sysPropertiesService;

    @Override
    public void start() {
        if (running) {
            return;
        }

        long period = 1000 * 60;
        workerSystemStateExecutor = Executors.newScheduledThreadPool(1,
                new NamedThreadFactory(MessageFormat.format("Probe-Type-{0}", WorkerSystemStateProbe.class.getSimpleName())));
        workerSystemStateExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    getWorkerSystemState();
                } catch (Exception e) {
                    logger.error("Worker system state probe failed.", e);
                }
            }
        }, period, period, TimeUnit.MILLISECONDS);

        logger.info("Worker system state probe started.");
        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        workerSystemStateExecutor.shutdownNow();
        logger.info("Worker system state probe stopped.");
    }

    private void getWorkerSystemState() throws SigarException {
        SystemSnapshot systemSnapshot = SystemUtils.buildSystemSnapshot();
        WorkerSystemStateInfo systemStateInfo = new WorkerSystemStateInfo();
        systemStateInfo.setLoadAverage(systemSnapshot.getLoadAverage());
        systemStateInfo.setUserCPUUtilization(systemSnapshot.getUserCPUUtilization());
        systemStateInfo.setSysCPUUtilization(systemSnapshot.getSysCPUUtilization());
        Map<String, Long> incomingMap = systemSnapshot.getIncomingNetworkTrafficMap();
        Map<String, Long> outgoingMap = systemSnapshot.getOutgoingNetworkTrafficMap();
        Map<String,String> sysMap = sysPropertiesService.map();
        String netTrafficName = StringUtils.isNotBlank(sysMap.get("net_traffic_name")) ? sysMap.get("net_traffic_name") : "eth0";
        long incoming = incomingMap.get(netTrafficName);
        long incomingNetworkTraffic = incoming - lastIncomingNetworkTraffic;
        lastIncomingNetworkTraffic = incoming;
        long outgoing = outgoingMap.get(netTrafficName);
        long outgoingNetworkTraffic = outgoing - lastOutgoingNetworkTraffic;
        lastOutgoingNetworkTraffic = outgoing;
        systemStateInfo.setIncomingNetworkTraffic(incomingNetworkTraffic);
        systemStateInfo.setOutgoingNetworkTraffic(outgoingNetworkTraffic);
        systemStateInfo.setTcpCurrentEstab(systemSnapshot.getTcpCurrentEstab());
        String clientId = WorkerConfig.current().getString(CommonClientConfigs.CLIENT_ID_CONFIG);
        systemStateInfo.setWorkerId(Long.valueOf(clientId));
        WorkerInfo workerInfo = workerService.getById(Long.valueOf(clientId));
        if (workerInfo != null) {
            systemStateInfo.setHost(workerInfo.getWorkerAddress());
        }
        workerSystemStateService.insert(systemStateInfo);
    }
}
