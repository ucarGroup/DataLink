package com.ucar.datalink.biz.cron;

import com.ucar.datalink.biz.cron.http.HttpCronTask;
import com.ucar.datalink.biz.cron.QuartzJob;
import com.ucar.datalink.biz.cron.QuartzManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by yang.wang09 on 2018-07-25 10:59.
 */
public class Test1 {

    public static void main(String[] args) throws IOException, SchedulerException {
        Test1 t = new Test1();
        t.gg();
    }


    public void gg() throws IOException, SchedulerException {
        Properties props = new Properties();
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("quartz.properties"));
        StdSchedulerFactory factory = new StdSchedulerFactory(props);
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();

        QuartzManager manager = QuartzManager.getInstance();
        manager.setScheduler(scheduler);

        HttpCronTask task = new HttpCronTask();
        QuartzJob job = new QuartzJob();
        job.setJobId(12345);
        job.setJobStatus(1);
        job.setJobName("test-1");
        job.setJobGroup("xx");
        job.setCronExpression("0 * * * * ?");
        manager.initJob(job,task.getClass());
        //manager.runJob(job);


    }
}
