package com.ucar.datalink.worker.api.transform;

import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import org.apache.commons.lang.StringUtils;

/**
 * Created by lubiao on 2017/12/13.
 */
public class BuiltInHRecordTransformer extends Transformer<HRecord> {

    @Override
    protected HRecord transformOne(HRecord record, MediaMappingInfo mappingInfo, TaskWriterContext context) {
        //暂时只支持namespace别名和tableName别名
        transformNamespaceAndTableName(record, mappingInfo);
        return record;
    }

    protected void transformNamespaceAndTableName(HRecord record, MediaMappingInfo mappingInfo) {
        String namespace = StringUtils.isEmpty(mappingInfo.getTargetMediaNamespace()) ?
                record.getNamespace() : mappingInfo.getTargetMediaNamespace();
        String tableName = StringUtils.isEmpty(mappingInfo.getTargetMediaName()) ?
                record.getTableName() : mappingInfo.getTargetMediaName();
        record.setNamespace(namespace);
        record.setTableName(tableName);
    }
}
