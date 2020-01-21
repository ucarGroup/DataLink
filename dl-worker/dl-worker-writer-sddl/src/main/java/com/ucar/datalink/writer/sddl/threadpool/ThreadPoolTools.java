package com.ucar.datalink.writer.sddl.threadpool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 19/03/2018.
 */
public class ThreadPoolTools {


    public static ExecutorService getCustomBasePool(ThreadPoolParameter params) {
        if (params == null) {
            params = new ThreadPoolParameter();
        }

        int corePoolSize    = params.getCorePoolSize();
        int maximumPoolSize = params.getMaximumPoolSize();
        int initialCapacity = params.getInitialCapacity();
        long keepAliveTime  = params.getKeepAliveTime();
        String threadName   = params.getThreadName();

        ThreadPoolExecutorExtend threadPool = new ThreadPoolExecutorExtend(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(initialCapacity),
                new ThreadFactoryCustom(threadName));

        return threadPool;
    }

    public static ExecutorService getCustomCache (int keepAliveTime, String threadName) {

        ThreadPoolExecutorExtend threadPool = new ThreadPoolExecutorExtend(
                0,
                Integer.MAX_VALUE,
                keepAliveTime, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactoryCustom(threadName));

        return threadPool;

    }

    static class ThreadFactoryCustom implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        public ThreadFactoryCustom(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix + "-threadPool-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
