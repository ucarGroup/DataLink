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

    public static final String ServiceRoot;

    public static final String ServiceMailRoot;

    public static final String ServiceMailNode;

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

        ServiceRoot = Root + "/service";
        ServiceMailRoot = ServiceRoot + "/mail";
        ServiceMailNode = ServiceMailRoot + "/{0}";
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

    public static String getServiceMailNode(String msg) {
        return MessageFormat.format(ServiceMailNode, msg);
    }

}
