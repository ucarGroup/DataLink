package com.ucar.datalink.flinker.core.admin.record;

import com.ucar.datalink.flinker.core.job.meta.State;

/**
 * 
 * @author lubiao
 * 
 */
public class JobExecution {
	private Long id;
	private Long jobId;
	private Integer pid;
	private String workerAddress;
	private Long startTime;
	private Long endTime;
	private State state;
	private Long byteSpeedPerSecond;
	private Long recordSpeedPerSecond;
	private Long totalRecord;
	private Long totalErrorRecords;
	private Float WaitReaderTime;
	private Float WaitWriterTime;
	private Double percentage;
	private String exception;
	private Long jobQueueExecutionId;
	private String taskCommunicationInfo;
	private String originalConfiguration;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getWorkerAddress() {
		return workerAddress;
	}

	public void setWorkerAddress(String workerAddress) {
		this.workerAddress = workerAddress;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Long getByteSpeedPerSecond() {
		return byteSpeedPerSecond;
	}

	public void setByteSpeedPerSecond(Long byteSpeedPerSecond) {
		this.byteSpeedPerSecond = byteSpeedPerSecond;
	}

	public Long getRecordSpeedPerSecond() {
		return recordSpeedPerSecond;
	}

	public void setRecordSpeedPerSecond(Long recordSpeedPerSecond) {
		this.recordSpeedPerSecond = recordSpeedPerSecond;
	}

	public Long getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(Long totalRecord) {
		this.totalRecord = totalRecord;
	}

	public Long getTotalErrorRecords() {
		return totalErrorRecords;
	}

	public void setTotalErrorRecords(Long totalErrorRecords) {
		this.totalErrorRecords = totalErrorRecords;
	}

	public Float getWaitReaderTime() {
		return WaitReaderTime;
	}

	public void setWaitReaderTime(Float waitReaderTime) {
		WaitReaderTime = waitReaderTime;
	}

	public Float getWaitWriterTime() {
		return WaitWriterTime;
	}

	public void setWaitWriterTime(Float waitWriterTime) {
		WaitWriterTime = waitWriterTime;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public Long getJobQueueExecutionId() {
		return jobQueueExecutionId;
	}

	public void setJobQueueExecutionId(Long jobQueueExecutionId) {
		this.jobQueueExecutionId = jobQueueExecutionId;
	}

	public String getTaskCommunicationInfo() {
		return taskCommunicationInfo;
	}

	public void setTaskCommunicationInfo(String taskCommunicationInfo) {
		this.taskCommunicationInfo = taskCommunicationInfo;
	}

	public String getOriginalConfiguration() {
		return originalConfiguration;
	}

	public void setOriginalConfiguration(String originalConfiguration) {
		this.originalConfiguration = originalConfiguration;
	}
}
