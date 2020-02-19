package com.ucar.datalink.flinker.api.zookeeper;

import java.text.MessageFormat;

/**
 * 
 * @author lubiao
 * 
 */
public class ZookeeperPathUtils {
	public static final String ZOOKEEPER_SEPARATOR = "/";

	public static final String DATAX_ROOT_NODE = "/datax";

	public static final String ADMIN_ROOT_NODE = DATAX_ROOT_NODE + ZOOKEEPER_SEPARATOR + "admin";

	public static final String WORKERS_ROOT_NODE = ADMIN_ROOT_NODE + ZOOKEEPER_SEPARATOR + "workers";

	public static final String WORKER_NODE = WORKERS_ROOT_NODE + ZOOKEEPER_SEPARATOR + "{0}";

	public static final String JOBS_ROOT_NODE = ADMIN_ROOT_NODE + ZOOKEEPER_SEPARATOR + "jobs";

	public static final String RUNNING_ROOT_NODE = JOBS_ROOT_NODE + ZOOKEEPER_SEPARATOR + "running";

	public static final String RUNNING_NODE = RUNNING_ROOT_NODE + ZOOKEEPER_SEPARATOR + "{0}";

	public static final String CONF_ROOT_NODE = JOBS_ROOT_NODE + ZOOKEEPER_SEPARATOR + "conf";

	public static final String CONF_NODE = CONF_ROOT_NODE + ZOOKEEPER_SEPARATOR + "{0}";

	/**
	 * 监控节点 /datax/admin/monitor，这个节点用于记录监控统计等信息
	 */
	public static final String MONITOR_ROOT_NODE = ADMIN_ROOT_NODE + ZOOKEEPER_SEPARATOR + "monitor";

	public static final String MONITOR_NODE = MONITOR_ROOT_NODE + ZOOKEEPER_SEPARATOR + "{0}";

	/**
	 * data-link注册的双机房信息，用作表示机房信息，logicA or logicB
	 */
	public static final String DATA_LINK_CENTER_LAB = "/datalink/doublecenter/centerLab";

	/**
	 *   双机房切换信息v2
	 */
	public static final String DATA_LINK_CENTER_LAB_NEW = "/datalink/doublecenter/centerLabNew";

	/**
	 * 机房信息
	 */
	public static final String DATA_LINK_LAB_INFO_LIST = "/datalink/doublecenter/labInfoList";

	/**
	 * 正在做双机房切换的标志信息
	 */
	public static final String DATA_LINK_LAB_SWITCH_PROCESSING = "/datalink/doublecenter/labSwitchProcessing";

	/**
	 * 双机房切换的znode节点的子节点名称
	 */
	public static final String DATA_LINK_LAB_SWITCH_PROCESSING_CHILD_ZNODE = "labSwitchProcessing";

	/**
	 * 正在做双机房切换的标志信息，只包含父节点信息
	 */
	public static final String DATA_LINK_LAB_SWITCH_PROCESSING_PARENT_ZNODE = "/datalink/doublecenter";


	public static String getWorkerPath(String workerIp) {
		return MessageFormat.format(WORKER_NODE, workerIp);
	}

	public static String getJobRunningPath(String jobName) {
		return MessageFormat.format(RUNNING_NODE, jobName);
	}

	public static String getJobConfPath(String jobName) {
		return MessageFormat.format(CONF_NODE, jobName);
	}

	public static String getMonitorPath(String monitorIp) { return MessageFormat.format(MONITOR_NODE, monitorIp); }


	public static String getDataLinkCenterLab() {
		return DATA_LINK_CENTER_LAB;
	}

	public static String getDataLinkLabInfoList() {
		return DATA_LINK_LAB_INFO_LIST;
	}

	public static String getDataLinkLabSwitchProcessing() {
		return DATA_LINK_LAB_SWITCH_PROCESSING;
	}

	public static String getDataLinkLabSwitchProcessingChildZnode() {
		return DATA_LINK_LAB_SWITCH_PROCESSING_CHILD_ZNODE;
	}

	public static String getDataLinkLabSwitchProcessingParentZnode() {
		return DATA_LINK_LAB_SWITCH_PROCESSING_PARENT_ZNODE;
	}
}
