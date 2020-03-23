package com.ucar.datalink.common.zookeeper;

import java.text.MessageFormat;

/**
 * 使用该类之前，应该先初始化DLinkZkUtils，以获取根节点名称
 * <p>
 * Created by lubiao on 2016/12/8.
 */
public class DLinkZkPathDef {

    public static final String Root;

    public static final String ManagerRoot;

    public static final String ManagerClusterRoot;

    public static final String ManagerClusterNode;

    public static final String ManagerActiveNode;

    public static final String WorkerRoot;

    public static final String WorkerNode;

    public static final String TaskRoot;

    public static final String TaskNode;

    public static final String TaskStatusNode;

    public static final String TaskPositionNode;

    public static final String TaskSyncStatusNode;

    public static final String FlinkerRoot;

    public static final String FlinkerWorkerRoot;

    public static final String FlinkerWorkerNode;

    public static final String FlinkerJobsRoot;

    public static final String FlinkerRunningRoot;

    public static final String FlinkerRunningNode;

    public static final String FlinkerMonitorRoot;

    public static final String FlinkerMonitorNode;

    public static final String FlinkerConfRoot;

    public static final String FlinkerConfNode;

    static {
        Root = DLinkZkUtils.get().zkRoot();

        ManagerRoot = Root + "/managers";
        ManagerClusterRoot = Root + "/managers/cluster";
        ManagerClusterNode = ManagerClusterRoot + "/{0}";
        ManagerActiveNode = Root + "/managers/active";

        WorkerRoot = Root + "/workers";
        WorkerNode = WorkerRoot + "/{0}";

        TaskRoot = Root + "/tasks";
        TaskNode = TaskRoot + "/{0}";
        TaskStatusNode = TaskNode + "/status";
        TaskPositionNode = TaskNode + "/position";
        TaskSyncStatusNode = TaskNode + "/syncstatus";

        FlinkerRoot = Root + "/flinker";
        FlinkerWorkerRoot = FlinkerRoot + "/workers";
        FlinkerWorkerNode = FlinkerWorkerRoot + "/{0}";
        FlinkerJobsRoot = FlinkerRoot + "/jobs";
        FlinkerRunningRoot = FlinkerJobsRoot + "/running";
        FlinkerRunningNode = FlinkerRunningRoot + "/{0}";
        FlinkerMonitorRoot = FlinkerRoot + "/monitor";
        FlinkerMonitorNode = FlinkerMonitorRoot + "/{0}";
        FlinkerConfRoot = FlinkerRoot + "/conf";
        FlinkerConfNode = FlinkerConfRoot + "/{0}";
    }

    public static String getManagerClusterNode(String node) {
        return MessageFormat.format(ManagerClusterNode, node);
    }

    public static String getWorkerNode(String node) {
        return MessageFormat.format(WorkerNode, node);
    }

    public static String getTaskNode(String taskId) {
        return MessageFormat.format(TaskNode, taskId);
    }

    public static String getTaskStatusNode(String taskId) {
        return MessageFormat.format(TaskStatusNode, taskId);
    }

    public static String getTaskPositionNode(String taskId) {
        return MessageFormat.format(TaskPositionNode, taskId);
    }

    public static String getTaskSyncStatusNode(String taskId) {
        return MessageFormat.format(TaskSyncStatusNode, taskId);
    }

    public static String getFlinkerWorkerNode(String workerIp) {
        return MessageFormat.format(FlinkerWorkerNode, workerIp);
    }

    public static String getJobRunningNode(String jobName) {
        return MessageFormat.format(FlinkerRunningNode, jobName);
    }

    public static String getJobConfNode(String jobName) {
        return MessageFormat.format(FlinkerConfNode, jobName);
    }

    public static String getMonitorNode(String monitorIp) { return MessageFormat.format(FlinkerMonitorNode, monitorIp); }
}
