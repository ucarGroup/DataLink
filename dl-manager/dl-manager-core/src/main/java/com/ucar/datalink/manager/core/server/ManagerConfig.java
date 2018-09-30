package com.ucar.datalink.manager.core.server;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.DbConfigEncryption;
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
                .define(CurrentEnv, ConfigDef.Type.STRING, null, ConfigDef.Importance.HIGH, "");
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

    }
}
