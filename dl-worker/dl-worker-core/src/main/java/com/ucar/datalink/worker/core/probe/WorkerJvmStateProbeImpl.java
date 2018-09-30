package com.ucar.datalink.worker.core.probe;

import com.ucar.datalink.biz.service.WorkerJvmStateService;
import com.ucar.datalink.biz.service.WorkerService;
import com.ucar.datalink.common.jvm.JvmSnapshot;
import com.ucar.datalink.common.jvm.JvmUtils;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.domain.worker.WorkerJvmStateInfo;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.worker.api.probe.WorkerJvmStateProbe;
import com.ucar.datalink.worker.core.runtime.WorkerConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sqq on 2018/3/14.
 */
@Service
public class WorkerJvmStateProbeImpl implements WorkerJvmStateProbe {

    private static final Logger logger = LoggerFactory.getLogger(WorkerJvmStateProbeImpl.class);

    private volatile boolean running = false;

    private long lastYoungCollectionCount;//新生代上一时刻的垃圾回收总次数

    private long lastOldCollectionCount;//老年代上一时刻的垃圾回收总次数

    private long lastYoungCollectionTime;//新生代上一时刻的垃圾回收总时间

    private long lastOldCollectionTime;//老年代上一时刻的垃圾回收总时间

    private ScheduledExecutorService workerJvmStateExecutor;

    @Autowired
    WorkerJvmStateService workerJvmStateService;

    @Autowired
    WorkerService workerService;

    @Override
    public void start() {
        if (running) {
            return;
        }

        long period = 1000 * 60;
        workerJvmStateExecutor = Executors.newScheduledThreadPool(1,
                new NamedThreadFactory(MessageFormat.format("Probe-Type-{0}", WorkerJvmStateProbe.class.getSimpleName())));
        workerJvmStateExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    getWorkerJvmState();
                } catch (Exception e) {
                    logger.error("Worker jvmstate probe failed.", e);
                }
            }
        }, period, period, TimeUnit.MILLISECONDS);

        logger.info("Worker jvmstate probe started.");
        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        workerJvmStateExecutor.shutdownNow();
        logger.info("Worker jvmstate probe stopped.");
    }

    private void getWorkerJvmState() {
        WorkerJvmStateInfo jvmStateInfo = new WorkerJvmStateInfo();
        JvmSnapshot jvmSnapshot = JvmUtils.buildJvmSnapshot();
        long youngGCCount = jvmSnapshot.getYoungCollectionCount();
        long intervalYoungCollectionCount = youngGCCount - lastYoungCollectionCount;
        lastYoungCollectionCount = youngGCCount;
        long youngGCTime = jvmSnapshot.getYoungCollectionTime();
        long intervalYoungCollectionTime = youngGCTime - lastYoungCollectionTime;
        lastYoungCollectionTime = youngGCTime;
        long oldGCCount = jvmSnapshot.getOldCollectionCount();
        long intervalOldCollectionCount = oldGCCount - lastOldCollectionCount;
        lastOldCollectionCount = oldGCCount;
        long oldGCTime = jvmSnapshot.getOldCollectionTime();
        long intervalOldCollectionTime = oldGCTime - lastOldCollectionTime;
        lastOldCollectionTime = oldGCTime;
        jvmStateInfo.setOldMemUsed(jvmSnapshot.getOldUsed());
        jvmStateInfo.setOldMemMax(jvmSnapshot.getOldMax());
        jvmStateInfo.setYoungMemUsed(jvmSnapshot.getYoungUsed());
        jvmStateInfo.setYoungMemMax(jvmSnapshot.getYoungMax());
        jvmStateInfo.setIntervalYoungCollectionCount(intervalYoungCollectionCount);
        jvmStateInfo.setIntervalOldCollectionCount(intervalOldCollectionCount);
        jvmStateInfo.setIntervalYoungCollectionTime(intervalYoungCollectionTime);
        jvmStateInfo.setIntervalOldCollectionTime(intervalOldCollectionTime);
        jvmStateInfo.setCurrentThreadCount(jvmSnapshot.getCurrentThreadCount());
        String clientId = WorkerConfig.current().getString(CommonClientConfigs.CLIENT_ID_CONFIG);
        jvmStateInfo.setWorkerId(Long.valueOf(clientId));
        WorkerInfo workerInfo = workerService.getById(Long.valueOf(clientId));
        if (workerInfo != null) {
            jvmStateInfo.setHost(workerInfo.getWorkerAddress());
        }
        workerJvmStateService.insert(jvmStateInfo);
    }

}
