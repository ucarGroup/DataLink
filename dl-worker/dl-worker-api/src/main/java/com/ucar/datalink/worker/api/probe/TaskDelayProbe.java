package com.ucar.datalink.worker.api.probe;

import com.ucar.datalink.worker.api.probe.index.TaskDelayProbeIndex;

/**
 * Created by lubiao on 2018/3/14.
 */
public interface TaskDelayProbe extends Probe {

    void record(TaskDelayProbeIndex index);
}
