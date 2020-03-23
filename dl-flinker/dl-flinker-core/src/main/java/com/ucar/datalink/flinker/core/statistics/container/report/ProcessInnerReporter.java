package com.ucar.datalink.flinker.core.statistics.container.report;

import com.ucar.datalink.flinker.core.admin.record.JobExecutionRecorder;
import com.ucar.datalink.flinker.core.statistics.communication.Communication;
import com.ucar.datalink.flinker.core.statistics.communication.LocalTGCommunicationManager;


public class ProcessInnerReporter extends AbstractReporter {

	@Override
	public void reportJobCommunication(Long jobId, Long executionId, Communication communication) {
		JobExecutionRecorder.getInstance().record(jobId, executionId, communication);//added by lubiao
	}

	@Override
	public void reportTGCommunication(Integer taskGroupId, Communication communication) {
		LocalTGCommunicationManager.updateTaskGroupCommunication(taskGroupId, communication);
	}
}