package com.ucar.datalink.manager.core.web.dto.job;

import java.sql.Timestamp;

/**
 * Created by user on 2017/7/14.
 */
public class JobConfigView {

    public static final String TIMING_TRANSFER_TYPE_FULL = "FULL";

    public static final String TIMING_TRANSFER_TYPE_INCREMENT = "INCREMENT";

    /**
     * 任务id，主键
     */
    private long id;

    /**
     * 任务名称
     */
    private String job_name;

    /**
     * 任务描述
     */
    private String job_desc;

    /**
     * 任务内存
     */
    private String job_content;

    /**
     * 参与同步的介质名称
     */
    private String job_media_name;

    /**
     * 参与同步的目标表名
     */
    private String job_media_target_name = "";

    /**
     * 源端 MediaSourceInfo的名称
     */
    private String job_src_media_source_name;

    /**
     * 目标端 MediaSourceInfo的名称
     */
    private String job_target_media_source_name;

    /**
     * 同步类型，增量还是全量
     */
    private String timing_transfer_type = TIMING_TRANSFER_TYPE_FULL;

    /**
     * cron任务的表达式
     */
    private String timing_expression;

    /**
     * 是否是定时任务
     */
    private String timing_yn;

    /**
     * 是否开启定时任务
     */
    private boolean timing_on_yn;

    /**
     * 定时任务的参数信息
     */
    private String timing_parameter;

    /**
     * 定时任务的目标机器IP
     */
    private String timing_target_worker;

    /**
     * 创建时间
     */
    private String create_time;

    /**
     * 修改时间
     */
    private Timestamp modify_time;

    /**
     * 任务是否被删除了
     */
    private boolean is_delete;

    /**
     * 源端类型
     */
    private String srcType;

    /**
     * 目标端类型
     */
    private String destType;

    /**
     * 扩展属性，hbase需要拆分的数量
     */
    private String hbase_split_count;

    /**
     * 读取指定的hbase数据后解析表结构
     */
    private String hbaseSpecifiedNum;


    //schedule 相关的参数
    private String schedule_cron;
    private String schedule_is_retry;
    private String schedule_max_retry;
    private String schedule_retry_interval;
    private String schedule_max_runtime;
    private String schedule_online_state;
    private String schedule_is_suppend;

    /**
     * 当前状态
     */
    private String currentState;



    public static String getTimingTransferTypeFull() {
        return TIMING_TRANSFER_TYPE_FULL;
    }

    public static String getTimingTransferTypeIncrement() {
        return TIMING_TRANSFER_TYPE_INCREMENT;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public String getJob_desc() {
        return job_desc;
    }

    public void setJob_desc(String job_desc) {
        this.job_desc = job_desc;
    }

    public String getJob_content() {
        return job_content;
    }

    public void setJob_content(String job_content) {
        this.job_content = job_content;
    }

    public String getJob_media_name() {
        return job_media_name;
    }

    public void setJob_media_name(String job_media_name) {
        this.job_media_name = job_media_name;
    }

    public String getJob_src_media_source_name() {
        return job_src_media_source_name;
    }

    public void setJob_src_media_source_name(String job_src_media_source_name) {
        this.job_src_media_source_name = job_src_media_source_name;
    }

    public String getJob_target_media_source_name() {
        return job_target_media_source_name;
    }

    public void setJob_target_media_source_name(String job_target_media_source_name) {
        this.job_target_media_source_name = job_target_media_source_name;
    }

    public String getTiming_transfer_type() {
        return timing_transfer_type;
    }

    public void setTiming_transfer_type(String timing_transfer_type) {
        this.timing_transfer_type = timing_transfer_type;
    }

    public String getTiming_expression() {
        return timing_expression;
    }

    public void setTiming_expression(String timing_expression) {
        this.timing_expression = timing_expression;
    }

    public String getTiming_yn() {
        return timing_yn;
    }

    public void setTiming_yn(String timing_yn) {
        this.timing_yn = timing_yn;
    }

    public boolean isTiming_on_yn() {
        return timing_on_yn;
    }

    public void setTiming_on_yn(boolean timing_on_yn) {
        this.timing_on_yn = timing_on_yn;
    }

    public String getTiming_parameter() {
        return timing_parameter;
    }

    public void setTiming_parameter(String timing_parameter) {
        this.timing_parameter = timing_parameter;
    }

    public String getTiming_target_worker() {
        return timing_target_worker;
    }

    public void setTiming_target_worker(String timing_target_worker) {
        this.timing_target_worker = timing_target_worker;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public Timestamp getModify_time() {
        return modify_time;
    }

    public void setModify_time(Timestamp modify_time) {
        this.modify_time = modify_time;
    }

    public boolean is_delete() {
        return is_delete;
    }

    public void setIs_delete(boolean is_delete) {
        this.is_delete = is_delete;
    }

    public String getSrcType() {
        return srcType;
    }

    public void setSrcType(String srcType) {
        this.srcType = srcType;
    }

    public String getHbase_split_count() {
        return hbase_split_count;
    }

    public void setHbase_split_count(String hbase_split_count) {
        this.hbase_split_count = hbase_split_count;
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


    public String getDestType() {
        return destType;
    }

    public void setDestType(String destType) {
        this.destType = destType;
    }

    public String getHbaseSpecifiedNum() {
        return hbaseSpecifiedNum;
    }

    public void setHbaseSpecifiedNum(String hbaseSpecifiedNum) {
        this.hbaseSpecifiedNum = hbaseSpecifiedNum;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getJob_media_target_name() {
        return job_media_target_name;
    }

    public void setJob_media_target_name(String job_media_target_name) {
        this.job_media_target_name = job_media_target_name;
    }

    @Override
    public String toString() {
        return "JobConfigView{" +
                "id=" + id +
                ", job_name='" + job_name + '\'' +
                ", job_desc='" + job_desc + '\'' +
                '}';
    }


}
