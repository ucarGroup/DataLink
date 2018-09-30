package com.ucar.datalink.worker.api.merge;

import com.ucar.datalink.worker.api.task.RecordChunk;

/**
 * Created by lubiao on 2017/3/28.
 */
public class BuiltInDummyMerger implements Merger{

    @Override
    public RecordChunk merge(RecordChunk recordChunk) {
        return recordChunk;//原样返回即可
    }
}
