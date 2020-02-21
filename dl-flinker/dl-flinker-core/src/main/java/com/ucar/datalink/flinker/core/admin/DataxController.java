package com.ucar.datalink.flinker.core.admin;

import java.util.Properties;

import com.ucar.datalink.flinker.core.admin.rest.RestServer;
import com.ucar.datalink.flinker.core.admin.util.DataSourceController;
import org.apache.commons.lang.StringUtils;

import com.ucar.datalink.flinker.api.zookeeper.ZkClientx;
import com.ucar.datalink.flinker.api.zookeeper.ZookeeperPathUtils;
import com.ucar.datalink.flinker.core.admin.JobConfigController;

/**
 * job运行管理控制器
 * 
 * @author lubiao
 * 
 */
public class DataxController {

	private JobRunningController jobRunningController;
	private JobConfigController jobConfigController;
	private ZkClientx zkClient;

	/**
	 * 内嵌jetty的rest服务，通过rest接口来控制dataX的启动和关闭
	 */
	private RestServer server;

	public DataxController(final Properties properties) {
		final String zkServers = getProperty(properties, AdminConstants.DATAX_ZKSERVERS);
		this.zkClient = ZkClientx.getZkClient(zkServers);
		this.zkClient.createPersistent(ZookeeperPathUtils.WORKERS_ROOT_NODE, true);
		this.zkClient.createPersistent(ZookeeperPathUtils.JOBS_ROOT_NODE, true);
		this.zkClient.createPersistent(ZookeeperPathUtils.MONITOR_ROOT_NODE, true);
		this.jobRunningController = new JobRunningController(zkClient);
		this.jobConfigController = new JobConfigController(zkClient);
		this.server = new RestServer(jobRunningController, zkClient);
		ChannelBase.setZkClient(zkClient);
		DataSourceController.getInstance().initialize(properties,ZkClientx.getZkClientForDatalink(zkServers));
	}

	public void start() {
		this.jobRunningController.start();
		this.jobConfigController.start();
		this.server.start();
	}

	public void stop() {
		this.jobRunningController.stop();
		this.jobConfigController.stop();
		this.server.stop();
		DataSourceController.getInstance().destroy();
	}

	private String getProperty(Properties properties, String key) {
		return StringUtils.trim(properties.getProperty(StringUtils.trim(key)));
	}
}
