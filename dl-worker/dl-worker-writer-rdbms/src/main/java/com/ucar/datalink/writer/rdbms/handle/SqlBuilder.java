package com.ucar.datalink.writer.rdbms.handle;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import com.ucar.datalink.worker.api.util.dialect.SqlTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class SqlBuilder {

    public static void buildSql(RdbEventRecord record) {
        // 初步构建sql
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);

        DbDialect dbDialect = DbDialectFactory.getDbDialect(mappingInfo.getTargetMediaSource());
        SqlTemplate sqlTemplate = dbDialect.getSqlTemplate();
        EventType type = record.getEventType();
        String sql = null;

        String schemaName = dbDialect.isGenerateSqlWithSchema() ? record.getSchemaName() : null;
        // 注意insert/update语句对应的字段数序都是将主键排在后面
        if (type.isInsert()) {
            if(dbDialect.isSupportMergeSql()){
                sql = sqlTemplate.getMergeSql(schemaName,
                        record.getTableName(),
                        buildColumnNames(record.getKeys()),
                        buildColumnNames(record.getColumns()),
                        new String[]{});
            }else{
                sql = sqlTemplate.getInsertSql(schemaName,
                        record.getTableName(),
                        buildColumnNames(record.getKeys()),
                        buildColumnNames(record.getColumns()));
            }
        } else if (type.isUpdate()) {
            boolean existOldKeys = !CollectionUtils.isEmpty(record.getOldKeys());
            String[] keyColumns;
            String[] otherColumns;
            if (existOldKeys) {
                // 需要考虑主键变更的场景
                // 构造sql如下：update table xxx set pk = newPK where pk = oldPk
                keyColumns = buildColumnNames(record.getOldKeys());
                otherColumns = buildColumnNames(record.getUpdatedColumns(), record.getKeys());
            } else {
                keyColumns = buildColumnNames(record.getKeys());
                otherColumns = buildColumnNames(record.getUpdatedColumns());
            }

            sql = sqlTemplate.getUpdateSql(schemaName, record.getTableName(), keyColumns, otherColumns);

        } else if (type.isDelete()) {
            sql = sqlTemplate.getDeleteSql(schemaName,
                    record.getTableName(),
                    buildColumnNames(record.getKeys()));
        }

        // 处理下hint sql
        if (record.getHint() != null) {
            record.setSql(record.getHint() + sql);
        } else {
            record.setSql(sql);
        }
    }

    private static String[] buildColumnNames(List<EventColumn> columns) {
        String[] result = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            EventColumn column = columns.get(i);
            result[i] = column.getColumnName();
        }
        return result;
    }

    private static String[] buildColumnNames(List<EventColumn> columns1, List<EventColumn> columns2) {
        String[] result = new String[columns1.size() + columns2.size()];
        int i = 0;
        for (i = 0; i < columns1.size(); i++) {
            EventColumn column = columns1.get(i);
            result[i] = column.getColumnName();
        }

        for (; i < columns1.size() + columns2.size(); i++) {
            EventColumn column = columns2.get(i - columns1.size());
            result[i] = column.getColumnName();
        }
        return result;
    }
}
