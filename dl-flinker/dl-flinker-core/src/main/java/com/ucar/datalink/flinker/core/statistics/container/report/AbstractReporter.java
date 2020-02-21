package com.ucar.datalink.flinker.core.statistics.container.report;

import com.ucar.datalink.flinker.core.statistics.communication.Communication;

public abstract class AbstractReporter {

    public abstract void reportJobCommunication(Long jobId,Long executionId, Communication communication);

    public abstract void reportTGCommunication(Integer taskGroupId, Communication communication);

}
