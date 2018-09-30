package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.errors.TaskConflictException;

/**
 * Created by lubiao on 2017/2/16.
 */
public class TaskStatusListenerAdapter implements TaskStatusListener {

    @Override
    public void onPrepare(TaskStatusEvent event) throws TaskConflictException {

    }

    @Override
    public void onStartup(TaskStatusEvent event) {

    }

    @Override
    public void onPause(TaskStatusEvent event) {

    }

    @Override
    public void onResume(TaskStatusEvent event) {

    }

    @Override
    public void onFailure(TaskStatusEvent event, Throwable cause) {

    }

    @Override
    public void onShutdown(TaskStatusEvent event) {

    }
}
