package com.ucar.datalink.flinker.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.ucar.datalink.flinker.core.admin.except.ZookeeperNodeExistsException;
import org.I0Itec.zkclient.IZkDataListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucar.datalink.flinker.api.util.HostUtils;
import com.ucar.datalink.flinker.api.zookeeper.ZkClientx;
import com.ucar.datalink.flinker.api.zookeeper.ZookeeperPathUtils;
import com.ucar.datalink.flinker.core.admin.AdminConstants;
import com.ucar.datalink.flinker.core.admin.ProcessUtils;
import com.ucar.datalink.flinker.core.job.meta.State;
import com.ucar.datalink.flinker.core.util.container.CoreConstant;
import com.alibaba.fastjson.JSONObject;

public class RunningDataManager {
	private static Logger logger = LoggerFactory.getLogger(RunningDataManager.class);
	private static ZkClientx zkClient;
	private static IZkDataListener configDataListener;
	private static volatile RunningData runningData;
	private static String zkPath;

	static {
		try {
			String adminConfPath = StringUtils.join(new String[] { CoreConstant.DATAX_HOME, "conf", "admin.properties" }, File.separator);
			Properties properties = new Properties();
			properties.load(new FileInputStream(adminConfPath));
			String zkServers = StringUtils.trim(properties.getProperty(StringUtils.trim(AdminConstants.DATAX_ZKSERVERS)));

			if (StringUtils.isNotEmpty(zkServers)) {
				zkClient = ZkClientx.getZkClient(zkServers);
				configDataListener = new IZkDataListener() {

					public void handleDataChange(String dataPath, Object data) throws Exception {
						final RunningData dataObj = JSONObject.parseObject(new String((byte[]) data), RunningData.class);
						try {
							runningData = dataObj;
						} catch (Exception e) {
							logger.error("somethong goes wrong when handle RunningData:{}" + dataObj, e);
						}
					}

					public void handleDataDeleted(String dataPath) throws Exception {

					}
				};
			}
		} catch (Exception e) {
			logger.error("something goes wrong when parse admin.properties.", e);
		}
	}

	public static void register(Long jqeid, Long jobId, String jobName) {
		if (zkClient != null) {
			try {
				init(jqeid, jobId, jobName);
				zkClient.createPersistent(ZookeeperPathUtils.RUNNING_ROOT_NODE, true);
				zkClient.createEphemeral(zkPath, JSONObject.toJSONString(runningData));
				zkClient.subscribeDataChanges(zkPath, configDataListener);
			}catch(Exception e) {
				logger.warn(e.getMessage(),e);
				throw new ZookeeperNodeExistsException(e.getMessage());
			}
		}
	}

	public static void release() {
		if (zkClient != null) {
			zkClient.unsubscribeDataChanges(zkPath, configDataListener);
			zkClient.delete(zkPath);
		}
	}

	public static boolean isJobKilling() {
		if (runningData != null) {
			return runningData.getState() == State.KILLING ? true : false;
		} else {
			return false;
		}
	}

	public static RunningData getRunningData() {
		return runningData;
	}

	private static void init(Long jqeid, Long jobId, String jobName) {
		runningData = new RunningData();
		runningData.setJobId(jobId);
		runningData.setJobName(jobName);
		runningData.setPid(ProcessUtils.getThisPid());
		runningData.setIp(HostUtils.IP);
		runningData.setJobQueueExecutionId(jqeid);

		zkPath = ZookeeperPathUtils.getJobRunningPath(jobName);
	}
}
