package com.ucar.datalink.manager.core.flinker.cron.entity;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.manager.core.flinker.cron.QuartzManager;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobScheduleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.UUID;

/**
 * Created by yang.wang09 on 2019-02-13 15:33.
 */
public class EntityCronUtil {

    private static Logger logger = LoggerFactory.getLogger(EntityCronUtil.class);

    private static final String SUCCESS = "SUCCESS";

    public static void schdule(EntityQuartzJob job) {
        QuartzManager.getInstance().deleteJob(job);
        QuartzManager.getInstance().initJob(job, EntityCronTask.class);
    }


    public static void scheduleCheckJob(EntityQuartzJob job) {
        if(job.getExecuteId() < 0) {
            return;
        }
        QuartzManager.getInstance().deleteCheckJob(job);
        QuartzManager.getInstance().initCheckJob(job,EntityCronCheck.class);
    }

    public static void scheduleMaxRuntimeJob(EntityQuartzJob job) {
        if(job.getExecuteId() < 0) {
            return;
        }
        QuartzManager.getInstance().deleteMaxTimeJob(job);
        QuartzManager.getInstance().initMaxTimeJob(job,EntityCronMaxRuntime.class);
    }

    public static void pause(EntityQuartzJob job) {
        QuartzManager.getInstance().pauseJob(job);
    }

    public static void resume(EntityQuartzJob job) {
        schdule(job);
    }

    public static void remove(EntityQuartzJob job) {
        QuartzManager.getInstance().deleteJob(job);
        //删除 状态监测的 定时任务
        if(getDefaultIntegerValue(job.getRetryNumber()) > 0) {
            QuartzManager.getInstance().deleteCheckJob(job);
        }

        //删除 最大运行时间监测的 定时任务
        if(getDefaultLongValue(job.getMaxRuntime()) > 0) {
            QuartzManager.getInstance().deleteMaxTimeJob(job);
        }
    }

    public static JobScheduleInfo assembleDefaultScheduleInfo(long jobConfigId) {
        JobScheduleInfo scheduleInfo = new JobScheduleInfo();
        scheduleInfo.setCron("0 0 0 * * ?");
        scheduleInfo.setJobId(jobConfigId);
        scheduleInfo.setScheduleName(UUID.randomUUID().toString());
        scheduleInfo.setIsDelete(false);
        scheduleInfo.setIsRetry(false);
        scheduleInfo.setIsSuspend(false);
        scheduleInfo.setMaxRunningTime(-1L);
        scheduleInfo.setRetryInterval(-1);
        scheduleInfo.setRetryNumber(-1);
        scheduleInfo.setOnlineState(-1);
        scheduleInfo.setScheduleState(true);
        return scheduleInfo;
    }

    public static EntityQuartzJob assembleCronTaskWithMD5(JobConfigInfo info, JobScheduleInfo scheduleInfo) {
        String md5 = assembleMD5(info);
        String id = info.getId()+"";
        EntityQuartzJob job = new EntityQuartzJob();
        job.setJobConfigId(info.getId());
        job.setMd5(md5);
        job.setCronExpression(scheduleInfo.getCron());
        job.setJobGroup("cron");
        job.setJobName(info.getJob_name() +"@"+scheduleInfo.getScheduleName());
        return job;
    }

    public static EntityQuartzJob assembleCronTask(JobConfigInfo info, JobScheduleInfo scheduleInfo) {
        EntityQuartzJob job = new EntityQuartzJob();
        job.setJobConfigId(info.getId());
        job.setCronExpression(scheduleInfo.getCron());
        job.setJobGroup("cron");
        job.setJobName(info.getJob_name() +"@"+ scheduleInfo.getScheduleName());
        return job;
    }


    public static String assembleMD5(JobConfigInfo info) {
        try {
            long src_media_id = info.getJob_src_media_source_id();
            long target_media_id = info.getJob_target_media_source_id();
            String job_media_name = info.getJob_media_name();
            long job_id = info.getId();
            String content = job_id + src_media_id + target_media_id + job_media_name;
            return getMd5(content);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return e.getMessage();
        }
    }

    public static String getMd5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(str.getBytes());
            StringBuilder sb = new StringBuilder(40);
            for(byte x:bs) {
                if((x & 0xff)>>4 == 0) {
                    sb.append("0").append(Integer.toHexString(x & 0xff));
                } else {
                    sb.append(Integer.toHexString(x & 0xff));
                }
            }
            return sb.toString();
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return e.getMessage();
        }
    }


    public static long startResultParse(String startResult) {
        JSONObject obj = (JSONObject)JSONObject.parse(startResult);
        String msg = (String)obj.get("msg");
        if( !SUCCESS.equals(msg) ) {
            return -1L;
        }
        String value = (String)obj.get("executId");
        if(value!=null && value.contains("_")) {
            return Long.parseLong(value.split("_")[0]);
        } else if(value != null) {
            return Long.parseLong(value);
        } else {
            return -1L;
        }
    }


    public static EntityQuartzJob cloneWithExecuteId(EntityQuartzJob job, long executeId) {
        EntityQuartzJob qjob = new EntityQuartzJob();
        qjob.setExecuteId(executeId);
        qjob.setJobConfigId(job.getJobConfigId());
        qjob.setMaxRuntime(job.getMaxRuntime());
        qjob.setMd5(job.getMd5());
        qjob.setRetryInterval(job.getRetryInterval());
        qjob.setRetryNumber(job.getRetryNumber());
        qjob.setJobGroup(job.getJobGroup());
        qjob.setJobName(job.getJobName());
        return qjob;
    }


    public static Integer parseRetryNumber(EntityQuartzJob job) {
        try {
            if(job == null) {
                return -1;
            }
            return job.getRetryNumber();
        } catch(Exception e) {
            return -1;
        }
    }


    public static Long parseMaxRuntime(EntityQuartzJob job) {
        try {
            if(job == null) {
                return -1L;
            }
            return job.getMaxRuntime();
        } catch(Exception e) {
            return -1L;
        }
    }

    private static int getDefaultIntegerValue(Integer i) {
        if(i == null) {
            return 0;
        }
        return i.intValue();
    }

    private static long getDefaultLongValue(Long i) {
        if(i == null) {
            return 0L;
        }
        return i.longValue();
    }



}
