package com.ucar.datalink.domain.job;

/**
 * Created by user on 2017/12/26.
 */
public class JobRunQueueState {


    /**
     * 创建完一个队列就处于 created状态，等待手动触发后变成 unexecute状态
     * 才能被执行
     */
    public static final String INIT = "INIT";

    /**
     * job 队列所有job都没执行
     * 初始状态
     */
    public static final String READY = "READY";

    /**
     * job队列中至少有一个处于运行状态
     * 中间状态
     */
    public static final String PROCESSING = "PROCESSING";

    /**
     * job队列暂时停止，只有处于中间状态的job队列才可以变成STOP状态
     */
    public static final String STOP = "STOP";

    /**
     * job队列执行完，并且全部执行成功
     * 最终状态
     */
    public static final String SUCCEEDED = "SUCCEEDED";

    /**
     * job队列执行完，其中至少有一个job运行失败
     * 最终状态
     */
    public static final String FAILED = "FAILED";



}
