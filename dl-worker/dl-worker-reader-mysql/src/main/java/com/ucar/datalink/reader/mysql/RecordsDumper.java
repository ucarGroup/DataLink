package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.reader.mysql.utils.Constants;
import com.ucar.datalink.worker.api.task.RecordChunk;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * dump记录
 *
 * @author lubiao
 */
public class RecordsDumper {
    private static final Logger logger = LoggerFactory.getLogger(RecordsDumper.class);

    private static final int LOG_SPLIT_SIZE = 50;
    private static final String SEP = SystemUtils.LINE_SEPARATOR;
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
    private static String context_format = null;
    private static String eventRecord_format = null;
    private static int event_default_capacity = 1024;                      // 预设值StringBuilder，减少扩容影响

    static {
        context_format = "* Batch Id: [{0}] ,total : [{1}] , normal : [{2}] , filter :[{3}] , Time : {4}" + SEP;
        context_format += "* Start : [{5}] " + SEP;
        context_format += "* End : [{6}] " + SEP;

        eventRecord_format = "-----------------" + SEP;
        eventRecord_format += "- Schema: {0} , Table: {1} " + SEP;
        eventRecord_format += "- Type: {2}  , ExecuteTime: {3} ," + SEP;
        eventRecord_format += "-----------------" + SEP;
        eventRecord_format += "---START" + SEP;
        eventRecord_format += "---Pks" + SEP;
        eventRecord_format += "{4}" + SEP;
        eventRecord_format += "---oldPks" + SEP;
        eventRecord_format += "{5}" + SEP;
        eventRecord_format += "---Columns" + SEP;
        eventRecord_format += "{6}" + SEP;
        eventRecord_format += "---END" + SEP;

    }

    public static void dumpRecords(RecordChunk<RdbEventRecord> recordChunk, String startPosition, String endPosition, int total, boolean isDumpDetail) {
        logger.info(SEP + "****************************************************" + SEP);
        logger.info(RecordsDumper.dumpMessageInfo(recordChunk, startPosition, endPosition, total));
        logger.info(SEP + "****************************************************" + SEP);
        if (isDumpDetail) {// 判断一下是否需要打印详细信息
            dumpEventRecords(recordChunk.getRecords());
            logger.info(SEP + "****************************************************" + SEP);
        }
    }

    public static void dumpEventRecords(List<RdbEventRecord> eventDatas) {
        int size = eventDatas.size();
        int index = 0;
        do {
            if (index + LOG_SPLIT_SIZE >= size) {
                logger.info(buildOutputStr(eventDatas.subList(index, size)));
            } else {
                logger.info(buildOutputStr(eventDatas.subList(index, index + LOG_SPLIT_SIZE)));
            }
            index += LOG_SPLIT_SIZE;
        } while (index < size);
    }

    public static String buildPositionForDump(CanalEntry.Entry entry) {
        long time = entry.getHeader().getExecuteTime();
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT);
        return entry.getHeader().getLogfileName() + ":" + entry.getHeader().getLogfileOffset() + ":"
                + entry.getHeader().getExecuteTime() + "(" + format.format(date) + ")";
    }

    public static String dumpMessageInfo(RecordChunk<RdbEventRecord> recordChunk, String startPosition, String endPosition, int total) {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP_FORMAT);
        int normal = recordChunk.getRecords().size();
        return MessageFormat.format(context_format, recordChunk.getMetaData(Constants.BATCH_ID), total, normal, (total - normal),
                format.format(now), startPosition, endPosition);
    }

    public static String buildOutputStr(List<RdbEventRecord> eventDatas) {
        if (CollectionUtils.isEmpty(eventDatas)) {
            return StringUtils.EMPTY;
        }

        // 预先设定容量大小
        StringBuilder builder = new StringBuilder(event_default_capacity * eventDatas.size());
        for (RdbEventRecord data : eventDatas) {
            builder.append(dumpEventRecord(data));
        }
        return builder.toString();
    }

    public static String dumpEventRecord(RdbEventRecord eventData) {
        return MessageFormat.format(eventRecord_format,
                eventData.getSchemaName(),
                eventData.getTableName(),
                eventData.getEventType().getValue(),
                String.valueOf(eventData.getExecuteTime()),
                dumpEventColumn(eventData.getKeys()),
                dumpEventColumn(eventData.getOldKeys()),
                dumpEventColumn(eventData.getColumns()),
                "\t" + eventData.getSql()
        );
    }

    private static String dumpEventColumn(List<EventColumn> columns) {
        StringBuilder builder = new StringBuilder(event_default_capacity);
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            EventColumn column = columns.get(i);
            builder.append("\t").append(column.toString());
            if (i < columns.size() - 1) {
                builder.append(SEP);
            }
        }
        return builder.toString();
    }
}
