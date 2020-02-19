package com.ucar.datalink.flinker.core.admin.bean;

import java.sql.Timestamp;

/**
 * Created by user on 2017/7/11.
 *
 FieldTypeComment
 idint(11) NOT NULL任务id，主键自增长
 job_namevarchar(100) NOT NULL任务名称，名称不可以重复
 job_desctext NULL任务描述
 job_contenttext NULL任务内容
 job_media_namevarchar(100) NULL参与同步的介质名称
 job_src_media_source_idint(11) NULL源端MediaSourceId
 job_target_media_source_idint(11) NULL目标端MediaSourceId
 timing_transfer_typeenum('FULL','INCREMENT') NULLFULL – 每次全量迁移,INCREMENT – 每次增量迁移
 timing_expressionvarchar(100) NULLcorn表达式
 timing_yntinyint(1) NULL是否定时任务
 timing_on_yntinyint(1) NULL是否开启定时任务
 timing_parametervarchar(200) NULL定时任务的参数信息
 timing_target_workervarchar(500) NULL执行定时任务的目标机器ip
 create_timetimestamp NOT NULL创建时间
 modify_timetimestamp NOT NULL修改时间
 is_deletetinyint(1) NULL任务是否被删除
 */
public class JobConfigBean {


    /**
     * 任务id，主键
     */
    private int id;

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
     * 源端 MediaSourceInfo的ID
     */
    private int job_src_media_source_id;

    /**
     * 目标端 MediaSourceInfo的ID
     */
    private int job_target_media_source_id;

    /**
     * 同步类型，增量还是全量
     */
    private String timing_transfer_type;

    /**
     * cron任务的表达式
     */
    private String timing_expression;

    /**
     * 是否是定时任务
     */
    private boolean timing_yn;

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
    private Timestamp create_time;

    /**
     * 修改时间
     */
    private Timestamp modify_time;

    /**
     * 任务是否被删除了
     */
    private boolean is_delete;


    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getJob_src_media_source_id() {
        return job_src_media_source_id;
    }

    public void setJob_src_media_source_id(int job_src_media_source_id) {
        this.job_src_media_source_id = job_src_media_source_id;
    }

    public int getJob_target_media_source_id() {
        return job_target_media_source_id;
    }

    public void setJob_target_media_source_id(int job_target_media_source_id) {
        this.job_target_media_source_id = job_target_media_source_id;
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

    public boolean isTiming_yn() {
        return timing_yn;
    }

    public void setTiming_yn(boolean timing_yn) {
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

    public Timestamp getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Timestamp create_time) {
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

    @Override
    public String toString() {
        return "JobConfigBean{" +
                "id=" + id +
                ", job_name='" + job_name + '\'' +
                ", job_desc='" + job_desc + '\'' +
                '}';
    }
}
