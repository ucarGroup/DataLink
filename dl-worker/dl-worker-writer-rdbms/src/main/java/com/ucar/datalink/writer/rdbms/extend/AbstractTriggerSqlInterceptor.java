package com.ucar.datalink.writer.rdbms.extend;

import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lubiao on 2017/6/14.
 */
public abstract class AbstractTriggerSqlInterceptor implements Interceptor<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTriggerSqlInterceptor.class);
    private static final Pattern pattern = Pattern.compile("#[^#]*#");

    protected void executeSql(String sqlTemplate, RdbEventRecord record) {
        Map<String, EventColumn> columnMap = getColumnMap(record);
        List<Object> args = new ArrayList<>();
        List<Integer> argTypes = new ArrayList<>();

        Matcher matcher = pattern.matcher(sqlTemplate);
        while (matcher.find()) {
            String placeHolder = matcher.group();
            String columnName = StringUtils.replace(placeHolder, "#", "");
            sqlTemplate = StringUtils.replace(sqlTemplate, placeHolder, "?");

            EventColumn column = columnMap.get(columnName);
            if (column == null) {
                throw new ValidationException("Can not find corresponding column for " + placeHolder);
            }
            args.add(column.getColumnValue());
            argTypes.add(column.getColumnType());
        }

        try {
            loadOneSql(sqlTemplate, RecordMeta.mediaMapping(record), args, argTypes);
        } catch (Throwable t) {
            logger.error("Trigger Sql Execute Failed, Sql is {}, Record Content is {}.", sqlTemplate, record);
            throw t;
        }
    }

    private Map<String, EventColumn> getColumnMap(RdbEventRecord record) {
        Map<String, EventColumn> result = new HashMap<>();
        record.getColumns().stream().forEach(i -> result.put(i.getColumnName(), i));
        record.getKeys().stream().forEach(i -> result.put(i.getColumnName(), i));
        return result;
    }

    public void loadOneSql(String sql, MediaMappingInfo mappingInfo, List<Object> args, List<Integer> argTypes) {
        try {
            DbDialect dialect = DbDialectFactory.getDbDialect(mappingInfo.getTargetMediaSource());
            dialect.getTransactionTemplate().execute((transactionStatus -> {
                return dialect.getJdbcTemplate().update(sql, args.toArray(), argTypes.stream().mapToInt(Integer::intValue).toArray());
            }));
        } catch (DuplicateKeyException e) {
            logger.warn("DuplicateKeyException for record load :" + e.getMessage());
        }
    }
}
