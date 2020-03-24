package com.ucar.datalink.writer.rdbms.utils;

import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by sqq on 2017/9/13.
 */
public class TableCheckUtils {

    private static final Logger logger = LoggerFactory.getLogger(TableCheckUtils.class);

    public static Boolean isRowNumLimit(RdbEventRecord record, String columnNameInfo) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        MediaSourceInfo targetMediaSource = mappingInfo.getTargetMediaSource();
        DbDialect dbDialect = DbDialectFactory.getDbDialect(targetMediaSource);
        String targetTableName = StringUtils.isEmpty(mappingInfo.getTargetMediaName()) ? record.getTableName() : mappingInfo.getTargetMediaName();

        String sql = String.format("SHOW TABLE STATUS WHERE NAME LIKE '%s'", targetTableName);
        logger.info("show table status sql is :" + sql);
        Map<String, Object> tableResult = dbDialect.getJdbcTemplate().queryForMap(sql);
        Long rowNum = Long.valueOf(tableResult.get("Rows").toString());
        if (rowNum > 5000000L) {
            logger.info("the row count of table {} is {}, please handle the ddl sql manually.", targetTableName, rowNum);
            return true;
        }
        return false;
    }
}
