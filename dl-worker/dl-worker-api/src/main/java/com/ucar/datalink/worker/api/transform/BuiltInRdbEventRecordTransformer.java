package com.ucar.datalink.worker.api.transform;

import com.google.common.collect.Maps;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.ColumnMappingMode;
import com.ucar.datalink.domain.media.MediaColumnMappingInfo;
import com.ucar.datalink.domain.media.MediaInfo;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Table;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2017/3/6.
 */
public class BuiltInRdbEventRecordTransformer extends Transformer<RdbEventRecord> {

    @Override
    protected RdbEventRecord transformOne(RdbEventRecord record, MediaMappingInfo mappingInfo, TaskWriterContext context) {
        //检查列白名单、黑名单设置的是否符合规范
        if (mappingInfo.getColumnMappingMode() != ColumnMappingMode.NONE) {
            MediaService mediaService = DataLinkFactory.getObject(MediaService.class);
            MediaInfo sourceMedia = mappingInfo.getSourceMedia();
            Table table = DbDialectFactory.getDbDialect(sourceMedia.getMediaSource()).findTable(sourceMedia.getNamespace(), sourceMedia.getName());
            mediaService.checkMediaColumnMappings(table, mappingInfo.getColumnMappings(), mappingInfo.getColumnMappingMode());
        }

        //创建列名映射字典
        Map<String, String> columnMappings = Maps.newHashMap();
        if (!mappingInfo.getColumnMappingMode().isNone()) {
            columnMappings = mappingInfo.getColumnMappings().stream().collect(
                    Collectors.toMap(MediaColumnMappingInfo::getSourceColumn, MediaColumnMappingInfo::getTargetColumn)
            );

            //如果配置了白名单，并且record中有扩展列，那么需要把扩展列加入到白名单中
            Set<String> extendCols = RecordMeta.extendColumns(record);
            if (mappingInfo.getColumnMappingMode().isInclude() && extendCols != null) {
                for (String c : extendCols) {
                    columnMappings.put(c, c);
                }
            }
        }

        //schema别名、表别名、列白名单、列黑名单、列别名转换
        transformSchemaAndTableName(record, mappingInfo);
        transformColumns(record, columnMappings, mappingInfo);

        //对于更新操作，经过黑白名单转换之后，发生更新操作的列可能已经都被过滤掉了，此时直接忽略该记录即可
        if (record.getEventType().equals(EventType.UPDATE) && record.getUpdatedColumns().isEmpty() && record.getOldKeys().isEmpty()) {
            return null;
        } else {
            return record;
        }
    }

    /**
     * 设置了schema别名或media-name别名的时候，需要进行名称转换
     */
    protected void transformSchemaAndTableName(RdbEventRecord record, MediaMappingInfo mappingInfo) {
        MediaSrcParameter targetMediaSrcPara = mappingInfo.getTargetMediaSource().getParameterObj();

        String schemaName = StringUtils.isEmpty(mappingInfo.getTargetMediaNamespace()) ?
                (targetMediaSrcPara instanceof RdbMediaSrcParameter ? targetMediaSrcPara.getNamespace() : record.getSchemaName())
                : mappingInfo.getTargetMediaNamespace();
        String tableName = StringUtils.isEmpty(mappingInfo.getTargetMediaName()) ?
                record.getTableName() : mappingInfo.getTargetMediaName();
        record.setSchemaName(schemaName);
        record.setTableName(tableName);
    }

    /**
     * 普通列转换,主要任务有2
     * 1.列名转换
     * 2.列过滤
     */
    protected void transformColumns(RdbEventRecord record, Map<String, String> columnMappings, MediaMappingInfo mappingInfo) {
        if (columnMappings.isEmpty() || mappingInfo.getColumnMappingMode().isNone()) {
            return;
        }

        record.setColumns(buildColumns(record.getColumns(), columnMappings, mappingInfo, false));
        record.setOldKeys(buildColumns(record.getOldKeys(), columnMappings, mappingInfo, true));
        record.setKeys(buildColumns(record.getKeys(), columnMappings, mappingInfo, true));
    }

    protected List<EventColumn> buildColumns(List<EventColumn> originalColumns, Map<String, String> columnMappings,
                                             MediaMappingInfo mappingInfo, boolean isKey) {
        if (mappingInfo.getColumnMappingMode().isInclude()) {
            List<EventColumn> list = new LinkedList<>();
            for (EventColumn column : originalColumns) {
                if (columnMappings.containsKey(column.getColumnName())) {
                    column.setColumnName(translateColumnName(column.getColumnName(), columnMappings));
                    list.add(column);
                } else {
                    if (isKey) {
                        throw new DatalinkException("Primary-key-column {" + column.getColumnName() + "} must be included in columnMappings, mapping-id is : " + mappingInfo.getId());
                    }
                }
            }
            return list;
        } else if (mappingInfo.getColumnMappingMode().isExclude()) {
            List<EventColumn> list = new LinkedList<>();
            for (EventColumn column : originalColumns) {
                if (!columnMappings.containsKey(column.getColumnName())) {
                    list.add(column);
                } else {
                    if (isKey) {
                        throw new DatalinkException("Primary-key-column {" + column.getColumnName() + "} can not be excluded in columnMappings, mapping-id is : " + mappingInfo.getId());
                    }
                }
            }
            return list;
        }

        throw new DatalinkException("invalid mapping mode.");
    }

    /**
     * 字段名字转化
     */
    protected String translateColumnName(String srcColumnName, Map<String, String> columnMappings) {
        if (columnMappings.isEmpty()) {
            return srcColumnName;
        } else {
            String targetColumnName = columnMappings.get(srcColumnName);
            if (StringUtils.isEmpty(targetColumnName)) {
                throw new DatalinkException(srcColumnName + " is not found in column pairs: " + columnMappings.toString());
            }
            return targetColumnName;
        }
    }
}
