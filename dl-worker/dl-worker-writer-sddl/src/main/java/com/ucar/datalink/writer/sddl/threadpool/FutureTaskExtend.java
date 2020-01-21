package com.ucar.datalink.writer.sddl.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 19/03/2018.
 */
public class FutureTaskExtend<V> extends FutureTask<V> {
    private final CallableExtend callableExtend;

    public FutureTaskExtend(Callable<V> callable) {
        super(callable);

        callableExtend = (CallableExtend) callable;
    }

    public CallableExtend getCallableExtend() {
        return callableExtend;
    }
}
