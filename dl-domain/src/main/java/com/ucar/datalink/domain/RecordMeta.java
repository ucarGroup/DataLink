package com.ucar.datalink.domain;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.media.MediaMappingInfo;

import java.util.Set;

/**
 * Created by lubiao on 2017/3/8.
 */
@SuppressWarnings("unchecked")
public abstract class RecordMeta {

    /**
     * Record对应的MediaMapping
     * 类型：MediaMappingInfo
     */
    private static final String MediaMapping = "MediaMapping";

    /**
     * 标识Record中的扩展列
     * 扩展列：
     * 指的是源端并不存在该列，而是在特定业务场景下，凭空创造出的列，如A和B两个Column合并成一个新的Column
     * 当我们既用到了[白名单]又用到了[扩展列]时，需要在Record的元数据中，把扩展列的名字放进去，否则扩展列不在白名单中，将被忽略
     */
    private static final String ExtendColumns = "ExtendColumns";

    public static void attachMediaMapping(Record record, MediaMappingInfo mappingInfo) {
        record.metaData().put(MediaMapping, mappingInfo);
    }

    public static MediaMappingInfo mediaMapping(Record record) {
        return (MediaMappingInfo) record.metaData().get(MediaMapping);
    }

    public static void attachExtendColumns(Record record, Set<String> extendColumnNames) {
        record.metaData().put(ExtendColumns, extendColumnNames);
    }

    public static Set<String> extendColumns(Record record) {
        return (Set<String>) record.metaData().get(ExtendColumns);
    }
}
