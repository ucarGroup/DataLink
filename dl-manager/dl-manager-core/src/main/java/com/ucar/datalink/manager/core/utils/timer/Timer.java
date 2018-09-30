package com.ucar.datalink.manager.core.utils.timer;

/**
 * Created by lubiao on 2016/12/12.
 */
public interface Timer {
    /**
     * Add a new task to this executor. It will be executed after the task's delay
     * (beginning parseFrom the time of submission)
     *
     * @param timerTask the task to add
     */
    void add(TimerTask timerTask);

    /**
     * Advance the internal clock, executing any tasks whose expiration has been
     * reached within the duration of the passed timeout.
     *
     * @param timeoutMs
     * @return whether or not any tasks were executed
     */
    Boolean advanceClock(Long timeoutMs);

    /**
     * Get the number of tasks pending execution
     *
     * @return the number of tasks
     */
    Integer size();

    /**
     * Shutdown the timer service, leaving pending tasks unexecuted
     */
    void shutdown();
}
