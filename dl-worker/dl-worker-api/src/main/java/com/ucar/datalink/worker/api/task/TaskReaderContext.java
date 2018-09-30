package com.ucar.datalink.worker.api.task;

import com.ucar.datalink.domain.plugin.PluginReaderParameter;
import com.ucar.datalink.worker.api.position.PositionManager;

/**
 * TaskReader专用的Context.
 *
 * Created by lubiao on 2017/3/9.
 */
public interface TaskReaderContext extends TaskContext {

    /**
     * Get the PositionManager for the TaskReader.
     */
    PositionManager positionManager();

    /**
     * Get the Parameter for the TaskReader.
     */
    <T extends PluginReaderParameter> T getReaderParameter();

    /**
     * Get the reader-part session of this task,so the scope of the session data is just for TaskReader.
     */
    TaskSession taskReaderSession();

    /**
     * Get the reader-part Attributes of this task,so the scope of the Attributes is just for TaskReader.
     */
    TaskAttributes taskReaderAttributes();
}
