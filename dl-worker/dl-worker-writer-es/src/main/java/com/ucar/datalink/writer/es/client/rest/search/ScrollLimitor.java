package com.ucar.datalink.writer.es.client.rest.search;

/**
 * @author forest
 * @email wei.wang09@ucarinc.com
 * @create 2017-06-16 13:14
 */
public class ScrollLimitor {

    private long timeStamp = 0L;
    private int reqCount = 0;
    private int limit = 3; // 时间窗口内最大请求数
    private long interval = 1000 * 60; // 时间窗口1分钟

    public ScrollLimitor() {}

    public ScrollLimitor(int limit,long interval) {
        this.interval = interval;
        this.limit = limit;
    }

    /**
     *
     * 限制单位时间内的调用次数
     * @return true 超过调用次数
     *
     */
    public synchronized boolean isTooMuchCalled() {

        if (timeStamp == 0L) { //第一次调用
            timeStamp = System.currentTimeMillis();
            reqCount = 1;
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < timeStamp + interval) {    // 在时间窗口内
            reqCount++;
            // 判断当前时间窗口内是否超过最大请求控制数,第4次则true
            return reqCount > limit;
        }
        else {  //超出时间窗口,当当前时间重新计时计数
            timeStamp = now;
            // 超时后重置
            reqCount = 1;
            return false;
        }
    }
}
