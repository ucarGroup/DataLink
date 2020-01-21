package com.ucar.datalink.writer.sddl.threadpool;

import java.util.concurrent.Callable;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 19/03/2018.
 */
public interface CallableExtend extends Callable<Object> {

    /**
     * @Description: 当前线程执行前调用
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 6:16 PM 19/03/2018
     * @param t
     */
    void executeBufore (Thread t);

    /**
     * @Description: 当前线程执行后调用
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 6:16 PM 19/03/2018
     */
    void executeAfter (Throwable t);


}
