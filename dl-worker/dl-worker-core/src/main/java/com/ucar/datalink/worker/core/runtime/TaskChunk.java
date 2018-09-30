package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.worker.api.task.RecordChunk;

/**
 * Data Chunk Between TaskReader and TaskWriter.
 *
 * Created by lubiao on 2017/2/15.
 */
public class TaskChunk {
    private RecordChunk recordChunk;
    private FutureCallback callback;

    public TaskChunk(RecordChunk recordChunk, FutureCallback callback) {
        this.recordChunk = recordChunk;
        this.callback = callback;
    }

    public RecordChunk getRecordChunk() {
        return recordChunk;
    }

    public FutureCallback getCallback() {
        return callback;
    }
}
