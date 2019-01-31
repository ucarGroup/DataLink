package com.ucar.datalink.writer.es.handle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.GeoPositionMapping;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.vo.BatchContentVo;
import com.ucar.datalink.writer.es.util.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by lubiao on 2017/6/28.
 */
public class BatchContentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(BatchContentBuilder.class);

    public static List<BatchContentVo> buildContents(List<RdbEventRecord> records) {
        List<BatchContentVo> contents = Lists.newArrayList();
        for (RdbEventRecord record : records) {
            //只支持insert和update类型
            if (record.getEventType() == EventType.INSERT || record.getEventType() == EventType.UPDATE) {
                BatchContentVo contentVo = buildContentVo(record);
                contents.add(contentVo);
            }
        }
        return contents;
    }

    /**
     * 根据输入源，生成ES操作命令说明
     */
    private static BatchContentVo buildContentVo(RdbEventRecord record) {

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
    private static void fillIndexAndType(BatchContentVo contentVo, RdbEventRecord record) {
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
    private static void fillPrimaryKey(BatchContentVo contentVo, RdbEventRecord record) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        String joinColumn = mappingInfo.getJoinColumn();

        if (StringUtils.isNotBlank(joinColumn)) {
            Optional<EventColumn> optional;

            optional = record.getKeys().stream().filter(i -> i.getColumnName().equals(joinColumn)).findFirst();
            if (optional.isPresent()) {
                contentVo.setId(optional.get().getColumnValue());
                return;
            }

            optional = record.getColumns().stream().filter(i -> i.getColumnName().equals(joinColumn)).findFirst();
            if (optional.isPresent()) {
                contentVo.setId(optional.get().getColumnValue());
                return;
            }

            throw new ValidationException(String.format("can not find the column [%s] for join.", joinColumn));
        } else {
            //没有指定joinColumn，则认为主键就是id
            if (record.getKeys().size() > 1) {
                throw new ValidationException("The size of the keys is more than one,it is not supported.");
            }

            contentVo.setId(record.getKeys().get(0).getColumnValue());
        }
    }

    /**
     * 填充列内容
     */
    private static void fillActionAndContent(BatchContentVo contentVo, RdbEventRecord record) {
        // 因为涉及到多表合并操作，所以不管原始操作是插入还是更新，Action都设置为Update
        // 当报404错误，更新失败的时候，再转为Index操作
//        contentVo.setBatchActionEnum(ESEnum.BatchActionEnum.UPDATE);
        contentVo.setBatchActionEnum(ESEnum.BatchActionEnum.UPSERT);

        if (record.getEventType() == EventType.INSERT) {
            contentVo.setContent(buildData(record, false));
        } else if (record.getEventType() == EventType.UPDATE) {
            contentVo.setContent(buildData(record, true));
        }
    }

    /**
     * 将RdbEventRecord数据转化为Es数据
     */
    private static Map<String, Object> buildData(RdbEventRecord record, boolean justUpdated) {
        String fieldNamePrefix = (String) record.metaData().get(Constants.FIELD_NAME_PREFIX);
        Map<String, Object> data = Maps.newLinkedHashMap();

        //主键信息必须要同步
        for (EventColumn column : record.getKeys()) {
            data.put(fieldNamePrefix + column.getColumnName(), column.isNull() ? null : column.getColumnValue());
        }

        //joinColumn列必须同步
//        String joinColumn = RecordMeta.mediaMapping(record).getJoinColumn();
/*        for (EventColumn column : record.getColumns()) {
            boolean isJoinColumn = column.getColumnName().equals(joinColumn);
            if (justUpdated && !column.isUpdate() && !isJoinColumn) {
                continue;
            }

            logger.debug(String.format("Column Name is %s,Sql Type is %s", column.getColumnName(), column.getColumnType()));

            if (isDate(column)) {
                data.put(fieldNamePrefix + column.getColumnName(), column.isNull() ? null : normalizeDate(column.getColumnValue()));
            } else {
                data.put(fieldNamePrefix + column.getColumnName(), column.isNull() ? null : column.getColumnValue());
            }
        }*/

        //将变更后当前行所有的值全部更新
        for (EventColumn column : record.getColumns()) {
            if (isDate(column)) {
                data.put(fieldNamePrefix + column.getColumnName(), column.isNull() ? null : normalizeDate(column.getColumnValue()));
            } else {
                data.put(fieldNamePrefix + column.getColumnName(), column.isNull() ? null : column.getColumnValue());
            }
        }



        buildGeoPosition(record, data, fieldNamePrefix);
        return data;
    }

    /**
     * 合并地理位置信息
     */
    private static void buildGeoPosition(RdbEventRecord record, Map<String, Object> data, String fieldNamePrefix) {
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
                for (EventColumn column : record.getKeys()) {
                    data.remove(column.getColumnName());
                }
            }

        }
    }

    private static Object formatGeoPosition(RdbEventRecord row, GeoPositionMapping geoPositionMapping) {
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

    private static String getColumnValue(RdbEventRecord record, String columnName) {
        Optional<EventColumn> optional = record.getColumns().stream().filter(i -> i.getColumnName().equals(columnName)).findFirst();
        return optional.isPresent() ? optional.get().getColumnValue() : null;
    }

    private static boolean isDate(EventColumn column) {
        int columnType = column.getColumnType();
        if (columnType == Types.DATE || columnType == Types.TIMESTAMP || columnType == Types.TIME) {
            return true;
        }
        return false;
    }

    private static String normalizeDate(String input) {
        // "2016-08-10 10:33:41.354" 这种有毫秒数的 把毫秒数干掉
        // "2016-08-10" 这种补成 "2016-08-10 00:00:00"
        Date time = DateUtils.parse(input);
        return DateUtils.format(time);
    }
}
