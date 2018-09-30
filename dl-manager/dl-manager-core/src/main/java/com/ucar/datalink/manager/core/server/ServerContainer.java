package com.ucar.datalink.manager.core.server;

import com.ucar.datalink.common.zookeeper.ZkConfig;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.manager.core.coordinator.GroupCoordinator;
import com.ucar.datalink.manager.core.monitor.MonitorManager;
import com.ucar.datalink.manager.core.schedule.ScheduleServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by lubiao on 2016/12/7.
 * <p>
 * Server容器，负责组装各个[Sub-Server]并对其进行生命周期管理.
 */
public class ServerContainer {
    private static final Logger logger = LoggerFactory.getLogger(ServerContainer.class);

    private ManagerConfig config;
    private DLinkZkUtils zkUtils;
    private NettyServer nettyServer;
    private JettyServer jettyServer;
    private GroupCoordinator groupCoordinator;
    private ServerStatusMonitor serverStatusMonitor;
    private MonitorManager monitorManager;
    private ScheduleServer scheduleServer;

    //单例
    private static ServerContainer container = new ServerContainer();

    public static ServerContainer getInstance() {
        return container;
    }

    private ServerContainer() {
    }

    public void init(Properties props) {
        this.config = ManagerConfig.fromProps(props, true);
        this.zkUtils = DLinkZkUtils.init(new ZkConfig(config.getZkServer(), config.getZkSessionTimeoutMs(), config.getZkConnectionTimeoutMs()), config.getZkRoot());
        this.serverStatusMonitor = new ServerStatusMonitor(config, zkUtils);
        this.serverStatusMonitor.setListener(new ServerStatusListener() {
            @Override
            public void onActiveChange() {
                //只要发生Manager-Active-Change，不管本机是否是新的Manager，都把Group信息清除
                groupCoordinator.clearGroupMetaInfo();
                scheduleServer.onActiveChange();
            }
        });
        this.groupCoordinator = new GroupCoordinator(config, zkUtils, serverStatusMonitor);
        this.nettyServer = new NettyServer(this.config, this.groupCoordinator);
        this.jettyServer = new JettyServer(config);
        this.monitorManager = new MonitorManager();
        this.scheduleServer = new ScheduleServer(serverStatusMonitor);

        logger.info("ServerContainer is initialized.");
    }

    public void startup() throws Exception {
        this.serverStatusMonitor.startup();
        this.groupCoordinator.startup();
        this.nettyServer.startup();
        this.jettyServer.startup();
        this.monitorManager.startup();
        this.scheduleServer.startup();

        logger.info("ServerContainer is started.");
    }

    public void shutdown() throws Exception {
        this.monitorManager.shutdown();
        this.jettyServer.shutdown();
        this.groupCoordinator.shutdown();
        this.serverStatusMonitor.shutdown();
        this.nettyServer.shutdown();
        this.scheduleServer.shutdown();

        logger.info("ServerContainer is shutdown.");
    }

    public GroupCoordinator getGroupCoordinator() {
        return groupCoordinator;
    }

    public ServerStatusMonitor getServerStatusMonitor() {
        return serverStatusMonitor;
    }
}
