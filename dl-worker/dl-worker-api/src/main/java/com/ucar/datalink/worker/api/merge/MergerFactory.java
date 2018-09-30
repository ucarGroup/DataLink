package com.ucar.datalink.worker.api.merge;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lubiao on 2017/3/28.
 */
public class MergerFactory {

    private static final String DUMMY_MERGER = "DUMMY";

    private static final Map<String, Merger> mergerMaps = new ConcurrentHashMap<>();

    static {
        mergerMaps.put(RdbEventRecord.class.getCanonicalName(), new BuiltInRdbEventRecordMerger());
        mergerMaps.put(DUMMY_MERGER, new BuiltInDummyMerger());
    }

    @SuppressWarnings("unchecked")
    public static <T, R extends Record> T getMerger(Class<R> clazz) {
        Merger merger = mergerMaps.get(clazz.getCanonicalName());
        if (merger == null) {
            return (T) mergerMaps.get(DUMMY_MERGER);
        }

        return (T) merger;
    }
}
