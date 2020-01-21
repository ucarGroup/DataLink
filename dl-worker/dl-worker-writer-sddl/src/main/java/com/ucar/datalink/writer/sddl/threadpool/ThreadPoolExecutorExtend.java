package com.ucar.datalink.writer.sddl.threadpool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 16/03/2018.
 */
public class ThreadPoolExecutorExtend extends ThreadPoolExecutor {

    final AtomicInteger submittedTasksCount = new AtomicInteger();

    public ThreadPoolExecutorExtend(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue,
                                    ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @Override
    public void execute(Runnable command) {
        submittedTasksCount.incrementAndGet();
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (r instanceof FutureTaskExtend) {
            CallableExtend ce = ((FutureTaskExtend) r).getCallableExtend();

            if (ce == null) {
                throw new NullPointerException("线程池参数对象为null!");
            }

            ce.executeBufore(t);
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        submittedTasksCount.decrementAndGet();

        if (r instanceof FutureTaskExtend) {
            CallableExtend ce = ((FutureTaskExtend) r).getCallableExtend();

            if (ce == null) {
                throw new NullPointerException("线程池参数对象为null!");
            }

            ce.executeAfter(t);
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTaskExtend<T>(callable);
    }

    @Override
    public String toString() {
        return "ThreadPoolExecutorExtend{" +
                "ActiveCount = "+this.getActiveCount() +
                " CompletedTaskCount = "+ this.getCompletedTaskCount() +
                " CorePoolSize = "+ this.getCorePoolSize() +
                " LargestPoolSize = "+this.getLargestPoolSize() +
                " MaximumPoolSize = "+ this.getMaximumPoolSize() +
                " PoolSize = "+this.getPoolSize() +
                " queueSize = "+this.getQueue().size() +
                " queueString=[" + this.getQueue().toString() + "]" +
                '}';
    }

    // newCachedThreadPool

    // newFixedThreadPool

    // newScheduledThreadPool

    // newSingleThreadExecutor
}
