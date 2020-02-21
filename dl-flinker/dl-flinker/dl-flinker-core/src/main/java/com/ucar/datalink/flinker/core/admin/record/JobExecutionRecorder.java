package com.ucar.datalink.flinker.core.admin.record;

import java.lang.Float;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.ucar.datalink.flinker.api.base.TaskInfo;
import com.ucar.datalink.flinker.api.statistics.PerfTrace;
import com.ucar.datalink.flinker.api.util.GsonUtil;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import com.ucar.datalink.flinker.core.job.meta.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ucar.datalink.flinker.api.util.HostUtils;
import com.ucar.datalink.flinker.core.RunningDataManager;
import com.ucar.datalink.flinker.core.admin.ProcessUtils;
import com.ucar.datalink.flinker.core.statistics.communication.Communication;
import com.ucar.datalink.flinker.core.statistics.communication.CommunicationTool;

/**
 * job运行状态记录器
 * 
 * @author lubiao
 * 
 */
public class JobExecutionRecorder {
	private static Logger logger = LoggerFactory.getLogger(JobExecutionRecorder.class);

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static JobExecutionRecorder jobExecutionRecorder = new JobExecutionRecorder();

	private JobExecution jobExecution;

	public static JobExecutionRecorder getInstance() {
		return jobExecutionRecorder;
	}



	public JobExecution getJobExecution() {
		return jobExecution;
	}

	public void record(Long jobId, Long exeutionId, Communication communication) {
		try {
			if (jobExecution == null) {
				if(exeutionId > 0) {
					initJobExecutionByHAInvoke(jobId, exeutionId, communication);
				} else {
					addJobExecution(jobId,communication);
				}
			} else {
				updateJobExecution(jobId, communication);
				//判断是否要更新定时任务
				//updateTimingTask(communication.getState());
			}
		} catch (Exception e) {
			logger.error("communication record error.", e);
		}
	}

	public void updateJobExecutionState(State state, Throwable e)throws SQLException {
		if(jobExecution != null) {
			jobExecution.setState(state);
			jobExecution.setEndTime(System.currentTimeMillis());
			if(e != null) {
				jobExecution.setException(getThrowableMessage("[JobExecutionRecorder]  updateJobExecutionState",e));
			}
			JobExecutionDbUtils.updateJobExecutionState(jobExecution);
		}
	}

	public void updateState(State state, Throwable e)throws SQLException {
		if(jobExecution != null) {
			jobExecution.setState(state);
			jobExecution.setEndTime(System.currentTimeMillis());
			if(e != null) {
				jobExecution.setException(getThrowableMessage("[JobExecutionRecorder]  updateJobExecutionState",e));
			}
			JobExecutionDbUtils.updateJobExecutionState(jobExecution);
		}
	}


	private void updateTimingTask(State state) throws SQLException {
		PerfTrace perfTrace = PerfTrace.getInstance();
		Long timingJobId = perfTrace.getTimingJobId();
		if(timingJobId!=null && timingJobId!=-1 && state!=null){
			if(State.FAILED.name().equals(state.name()) || State.SUCCEEDED.name().equals(state.name())){
				logger.error("-------timingJobId-------"+timingJobId);
				JobExecutionDbUtils.updateTimingTask(timingJobId,state.value());
			}
		}
	}


	private void initJobExecutionByHAInvoke(Long jobId, Long executionId,  Communication communication) throws SQLException {
		jobExecution = new JobExecution();
		jobExecution.setId(executionId);
		jobExecution.setStartTime(communication.getTimestamp());
		jobExecution.setJobId(jobId);
		jobExecution.setState(communication.getState());
		jobExecution.setWorkerAddress(HostUtils.IP);
		jobExecution.setPid(ProcessUtils.getThisPid());
		if(RunningDataManager.getRunningData()!=null && (RunningDataManager.getRunningData().getJobQueueExecutionId()!=null)) {
			jobExecution.setJobQueueExecutionId(RunningDataManager.getRunningData().getJobQueueExecutionId());
		} else {
			jobExecution.setJobQueueExecutionId(-1L);
		}
		if (communication.getThrowable() != null) {
			jobExecution.setException( getThrowableMessage("[JobExecutionRecorder]  initJobExecutionByHAInvoke",communication.getThrowable()) );
		}
		setOriginalConfiguration(jobExecution);
		jobExecution.setByteSpeedPerSecond(0L);
		jobExecution.setRecordSpeedPerSecond(0L);
		jobExecution.setTotalRecord(0L);
		jobExecution.setTotalErrorRecords(0L);
		jobExecution.setWaitReaderTime(0.0F);
		jobExecution.setWaitWriterTime(0.0F);
		jobExecution.setPercentage(0.0D);
		JobExecutionDbUtils.initJobExecutionByHAInvoke(jobExecution);
	}

	private void addJobExecution(Long jobId, Communication communication) throws SQLException {
		jobExecution = new JobExecution();
		jobExecution.setStartTime(communication.getTimestamp());
		jobExecution.setJobId(jobId);
		jobExecution.setState(communication.getState());
		jobExecution.setWorkerAddress(HostUtils.IP);
		jobExecution.setPid(ProcessUtils.getThisPid());
		jobExecution.setJobQueueExecutionId(RunningDataManager.getRunningData().getJobQueueExecutionId());
		if (communication.getThrowable() != null) {
			jobExecution.setException( getThrowableMessage("[JobExecutionRecorder]  addJobExecution",communication.getThrowable()) );
		}
		setOriginalConfiguration(jobExecution);
		JobExecutionDbUtils.insertJobExecution(jobExecution);
	}


	private void updateJobExecution(Long jobId, Communication communication) throws SQLException {
		jobExecution.setByteSpeedPerSecond(communication.getLongCounter(CommunicationTool.BYTE_SPEED));
		jobExecution.setPercentage(communication.getDoubleCounter(CommunicationTool.PERCENTAGE));
		jobExecution.setRecordSpeedPerSecond(communication.getLongCounter(CommunicationTool.RECORD_SPEED));
		jobExecution.setState(communication.getState());
		jobExecution.setTotalErrorRecords(communication.getLongCounter(CommunicationTool.TOTAL_ERROR_RECORDS));
		jobExecution.setTotalRecord(communication.getLongCounter(CommunicationTool.TOTAL_READ_RECORDS));
		jobExecution.setWaitReaderTime(unitTime(communication.getLongCounter(CommunicationTool.WAIT_READER_TIME)));
		jobExecution.setWaitWriterTime(unitTime(communication.getLongCounter(CommunicationTool.WAIT_WRITER_TIME)));

		if (communication.getThrowable() != null) {
			jobExecution.setException( getThrowableMessage("[JobExecutionRecorder]  updateJobExecution",communication.getThrowable()) );
		}
		if (jobExecution.getState().isFinished()) {
			jobExecution.setEndTime(communication.getTimestamp());
		}
		setTaskCommunicationInfo(jobExecution);

		//高可用方式插入job execution id时是刚启动的时候，此时Configuration还没有生成好，所以插入的job execution这个记录的 原始配置这个字段就是null
		//等初始化完成了，原始配置才生成完毕，所以等待下一次更新 (job运行的时候/或者是job执行完的时候)，再更新这个原始配置字段
		if(jobExecution.getOriginalConfiguration() == null) {
			setOriginalConfiguration(jobExecution);
			JobExecutionDbUtils.updateJobExecutionAndOriginalConfiguration(jobExecution);
		} else {
			JobExecutionDbUtils.updateJobExecution(jobExecution);
		}
	}

	private void setTaskCommunicationInfo(JobExecution jobExecution){
		try {
			PerfTrace perfTrace = PerfTrace.getInstance();
			if(perfTrace!=null){
				List<TaskInfo> list = new ArrayList<TaskInfo>();
				Map<Integer, TaskInfo> map = perfTrace.getTaskCommunicationMap();
				for(Map.Entry<Integer, TaskInfo> entry:map.entrySet()){
					list.add(entry.getValue());
				}
				jobExecution.setTaskCommunicationInfo(GsonUtil.toJson(list));
			}
		}catch (Exception e){
			logger.error("set task communication info is error",e);
		}
	}




	private String getThrowableMessage(String str ,Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append(str).append(LINE_SEPARATOR);
		sb.append(t.getMessage());
		sb.append(LINE_SEPARATOR);
		StackTraceElement[] ste = t.getStackTrace();
		for(StackTraceElement s : ste) {
			sb.append(s.toString());
			sb.append(LINE_SEPARATOR);
		}
		String throwableMessage = sb.toString();
		String error = ErrorRecord.assembleError(throwableMessage);
		return error;
	}



	private void setOriginalConfiguration(JobExecution jobExecution){
		try {
			PerfTrace perfTrace = PerfTrace.getInstance();
			if(perfTrace!=null){
				String str = perfTrace.getOriginalConfiguration();
				if(str!=null && str.length() >= 4900){
					str = str.substring(0,4900);
				}
				jobExecution.setOriginalConfiguration(str);
			}
		}catch (Exception e){
			logger.error("set original configuration is error",e);
		}
	}

	private static float unitTime(long time) {
		return ((float) TimeUnit.NANOSECONDS.toNanos(time)) / 1000000000;
	}
}
