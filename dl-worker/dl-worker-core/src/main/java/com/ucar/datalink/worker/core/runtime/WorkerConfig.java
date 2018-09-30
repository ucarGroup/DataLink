package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.errors.DatalinkException;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Range.atLeast;

/**
 * Common base class providing configuration for Datalink workers.
 * <p>
 * 参考自Kafka connect的org.apache.kafka.connect.runtime.WorkerConfig
 */
public class WorkerConfig extends AbstractConfig {
    private static final ConfigDef CONFIG;

    /**
     * <code>bootstrap.servers</code>
     */
    public static final String BOOTSTRAP_SERVERS_CONFIG = "bootstrap.servers";
    public static final String BOOTSTRAP_SERVERS_DOC
            = "A list of host/port pairs to use for establishing the initial connection to the Datalink Manager "
            + "The worker will make use of all servers irrespective of which servers are "
            + "specified here for bootstrapping;this list only impacts the initial hosts used "
            + "to discover the full set of servers. This list should be in the form "
            + "<code>host1:port1,host2:port2,...</code>. Since these servers are just used for the "
            + "initial connection to discover the full cluster membership (which may change "
            + "dynamically), this list need not contain the full set of servers (you may want more "
            + "than one, though, in case a server is down).";
    public static final String BOOTSTRAP_SERVERS_DEFAULT = "localhost:8899";

    /**
     * <code>task.shutdown.graceful.timeout.ms</code>
     */
    public static final String TASK_SHUTDOWN_GRACEFUL_TIMEOUT_MS_CONFIG
            = "task.shutdown.graceful.timeout.ms";
    private static final String TASK_SHUTDOWN_GRACEFUL_TIMEOUT_MS_DOC =
            "Amount of time to wait for tasks to shutdown gracefully. This is the total amount of time,"
                    + " not per task. All task have shutdown triggered, then they are waited on sequentially.";
    private static final String TASK_SHUTDOWN_GRACEFUL_TIMEOUT_MS_DEFAULT = "5000";

    /**
     * <code>task.position.commit.interval.ms</code>
     */
    public static final String POSITION_COMMIT_INTERVAL_MS_CONFIG = "task.position.commit.interval.ms";
    private static final String POSITION_COMMIT_INTERVAL_MS_DOC
            = "Interval at which to try committing positions for tasks.";
    public static final long POSITION_COMMIT_INTERVAL_MS_DEFAULT = 1000L;

    /**
     * <code>rest.host.name</code>
     */
    public static final String REST_HOST_NAME_CONFIG = "rest.host.name";
    private static final String REST_HOST_NAME_DOC
            = "Hostname for the REST API. If this is set, it will only bind to this interface.";

    /**
     * <code>rest.port</code>
     */
    public static final String REST_PORT_CONFIG = "rest.port";
    private static final String REST_PORT_DOC
            = "Port for the REST API to listen on.";
    public static final int REST_PORT_DEFAULT = 8083;

    /**
     * <code>rest.advertised.host.name</code>
     */
    public static final String REST_ADVERTISED_HOST_NAME_CONFIG = "rest.advertised.host.name";
    private static final String REST_ADVERTISED_HOST_NAME_DOC
            = "If this is set, this is the hostname that will be given out to other workers to connect to.";

    /**
     * <code>rest.advertised.port</code>
     */
    public static final String REST_ADVERTISED_PORT_CONFIG = "rest.advertised.port";
    private static final String REST_ADVERTISED_PORT_DOC
            = "If this is set, this is the port that will be given out to other workers to connect to.";

    /**
     * <code>access.control.allow.origin</code>
     */
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_CONFIG = "access.control.allow.origin";
    protected static final String ACCESS_CONTROL_ALLOW_ORIGIN_DOC =
            "Value to set the Access-Control-Allow-Origin header to for REST API requests." +
                    "To enable cross origin access, set this to the domain of the application that should be permitted" +
                    " to access the API, or '*' to allow access parseFrom any domain. The default value only allows access" +
                    " parseFrom the domain of the REST API.";
    protected static final String ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT = "";

    /**
     * <code>access.control.allow.methods</code>
     */
    public static final String ACCESS_CONTROL_ALLOW_METHODS_CONFIG = "access.control.allow.methods";
    protected static final String ACCESS_CONTROL_ALLOW_METHODS_DOC =
            "Sets the methods supported for cross origin requests by setting the Access-Control-Allow-Methods header. "
                    + "The default value of the Access-Control-Allow-Methods header allows cross origin requests for GET, POST and HEAD.";
    protected static final String ACCESS_CONTROL_ALLOW_METHODS_DEFAULT = "";

    /**
     * <code>group.id</code>
     */
    public static final String GROUP_ID_CONFIG = "group.id";
    private static final String GROUP_ID_DOC = "A unique string that identifies the Datalink cluster group this worker belongs to.";

    /**
     * <code>session.timeout.ms</code>
     */
    public static final String SESSION_TIMEOUT_MS_CONFIG = "session.timeout.ms";
    private static final String SESSION_TIMEOUT_MS_DOC = "The timeout used to detect worker failures. " +
            "The worker sends periodic heartbeats to indicate its liveness to the Datalink Manager. If no heartbeats are " +
            "received by the Manager before the expiration of this session timeout, then the Manager will remove the " +
            "worker parseFrom the group and initiate a rebalance. Note that the value must be in the allowable range as " +
            "configured in the Manager configuration by <code>group.min.session.timeout.ms</code> " +
            "and <code>group.max.session.timeout.ms</code>.";

    /**
     * <code>heartbeat.interval.ms</code>
     */
    public static final String HEARTBEAT_INTERVAL_MS_CONFIG = "heartbeat.interval.ms";
    private static final String HEARTBEAT_INTERVAL_MS_DOC = "The expected time between heartbeats to the group " +
            "coordinator when using Manager's group management facilities. Heartbeats are used to ensure that the " +
            "worker's session stays active and to facilitate rebalancing when new members join or leave the group. " +
            "The value must be set lower than <code>session.timeout.ms</code>, but typically should be set no higher " +
            "than 1/3 of that value. It can be adjusted even lower to control the expected time for normal rebalances.";

    /**
     * <code>rebalance.timeout.ms</code>
     */
    public static final String REBALANCE_TIMEOUT_MS_CONFIG = "rebalance.timeout.ms";
    private static final String REBALANCE_TIMEOUT_MS_DOC = "The maximum allowed time for each worker to join the group " +
            "once a rebalance has begun. This is basically a limit on the amount of time needed for all tasks to " +
            "flush any pending data and commit positions. If the timeout is exceeded, then the worker will be removed " +
            "parseFrom the group, which will cause version commit failures.";

    /**
     * <code>worker.sync.timeout.ms</code>
     */
    public static final String WORKER_SYNC_TIMEOUT_MS_CONFIG = "worker.sync.timeout.ms";
    private static final String WORKER_SYNC_TIMEOUT_MS_DOC = "When the worker is out of sync with other workers and needs" +
            " to resynchronize configurations, wait up to this amount of time before giving up, leaving the group, and" +
            " waiting a backoff period before rejoining.";

    /**
     * <code>group.unsync.timeout.ms</code>
     */
    public static final String WORKER_UNSYNC_BACKOFF_MS_CONFIG = "worker.unsync.backoff.ms";
    private static final String WORKER_UNSYNC_BACKOFF_MS_DOC = "When the worker is out of sync with other workers and " +
            " fails to catch up within worker.sync.timeout.ms, leave the worker cluster for this long before rejoining.";
    public static final int WORKER_UNSYNC_BACKOFF_MS_DEFAULT = 5 * 60 * 1000;

    /**
     * <code>zookeeper.root</code>
     */
    public static final String ZK_ROOT_CONFIG = "zookeeper.root";
    public static final String ZK_ROOT_DOC = "The root node for datalink manager&worker on zookeeper.";
    public static final String ZK_ROOT_DEFAULT = "/datalink";

    /**
     * <code>zookeeper.servers</code>
     */
    public static final String ZK_SERVER_CONFIG = "zookeeper.servers";
    public static final String ZK_SERVER_DOC = "The zookeeper servers used by datalink cluster,This list should be in the form "
            + "<code>host1:port1,host2:port2,...</code>";
    public static final String ZK_SERVER_DEFAULT = "localhost:2181";

    /**
     * <code>zookeeper.session.timeout.ms</code>
     */
    public static final String ZK_SESSION_TIMEOUT_MS_CONFIG = "zookeeper.session.timeout.ms";
    public static final String ZK_SESSION_TIMEOUT_MS_DOC = "Session timeout config for zookeeper,the value should be "
            + "larger than the value of <code>session.timeout.ms</code>.";
    public static final int ZK_SESSION_TIMEOUT_MS_DEFAULT = 30000;

    /**
     * <code>zookeeper.connection.timeout.ms</code>
     */
    public static final String ZK_CONNECTION_TIMEOUT_MS_CONFIG = "zookeeper.connection.timeout.ms";
    public static final String ZK_CONNECTION_TIMEOUT_MS_DOC = "Connection timeout config for zookeeper.";
    public static final int ZK_CONNECTION_TIMEOUT_MS_DEFAULT = 10000;

    /**
     * <code>classloader.type</code>
     */
    public static final String CLASSLOADER_TYPE_CONFIG = "classloader.type";
    public static final String CLASSLOADER_TYPE_DOC = "The classloader type for taskreaders&taskwriters,there are three"
            + " options: <Dev> using DevPluginClassLoader for development environment,<Rel> using RelPluginClassLoader "
            + "for release environment, <Inherit> using AppClassLoader for some special scenes.";
    public static final String CLASSLOADER_TYPE_DEFAULT = "Rel";//三个选项，Dev-开发版，Rel-release版,Inherit-直接使用框架的Loader

    /**
     * <code>ddl.sync</code>
     */
    public static final String DDL_SYNC_CONFIG = "ddl.sync";
    public static final boolean DDL_SYNC_DEFAULT = false;//是否同步ddl语句

    /**
     * <code>sync.auto.addcolumn</code>
     */
    public static final String SYNC_AUTO_ADD_COLUMN_CONFIG = "sync.auto.addcolumn";
    public static final boolean SYNC_AUTO_ADD_COLUMN_DEFAULT = false;//是否自动加列

    /**
     * <code>worker.bootMode</code>
     */
    public static final String WORKER_BOOT_MODE_CONFIG = "worker.bootMode";
    public static final String WORKER_BOOT_MODE_DOC = "The boot mode for datalink worker,two options:standalone and distributed";
    public static final String WORKER_BOOT_MODE_DEFAULT = "distributed";

    /**
     * <code>worker.probe.blackList</code>
     */
    public static final String WORKER_PROBE_BLACKLIST_CONFIG = "worker.probe.blackList";
    public static final String WORKER_PROBE_BLACKLIST_DOC = "The probe in the blacklist will not start,This list should be"
            + " in the form <code>probe1,probe2,...</code>";

    static {
        CONFIG = new ConfigDef()
                .define(BOOTSTRAP_SERVERS_CONFIG,
                        Type.LIST, BOOTSTRAP_SERVERS_DEFAULT,
                        Importance.HIGH, BOOTSTRAP_SERVERS_DOC)
                .define(TASK_SHUTDOWN_GRACEFUL_TIMEOUT_MS_CONFIG,
                        Type.LONG,
                        TASK_SHUTDOWN_GRACEFUL_TIMEOUT_MS_DEFAULT,
                        Importance.LOW,
                        TASK_SHUTDOWN_GRACEFUL_TIMEOUT_MS_DOC)
                .define(POSITION_COMMIT_INTERVAL_MS_CONFIG,
                        Type.LONG, POSITION_COMMIT_INTERVAL_MS_DEFAULT,
                        Importance.LOW, POSITION_COMMIT_INTERVAL_MS_DOC)
                .define(REST_HOST_NAME_CONFIG,
                        Type.STRING,
                        null,
                        Importance.LOW,
                        REST_HOST_NAME_DOC)
                .define(REST_PORT_CONFIG,
                        Type.INT, REST_PORT_DEFAULT,
                        Importance.LOW, REST_PORT_DOC)
                .define(REST_ADVERTISED_HOST_NAME_CONFIG,
                        Type.STRING,
                        null,
                        Importance.LOW,
                        REST_ADVERTISED_HOST_NAME_DOC)
                .define(REST_ADVERTISED_PORT_CONFIG,
                        Type.INT,
                        null,
                        Importance.LOW,
                        REST_ADVERTISED_PORT_DOC)
                .define(ACCESS_CONTROL_ALLOW_ORIGIN_CONFIG,
                        Type.STRING,
                        ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT,
                        Importance.LOW,
                        ACCESS_CONTROL_ALLOW_ORIGIN_DOC)
                .define(ACCESS_CONTROL_ALLOW_METHODS_CONFIG,
                        Type.STRING,
                        ACCESS_CONTROL_ALLOW_METHODS_DEFAULT,
                        Importance.LOW,
                        ACCESS_CONTROL_ALLOW_METHODS_DOC)
                .define(GROUP_ID_CONFIG,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        GROUP_ID_DOC)
                .define(SESSION_TIMEOUT_MS_CONFIG,
                        ConfigDef.Type.INT,
                        10000,
                        ConfigDef.Importance.HIGH,
                        SESSION_TIMEOUT_MS_DOC)
                .define(REBALANCE_TIMEOUT_MS_CONFIG,
                        ConfigDef.Type.INT,
                        60000,
                        ConfigDef.Importance.HIGH,
                        REBALANCE_TIMEOUT_MS_DOC)
                .define(HEARTBEAT_INTERVAL_MS_CONFIG,
                        ConfigDef.Type.INT,
                        3000,
                        ConfigDef.Importance.HIGH,
                        HEARTBEAT_INTERVAL_MS_DOC)
                .define(CommonClientConfigs.METADATA_MAX_AGE_CONFIG,
                        ConfigDef.Type.LONG,
                        5 * 60 * 1000,
                        atLeast(0),
                        ConfigDef.Importance.LOW,
                        CommonClientConfigs.METADATA_MAX_AGE_DOC)
                .define(CommonClientConfigs.CLIENT_ID_CONFIG,
                        ConfigDef.Type.STRING,
                        "",
                        ConfigDef.Importance.LOW,
                        CommonClientConfigs.CLIENT_ID_DOC)
                .define(CommonClientConfigs.SEND_BUFFER_CONFIG,
                        ConfigDef.Type.INT,
                        128 * 1024,
                        atLeast(0),
                        ConfigDef.Importance.MEDIUM,
                        CommonClientConfigs.SEND_BUFFER_DOC)
                .define(CommonClientConfigs.RECEIVE_BUFFER_CONFIG,
                        ConfigDef.Type.INT,
                        32 * 1024,
                        atLeast(0),
                        ConfigDef.Importance.MEDIUM,
                        CommonClientConfigs.RECEIVE_BUFFER_DOC)
                .define(CommonClientConfigs.RECONNECT_BACKOFF_MS_CONFIG,
                        ConfigDef.Type.LONG,
                        50L,
                        atLeast(0L),
                        ConfigDef.Importance.LOW,
                        CommonClientConfigs.RECONNECT_BACKOFF_MS_DOC)
                .define(CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG,
                        ConfigDef.Type.LONG,
                        100L,
                        atLeast(0L),
                        ConfigDef.Importance.LOW,
                        CommonClientConfigs.RETRY_BACKOFF_MS_DOC)
                .define(CommonClientConfigs.METRICS_SAMPLE_WINDOW_MS_CONFIG,
                        ConfigDef.Type.LONG,
                        30000,
                        atLeast(0),
                        ConfigDef.Importance.LOW,
                        CommonClientConfigs.METRICS_SAMPLE_WINDOW_MS_DOC)
                .define(CommonClientConfigs.METRICS_NUM_SAMPLES_CONFIG,
                        ConfigDef.Type.INT,
                        2,
                        atLeast(1),
                        ConfigDef.Importance.LOW,
                        CommonClientConfigs.METRICS_NUM_SAMPLES_DOC)
                .define(CommonClientConfigs.METRIC_REPORTER_CLASSES_CONFIG,
                        ConfigDef.Type.LIST,
                        "",
                        ConfigDef.Importance.LOW,
                        CommonClientConfigs.METRIC_REPORTER_CLASSES_DOC)
                .define(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG,
                        ConfigDef.Type.INT,
                        40 * 1000,
                        atLeast(0),
                        ConfigDef.Importance.MEDIUM,
                        CommonClientConfigs.REQUEST_TIMEOUT_MS_DOC)
                        /* default is set to be a bit lower than the server default (10 min), to avoid both client and server closing connection at same time */
                .define(CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG,
                        ConfigDef.Type.LONG,
                        9 * 60 * 1000,
                        ConfigDef.Importance.MEDIUM,
                        CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_DOC)
                        // security support
                .define(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                        ConfigDef.Type.STRING,
                        CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL,
                        ConfigDef.Importance.MEDIUM,
                        CommonClientConfigs.SECURITY_PROTOCOL_DOC)
                .withClientSslSupport()
                .withClientSaslSupport()
                .define(WORKER_SYNC_TIMEOUT_MS_CONFIG,
                        ConfigDef.Type.INT,
                        3000,
                        ConfigDef.Importance.MEDIUM,
                        WORKER_SYNC_TIMEOUT_MS_DOC)
                .define(WORKER_UNSYNC_BACKOFF_MS_CONFIG,
                        ConfigDef.Type.INT,
                        WORKER_UNSYNC_BACKOFF_MS_DEFAULT,
                        ConfigDef.Importance.MEDIUM,
                        WORKER_UNSYNC_BACKOFF_MS_DOC)
                .define(ZK_ROOT_CONFIG,
                        ConfigDef.Type.STRING,
                        ZK_ROOT_DEFAULT,
                        ConfigDef.Importance.HIGH,
                        ZK_ROOT_DOC)
                .define(ZK_SERVER_CONFIG,
                        ConfigDef.Type.STRING,
                        ZK_SERVER_DEFAULT,
                        ConfigDef.Importance.HIGH,
                        ZK_SERVER_DOC)
                .define(ZK_SESSION_TIMEOUT_MS_CONFIG,
                        ConfigDef.Type.INT,
                        ZK_SESSION_TIMEOUT_MS_DEFAULT,
                        ConfigDef.Importance.HIGH,
                        ZK_SESSION_TIMEOUT_MS_DOC)
                .define(ZK_CONNECTION_TIMEOUT_MS_CONFIG,
                        ConfigDef.Type.INT,
                        ZK_CONNECTION_TIMEOUT_MS_DEFAULT,
                        ConfigDef.Importance.HIGH,
                        ZK_CONNECTION_TIMEOUT_MS_DOC)
                .define(CLASSLOADER_TYPE_CONFIG,
                        Type.STRING,
                        CLASSLOADER_TYPE_DEFAULT,
                        Importance.HIGH,
                        CLASSLOADER_TYPE_DOC)
                .define(DDL_SYNC_CONFIG,
                        Type.BOOLEAN,
                        DDL_SYNC_DEFAULT,
                        Importance.HIGH,
                        "")
                .define(SYNC_AUTO_ADD_COLUMN_CONFIG,
                        Type.BOOLEAN,
                        SYNC_AUTO_ADD_COLUMN_DEFAULT,
                        Importance.HIGH,
                        "")
                .define(WORKER_BOOT_MODE_CONFIG,
                        Type.STRING,
                        WORKER_BOOT_MODE_DEFAULT,
                        Importance.HIGH,
                        WORKER_BOOT_MODE_DOC)
                .define(WORKER_PROBE_BLACKLIST_CONFIG,
                        Type.STRING,
                        "",
                        Importance.LOW,
                        WORKER_PROBE_BLACKLIST_DOC);
    }

    private static WorkerConfig instance;

    private WorkerConfig(Map<String, String> props, boolean doLog) {
        super(CONFIG, props, doLog);
    }

    public static synchronized WorkerConfig fromProps(Map<String, String> props, boolean doLog) {
        if (instance == null) {
            instance = new WorkerConfig(props, doLog);
        }
        return instance;
    }

    public static WorkerConfig current() {
        if (instance == null) {
            throw new DatalinkException("worker config has not yet been initialized.");
        }
        return instance;
    }
}
