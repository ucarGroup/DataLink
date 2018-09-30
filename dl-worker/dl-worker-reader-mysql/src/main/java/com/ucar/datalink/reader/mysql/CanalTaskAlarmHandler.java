package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.common.alarm.CanalAlarmHandler;
import com.ucar.datalink.worker.api.probe.ProbeManager;
import com.ucar.datalink.worker.api.probe.index.TaskExceptionProbeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lubiao on 2017/2/6.
 */
public class CanalTaskAlarmHandler implements CanalAlarmHandler {

    private static final Logger logger = LoggerFactory.getLogger(CanalTaskAlarmHandler.class);

    private String taskId;

    public CanalTaskAlarmHandler(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStart() {
        return false;
    }

    @Override
    public void sendAlarm(String s, String s1) {
        try {
            TaskExceptionProbeIndex index = new TaskExceptionProbeIndex(Long.valueOf(taskId), s1, false);
            ProbeManager.getInstance().getTaskExceptionProbe().record(index);
        } catch (Exception e) {
            logger.error("something goes wrong when append exception monitor.", e);
        }
    }
}
