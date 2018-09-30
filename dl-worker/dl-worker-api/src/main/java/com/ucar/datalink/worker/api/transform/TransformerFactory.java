package com.ucar.datalink.worker.api.transform;

import com.ucar.datalink.common.errors.RecordNotSupportException;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lubiao on 2017/3/8.
 */
public class TransformerFactory {
    private static final Map<String, Transformer> transformerMaps = new ConcurrentHashMap<>();

    static {
        transformerMaps.put(RdbEventRecord.class.getCanonicalName(), new BuiltInRdbEventRecordTransformer());
        transformerMaps.put(HRecord.class.getCanonicalName(), new BuiltInHRecordTransformer());
    }

    @SuppressWarnings("unchecked")
    public static <T, R extends Record> T getTransformer(Class<R> clazz) {
        Transformer transformer = transformerMaps.get(clazz.getCanonicalName());
        if (transformer == null) {
            throw new RecordNotSupportException(clazz);
        }

        return (T) transformer;
    }
}
