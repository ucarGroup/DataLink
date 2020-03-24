package com.ucar.datalink.flinker.core.admin;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.ZkClientX;
import com.ucar.datalink.flinker.api.util.GsonUtil;
import com.ucar.datalink.flinker.api.util.HostUtils;
import com.ucar.datalink.flinker.core.RunningData;
import com.ucar.datalink.flinker.core.admin.bean.FlowControlData;
import com.ucar.datalink.flinker.core.admin.record.JobExecution;
import com.ucar.datalink.flinker.core.admin.record.JobExecutionDbUtils;
import com.ucar.datalink.flinker.core.admin.record.JobExecutionRecorder;
import com.ucar.datalink.flinker.core.job.meta.State;
import com.ucar.datalink.flinker.core.util.container.CoreConstant;
import org.I0Itec.zkclient.DataUpdater;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * job运行管理器
 * 
 * @author lubiao
 */
public class JobRunningController {
	private static Logger logger = LoggerFactory.getLogger(JobRunningController.class);

	private ZkClientX zkClient;
	private IZkDataListener configDataListener;
	private IZkStateListener zkStateListener;
	private IZkDataListener monitorConfigDataLister;

	public JobRunningController(final ZkClientX zkClient) {
		this.configDataListener = new IZkDataListener() {
			public void handleDataChange(String dataPath, Object data) throws Exception {
				final Command command = JSONObject.parseObject(new String((byte[]) data), Command.class);
				try {
					logger.info("a command is received,command content is:{}", command);
					handleCommand(command);
				} catch (Exception e) {
					logger.error("somethong goes wrong when handle command:{}" + command, e);
				}
			}

			public void handleDataDeleted(String dataPath) throws Exception {

			}
		};




		this.zkStateListener = new IZkStateListener() {
			@Override
			public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {

			}

			@Override
			public void handleNewSession() throws Exception {
				logger.info("[JobRunningController] handle new session");
				String workerPath = DLinkZkPathDef.getFlinkerWorkerNode(HostUtils.IP);
				zkClient.createEphemeral(workerPath);
			}

			@Override
			public void handleSessionEstablishmentError(Throwable throwable) throws Exception {

			}
		};

		this.monitorConfigDataLister = new IZkDataListener() {
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				final FlowControlData cdata = JSONObject.parseObject(new String((byte[]) data), FlowControlData.class);
				try {
					logger.info("a command is received,command content is:{}", cdata);
					if(cdata == null) {
						return;
					}
					if( cdata.getRecoredSpeedByZk() > 0 ) {
						ChannelBase.setRecoredSpeedByZk(cdata.getRecoredSpeedByZk());
					}
					else if( cdata.getByteSpeedByZk() > 0 ) {
						ChannelBase.setByteSpeedByZk(cdata.getByteSpeedByZk());
					}
					else {

					}
				} catch (Exception e) {
					logger.error("somethong goes wrong when handle command:{}" + cdata, e);
				}
			}

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {

			}
		};

		this.zkClient = zkClient;
	}

	public void start() {
		String workerPath = DLinkZkPathDef.getFlinkerWorkerNode(HostUtils.IP);
		String flowPath = DLinkZkPathDef.getMonitorNode(HostUtils.IP);
		zkClient.createEphemeral(workerPath);
		zkClient.createEphemeral(flowPath);

		zkClient.subscribeDataChanges(workerPath, configDataListener);
		zkClient.subscribeStateChanges(zkStateListener);
		zkClient.subscribeDataChanges(flowPath,monitorConfigDataLister);

	}

	public void stop() {
		String workerPath = DLinkZkPathDef.getFlinkerWorkerNode(HostUtils.IP);
		String flowPath = DLinkZkPathDef.getMonitorNode(HostUtils.IP);
		zkClient.unsubscribeStateChanges(zkStateListener);
		zkClient.unsubscribeDataChanges(flowPath,monitorConfigDataLister);
		zkClient.unsubscribeDataChanges(workerPath, configDataListener);
		zkClient.delete(flowPath);
		zkClient.delete(workerPath);
	}

	public void handleCommand(final Command command) {
		if (command.getType() == Command.Type.Start) {
			startJob(command);
		} else if (command.getType() == Command.Type.Stop) {
			stopJob(command);
		} else {
			logger.error("invalid command {}", command);
		}
	}

	private void startJob(final Command command) {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				String[] cmdArray = getCmdArray(command);
				logger.info("command line is : " + cmdArray==null ? "":Arrays.toString(cmdArray));

				try {
					Process proc = Runtime.getRuntime().exec(cmdArray);
					logger.info("process is started for job : {}", command.getJobName());

					int exitValue = proc.waitFor();
					if (0 != exitValue) {
						StringBuilder errorMsg = new StringBuilder();
						errorMsg.append("DataxAdminLauncher start job processor failure,please check job config file or start script file !\n");
						errorMsg.append( logProcessError(proc, command.getJobName()) );
						try {
							//这里特别加了一个try -catch，因为JobExecutionDbUtils的函数都没有catch异常，如果出现错误会影响后面
							//zk的delete执行
							//这段逻辑是当进程创建失败了，直接更新库状态，改为failure
							if(command.getExecuteId()!=null && command.getExecuteId()>0) {
								State state = JobExecutionDbUtils.getJobState(command.getExecuteId());
								if(State.UNEXECUTE == state) {
									JobExecution je = new JobExecution();
									je.setId(command.getExecuteId());
									je.setStartTime(System.currentTimeMillis());
									je.setEndTime(System.currentTimeMillis());
									je.setException(errorMsg.toString());
									je.setState(State.FAILED);
									JobExecutionDbUtils.updateJobExecutionState(je);
									logger.info("update db set job state failure!");
								} else {
									logger.info("job start failure");
								}
							} else {
								logger.warn("can not parse job execution id, please check job config file or start script file !");
							}
						} catch(Exception e) {
							logger.error(e.getMessage(),e);
						}
					}
					zkClient.delete(DLinkZkPathDef.getJobRunningNode(command.getJobName()));
					logger.info("process is stopped for job : {}", command.getJobName());
				} catch (Exception e) {
					logger.error("an error occurred when handle a process with command " + command, e);
				}

			}
		});
		t.start();
	}


	private boolean isWindowsSystem() {
		String os = System.getProperty("os.name");
		if(os.toLowerCase().startsWith("win")){
			return true;
		}
		return false;
	}

	private String[] getCmdArray(Command command) {
		List<String> cmdList = new ArrayList<String>();
		if( !isWindowsSystem() ) {
			cmdList.add("sudo");
		}
		cmdList.add("python");
		cmdList.add(MessageFormat.format("{0}/datax.py", CoreConstant.DATAX_BIN_HOME));
		if (StringUtils.isNotBlank(command.getJvmArgs())) {
			cmdList.add("-j");
			cmdList.add(MessageFormat.format("{0} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath={1}/log", command.getJvmArgs(),
					CoreConstant.DATAX_HOME));
		}
		cmdList.add("--jobid");
		cmdList.add(command.getJobId().toString());
		cmdList.add("--timingJobId");
		cmdList.add(command.getTimingJobId().toString());
		cmdList.add("--jqeid");
		cmdList.add(command.getJobQueueExecutionId().toString());
		cmdList.add("--executeId");
		cmdList.add(command.getExecuteId().toString());
		if (command.isDebug()) {
			cmdList.add("-d");
		}
		if (command.isDynamicParam()) {
			logger.error("------isDynamicParam------"+ GsonUtil.toJson(command));
			buildDynParam(command.getMapParam(),cmdList);
		}
		cmdList.add(MessageFormat.format("{0}/{1}", CoreConstant.DATAX_JOB_HOME, command.getJobName()));

		int size = cmdList.size();
		return cmdList.toArray(new String[size]);
	}

	private void buildDynParam(Map<String,String> mapParam,List<String> cmdList){
		if(mapParam==null || mapParam.size()==0){
			return;
		}
		cmdList.add("-p");
		StringBuffer buf = new StringBuffer();
		int i=0;
		for (Map.Entry<String, String> entry : mapParam.entrySet()) {
			i++;
			if(i==mapParam.size()){
				buf.append("-D" + entry.getKey() + "=" + entry.getValue());
			}else{
				buf.append("-D"+entry.getKey()+"="+entry.getValue()).append(" ");
			}
		}
		cmdList.add(buf.toString());
	}


	private String logProcessError(Process proc, String jobName) {
		BufferedReader br = null;
		String errString = "";
		try {
			br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			errString = "process failed for job " + jobName + ".Error Details:\n";
			String line = null;

			while ((line = br.readLine()) != null) {
				errString = errString.concat(line).concat("\n");
			}
			logger.error(errString);
		} catch (Exception e) {
			logger.error("something goew wrong when get process error message.", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					
				}
			}
		}
		return errString;
	}

	private void stopJob(final Command command) {
		try {
			String data = null;
			byte[] bytes = zkClient.readData(DLinkZkPathDef.getJobRunningNode(command.getJobName()), true);
			if (bytes != null) {
				data = new String(bytes);
			}
			if (StringUtils.isNotEmpty(data)) {
				RunningData runningData = JSONObject.parseObject(data, RunningData.class);
				if (command.isForceStop()) {
					if (ProcessUtils.checkIfJobProcessExists(runningData.getPid())) {
						ProcessUtils.killProcess(Integer.valueOf(runningData.getPid()));
						zkClient.delete(DLinkZkPathDef.getJobRunningNode(command.getJobName()));
						JobExecutionRecorder.getInstance().updateJobExecutionState(State.KILLED,null);
						logger.warn("kill -SIGKILL success -> "+command.toString());
						return;
					}
					logger.info("the process with pid {} does not exist.", runningData.getPid());
				} else {
					runningData.setState(State.KILLING);
					updateRunningData(runningData);
				}
			}
		} catch (Exception e) {
			logger.error("something goes wrong when stop job : ." + command.getJobName(), e);
		}
	}

	private void updateRunningData(final RunningData runningData) {
		zkClient.updateDataSerialized(DLinkZkPathDef.getJobRunningNode(runningData.getJobName()), new DataUpdater<byte[]>() {

			@Override
			public byte[] update(byte[] currentData) {
				return JSONObject.toJSONString(runningData).getBytes();
			}
		});
	}

}
