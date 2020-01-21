package com.ucar.datalink.domain.plugin.reader.fq;

import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.contract.Record;

import java.util.List;

/**
 * Created by sqq on 2017/6/14.
 */
public class FqChunk {
    private List<Record> recordList;
    private FutureCallback callback;

    public FqChunk(List<Record> recordList, FutureCallback callback){
        this.recordList = recordList;
        this.callback = callback;
    }

    public List<Record> getRecordList() {
        return recordList;
    }

    public FutureCallback getCallback() {
        return callback;
    }
}
