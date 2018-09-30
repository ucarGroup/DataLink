package com.ucar.datalink.worker.api.probe;

import com.ucar.datalink.worker.api.probe.index.TaskExceptionProbeIndex;

/**
 * Created by lubiao on 2018/3/14.
 */
public interface TaskExceptionProbe extends Probe{

    void record(TaskExceptionProbeIndex index);

}
