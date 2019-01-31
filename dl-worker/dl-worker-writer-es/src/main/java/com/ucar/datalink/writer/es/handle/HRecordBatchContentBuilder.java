package com.ucar.datalink.writer.es.handle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.contract.log.hbase.HColumn;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.contract.log.hbase.HUtil;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.GeoPositionMapping;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.vo.BatchContentVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by qianqian.shi on 2018/12/19.
 */
public class HRecordBatchContentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(BatchContentBuilder.class);

    public static List<BatchContentVo> buildContents(List<HRecord> records) {
        List<BatchContentVo> contents = Lists.newArrayList();
        for (HRecord record : records) {
            BatchContentVo contentVo = buildContentVo(record);
            contents.add(contentVo);
        }
        return contents;
    }

    /**
     * 根据输入源，生成ES操作命令说明
     */
    private static BatchContentVo buildContentVo(HRecord record) {

        BatchContentVo contentVo = new BatchContentVo();
        contentVo.setRetainNullValue(true);

        fillIndexAndType(contentVo, record);
        fillPrimaryKey(contentVo, record);
        fillActionAndContent(contentVo, record);

        return contentVo;
    }

    /**
     * 填充Index和Type
     */
    private static void fillIndexAndType(BatchContentVo contentVo, HRecord record) {
        String tableName = record.getTableName();//在Transform阶段tableName已经转化成了别名
        if (!tableName.contains(".")) {
            throw new ValidationException("Please specify the index and type");
        }

        contentVo.setIndex(StringUtils.substringBefore(tableName, "."));
        contentVo.setType(StringUtils.substringAfter(tableName, "."));
    }

    /**
     * 填充主键
     */
    private static void fillPrimaryKey(BatchContentVo contentVo, HRecord record) {

        String rowKey = HUtil.toString(record.getRowKey());
        contentVo.setId(rowKey);

    }

    /**
     * 填充列内容
     */
    private static void fillActionAndContent(BatchContentVo contentVo, HRecord record) {
        // Action都设置为Update
        // 当报404错误，更新失败的时候，再转为Index操作
//        contentVo.setBatchActionEnum(ESEnum.BatchActionEnum.UPDATE);
        contentVo.setBatchActionEnum(ESEnum.BatchActionEnum.UPSERT);
        contentVo.setContent(buildData(record));
    }

    /**
     * 将HRecord数据转化为Es数据
     */
    private static Map<String, Object> buildData(HRecord record) {
        String fieldNamePrefix = (String) record.metaData().get(Constants.FIELD_NAME_PREFIX);
        Map<String, Object> data = Maps.newLinkedHashMap();

        //主键信息必须要同步
        String rowKey = HUtil.toString(record.getRowKey());
        data.put(fieldNamePrefix + "rowKey", StringUtils.isBlank(rowKey) ? null : rowKey);

        for (HColumn column : record.getColumns()) {
            String columnName = HUtil.toString(column.getFamily()) + "_" + HUtil.toString(column.getQualifier());
            String columnValue = HUtil.toString(column.getValue());
            Type columnType = Type.codeToType(column.getType());
            logger.debug(String.format("Column Name is %s,Sql Type is %s", columnName, columnType));

            //只同步Put类型的数据
            if (columnType == Type.Put) {
                data.put(fieldNamePrefix + columnName, StringUtils.isBlank(columnValue) ? null : columnValue);
            }

        }

        buildGeoPosition(record, data, fieldNamePrefix);
        return data;
    }

    /**
     * 合并地理位置信息
     */
    private static void buildGeoPosition(HRecord record, Map<String, Object> data, String fieldNamePrefix) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        List<GeoPositionMapping> geoPositionMappings = mappingInfo.getGeoPositionMappings();

        if (geoPositionMappings != null && !geoPositionMappings.isEmpty()) {
            for (GeoPositionMapping geoPositionMapping : geoPositionMappings) {
                Object value = formatGeoPosition(record, geoPositionMapping);

                if (data.containsKey(fieldNamePrefix + geoPositionMapping.getLonColumnName())) {
                    data.remove(fieldNamePrefix + geoPositionMapping.getLonColumnName());
                }

                if (data.containsKey(fieldNamePrefix + geoPositionMapping.getLatColumnName())) {
                    data.remove(fieldNamePrefix + geoPositionMapping.getLatColumnName());
                }

                data.put(fieldNamePrefix + geoPositionMapping.getColumnName(), value);
            }

            //地理点位合并比较特殊,因为历史设计原因，多表合并时可以不使用表前缀,那么不同表的主键值是冲突的
            //所以在多表合并并且未使用表前缀的情况下，直接忽略主键，不进行同步
            if (StringUtils.isBlank(fieldNamePrefix) && StringUtils.isNotBlank(mappingInfo.getJoinColumn())) {
                data.remove(fieldNamePrefix + "rowKey");
            }

        }
    }

    private static Object formatGeoPosition(HRecord row, GeoPositionMapping geoPositionMapping) {
        String lonVal = getColumnValue(row, geoPositionMapping.getLonColumnName());
        String latVal = getColumnValue(row, geoPositionMapping.getLatColumnName());

        if (StringUtils.isEmpty(lonVal)) {
            lonVal = "0.0";
        }
        if (StringUtils.isEmpty(latVal)) {
            latVal = "0.0";
        }

        Double lon;
        Double lat;
        //经度 范围
        try {
            lon = Double.valueOf(lonVal);
            if (lon > 180d || lon < -180d) {
                lon = 0.0d;
            }
        } catch (Exception e) {
            lon = 0.0d;
        }
        //维度范围
        try {
            lat = Double.valueOf(latVal);

            if (lat > 90d || lat < -90d) {
                lat = 0.0d;
            }

        } catch (Exception e) {
            lat = 0.0;
        }

        Map<String, Double> geoMap = Maps.newHashMap();
        geoMap.put("lon", lon);
        geoMap.put("lat", lat);
        return geoMap;
    }

    private static String getColumnValue(HRecord record, String columnName) {
        Optional<HColumn> optional = record.getColumns().stream().filter(i -> (HUtil.toString(i.getFamily()) + "_" + HUtil.toString(i.getQualifier())).equals(columnName)).findFirst();
        return optional.isPresent() ? HUtil.toString(optional.get().getValue()) : null;
    }

    public static enum Type {
        Minimum((byte) 0),
        Put((byte) 4),
        Delete((byte) 8),
        DeleteFamilyVersion((byte) 10),
        DeleteColumn((byte) 12),
        DeleteFamily((byte) 14),
        Maximum((byte) -1);

        private final byte code;

        private Type(byte c) {
            this.code = c;
        }

        public byte getCode() {
            return this.code;
        }

        public static Type codeToType(byte b) {
            Type[] arr$ = values();
            int len$ = arr$.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                Type t = arr$[i$];
                if (t.getCode() == b) {
                    return t;
                }
            }

            throw new RuntimeException("Unknown code " + b);
        }
    }
}
