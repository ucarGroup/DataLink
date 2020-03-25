package com.ucar.datalink.manager.core.flinker.cron;

/**
 * Created by yang.wang09 on 2018-07-25 10:38.
 */

import com.ucar.datalink.manager.core.flinker.cron.entity.EntityCronUtil;
import com.ucar.datalink.manager.core.flinker.cron.entity.EntityQuartzJob;
import com.ucar.datalink.biz.service.JobScheduleService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobScheduleInfo;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.wang09 on 2018-07-25 10:31.
 */
public class QuartzManager {

    private static Logger logger = LoggerFactory.getLogger(QuartzManager.class);

    private Scheduler scheduler;

    private static final QuartzManager INSTANCE = new QuartzManager();

    public static final String HTTP_CRON_CHECK_PREFIX = "check@";

    public static final String HTTP_CRON_MAX_TIME_PREFIX = "maxtime@";

    private static final Map<String,String> NAME_EXECUTION_ID_MAPS = new HashMap<>();

    public static final String CHECK_PER_MINUTES_CRON_EXPRESS = "* 0/1 * * * ?";

    JobScheduleService jobScheduleService = null;

    JobService jobService = null;


    private QuartzManager() {

    }

    public static final QuartzManager getInstance() {
        return INSTANCE;
    }



    /**
     * @param scheduler the scheduler to set
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    public void startAllSchduleTask() {
        List<JobScheduleInfo> scheduleInfoList = jobScheduleService.selectBatchJobSchedule(null,null,null,null,null,null,1,1);
        for(JobScheduleInfo scheduleInfo : scheduleInfoList) {
            long jobConfigId = scheduleInfo.getJobId();
            JobConfigInfo configInfo = jobService.getJobConfigById(jobConfigId);
            EntityQuartzJob job = EntityCronUtil.assembleCronTaskWithMD5(configInfo, scheduleInfo);
            EntityCronUtil.schdule(job);
            logger.info("[QuartzManager] start cron job->"+job.toString());
        }
    }

    public void bootstrap() {
        try {
            jobScheduleService = DataLinkFactory.getObject(JobScheduleService.class);
            jobService = DataLinkFactory.getObject(JobService.class);
            startAllSchduleTask();
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    /**
     * 启动服务
     * @throws SchedulerException
     */
    public void startSchedule() throws SchedulerException {
        logger.info("启动schedule");
        if( scheduler.isShutdown() ) {
            bootstrap();
        }
    }

    /**
     * 停止服务
     * @throws SchedulerException
     */
    public void stopSchedule() throws SchedulerException {
        logger.info("停止schedule");
        if( !scheduler.isShutdown() ) {
            scheduler.shutdown();
        }
     }

    /**
     * 启动备份服务
     * @throws SchedulerException
     */
    public void standby() throws SchedulerException {
        logger.info("standby...");
        scheduler.standby();
    }


    /**
     * 服务是否关闭
     * @return
     * @throws SchedulerException
     */
    public boolean isShutdown() {
        try {
            return scheduler.isShutdown();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }


    /**
     * 每分钟检查一次状态，如果状态是成功则删除这个任务检查
     * 如果是失败，则调用带有重试功能的定时任务
     * @param job
     * @param clazz
     */
    public void initCheckJob(QuartzJob job, Class clazz) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(clazz)
                    .withIdentity(job.getJobName(), job.getJobGroup()).build();
            jobDetail.getJobDataMap().put(job.getJobName(), job);
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(CHECK_PER_MINUTES_CRON_EXPRESS);
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(HTTP_CRON_CHECK_PREFIX+job.getJobName(), job.getJobGroup())
                    .withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    public void initMaxTimeJob(QuartzJob job, Class clazz) {
        try {
            long maxTime = job.getMaxRuntime();
            JobDetail jobDetail = JobBuilder.newJob(clazz).build();
            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForTotalCount(1,(int)maxTime);
            SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(HTTP_CRON_MAX_TIME_PREFIX+job.getJobName(), job.getJobGroup())
                    .startAt(new Date())
                    .withSchedule(scheduleBuilder)
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
    }


    /**
     * 初始化任务调度
     */
    @SuppressWarnings("unchecked")
    public void initJob(QuartzJob job, Class cls){
        logger.info("初始化任务调度");
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (null == trigger) {
                addQuartzJob(job, trigger, cls);
            }
        } catch (Exception e) {
            logger.error("初始化任务调度异常！" + e.getMessage(), e);
        }
    }

    /**
     * 向任务调度中添加定时任务
     */
    @SuppressWarnings("unchecked")
    private void addQuartzJob(QuartzJob job, CronTrigger trigger, Class cls){
        logger.info("向任务调度中添加定时任务");
        try {
            JobDetail jobDetail = JobBuilder.newJob(cls)
                    .withIdentity(job.getJobName(), job.getJobGroup()).build();
            jobDetail.getJobDataMap().put(job.getJobName(), job);
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
            trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup())
                    .withSchedule(scheduleBuilder).build();
            //scheduler.addJob(jobDetail,true);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            logger.error("向任务调度中添加定时任务异常！" + e.getMessage(), e);
        }
    }

    /**
     * 立即运行定时任务
     */
    public void runJob(QuartzJob job){
        logger.info("立即运行任务调度中的定时任务");
        try {
            if (null == job) {
                logger.info("定时任务信息为空，无法立即运行");
                return;
            }
            JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
            if(null == jobKey){
                logger.info("任务调度中不存在[" + job.getJobName() + "]定时任务，不予立即运行！");
                return;
            }
            scheduler.triggerJob(jobKey);
        } catch (Exception e) {
            logger.error("立即运行任务调度中的定时任务异常！" + e.getMessage(), e);
        }
    }

    /**
     * 修改任务调度中的定时任务
     */
    public void updateQuartzJob(QuartzJob job, TriggerKey triggerKey, CronTrigger trigger){
        logger.info("修改任务调度中的定时任务");
        try {
            if (null == job || null == triggerKey || null == trigger) {
                logger.info("修改调度任务参数不正常！");
                return;
            }
            logger.info("原始任务表达式:" + trigger.getCronExpression()
                    + "，现在任务表达式:" + job.getCronExpression());
            if (trigger.getCronExpression().equals(job.getCronExpression())) {
                logger.info("任务调度表达式一致，不予进行修改！");
                return;
            }
            logger.info("任务调度表达式不一致，进行修改");
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (Exception e) {
            logger.error("修改任务调度中的定时任务异常！" + e.getMessage(), e);
        }
    }

    /**
     * 暂停任务调度中的定时任务
     */
    public void pauseJob(QuartzJob job){
        logger.info("暂停任务调度中的定时任务");
        try {
            if (null == job) {
                logger.info("暂停调度任务参数不正常！");
                return;
            }
            JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
            if(null == jobKey){
                logger.info("任务调度中不存在[" + job.getJobName() + "]定时任务，不予进行暂停！");
                return;
            }
            scheduler.pauseJob(jobKey);
        } catch (Exception e) {
            logger.error("暂停任务调度中的定时任务异常！" + e.getMessage(), e);
        }
    }

    /**
     * 恢复任务调度中的定时任务
     */
    public void resumeJob(QuartzJob job){
        logger.info("恢复任务调度中的定时任务");
        try {
            if (null == job) {
                logger.info("恢复调度任务参数不正常！");
                return;
            }
            JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
            if(null == jobKey){
                logger.info("任务调度中不存在[" + job.getJobName() + "]定时任务，不予进行恢复！");
                return;
            }
            scheduler.resumeJob(jobKey);
        } catch (Exception e) {
            logger.error("恢复任务调度中的定时任务异常！" + e.getMessage(), e);
        }
    }

    /**
     * 删除任务调度中的定时任务
     */
    public void deleteJob(QuartzJob job){
        logger.info("删除任务调度中的定时任务");
        try {
            if (null == job) {
                logger.info("删除调度任务参数不正常！");
                return;
            }
            JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
            if(null == jobKey){
                logger.info("任务调度中不存在[" + job.getJobName() + "]定时任务，不予进行删除！");
                return;
            }
            scheduler.deleteJob(jobKey);
            removeMapping(job.getJobName());
        } catch (Exception e) {
            logger.error("删除任务调度中的定时任务异常！" + e.getMessage(), e);
        }
    }

    /**
     * 删除任务调度定时器
     */
    public void deleteJob(TriggerKey triggerKey){
        logger.info("删除任务调度定时器");
        try {
            if(null == triggerKey){
                logger.info("停止任务定时器参数不正常，不予进行停止！");
                return;
            }
            logger.info("停止任务定时器");
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
        } catch (Exception e) {
            logger.info("删除任务调度定时器异常！" + e.getMessage() ,e);
        }
    }

    /**
     * 删除任务调度中的定时任务
     */
    public void deleteCheckJob(QuartzJob job){
        logger.info("删除任务调度中的定时任务");
        try {
            if (null == job) {
                logger.info("删除调度任务参数不正常！");
                return;
            }
            String name = HTTP_CRON_CHECK_PREFIX+job.getJobName();
            JobKey jobKey = JobKey.jobKey(name, job.getJobGroup());
            if(null == jobKey){
                logger.info("任务调度中不存在[" + job.getJobName() + "]定时任务，不予进行删除！");
                return;
            }
            scheduler.deleteJob(jobKey);
            removeMapping(job.getJobName());
        } catch (Exception e) {
            logger.error("删除任务调度中的定时任务异常！" + e.getMessage(), e);
        }
    }

    /**
     * 删除任务调度中的定时任务
     */
    public void deleteMaxTimeJob(QuartzJob job){
        logger.info("删除任务调度中的定时任务");
        try {
            if (null == job) {
                logger.info("删除调度任务参数不正常！");
                return;
            }
            String name = HTTP_CRON_MAX_TIME_PREFIX + job.getJobName();
            JobKey jobKey = JobKey.jobKey(name, job.getJobGroup());
            if(null == jobKey){
                logger.info("任务调度中不存在[" + job.getJobName() + "]定时任务，不予进行删除！");
                return;
            }
            scheduler.deleteJob(jobKey);
            removeMapping(job.getJobName());
        } catch (Exception e) {
            logger.error("删除任务调度中的定时任务异常！" + e.getMessage(), e);
        }
    }


    public void putMapping(String jobName, String executionId) {
        NAME_EXECUTION_ID_MAPS.put(jobName,executionId);
    }

    public void removeMapping(String jobName) {
        NAME_EXECUTION_ID_MAPS.remove(jobName);
    }


    public void removeJobScheduleList(JobConfigInfo jobConfigInfo, List<JobScheduleInfo> jobScheduleInfoList) {
        for(JobScheduleInfo scheduleInfo : jobScheduleInfoList) {
            EntityQuartzJob job = EntityCronUtil.assembleCronTask(jobConfigInfo, scheduleInfo);
            deleteJob(job);
        }

    }



}