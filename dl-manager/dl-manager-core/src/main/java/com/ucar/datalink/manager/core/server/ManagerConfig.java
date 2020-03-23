package com.ucar.datalink.manager.core.server;

import com.ucar.datalink.common.errors.DatalinkException;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Properties;

/**
 * Created by lubiao on 2016/12/7.
 */
public class ManagerConfig extends AbstractConfig {
    private static final ConfigDef configDef;
    //---------host config------------
    private static final String hostNameProp = "host.name";
    private static final String portProp = "port";
    private static final String httpPortProp = "http.port";
    //---------zookeeper config-------
    private static final String zkRootProp = "zookeeper.root";
    private static final String zkServerProp = "zookeeper.servers";
    private static final String zkSessionTimeoutMsProp = "zookeeper.session.timeout.ms";
    private static final String zkConnectionTimeoutMsProp = "zookeeper.connection.timeout.ms";
    //---------group coordinator config-------------
    private static final String groupMinSessionTimeoutMsProp = "group.min.session.timeout.ms";
    private static final String groupMaxSessionTimeoutMsProp = "group.max.session.timeout.ms";
    //---------other configs-------------
    private static final String currentEnv = "currentEnv";
    /**
     * 检测频率
     */
    private static final String monitorCheckIntervalTime = "monitor.check.intervalTime";
    /**
     * 是否开启读端数据多路复用
     */
    private static final String multiplexingRead = "multiplexingRead";
    /**
     * Flinker启动Job时的最小内存
     */
    private static final String flinkerJobMinJVMMemory = "flinker.job.min.jvmMemory";
    /**
     * Flinker启动Job时的最大内存
     */
    private static final String flinkerJobMaxJVMMemory = "flinker.job.max.jvmMemory";

    private static ManagerConfig instance;

    static {
        configDef = new ConfigDef()
                .define(portProp, ConfigDef.Type.INT, Defaults.Port, ConfigDef.Importance.HIGH, "")
                .define(httpPortProp, ConfigDef.Type.INT, Defaults.HttpPort, ConfigDef.Importance.HIGH, "")
                .define(hostNameProp, ConfigDef.Type.STRING, Defaults.HostName, ConfigDef.Importance.HIGH, "")
                .define(zkRootProp, ConfigDef.Type.STRING, Defaults.ZkRoot, ConfigDef.Importance.HIGH, "")
                .define(zkServerProp, ConfigDef.Type.STRING, Defaults.ZkServer, ConfigDef.Importance.HIGH, "")
                .define(zkSessionTimeoutMsProp, ConfigDef.Type.INT, Defaults.ZkSessionTimeoutMs, ConfigDef.Importance.HIGH, "")
                .define(zkConnectionTimeoutMsProp, ConfigDef.Type.INT, Defaults.ZkConnectionTimeout, ConfigDef.Importance.HIGH, "")
                .define(groupMinSessionTimeoutMsProp, ConfigDef.Type.INT, Defaults.GroupMinSessionTimeoutMs, ConfigDef.Importance.MEDIUM, "")
                .define(groupMaxSessionTimeoutMsProp, ConfigDef.Type.INT, Defaults.GroupMaxSessionTimeoutMs, ConfigDef.Importance.MEDIUM, "")
                .define(currentEnv, ConfigDef.Type.STRING, null, ConfigDef.Importance.HIGH, "")
                .define(monitorCheckIntervalTime, ConfigDef.Type.INT, Defaults.monitorCheckIntervalTime, ConfigDef.Importance.HIGH, "")
                .define(multiplexingRead, ConfigDef.Type.BOOLEAN, Defaults.multiplexingRead, ConfigDef.Importance.HIGH, "")
                .define(flinkerJobMinJVMMemory, ConfigDef.Type.STRING, Defaults.flinkerJobMinJVMMemory,ConfigDef.Importance.HIGH, "")
                .define(flinkerJobMaxJVMMemory, ConfigDef.Type.STRING, Defaults.flinkerJobMaxJVMMemory,ConfigDef.Importance.HIGH, "");
    }

    private ManagerConfig(Properties props, boolean doLog) {
        super(configDef, props, doLog);
    }

    public static synchronized ManagerConfig fromProps(Properties props, boolean doLog) {
        if (instance == null) {
            instance = new ManagerConfig(props, doLog);
        }
        return instance;
    }

    public static ManagerConfig current() {
        if (instance == null) {
            throw new DatalinkException("manager config has not yet been initialized.");
        }
        return instance;
    }

    public String getHostName() {
        return getString(hostNameProp);
    }

    public int getPort() {
        return getInt(portProp);
    }

    public int getHttpPort() {
        return getInt(httpPortProp);
    }

    public String getZkRoot() {
        return getString(zkRootProp);
    }

    public String getZkServer() {
        return getString(zkServerProp);
    }

    public int getZkSessionTimeoutMs() {
        return getInt(zkSessionTimeoutMsProp);
    }

    public int getZkConnectionTimeoutMs() {
        return getInt(zkConnectionTimeoutMsProp);
    }

    public int getGroupMinSessionTimeoutMs() {
        return getInt(groupMinSessionTimeoutMsProp);
    }

    public int getGroupMaxSessionTimeoutMsProp() {
        return getInt(groupMaxSessionTimeoutMsProp);
    }

    public String getCurrentEnv() {
        return getString(currentEnv);
    }

    public int getMonitorCheckIntervalTime() {
        return getInt(monitorCheckIntervalTime);
    }

    public boolean getMultiplexingRead() {
        return getBoolean(multiplexingRead);
    }

    public String getFlinkerJobMinJVMMemory(){
        return getString(flinkerJobMinJVMMemory);
    }

    public String getFlinkerJobMaxJVMMemory(){
        return getString(flinkerJobMaxJVMMemory);
    }

    static class Defaults {
        public static final String HostName = "";
        public static final int Port = 8899;
        public static final int HttpPort = 8080;
        public static final String ZkRoot = "/datalink";
        public static final String ZkServer = "localhost:2181";
        public static final int ZkSessionTimeoutMs = 10000;
        public static final int ZkConnectionTimeout = 10000;
        public static final int GroupMinSessionTimeoutMs = 6000;
        public static final int GroupMaxSessionTimeoutMs = 300000;
        public static final int monitorCheckIntervalTime = 30;
        public static final boolean multiplexingRead = false;
        public static final String flinkerJobMinJVMMemory = "1G";
        public static final String flinkerJobMaxJVMMemory = "2G";
    }
}
