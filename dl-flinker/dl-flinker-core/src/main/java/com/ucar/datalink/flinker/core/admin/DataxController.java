package com.ucar.datalink.flinker.core.admin;

import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.common.zookeeper.ZkClientX;
import com.ucar.datalink.common.zookeeper.ZkConfig;
import com.ucar.datalink.flinker.core.admin.rest.RestServer;
import com.ucar.datalink.flinker.core.admin.util.DataSourceController;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

/**
 * job运行管理控制器
 * 
 * @author lubiao
 * 
 */
public class DataxController {

	private JobRunningController jobRunningController;
	private JobConfigController jobConfigController;
	private ZkClientX zkClient;

	/**
	 * 内嵌jetty的rest服务，通过rest接口来控制dataX的启动和关闭
	 */
	private RestServer server;

	public DataxController(final Properties properties) {
		final String zkServers = getProperty(properties, AdminConstants.DATAX_ZKSERVERS);
		DLinkZkUtils zkUtils = DLinkZkUtils.init(new ZkConfig(zkServers, 10000, 10000),"/datalink");
		this.zkClient = zkUtils.zkClient();
		this.zkClient.createPersistent(DLinkZkPathDef.FlinkerWorkerRoot, true);
		this.zkClient.createPersistent(DLinkZkPathDef.FlinkerJobsRoot, true);
		this.zkClient.createPersistent(DLinkZkPathDef.FlinkerMonitorRoot, true);
		this.jobRunningController = new JobRunningController(zkClient);
		this.jobConfigController = new JobConfigController(zkClient);
		this.server = new RestServer(jobRunningController, zkClient, properties);
		ChannelBase.setZkClient(zkClient);
		DataSourceController.getInstance().initialize(properties, zkClient);
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
