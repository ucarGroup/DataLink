package com.ucar.datalink.manager.core.web.dto.job;

/**
 * Created by yang.wang09 on 2018-07-24 10:49.
 */
public class JobScheduleView {

    //schedule 相关的参数
    private String id;
    private String job_id;
    private String schedule_name;
    private String schedule_cron;
    private String schedule_is_retry;
    private String schedule_max_retry;
    private String schedule_retry_interval;
    private String schedule_max_runtime;
    private String schedule_online_state;
    private String schedule_is_suppend;
    private String job_name;
    private String src_media_name;
    private String target_media_name;
    private String media_name;
    private String schedule_state;
    private String fillDataState;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchedule_cron() {
        return schedule_cron;
    }

    public void setSchedule_cron(String schedule_cron) {
        this.schedule_cron = schedule_cron;
    }

    public String getSchedule_is_retry() {
        return schedule_is_retry;
    }

    public void setSchedule_is_retry(String schedule_is_retry) {
        this.schedule_is_retry = schedule_is_retry;
    }

    public String getSchedule_max_retry() {
        return schedule_max_retry;
    }

    public void setSchedule_max_retry(String schedule_max_retry) {
        this.schedule_max_retry = schedule_max_retry;
    }

    public String getSchedule_retry_interval() {
        return schedule_retry_interval;
    }

    public void setSchedule_retry_interval(String schedule_retry_interval) {
        this.schedule_retry_interval = schedule_retry_interval;
    }

    public String getSchedule_max_runtime() {
        return schedule_max_runtime;
    }

    public void setSchedule_max_runtime(String schedule_max_runtime) {
        this.schedule_max_runtime = schedule_max_runtime;
    }

    public String getSchedule_online_state() {
        return schedule_online_state;
    }

    public void setSchedule_online_state(String schedule_online_state) {
        this.schedule_online_state = schedule_online_state;
    }

    public String getSchedule_is_suppend() {
        return schedule_is_suppend;
    }

    public void setSchedule_is_suppend(String schedule_is_suppend) {
        this.schedule_is_suppend = schedule_is_suppend;
    }


    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public String getSrc_media_name() {
        return src_media_name;
    }

    public void setSrc_media_name(String src_media_name) {
        this.src_media_name = src_media_name;
    }

    public String getTarget_media_name() {
        return target_media_name;
    }

    public void setTarget_media_name(String target_media_name) {
        this.target_media_name = target_media_name;
    }

    public String getMedia_name() {
        return media_name;
    }

    public void setMedia_name(String media_name) {
        this.media_name = media_name;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public String getSchedule_state() {
        return schedule_state;
    }

    public void setSchedule_state(String schedule_state) {
        this.schedule_state = schedule_state;
    }

    public String getSchedule_name() {
        return schedule_name;
    }

    public void setSchedule_name(String schedule_name) {
        this.schedule_name = schedule_name;
    }

    public String getFillDataState() {
        return fillDataState;
    }

    public void setFillDataState(String fillDataState) {
        this.fillDataState = fillDataState;
    }
}
