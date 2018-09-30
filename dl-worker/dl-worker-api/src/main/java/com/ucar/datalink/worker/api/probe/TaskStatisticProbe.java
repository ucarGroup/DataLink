package com.ucar.datalink.worker.api.probe;

import com.ucar.datalink.worker.api.probe.index.TaskStatisticProbeIndex;

/**
 * Created by lubiao on 2018/3/14.
 */
public interface TaskStatisticProbe extends Probe {

    void record(TaskStatisticProbeIndex index);
}
