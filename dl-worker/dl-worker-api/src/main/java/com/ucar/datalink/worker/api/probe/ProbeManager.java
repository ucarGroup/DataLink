package com.ucar.datalink.worker.api.probe;

import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.worker.api.probe.index.TaskDelayProbeIndex;
import com.ucar.datalink.worker.api.probe.index.TaskExceptionProbeIndex;
import com.ucar.datalink.worker.api.probe.index.TaskStatisticProbeIndex;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by lubiao on 2018/3/14.
 */
public class ProbeManager {

    private static final Logger logger = LoggerFactory.getLogger(ProbeManager.class);
    private static final ProbeManager instance = new ProbeManager();

    private TaskDelayProbe taskDelayProbe;
    private TaskExceptionProbe taskExceptionProbe;
    private TaskStatisticProbe taskStatisticProbe;
    private WorkerJvmStateProbe workerJvmStateProbe;
    private WorkerSystemStateProbe workerSystemStateProbe;

    private ProbeManager() {

    }

    public static ProbeManager getInstance() {
        return instance;
    }

    public void start(List<String> probeBlackList) {
        //init
        initTaskDelayProbe(probeBlackList);
        initTaskExceptionProbe(probeBlackList);
        initTaskStatisticProbe(probeBlackList);
        initWorkerJvmStateProbe(probeBlackList);
        initWorkerSystemStateProbe(probeBlackList);

        //start
        taskDelayProbe.start();
        taskStatisticProbe.start();
        taskExceptionProbe.start();
        workerJvmStateProbe.start();
        workerSystemStateProbe.start();

        logger.info("Probe manager started.");
    }

    public void stop() {
        taskDelayProbe.stop();
        taskStatisticProbe.stop();
        taskExceptionProbe.stop();
        workerJvmStateProbe.stop();
        workerSystemStateProbe.stop();

        logger.info("Probe manager stopped.");
    }

    private void initTaskDelayProbe(List<String> probeBlackList) {
        if (CollectionUtils.isNotEmpty(probeBlackList) && probeBlackList.contains(TaskDelayProbe.class.getSimpleName())) {
            taskDelayProbe = new TaskDelayProbe() {
                @Override
                public void record(TaskDelayProbeIndex index) {

                }

                @Override
                public void start() {

                }

                @Override
                public void stop() {

                }
            };
        } else {
            taskDelayProbe = DataLinkFactory.getObject(TaskDelayProbe.class);
        }
    }

    private void initTaskExceptionProbe(List<String> probeBlackList) {
        if (CollectionUtils.isNotEmpty(probeBlackList) && probeBlackList.contains(TaskExceptionProbe.class.getSimpleName())) {
            taskExceptionProbe = new TaskExceptionProbe() {
                @Override
                public void record(TaskExceptionProbeIndex index) {

                }

                @Override
                public void start() {

                }

                @Override
                public void stop() {

                }
            };
        } else {
            taskExceptionProbe = DataLinkFactory.getObject(TaskExceptionProbe.class);
        }
    }

    private void initTaskStatisticProbe(List<String> probeBlackList) {
        if (CollectionUtils.isNotEmpty(probeBlackList) && probeBlackList.contains(TaskStatisticProbe.class.getSimpleName())) {
            taskStatisticProbe = new TaskStatisticProbe() {
                @Override
                public void record(TaskStatisticProbeIndex index) {

                }

                @Override
                public void start() {

                }

                @Override
                public void stop() {

                }
            };
        } else {
            taskStatisticProbe = DataLinkFactory.getObject(TaskStatisticProbe.class);
        }
    }

    private void initWorkerJvmStateProbe(List<String> probeBlackList) {
        if (CollectionUtils.isNotEmpty(probeBlackList) && probeBlackList.contains(WorkerJvmStateProbe.class.getSimpleName())) {
            workerJvmStateProbe = new WorkerJvmStateProbe() {
                @Override
                public void start() {

                }

                @Override
                public void stop() {

                }
            };
        } else {
            workerJvmStateProbe = DataLinkFactory.getObject(WorkerJvmStateProbe.class);
        }
    }

    private void initWorkerSystemStateProbe(List<String> probeBlackList) {
        if (CollectionUtils.isNotEmpty(probeBlackList) && probeBlackList.contains(WorkerSystemStateProbe.class.getSimpleName())) {
            workerSystemStateProbe = new WorkerSystemStateProbe() {
                @Override
                public void start() {

                }

                @Override
                public void stop() {

                }
            };
        } else {
            workerSystemStateProbe = DataLinkFactory.getObject(WorkerSystemStateProbe.class);
        }
    }

    public TaskDelayProbe getTaskDelayProbe() {
        return taskDelayProbe;
    }

    public TaskExceptionProbe getTaskExceptionProbe() {
        return taskExceptionProbe;
    }

    public TaskStatisticProbe getTaskStatisticProbe() {
        return taskStatisticProbe;
    }

    public WorkerJvmStateProbe getWorkerJvmStateProbe() {
        return workerJvmStateProbe;
    }

    public WorkerSystemStateProbe getWorkerSystemStateProbe() {
        return workerSystemStateProbe;
    }
}
