package com.ucar.datalink.worker.api.handle;

import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;

/**
 * Created by lubiao on 2017/3/15.
 */
public interface Handler {

     void initialize(TaskWriterContext context);
     void destroy();
     void writeData(RecordChunk recordChunk, TaskWriterContext context);
}
