package com.ucar.datalink.worker.api.task;

import com.ucar.datalink.common.errors.DatalinkException;

/**
 * Created by lubiao on 2017/2/21.
 */
public abstract class TaskLifeCycle {
    private volatile boolean running = false;

    public boolean isStart() {
        return this.running;
    }

    public void start() {
        if (this.running) {
            throw new DatalinkException(this.getClass().getName() + " has startup , don\'t repeat start");
        } else {
            this.running = true;
        }
    }

    /**
     * Stop this task, This method does not block, it only triggers shutdown.
     * When override this method ,please follow this principle.
     */
    public void stop() {
        if (!this.running) {
            throw new DatalinkException(this.getClass().getName() + " isn\'t start , please check");
        } else {
            this.running = false;
        }
    }
}
