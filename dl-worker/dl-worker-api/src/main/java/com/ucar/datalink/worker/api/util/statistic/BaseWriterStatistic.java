package com.ucar.datalink.worker.api.util.statistic;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 15/12/2017.
 */
public class BaseWriterStatistic extends Statistic {
    private long timeForTotalPush;

    public void setTimeForTotalPush(long timeForTotalPush) {
        this.timeForTotalPush = timeForTotalPush;
    }

    public long getTimeForTotalPush() {
        return timeForTotalPush;
    }

}
