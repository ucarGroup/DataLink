package com.ucar.datalink.reader.mysql;

import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.contract.log.rdbms.EventColumnIndexComparable;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderParameter;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.reader.mysql.utils.StatisticKey;
import com.ucar.datalink.worker.api.task.TaskReaderContext;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import com.ucar.datalink.worker.api.util.statistic.ReaderStatistic;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;

import java.util.*;

/**
 * 数据对象解析
 * 参考自Aalibaba-otter的 -> com.alibaba.otter.node.etl.select.selector.MessageParser
 *
 * @author lubiao
 */
public class MessageParser {

    private static final Logger logger = LoggerFactory.getLogger(MessageParser.class);

    private TaskReaderContext context;

    private MysqlReaderParameter readerParameter;


    public MessageParser(TaskReaderContext context, MysqlReaderParameter readerParameter) {
        this.context = context;
        this.readerParameter = readerParameter;
    }

    /**
     * 将对应canal送出来的Entry对象解析为Contract包中定义的RdbEventRecord对象
     * <p>
     * <pre>
     * 需要处理数据过滤：
     * 1. Transaction Begin/End过滤
     * 2. retl.retl_client/retl.retl_mark 回环标记处理以及后续的回环数据过滤
     * 3. retl.xdual canal心跳表数据过滤
     * </pre>
     */
    public List<RdbEventRecord> parse(List<Entry> entryList, TaskReaderContext taskReaderContext) {
        //statistic before
        ReaderStatistic readerStatistic = taskReaderContext.taskReaderSession().getData(ReaderStatistic.KEY);
        long startTime = System.currentTimeMillis();

        //do parse
        List<RdbEventRecord> eventRecords = new ArrayList<>();
        List<Entry> transactionDataBuffer = new ArrayList<>();

        boolean isLoopback = false;
        try {
            for (Entry entry : entryList) {
                switch (entry.getEntryType()) {
                    case TRANSACTIONBEGIN:
                        isLoopback = false;
                        break;
                    case ROWDATA:
                        String tableName = entry.getHeader().getTableName();
                        // 判断是否是回环表retl_mark
                        boolean isMarkTable = tableName.equalsIgnoreCase("retl");//TODO,支持双向同步时进行改造，从参数获取回环表名
                        if (isMarkTable) {
                            RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
                            if (!rowChange.getIsDdl()) {
                                int loopback = checkLoopback(rowChange.getRowDatas(0));
                                isLoopback |= loopback > 0;
                            }
                        }

                        if (!isLoopback && !isMarkTable) {
                            transactionDataBuffer.add(entry);
                        }
                        break;
                    case TRANSACTIONEND:
                        if (!isLoopback) {
                            // 添加数据解析
                            for (Entry bufferEntry : transactionDataBuffer) {
                                List<RdbEventRecord> parsedRecords = internParse(bufferEntry);
                                if (CollectionUtils.isEmpty(parsedRecords)) {// 可能为空，如：ddl类型的事件
                                    continue;
                                }

                                for (RdbEventRecord eventData : parsedRecords) {
                                    if (eventData == null) {
                                        continue;
                                    }
                                    eventRecords.add(eventData);
                                }
                            }
                        }

                        isLoopback = false;
                        transactionDataBuffer.clear();
                        break;
                    default:
                        break;
                }
            }

            // 添加最后一次的数据,可能没有TRANSACTIONEND
            if (!isLoopback) {
                // 添加数据解析
                for (Entry bufferEntry : transactionDataBuffer) {
                    List<RdbEventRecord> parseDatas = internParse(bufferEntry);
                    if (CollectionUtils.isEmpty(parseDatas)) {// 可能为空，如：ddl类型的事件
                        continue;
                    }

                    for (RdbEventRecord eventData : parseDatas) {
                        if (eventData == null) {
                            continue;
                        }
                        eventRecords.add(eventData);
                    }
                }
            }
        } catch (Exception e) {
            throw new DatalinkException("Message Parse Failed.", e);
        }

        checkDdlRecords(eventRecords);

        //statistic after
        readerStatistic.getExtendStatistic().put(StatisticKey.PARSE_MESSAGE_TIME_THROUGH, System.currentTimeMillis() - startTime);
        readerStatistic.getExtendStatistic().put(StatisticKey.PARSE_MESSAGE_RECORDS_COUNT, eventRecords.size());

        return eventRecords;
    }


    private int checkLoopback(RowData rowData) {
        return 0;//TODO 回环数据判断,支持双向同步时进行改造
    }

    private boolean checkDdlRecords(List<RdbEventRecord> records) {
        boolean result = false;
        for (RdbEventRecord record : records) {
            result |= record.getEventType().isDdl();
            if (result && !record.getEventType().isDdl()) {
                throw new DatalinkException("ddl/dml can't be in one batch, it's may be a bug , pls submit issues.\r\n" +
                        RecordsDumper.buildOutputStr(records));
            }
        }

        return result;
    }

    private List<RdbEventRecord> internParse(Entry entry) {
        RowChange rowChange;
        try {
            rowChange = RowChange.parseFrom(entry.getStoreValue());
        } catch (Exception e) {
            throw new DatalinkException("parser of mysql-event has an error , data:" + entry.toString(), e);
        }

        if (rowChange == null) {
            return null;
        }

        EventType eventType = EventType.valueOf(rowChange.getEventType().name());
        if (eventType.isQuery()) {
            return null;
        }

        if (eventType.isDdl()) {
            logger.info("find a ddl event in MessageParser , ddl sql is {}.", rowChange.getSql());

            MediaService mediaService = DataLinkFactory.getObject(MediaService.class);
            MediaSourceInfo mediaSourceInfo = mediaService.getMediaSourceById(readerParameter.getMediaSourceId());
            DbDialect dbDialect = DbDialectFactory.getDbDialect(mediaSourceInfo);
            dbDialect.reloadTable(entry.getHeader().getSchemaName(), entry.getHeader().getTableName());

            if (readerParameter.isDdlSync()) {
                logger.info("The value of the ddl-sync is true , prepare to parse ddl event.");

                RdbEventRecord record = new RdbEventRecord();
                record.setSchemaName(entry.getHeader().getSchemaName());
                record.setTableName(entry.getHeader().getTableName());
                record.setEventType(eventType);
                record.setExecuteTime(entry.getHeader().getExecuteTime());
                record.setSql(rowChange.getSql());
                record.setDdlSchemaName(rowChange.getDdlSchemaName());
                return Arrays.asList(record);
            } else {
                logger.info("The value of the ddl-sync is false , the ddl event is ignored.");
                return null;
            }
        }


        List<RdbEventRecord> eventDatas = new ArrayList<>();
        for (RowData rowData : rowChange.getRowDatasList()) {
            RdbEventRecord eventData = internParse(entry, rowChange, rowData);
            if (eventData != null) {
                eventDatas.add(eventData);
            }
        }

        return eventDatas;
    }


    /**
     * 解析出从canal中获取的Event事件<br>
     * Oracle:有变更的列值. <br>
     * <i>insert:从afterColumns中获取所有的变更数据<br>
     * <i>delete:从beforeColumns中获取所有的变更数据<br>
     * <i>update:在before中存放所有的主键和变化前的非主键值，在after中存放变化后的主键和非主键值,如果是复合主键，只会存放变化的主键<br>
     * Mysql:可以得到所有变更前和变更后的数据.<br>
     * <i>insert:从afterColumns中获取所有的变更数据<br>
     * <i>delete:从beforeColumns中获取所有的变更数据<br>
     * <i>update:在beforeColumns中存放变更前的所有数据,在afterColumns中存放变更后的所有数据<br>
     */
    private RdbEventRecord internParse(Entry entry, RowChange rowChange, RowData rowData) {
        RdbEventRecord eventRecord = new RdbEventRecord();
        eventRecord.setTableName(entry.getHeader().getTableName());
        eventRecord.setSchemaName(entry.getHeader().getSchemaName());
        eventRecord.setEventType(EventType.valueOf(rowChange.getEventType().name()));
        eventRecord.setExecuteTime(entry.getHeader().getExecuteTime());
        EventType eventType = eventRecord.getEventType();

        List<Column> beforeColumns = rowData.getBeforeColumnsList();
        List<Column> afterColumns = rowData.getAfterColumnsList();

        // 变更后的主键
        Map<String, EventColumn> keyColumns = new LinkedHashMap<>();
        // 变更前的主键
        Map<String, EventColumn> oldKeyColumns = new LinkedHashMap<>();
        // 变更后的非主键
        Map<String, EventColumn> notKeyColumns = new LinkedHashMap<>();
        // 变更前的非主键
        Map<String, EventColumn> notKeyOldColunms = new LinkedHashMap<>();

        if (eventType.isInsert()) {
            for (Column column : afterColumns) {
                if (column.getIsKey()) {
                    keyColumns.put(column.getName(), copyEventColumn(column));
                } else {
                    // mysql 有效
                    notKeyColumns.put(column.getName(), copyEventColumn(column));
                }
            }
        } else if (eventType.isDelete()) {
            for (Column column : beforeColumns) {
                if (column.getIsKey()) {
                    keyColumns.put(column.getName(), copyEventColumn(column));
                } else {
                    // mysql 有效
                    notKeyColumns.put(column.getName(), copyEventColumn(column));
                }
            }
        } else if (eventType.isUpdate()) {
            // 获取变更前的主键.
            for (Column column : beforeColumns) {
                if (column.getIsKey()) {
                    oldKeyColumns.put(column.getName(), copyEventColumn(column));
                    // 同时记录一下new
                    // key,因为mysql5.6之后出现了minimal模式,after里会没有主键信息,需要在before记录中找
                    keyColumns.put(column.getName(), copyEventColumn(column));
                } else {
                    notKeyOldColunms.put(column.getName(), copyEventColumn(column));
                }
            }
            for (Column column : afterColumns) {
                if (column.getIsKey()) {
                    // 获取变更后的主键
                    keyColumns.put(column.getName(), copyEventColumn(column));
                } else {
                    notKeyColumns.put(column.getName(), copyEventColumn(column));
                }
            }
        }

        List<EventColumn> keys = new ArrayList<>(keyColumns.values());
        List<EventColumn> oldKeys = new ArrayList<>(oldKeyColumns.values());
        List<EventColumn> columns = new ArrayList<>(notKeyColumns.values());
        List<EventColumn> oldColumns = new ArrayList<>(notKeyOldColunms.values());

        Collections.sort(keys, new EventColumnIndexComparable());
        Collections.sort(oldKeys, new EventColumnIndexComparable());
        Collections.sort(columns, new EventColumnIndexComparable());
        Collections.sort(oldColumns, new EventColumnIndexComparable());
        if (!keyColumns.isEmpty()) {
            eventRecord.setKeys(keys);
            if (eventRecord.getEventType().isUpdate() && !oldKeys.equals(keys)) { // update类型，如果存在主键不同,则记录下old
                eventRecord.setOldKeys(oldKeys);
            }
            eventRecord.setColumns(columns);
            eventRecord.setOldColumns(oldColumns);
        } else {
            logger.error("this rowdata has no pks , entry: " + entry.toString() + " and rowData: "
                    + rowData);
            return null;
        }

        return eventRecord;
    }

    /**
     * Transfer Canal-Column to Datalink`s Event-Column
     */
    private EventColumn copyEventColumn(Column column) {
        EventColumn eventColumn = new EventColumn();
        eventColumn.setIndex(column.getIndex());
        eventColumn.setKey(column.getIsKey());
        eventColumn.setNull(column.getIsNull());
        eventColumn.setColumnName(column.getName());
        eventColumn.setColumnValue(column.getValue());
        eventColumn.setUpdate(column.getUpdated());
        eventColumn.setColumnType(column.getSqlType());

        return eventColumn;
    }
}
