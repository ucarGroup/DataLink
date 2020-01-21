package com.ucar.datalink.writer.sddl.threadpool;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 19/03/2018.
 */
public class ThreadPoolParameter {

    private int corePoolSize = CORE_POOL_SIZE;

    private int maximumPoolSize = MAXIMUM_POOLSIZE;

    private int initialCapacity = INITIAL_CAPACITY;

    private long keepAliveTime = KEEP_ALIVE_TIME;

    private String threadName = "sddlwriter-threadPool-";

    private boolean isDiscard = true ;

    private static final int CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOLSIZE = 100;
    private static final int INITIAL_CAPACITY = 1000000;
    private static final int KEEP_ALIVE_TIME = 120;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public boolean isDiscard() {
        return isDiscard;
    }

    public void setDiscard(boolean discard) {
        isDiscard = discard;
    }
}
