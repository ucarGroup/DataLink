package com.ucar.datalink.flinker.core.statistics.container.collector;

import com.ucar.datalink.flinker.core.statistics.communication.Communication;
import com.ucar.datalink.flinker.core.statistics.communication.LocalTGCommunicationManager;

public class ProcessInnerCollector extends AbstractCollector {

    public ProcessInnerCollector(Long jobId) {
        super.setJobId(jobId);
    }

    @Override
    public Communication collectFromTaskGroup() {
        return LocalTGCommunicationManager.getJobCommunication();
    }

}
