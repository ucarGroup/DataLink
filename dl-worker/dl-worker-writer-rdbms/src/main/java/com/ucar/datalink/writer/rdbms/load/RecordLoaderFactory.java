package com.ucar.datalink.writer.rdbms.load;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.writer.rdbms.load.impl.RdbEventRecordLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lubiao on 2017/3/14.
 */
public class RecordLoaderFactory {

    private static final Map<String, RecordLoader> recordLoaderMaps = new ConcurrentHashMap<>();

    static {
        recordLoaderMaps.put(RdbEventRecord.class.getCanonicalName(), new RdbEventRecordLoader());
    }

    public static <T extends Record> RecordLoader getLoader(Class<T> clazz) {
        return recordLoaderMaps.get(clazz.getCanonicalName());
    }
}
