package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.utils.Callback;
import com.ucar.datalink.domain.Position;

/**
 * State Keeper for Datalink Worker.
 *
 * Created by lubiao on 2018/3/9.
 */
public interface Keeper {

    /**
     * start the keeper
     */
    void start();

    /**
     * stop the keeper
     */
    void stop();

    /**
     * restart the assigned task
     *
     * @param taskId   identifier of the task for restarting
     * @param position the position for resetting
     * @param callback the callback for caller
     */
    void restartTask(final String taskId, final Position position, final Callback<Void> callback);
}
