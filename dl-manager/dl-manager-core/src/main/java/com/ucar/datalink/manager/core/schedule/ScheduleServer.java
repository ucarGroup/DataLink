package com.ucar.datalink.manager.core.schedule;

import com.ucar.datalink.manager.core.server.ServerStatusMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lubiao on 2018/4/16.
 * <p>
 */
public class ScheduleServer {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleServer.class);

    private final ScheduleService scheduleService;
    private final ServerStatusMonitor serverStatusMonitor;

    public ScheduleServer(ServerStatusMonitor serverStatusMonitor) {
        this.scheduleService = new ScheduleService();
        this.serverStatusMonitor = serverStatusMonitor;
    }

    public void startup() {
        scheduleService.startup();
        if (serverStatusMonitor.activeIsMine()) {
            scheduleService.active();
        } else {
            scheduleService.standby();
        }
        logger.info(" ##ScheduleServer is started.");
    }

    public void shutdown() {
        scheduleService.shutdown();
        logger.info(" ##ScheduleServer is shutdown.");
    }

    public void onActiveChange() {
        if (serverStatusMonitor.activeIsMine()) {
            scheduleService.active();
        } else {
            scheduleService.standby();
        }
    }
}
