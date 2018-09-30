package com.ucar.datalink.worker.api.merge;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.worker.api.task.RecordChunk;

/**
 * Created by lubiao on 2017/3/28.
 */
public interface Merger<T extends Record> {
    RecordChunk<T> merge(RecordChunk<T> recordChunk);
}
