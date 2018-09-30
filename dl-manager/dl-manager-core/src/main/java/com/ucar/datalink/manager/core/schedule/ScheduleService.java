package com.ucar.datalink.manager.core.schedule;

import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Properties;

/**
 * Created by csf on 17/3/3.
 * [通用定时任务]管理器,基于Quartz.
 */
class ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private SchedulerFactory schedulerFactory;
    private Scheduler scheduler;
    private Collection<Trigger> triggers;

    public ScheduleService() {
        try {
            Properties props = new Properties();
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("quartz.properties"));
            this.schedulerFactory = new StdSchedulerFactory(props);
            this.scheduler = schedulerFactory.getScheduler();
        } catch (Throwable e) {
            throw new DatalinkException("ScheduleService initial failed!", e);
        }
    }

    public void startup() {
        try {
            triggers = DataLinkFactory.getBeansOfType(Trigger.class).values();
            for (Trigger trigger : triggers) {
                String[] keys = trigger.getJobDataMap().getKeys();
                JobDetailImpl jd = (JobDetailImpl)trigger.getJobDataMap().get(keys[0]);
                scheduler.addJob(jd,true);
                scheduler.scheduleJob(trigger);
            }
        } catch (Exception e) {
            throw new DatalinkException("schedule service startup failed.", e);
        }
    }

    public void shutdown() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (Exception e) {
            logger.error("schedule service shutdown failed.", e);
        }
    }

    public void active() {
        try {
            scheduler.start();
            logger.info("schedule service change to active mode success.");
        } catch (Exception e) {
            logger.error("schedule service change to active mode failed.", e);
        }
    }

    public void standby() {
        try {
            scheduler.standby();
            logger.info("schedule service change to standby mode success.");
        } catch (Exception e) {
            logger.error("schedule service change to standby mode failed.", e);
        }
    }
}
