package com.ucar.datalink.flinker.core.job.scheduler.processinner;

import com.ucar.datalink.flinker.core.RunningDataManager;
import com.ucar.datalink.flinker.core.statistics.container.communicator.AbstractContainerCommunicator;

/**
 * Created by hongjiao.hj on 2014/12/22.
 */
public class StandAloneScheduler extends ProcessInnerScheduler {

	public StandAloneScheduler(AbstractContainerCommunicator containerCommunicator) {
		super(containerCommunicator);
	}

	@Override
	protected boolean isJobKilling(Long jobId) {
		return RunningDataManager.isJobKilling();
	}

}
