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
    private static ManagerConfig instance;

    //---------host config------------
    private static final String HostNameProp = "host.name";
    private static final String PortProp = "port";
    private static final String HttpPortProp = "http.port";
    //---------zookeeper config-------
    private static final String ZkRootProp = "zookeeper.root";
    private static final String ZkServerProp = "zookeeper.servers";
    private static final String ZkSessionTimeoutMsProp = "zookeeper.session.timeout.ms";
    private static final String ZkConnectionTimeoutMsProp = "zookeeper.connection.timeout.ms";
    //---------group coordinator config-------------
    private static final String GroupMinSessionTimeoutMsProp = "group.min.session.timeout.ms";
    private static final String GroupMaxSessionTimeoutMsProp = "group.max.session.timeout.ms";
    //---------other configs-------------
    private static final String CurrentEnv = "currentEnv";

    private static final String MonitorManager = "monitorManager";
    private static final String ScheduleServer = "scheduleServer";
    /**
     * 检测频率
     */
    private static final String monitorCheckIntervalTime = "monitor.check.intervalTime";
    /**
     * 是否复用task
     */
    private static final String isReuseTask = "isReuseTask";

    /**
     * 单库切换通知dbms接口地址
     */
    private static final String notifyDbmsDbSwitchResultUrl = "doubleCenter.dbSwitchLab.notifyDbmsUrl";
    /**
     * 单库切换通知dbms，双方约定的秘钥
     */
    private static final String notifyDbmsDbSwitchSecurekey = "doubleCenter.dbSwitchLab.securekey";

    /**
     * 是否开启读端数据多路复用
     */
    private static final String multiplexingRead = "multiplexingRead";

    private static final String passwordAesKey = "passwordAesKey";

    static {
        configDef = new ConfigDef()
                .define(PortProp, ConfigDef.Type.INT, Defaults.Port, ConfigDef.Importance.HIGH, "")
                .define(HttpPortProp, ConfigDef.Type.INT, Defaults.HttpPort, ConfigDef.Importance.HIGH, "")
                .define(HostNameProp, ConfigDef.Type.STRING, Defaults.HostName, ConfigDef.Importance.HIGH, "")
                .define(ZkRootProp, ConfigDef.Type.STRING, Defaults.ZkRoot, ConfigDef.Importance.HIGH, "")
                .define(ZkServerProp, ConfigDef.Type.STRING, Defaults.ZkServer, ConfigDef.Importance.HIGH, "")
                .define(ZkSessionTimeoutMsProp, ConfigDef.Type.INT, Defaults.ZkSessionTimeoutMs, ConfigDef.Importance.HIGH, "")
                .define(ZkConnectionTimeoutMsProp, ConfigDef.Type.INT, Defaults.ZkConnectionTimeout, ConfigDef.Importance.HIGH, "")
                .define(GroupMinSessionTimeoutMsProp, ConfigDef.Type.INT, Defaults.GroupMinSessionTimeoutMs, ConfigDef.Importance.MEDIUM, "")
                .define(GroupMaxSessionTimeoutMsProp, ConfigDef.Type.INT, Defaults.GroupMaxSessionTimeoutMs, ConfigDef.Importance.MEDIUM, "")
                .define(CurrentEnv, ConfigDef.Type.STRING, null, ConfigDef.Importance.HIGH, "")
                .define(MonitorManager, ConfigDef.Type.BOOLEAN, Defaults.MonitorManager, ConfigDef.Importance.HIGH, "")
                .define(ScheduleServer, ConfigDef.Type.BOOLEAN, Defaults.ScheduleServer, ConfigDef.Importance.HIGH, "")
                .define(monitorCheckIntervalTime,ConfigDef.Type.INT,Defaults.monitorCheckIntervalTime,ConfigDef.Importance.HIGH,"")
                .define(notifyDbmsDbSwitchResultUrl,ConfigDef.Type.STRING,Defaults.notifyDbmsDbSwitchResultUrl,ConfigDef.Importance.HIGH,"")
                .define(notifyDbmsDbSwitchSecurekey,ConfigDef.Type.STRING,Defaults.notifyDbmsDbSwitchSecurekey,ConfigDef.Importance.HIGH,"")
                .define(isReuseTask, ConfigDef.Type.BOOLEAN, Defaults.isReuseTask, ConfigDef.Importance.HIGH, "")
                .define(passwordAesKey,ConfigDef.Type.STRING,Defaults.passwordAesKey,ConfigDef.Importance.HIGH,"")
                .define(multiplexingRead, ConfigDef.Type.BOOLEAN, Defaults.multiplexingRead, ConfigDef.Importance.HIGH, "");
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
        return getString(HostNameProp);
    }

    public int getPort() {
        return getInt(PortProp);
    }

    public int getHttpPort() {
        return getInt(HttpPortProp);
    }

    public String getZkRoot() {
        return getString(ZkRootProp);
    }

    public String getZkServer() {
        return getString(ZkServerProp);
    }

    public int getZkSessionTimeoutMs() {
        return getInt(ZkSessionTimeoutMsProp);
    }

    public int getZkConnectionTimeoutMs() {
        return getInt(ZkConnectionTimeoutMsProp);
    }

    public int getGroupMinSessionTimeoutMs() {
        return getInt(GroupMinSessionTimeoutMsProp);
    }

    public int getGroupMaxSessionTimeoutMsProp() {
        return getInt(GroupMaxSessionTimeoutMsProp);
    }

    public String getCurrentEnv() {
        return getString(CurrentEnv);
    }

    public boolean getMonitorManager() {
        return getBoolean(MonitorManager);
    }

    public boolean getScheduleServer() {
        return getBoolean(ScheduleServer);
    }

    public int getMonitorCheckIntervalTime() {
        return getInt(monitorCheckIntervalTime);
    }

    public String getNotifyDbmsDbSwitchResultUrl() {
        return getString(notifyDbmsDbSwitchResultUrl);
    }

    public String getNotifyDbmsDbSwitchSecurekey() {
        return getString(notifyDbmsDbSwitchSecurekey);
    }

    public boolean getIsReuseTask() {
        return getBoolean(isReuseTask);
    }


    public boolean getMultiplexingRead() {
        return getBoolean(multiplexingRead);
    }

    public  String getPasswordAesKey() {
        return getString(passwordAesKey);
    }

    static class Defaults {
        public static final String HostName = "";
        public static final int Port = 8899;
        public static final int HttpPort = 80;
        public static final String ZkRoot = "/datalink";
        public static final String ZkServer = "localhost:2181";
        public static final int ZkSessionTimeoutMs = 10000;
        public static final int ZkConnectionTimeout = 10000;
        public static final int GroupMinSessionTimeoutMs = 6000;
        public static final int GroupMaxSessionTimeoutMs = 300000;
        public static final boolean MonitorManager = false;
        public static final boolean ScheduleServer = false;
        public static final int monitorCheckIntervalTime = 30;
        public static final String notifyDbmsDbSwitchResultUrl = "";
        public static final String notifyDbmsDbSwitchSecurekey = "";
        public static final boolean isReuseTask = false;
        public static final boolean multiplexingRead = false;
        public static final String passwordAesKey = "datalink_123456a";
    }
}
