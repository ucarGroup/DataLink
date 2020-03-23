package com.ucar.datalink.manager.core.flinker.cron.http;

/**
 * Created by yang.wang09 on 2018-07-25 15:05.
 */
public final class JobTaskScheduleManager {


    private JobTaskScheduleManager() {

    }

    private static final JobTaskScheduleManager INSTANCE = new JobTaskScheduleManager();

    public static final JobTaskScheduleManager getInstance() {
        return INSTANCE;
    }


    public void createSchedule() {

    }

    public void pauseSchedule() {

    }

    public void resumeSchedule() {

    }

    public void removeSchedule() {

    }

}
