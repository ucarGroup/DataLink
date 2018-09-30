package com.ucar.datalink.worker.api.task;

import com.ucar.datalink.domain.plugin.PluginWriterParameter;

/**
 * Created by lubiao on 2017/3/9.
 */
public interface TaskWriterContext extends TaskContext {

    /**
     * Get the Parameter for the TaskWriter.
     */
    PluginWriterParameter getWriterParameter();

    /**
     * Get the writer-part session of this task,so the scope of the session data is just for TaskWriter.
     */
    TaskSession taskWriterSession();

    /**
     * Get the writer-part Attributes of this task,so the scope of the Attributes is just for TaskWriter.
     */
    TaskAttributes taskWriterAttributes();
}
