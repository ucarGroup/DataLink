package com.ucar.datalink.writer.hdfs.handle.util;

import com.ucar.datalink.domain.plugin.writer.hdfs.*;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.task.TaskWriterContext;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sqq on 2017/7/17.
 */
public class HdfsFilePathGenerator {
    private static final String FILE_PATH_BINLOG_PATTERN_DAY = "/{0}/{1}/{2}/{3}.txt";// 0-MysqlBinlog路径，1-库名，2-表名，3文件名
    private static final String FILE_PATH_BINLOG_PATTERN_DAY_SPLIT = "/{0}/{1}/{2}/{3}/{4}.txt";// 0-MysqlBinlog路径，1-库名，2-表名，3-日期，4-文件名
    private static final String FILE_PATH_HBASE_PATTERN_DAY = "/{0}/{1}/{2}/{3}.txt";//0-Hbase路径，1-表名，2-日期，3-文件名
    private static final String FILE_PATH_HBASE_PATTERN_DAY_SPLIT = "/{0}/{1}/{2}/{3}/{4}.txt";//0-Hbase路径，1-表名，2-日期，3-时间，4-文件名
    private static final String FILE_PATH_HBASE_NAMESPACE_PATTERN_DAY = "/{0}/{1}/{2}/{3}/{4}.txt";//0-Hbase路径，1-库名，2-表名，3-日期，4-文件名
    private static final String FILE_PATH_HBASE_NAMESPACE_PATTERN_DAY_SPLIT = "/{0}/{1}/{2}/{3}/{4}/{5}.txt";//0-Hbase路径，1-库名，2-表名，3-日期，4-时间，5-文件名
    private static final String HDFS_DATE_PATTERN = "yyyy-MM-dd";


    private TaskWriterContext taskWriterContext;
    private HdfsWriterParameter hdfsWriterParameter;

    public HdfsFilePathGenerator(TaskWriterContext taskWriterContext, HdfsWriterParameter hdfsWriterParameter) {
        this.taskWriterContext = taskWriterContext;
        this.hdfsWriterParameter = hdfsWriterParameter;
    }

    public String getHdfsFilePath(String schemaName, String tableName, MediaMappingInfo mappingInfo, String dbType, String transferDataType) {
        if (Dict.HDFS_DB_TYPE_BINLOG.equals(dbType)) {
            return getBinlogFilePath(schemaName, tableName, mappingInfo, transferDataType);
        } else if (Dict.HDFS_DB_TYPE_HBASE.equals(dbType)) {
            return getHbaseFilePath(schemaName, tableName, mappingInfo);
        } else {
            throw new RuntimeException(MessageFormat.format("invalid dbType:{0}", dbType));
        }
    }

    private String getBinlogFilePath(String schemaName, String tableName, MediaMappingInfo mappingInfo, String transferDataType) {
        FileSplitMode splitMode = getFileSplitMode(mappingInfo);
        String filename;
        if (Dict.HDFS_TRANSFER_DATA_TYPE_NORMAL.equals(transferDataType)) {
            filename = tableName + "-" + buildSplitRange(splitMode);
        } else if (Dict.HDFS_TRANSFER_DATA_TYPE_DELETE.equals(transferDataType)) {
            filename = tableName + "-delete-" + buildSplitRange(splitMode);
        } else {
            throw new RuntimeException("invalid transferDataType: " + transferDataType);
        }

        if (splitMode.equals(FileSplitMode.DAY)) {
            return MessageFormat.format(FILE_PATH_BINLOG_PATTERN_DAY,hdfsWriterParameter.getMysqlBinlogPath(), schemaName, tableName, filename);
        } else {
            return MessageFormat.format(FILE_PATH_BINLOG_PATTERN_DAY_SPLIT, hdfsWriterParameter.getMysqlBinlogPath(), schemaName, tableName, getDateStr(), filename);
        }
    }

    private String getHbaseFilePath(String schemaName, String tableName, MediaMappingInfo mappingInfo) {
        FileSplitMode splitMode = getFileSplitMode(mappingInfo);

        String dateStr = getDateStr();
        String taskFileName = tableName + "-" + taskWriterContext.taskId();

        if ("default".equals(schemaName)) {
            if (splitMode.equals(FileSplitMode.DAY)) {
                return MessageFormat.format(FILE_PATH_HBASE_PATTERN_DAY,hdfsWriterParameter.getHbasePath(), tableName, dateStr, taskFileName);
            } else {
                return MessageFormat.format(FILE_PATH_HBASE_PATTERN_DAY_SPLIT,hdfsWriterParameter.getHbasePath(), tableName, dateStr, buildSplitRange(splitMode), taskFileName);
            }
        } else {
            if (splitMode.equals(FileSplitMode.DAY)) {
                return MessageFormat.format(FILE_PATH_HBASE_NAMESPACE_PATTERN_DAY, hdfsWriterParameter.getHbasePath(),schemaName, tableName, dateStr, taskFileName);
            } else {
                return MessageFormat.format(FILE_PATH_HBASE_NAMESPACE_PATTERN_DAY_SPLIT, hdfsWriterParameter.getHbasePath(),schemaName, tableName, dateStr, buildSplitRange(splitMode), taskFileName);
            }
        }
    }

    private String buildSplitRange(FileSplitMode splitMode) {
        if (splitMode.equals(FileSplitMode.DAY)) {
            return getDateStr();
        } else if (splitMode.equals(FileSplitMode.HOUR)) {
            return getHourStr();
        } else if (splitMode.equals(FileSplitMode.HALFHOUR)) {
            return getHalfHourStr();
        } else {
            throw new RuntimeException("invalid splitMode: " + splitMode);
        }
    }

    private String getDateStr() {
        Date now = new Date();
        now.setTime(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(HDFS_DATE_PATTERN);
        return sdf.format(now);
    }

    private String getHourStr() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        return formatedHour(cal) + "-00";

    }

    private String getHalfHourStr() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        String hour = formatedHour(cal);
        int minute = cal.get(Calendar.MINUTE);
        if (minute < 30) {
            return hour + "-00";
        } else {
            return hour + "-30";
        }
    }

    private String formatedHour(Calendar cal) {
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            return "0" + hour;
        } else {
            return String.valueOf(hour);
        }
    }

    public FileSplitMode getFileSplitMode(MediaMappingInfo mappingInfo) {
        HdfsFileParameter hdfsFileParameter = mappingInfo.getParameterObj();
        if (hdfsFileParameter != null) {
            List<FileSplitStrategy> fileSplitStrategieList = hdfsFileParameter.getFileSplitStrategieList();
            if (fileSplitStrategieList != null && fileSplitStrategieList.size() > 0) {
                Date current = new Date();
                TreeMap<Date, FileSplitMode> treeMap = new TreeMap<>();
                for (FileSplitStrategy fileSplitStrategy : fileSplitStrategieList) {
                    treeMap.put(fileSplitStrategy.getEffectiveDate(), fileSplitStrategy.getFileSplitMode());
                }
                Date greatest = treeMap.floorKey(current);
                if (greatest != null) {
                    return treeMap.get(greatest);
                } else {
                    return FileSplitMode.DAY;
                }
            } else {
                return FileSplitMode.DAY;
            }
        } else {
            return FileSplitMode.DAY;// 没有明确配置的话，一天一个文件
        }
    }
}
