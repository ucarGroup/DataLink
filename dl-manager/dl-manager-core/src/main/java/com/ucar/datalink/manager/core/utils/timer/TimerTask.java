package com.ucar.datalink.manager.core.utils.timer;

/**
 * Created by lubiao on 2016/12/12.
 */
public abstract class TimerTask implements Runnable {
    //timestamp in millisecond
    private final Long delayMs;

    private TimerTaskEntry timerTaskEntry;

    public TimerTask(Long delayMs) {
        this.delayMs = delayMs;
    }

    public void cancel() {
        synchronized (this) {
            if (timerTaskEntry != null) {
                timerTaskEntry.remove();
            }
            timerTaskEntry = null;
        }
    }

    void setTimerTaskEntry(TimerTaskEntry entry) {
        synchronized (this) {
            // if this timerTask is already held by an existing timer task entry,
            // we will remove such an entry first.
            if (timerTaskEntry != null && timerTaskEntry != entry) {
                timerTaskEntry.remove();
            }

            timerTaskEntry = entry;
        }
    }

    TimerTaskEntry getTimerTaskEntry() {
        return timerTaskEntry;
    }

    Long getDelayMs(){
        return this.delayMs;
    }
}
