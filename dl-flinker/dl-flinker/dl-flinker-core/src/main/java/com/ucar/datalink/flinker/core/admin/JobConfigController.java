package com.ucar.datalink.flinker.core.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ucar.datalink.flinker.api.util.HostUtils;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucar.datalink.flinker.api.zookeeper.ZkClientx;
import com.ucar.datalink.flinker.api.zookeeper.ZookeeperPathUtils;
/**
 * job配置管理器
 * 
 * @author lubiao
 * 
 */
public class JobConfigController {
	private static Logger logger = LoggerFactory.getLogger(JobConfigController.class);

	private ZkClientx zkClient;
	private IZkChildListener configNodeListener;
	private IZkDataListener configDataListener;
	private Set<String> localJobNames;
	private JobFileManager jobFileManager;

	public JobConfigController(ZkClientx zkClient) {
		this.configNodeListener = new IZkChildListener() {
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				syncJobs(currentChilds, false);
			}
		};

		this.configDataListener = new IZkDataListener() {
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
			}

			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				String jobName = dataPath.substring(dataPath.lastIndexOf("/") + 1);
				updateJobToLocal(jobName, new String((byte[]) data));
				//reConnect();
			}
		};

		this.zkClient = zkClient;
		this.jobFileManager = new JobFileManager();
	}

	public void start() {
		localJobNames = jobFileManager.getAllJobNames();
		zkClient.createPersistent(ZookeeperPathUtils.CONF_ROOT_NODE, true);// 不管有没有，都尝试创建一次
		//syncJobs(zkClient.getChildren(ZookeeperPathUtils.CONF_ROOT_NODE), true);
		zkClient.subscribeChildChanges(ZookeeperPathUtils.CONF_ROOT_NODE, configNodeListener);
	}

	public void stop() {
		zkClient.unsubscribeChildChanges(ZookeeperPathUtils.CONF_ROOT_NODE, configNodeListener);
		if (localJobNames != null && !localJobNames.isEmpty()) {
			for (String name : localJobNames) {
				zkClient.unsubscribeDataChanges(ZookeeperPathUtils.getJobConfPath(name), configDataListener);
			}
		}
		localJobNames = null;
	}

	private synchronized void syncJobs(List<String> currentChilds, boolean isStartCheck) {
		List<String> newJobs = new ArrayList<String>();
		List<String> deletedJobs = new ArrayList<String>();

		if (currentChilds != null && !currentChilds.isEmpty()) {
			if (localJobNames == null || localJobNames.isEmpty()) {
				newJobs.addAll(currentChilds);
			} else {
				for (String job : currentChilds) {
					if (!localJobNames.contains(job)) {
						newJobs.add(job);// 判断是否有新增的instance
					}
				}
				for (String job : localJobNames) {
					if (!currentChilds.contains(job)) {
						deletedJobs.add(job);// 判断本地的instance是否应该被移除
					}
				}
			}
		} else {
			if (localJobNames != null && !localJobNames.isEmpty()) {
				deletedJobs.addAll(localJobNames);// 删除本地所有instance
			}
		}

		if (!newJobs.isEmpty()) {
			for (String job : newJobs) {
				addJobToLocal(job);
			}
		}
		if (!deletedJobs.isEmpty()) {
			for (String job : deletedJobs) {
				deleteJobFromLocal(job);
			}
		}

		// 如果是启动时同步，则进行[数据比对]和[注册监听]操作
		if (isStartCheck && localJobNames != null && !localJobNames.isEmpty()) {
			for (String job : localJobNames) {
				if (!newJobs.contains(job)) {// 新添加的实例在前面已经进行了注册，数据也是最新的，此处过滤掉
					String path = ZookeeperPathUtils.getJobConfPath(job);
					byte[] data = (byte[]) zkClient.readData(path, true);
					if (data != null) {
						String jobContent = new String(data);
						updateJobToLocal(job, jobContent);
					}
					zkClient.subscribeDataChanges(path, configDataListener);
				}
			}
		}
	}

	private void addJobToLocal(String jobName) {
		try {
			String path = ZookeeperPathUtils.getJobConfPath(jobName);
			byte[] bytes = zkClient.readData(path, true);
			if (bytes != null) {
				String jobContent = new String(bytes);
				jobFileManager.addJob(jobName, jobContent);
				localJobNames.add(jobName);
			}
			zkClient.subscribeDataChanges(path, configDataListener);

			logger.info("job with name {} is successfully added to local file.", jobName);
		} catch (Exception e) {
			logger.error("something goes wrong when add job to local file.job name is " + jobName, e);
		}
	}

	private void deleteJobFromLocal(String jobName) {
		try {
			jobFileManager.deleteJob(jobName);
			zkClient.unsubscribeDataChanges(ZookeeperPathUtils.getJobConfPath(jobName), configDataListener);
			localJobNames.remove(jobName);

			logger.info("job with name {} is successfully deleted from local file.", jobName);
		} catch (Exception e) {
			logger.error("something goew wrong when delete job from local file.job name is " + jobName, e);
		}
	}

	private void updateJobToLocal(String jobName, String jobContent) {
		try {
			jobFileManager.updataInstance(jobName, jobContent);
			logger.info("job with name {} is successfully updated to local file.", jobName);
		} catch (Exception e) {
			logger.error("something goew wrong when update job to local file.job name is " + jobName, e);
		}
	}

	private void reConnect() {
		logger.info("received handle new session event.");
		String workerPath = ZookeeperPathUtils.getWorkerPath(HostUtils.IP);
		zkClient.createEphemeral(workerPath);
	}

}
