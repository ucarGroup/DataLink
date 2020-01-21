package com.ucar.datalink.writer.es.handle;

import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.ModeUtils;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.transform.BuiltInHRecordTransformer;

/**
 * Created by qianqian.shi on 2018/12/19.
 */
public class HRecordTransformer extends BuiltInHRecordTransformer {
    @Override
    protected HRecord transformOne(HRecord record, MediaMappingInfo mappingInfo, TaskWriterContext context) {
        //在执行父类的方法之前，先拿到原始的表名，然后构造列名前缀
        String tableName = record.getTableName();
        String fieldNamePrefix = getEsPrefix(record, mappingInfo);
        HRecord result = super.transformOne(record, mappingInfo, context);
        if (result != null) {
            result.metaData().put(Constants.ORIGIN_TABLE_NAME, tableName);
            result.metaData().put(Constants.FIELD_NAME_PREFIX, fieldNamePrefix);
        }
        return result;
    }

    /**
     * 根据mapping配置情况获取前缀信息
     */
    private static String getEsPrefix(HRecord record, MediaMappingInfo mappingInfo) {
        if (mappingInfo.isEsUsePrefix()) {
            String tableName = record.getTableName();

            String result = ModeUtils.tryBuildMonthlyPattern(tableName);
            if (ModeUtils.isMonthlyPattern(result)) {
                tableName = tableName.substring(0, tableName.length()-7);
            } else {
                result = ModeUtils.tryBuildYearlyPattern(tableName);
                if (ModeUtils.isYearlyPattern(result)) {
                    tableName = tableName.substring(0, tableName.length()-5);
                }
            }
            return tableName + Constants.SEPARATOR;
        }
        return "";
    }
}
