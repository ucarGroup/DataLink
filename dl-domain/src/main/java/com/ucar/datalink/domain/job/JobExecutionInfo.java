package com.ucar.datalink.domain.job;


import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by user on 2017/7/14.
 *
 `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键，自增类型',
 `job_id` bigint(20) unsigned NOT NULL COMMENT '任务id',
 `worker_address` varchar(100) DEFAULT NULL COMMENT '所在节点',
 `pid` int(10) DEFAULT NULL COMMENT '进程编号',
 `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '开始时间(创建索引)',
 `end_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间',
 `state` varchar(100) DEFAULT NULL COMMENT '运行状态',
 `byte_speed_per_second` bigint(20) DEFAULT NULL COMMENT '任务平均流量',
 `record_speed_per_second` bigint(20) DEFAULT NULL COMMENT '记录写入速度',
 `total_error_records` bigint(20) DEFAULT NULL COMMENT '同步失败总数',
 `total_record` bigint(20) DEFAULT NULL COMMENT '同步总数',
 `wait_reader_time` decimal(50,10) DEFAULT NULL COMMENT '等待读的时间',
 `wait_writer_time` decimal(50,10) DEFAULT NULL COMMENT '等待写的时间',
 `percentage` decimal(50,10) DEFAULT NULL COMMENT '完成百分比',
 `exception` text COMMENT '异常信息',
 `job_queue_execution_id` bigint(20) DEFAULT NULL COMMENT '所属队列的运行id',
 `task_communication_info` mediumtext COMMENT '任务详细信息',
 `original_configuration` mediumtext COMMENT '此次运行的job镜像',
 */
@Alias("jobexecution")
public class JobExecutionInfo implements Serializable, Storable {
    /**
     * 主键，自增类型
     */
    private long id;

    /**
     * job任务id，关联job表的id
     */
    private long job_id;

    /**
     * 所在节点
     */
    private String worker_address;

    /**
     * job运行时的进程id
     */
    private int pid;

    /**
     * 任务开始时间
     */
    private Timestamp start_time;

    /**
     * 任务结束时间
     */
    private Timestamp end_time;

    /**
     * 任务运行状态
     */
    private String state;

    /**
     * 任务平均流量
     */
    private long byte_speed_per_second;

    /**
     * 记录写入速度
     */
    private long record_speed_per_second;

    /**
     * 同步失败总数
     */
    private long total_error_records;

    /**
     * 同步总数
     */
    private long total_record;

    /**
     * 等待读的时间
     */
    private double wait_reader_time;

    /**
     * 等待写的时间
     */
    private double wait_writer_time;

    /**
     * 完成百分比
     */
    private double percentage;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * job所属队列运行id
     */
    private long job_queue_execution_id;

    /**
     * 任务详细信息
     */
    private String task_communication_info;

    /**
     * 此次运行的job镜像
     */
    private String original_configuration;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getJob_id() {
        return job_id;
    }

    public void setJob_id(long job_id) {
        this.job_id = job_id;
    }

    public String getWorker_address() {
        return worker_address;
    }

    public void setWorker_address(String worker_address) {
        this.worker_address = worker_address;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public Timestamp getStart_time() {
        return start_time;
    }

    public void setStart_time(Timestamp start_time) {
        this.start_time = start_time;
    }

    public Timestamp getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Timestamp end_time) {
        this.end_time = end_time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getByte_speed_per_second() {
        return byte_speed_per_second;
    }

    public void setByte_speed_per_second(long byte_speed_per_second) {
        this.byte_speed_per_second = byte_speed_per_second;
    }

    public long getRecord_speed_per_second() {
        return record_speed_per_second;
    }

    public void setRecord_speed_per_second(long record_speed_per_second) {
        this.record_speed_per_second = record_speed_per_second;
    }

    public long getTotal_error_records() {
        return total_error_records;
    }

    public void setTotal_error_records(long total_error_records) {
        this.total_error_records = total_error_records;
    }

    public long getTotal_record() {
        return total_record;
    }

    public void setTotal_record(long total_record) {
        this.total_record = total_record;
    }

    public double getWait_reader_time() {
        return wait_reader_time;
    }

    public void setWait_reader_time(double wait_reader_time) {
        this.wait_reader_time = wait_reader_time;
    }

    public double getWait_writer_time() {
        return wait_writer_time;
    }

    public void setWait_writer_time(double wait_writer_time) {
        this.wait_writer_time = wait_writer_time;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public long getJob_queue_execution_id() {
        return job_queue_execution_id;
    }

    public void setJob_queue_execution_id(long job_queue_execution_id) {
        this.job_queue_execution_id = job_queue_execution_id;
    }

    public String getTask_communication_info() {
        return task_communication_info;
    }

    public void setTask_communication_info(String task_communication_info) {
        this.task_communication_info = task_communication_info;
    }

    public String getOriginal_configuration() {
        return original_configuration;
    }

    public void setOriginal_configuration(String original_configuration) {
        this.original_configuration = original_configuration;
    }


    @Override
    public String toString() {
        return "JobExecutionInfo{" +
                "id=" + id +
                ", job_id=" + job_id +
                ", worker_address='" + worker_address + '\'' +
                ", pid=" + pid +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", state='" + state + '\'' +
                '}';
    }
}
