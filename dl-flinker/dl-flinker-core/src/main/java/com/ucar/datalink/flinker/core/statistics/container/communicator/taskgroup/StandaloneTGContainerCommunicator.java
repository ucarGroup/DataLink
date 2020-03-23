package com.ucar.datalink.flinker.core.statistics.container.communicator.taskgroup;

import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.core.statistics.container.report.ProcessInnerReporter;
import com.ucar.datalink.flinker.core.statistics.communication.Communication;

public class StandaloneTGContainerCommunicator extends AbstractTGContainerCommunicator {

    public StandaloneTGContainerCommunicator(Configuration configuration) {
        super(configuration);
        super.setReporter(new ProcessInnerReporter());
    }

    @Override
    public void report(Communication communication) {
        super.getReporter().reportTGCommunication(super.taskGroupId, communication);
    }

}
