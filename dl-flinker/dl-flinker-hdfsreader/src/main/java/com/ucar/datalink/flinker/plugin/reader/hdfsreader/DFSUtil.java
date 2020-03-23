package com.ucar.datalink.flinker.plugin.reader.hdfsreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.element.*;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordSender;
import com.ucar.datalink.flinker.api.plugin.TaskPluginCollector;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.UnstructuredStorageReaderErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hive.ql.io.orc.*;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.RecordReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;

/**
 * Created by mingya.wmy on 2015/8/12.
 */
public class DFSUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HdfsReader.Job.class);

    private org.apache.hadoop.conf.Configuration hadoopConf = null;

    private static final int DIRECTORY_SIZE_GUESS = 16 * 1024;

    private String specifiedFileType = null;

    public DFSUtil(Configuration taskConfig) {
        hadoopConf = new org.apache.hadoop.conf.Configuration();
        Configuration hadoopSiteParams = taskConfig.getConfiguration(Key.HADOOP_CONFIG);
        if(null != hadoopSiteParams) {
            JSONObject hadoopSiteParamsAsJsonObject = JSON.parseObject(taskConfig.getString("hadoopConfig"));
            Set paramKeys = hadoopSiteParams.getKeys();
            Iterator iter = paramKeys.iterator();
            while(iter.hasNext()) {
                String key = (String)iter.next();
                this.hadoopConf.set(key, hadoopSiteParamsAsJsonObject.getString(key));
            }
        }
        this.hadoopConf.set("fs.defaultFS", taskConfig.getString("defaultFS"));
        //hadoopConf.set("fs.defaultFS", defaultFS);
    }


    /**
     * @param @param  srcPaths 路径列表
     * @param @return
     * @return HashSet<String>
     * @throws
     * @Title: getAllFiles
     * @Description: 获取指定路径列表下符合条件的所有文件的绝对路径
     */
    public HashSet<String> getAllFiles(List<String> srcPaths, String specifiedFileType, boolean isIgnoreExcept) {

        this.specifiedFileType = specifiedFileType;

        if (!srcPaths.isEmpty()) {
            for (String eachPath : srcPaths) {
                getHDFSAllFiles(eachPath,isIgnoreExcept);
            }
        }
        return sourceHDFSAllFilesList;
    }

    private HashSet<String> sourceHDFSAllFilesList = new HashSet<String>();

    public HashSet<String> getHDFSAllFiles(String hdfsPath, boolean isIgnoreExcept) {

        try {
            FileSystem hdfs = FileSystem.get(hadoopConf);
            //判断hdfsPath是否包含正则符号
            if (hdfsPath.contains("*") || hdfsPath.contains("?")) {
                Path path = new Path(hdfsPath);
                FileStatus stats[] = hdfs.globStatus(path);
                for (FileStatus f : stats) {
                    if (f.isFile()) {

                        addSourceFileByType(f.getPath().toString());
                    } else if (f.isDirectory()) {
                        getHDFSALLFiles_NO_Regex(f.getPath().toString(), hdfs);
                    }
                }
            } else {
                getHDFSALLFiles_NO_Regex(hdfsPath, hdfs);
            }

            return sourceHDFSAllFilesList;

        } catch (IOException e) {
            String message = String.format("无法读取路径[%s]下的所有文件,请确认您的配置项path是否正确，" +
                    "是否有读写权限，网络是否已断开！", hdfsPath);
            LOG.error(message);
            ErrorRecord.addError(message);
            if( !isIgnoreExcept ) {
                throw DataXException.asDataXException(HdfsReaderErrorCode.PATH_CONFIG_ERROR, message);
            }
            return sourceHDFSAllFilesList;
        }
    }

    private HashSet<String> getHDFSALLFiles_NO_Regex(String path, FileSystem hdfs) throws IOException {

        // 获取要读取的文件的根目录
        Path listFiles = new Path(path);

        // If the network disconnected, this method will retry 45 times
        // each time the retry interval for 20 seconds
        // 获取要读取的文件的根目录的所有二级子文件目录
        FileStatus stats[] = hdfs.listStatus(listFiles);

        for (FileStatus f : stats) {
            // 判断是不是目录，如果是目录，递归调用
            if (f.isDirectory()) {
                getHDFSALLFiles_NO_Regex(f.getPath().toString(), hdfs);
            } else if (f.isFile()) {

                addSourceFileByType(f.getPath().toString());
            } else {
                String message = String.format("该路径[%s]文件类型既不是目录也不是文件，插件自动忽略。"
                        , f.getPath().toString());
                LOG.info(message);
            }
        }
        return sourceHDFSAllFilesList;
    }

    // 根据用户指定的文件类型，将指定的文件类型的路径加入sourceHDFSAllFilesList
    private void addSourceFileByType(String filePath) {
        HdfsFileType type = checkHdfsFileType(filePath);

        if (type.toString().contains(specifiedFileType.toUpperCase())) {
            sourceHDFSAllFilesList.add(filePath);
        } else {
            String message = String.format("文件[%s]的类型与用户配置的fileType类型不一致，" +
                    "请确认您配置的目录下面所有文件的类型均为[%s]"
                    , filePath, this.specifiedFileType);
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(
                    HdfsReaderErrorCode.FILE_TYPE_UNSUPPORT, message);
        }
    }


    public InputStream getInputStream(String filepath) {
        InputStream inputStream = null;
        Path path = new Path(filepath);
        try {
            FileSystem fs = FileSystem.get(hadoopConf);
            inputStream = fs.open(path);
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BufferedReader getBufferedReader(String filepath, HdfsFileType fileType, String encoding) {
        try {
            FileSystem fs = FileSystem.get(hadoopConf);
            Path path = new Path(filepath);
            FSDataInputStream in = null;

            CompressionInputStream cin = null;
            BufferedReader br = null;

            if (fileType.equals(HdfsFileType.COMPRESSED_TEXT)) {
                CompressionCodecFactory factory = new CompressionCodecFactory(hadoopConf);
                CompressionCodec codec = factory.getCodec(path);
                if (codec == null) {
                    String message = String.format(
                            "Can't find any suitable CompressionCodec to this file:%value",
                            path.toString());
                    throw DataXException.asDataXException(HdfsReaderErrorCode.CONFIG_INVALID_EXCEPTION, message);
                }
                //If the network disconnected, this method will retry 45 times
                //each time the retry interval for 20 seconds
                in = fs.open(path);
                cin = codec.createInputStream(in);
                br = new BufferedReader(new InputStreamReader(cin, encoding));
            } else {
                //If the network disconnected, this method will retry 45 times
                // each time the retry interval for 20 seconds
                in = fs.open(path);
                br = new BufferedReader(new InputStreamReader(in, encoding));
            }
            return br;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void orcFileStartRead(String sourceOrcFilePath, Configuration readerSliceConfig,
                                 RecordSender recordSender, TaskPluginCollector taskPluginCollector) {

        List<Configuration> columnConfigs = readerSliceConfig.getListConfiguration(Key.COLUMN);
        String nullFormat = readerSliceConfig.getString(Key.NULL_FORMAT);
        String allColumns = "";
        String allColumnTypes = "";
        boolean isReadAllColumns = false;
        int columnIndexMax = -1;
        // 判断是否读取所有列
        if (null == columnConfigs || columnConfigs.size() == 0) {
            int allColumnsCount = getAllColumnsCount(sourceOrcFilePath);
            columnIndexMax = allColumnsCount - 1;
            isReadAllColumns = true;
        } else {
            columnIndexMax = getMaxIndex(columnConfigs);
        }
        for (int i = 0; i <= columnIndexMax; i++) {
            allColumns += "col";
            allColumnTypes += "string";
            if (i != columnIndexMax) {
                allColumns += ",";
                allColumnTypes += ":";
            }
        }
        if (columnIndexMax >= 0) {
            JobConf conf = new JobConf(hadoopConf);
            Path orcFilePath = new Path(sourceOrcFilePath);
            Properties p = new Properties();
            p.setProperty("columns", allColumns);
            p.setProperty("columns.types", allColumnTypes);
            try {
                OrcSerde serde = new OrcSerde();
                serde.initialize(conf, p);
                StructObjectInspector inspector = (StructObjectInspector) serde.getObjectInspector();
                InputFormat<?, ?> in = new OrcInputFormat();
                FileInputFormat.setInputPaths(conf, orcFilePath.toString());

                //If the network disconnected, will retry 45 times, each time the retry interval for 20 seconds
                //Each file as a split
                InputSplit[] splits = in.getSplits(conf, 1);

                RecordReader reader = in.getRecordReader(splits[0], conf, Reporter.NULL);
                Object key = reader.createKey();
                Object value = reader.createValue();
                // 获取列信息
                List<? extends StructField> fields = inspector.getAllStructFieldRefs();

                List<Object> recordFields = null;
                while (reader.next(key, value)) {
                    recordFields = new ArrayList<Object>();

                    for (int i = 0; i <= columnIndexMax; i++) {
                        Object field = inspector.getStructFieldData(value, fields.get(i));
                        recordFields.add(field);
                    }
                    transportOneRecord(columnConfigs, recordFields, recordSender,
                            taskPluginCollector, isReadAllColumns, nullFormat);
                }
                reader.close();
            } catch (Exception e) {
                String message = String.format("从orcfile文件路径[%s]中读取数据发生异常，请联系系统管理员。"
                        , sourceOrcFilePath);
                LOG.error(message);
                LOG.error(e.getMessage(),e);
                ErrorRecord.addError(message);
                ErrorRecord.addError(e);
                throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, message);
            }
        } else {
            String message = String.format("请确认您所读取的列配置正确！");
            LOG.error(message);
            throw DataXException.asDataXException(HdfsReaderErrorCode.BAD_CONFIG_VALUE, message);
        }
    }

    private Record transportOneRecord(List<Configuration> columnConfigs, List<Object> recordFields
            , RecordSender recordSender, TaskPluginCollector taskPluginCollector, boolean isReadAllColumns, String nullFormat) {
        Record record = recordSender.createRecord();
        Column columnGenerated = null;
        try {
            if (isReadAllColumns) {
                // 读取所有列，创建都为String类型的column
                for (Object recordField : recordFields) {
                    String columnValue = null;
                    if (recordField != null) {
                        columnValue = recordField.toString();
                    }
                    columnGenerated = new StringColumn(columnValue);
                    record.addColumn(columnGenerated);
                }
            } else {
                for (Configuration columnConfig : columnConfigs) {
                    String columnType = columnConfig
                            .getNecessaryValue(Key.TYPE, HdfsReaderErrorCode.CONFIG_INVALID_EXCEPTION);
                    Integer columnIndex = columnConfig.getInt(Key.INDEX);
                    String columnConst = columnConfig.getString(Key.VALUE);

                    String columnValue = null;

                    if (null != columnIndex) {
                        if (null != recordFields.get(columnIndex)) {
                            columnValue = recordFields.get(columnIndex).toString();
                        }
                    } else {
                        columnValue = columnConst;
                    }
                    Type type = Type.valueOf(columnType.toUpperCase());
                    // it's all ok if nullFormat is null
                    if (columnValue != null && columnValue.equals(nullFormat)) {
                        columnValue = null;
                    }
                    switch (type) {
                        case STRING:
                            columnGenerated = new StringColumn(columnValue);
                            break;
                        case LONG:
                            try {
                                columnGenerated = new LongColumn(columnValue);
                            } catch (Exception e) {
                                throw new IllegalArgumentException(String.format(
                                        "类型转换错误, 无法将[%s] 转换为[%s]", columnValue,
                                        "LONG"));
                            }
                            break;
                        case DOUBLE:
                            try {
                                columnGenerated = new DoubleColumn(columnValue);
                            } catch (Exception e) {
                                throw new IllegalArgumentException(String.format(
                                        "类型转换错误, 无法将[%s] 转换为[%s]", columnValue,
                                        "DOUBLE"));
                            }
                            break;
                        case BOOLEAN:
                            try {
                                columnGenerated = new BoolColumn(columnValue);
                            } catch (Exception e) {
                                throw new IllegalArgumentException(String.format(
                                        "类型转换错误, 无法将[%s] 转换为[%s]", columnValue,
                                        "BOOLEAN"));
                            }

                            break;
                        case DATE:
                            try {
                                if (columnValue == null) {
                                    Date date = null;
                                    columnGenerated = new DateColumn(date);
                                } else {
                                    String formatString = columnConfig.getString(Key.FORMAT);
                                    if (StringUtils.isNotBlank(formatString)) {
                                        // 用户自己配置的格式转换
                                        SimpleDateFormat format = new SimpleDateFormat(
                                                formatString);
                                        columnGenerated = new DateColumn(
                                                format.parse(columnValue));
                                    } else {
                                        // 框架尝试转换
                                        columnGenerated = new DateColumn(
                                                new StringColumn(columnValue)
                                                        .asDate());
                                    }
                                }
                            } catch (Exception e) {
                                throw new IllegalArgumentException(String.format(
                                        "类型转换错误, 无法将[%s] 转换为[%s]", columnValue,
                                        "DATE"));
                            }
                            break;
                        default:
                            String errorMessage = String.format(
                                    "您配置的列类型暂不支持 : [%s]", columnType);
                            LOG.error(errorMessage);
                            ErrorRecord.addError(errorMessage);
                            throw DataXException
                                    .asDataXException(
                                            UnstructuredStorageReaderErrorCode.NOT_SUPPORT_TYPE,
                                            errorMessage);
                    }

                    record.addColumn(columnGenerated);
                }
            }
            recordSender.sendToWriter(record);
        } catch (IllegalArgumentException iae) {
            LOG.error("", iae);
            ErrorRecord.addError(iae);
            taskPluginCollector
                    .collectDirtyRecord(record, iae.getMessage());
        } catch (IndexOutOfBoundsException ioe) {
            LOG.error("", ioe);
            ErrorRecord.addError(ioe);
            taskPluginCollector
                    .collectDirtyRecord(record, ioe.getMessage());
        } catch (Exception e) {
            LOG.error("", e);
            ErrorRecord.addError(e);
            if (e instanceof DataXException) {
                throw (DataXException) e;
            }
            // 每一种转换失败都是脏数据处理,包括数字格式 & 日期格式
            taskPluginCollector.collectDirtyRecord(record, e.getMessage());
        }

        return record;
    }

    private int getAllColumnsCount(String filePath) {
        int columnsCount = 0;
        final String colFinal = "_col";
        Path path = new Path(filePath);
        try {
            Reader reader = OrcFile.createReader(path, OrcFile.readerOptions(hadoopConf));
            String type_struct = reader.getObjectInspector().getTypeName();
            columnsCount = (type_struct.length() - type_struct.replace(colFinal, "").length())
                    / colFinal.length();
            return columnsCount;
        } catch (IOException e) {
            String message = "读取orcfile column列数失败，请联系系统管理员";
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, message);
        }
    }

    private int getMaxIndex(List<Configuration> columnConfigs) {
        int maxIndex = -1;
        for (Configuration columnConfig : columnConfigs) {
            Integer columnIndex = columnConfig.getInt(Key.INDEX);
            if (columnIndex != null && columnIndex < 0) {
                String message = String.format("您column中配置的index不能小于0，请修改为正确的index");
                LOG.error(message);
                ErrorRecord.addError(message);
                throw DataXException.asDataXException(HdfsReaderErrorCode.CONFIG_INVALID_EXCEPTION, message);
            } else if (columnIndex != null && columnIndex > maxIndex) {
                maxIndex = columnIndex;
            }
        }
        return maxIndex;
    }

    private static enum Type {
        STRING, LONG, BOOLEAN, DOUBLE, DATE,
    }

    public HdfsFileType checkHdfsFileType(String filepath) {

        Path path = new Path(filepath);

        try {
            FileSystem fs = FileSystem.get(hadoopConf);

            // figure out the size of the file using the option or filesystem
            long size = fs.getFileStatus(path).getLen();

            //read last bytes into buffer to get PostScript
            int readSize = (int) Math.min(size, DIRECTORY_SIZE_GUESS);
            FSDataInputStream file = fs.open(path);
            file.seek(size - readSize);
            ByteBuffer buffer = ByteBuffer.allocate(readSize);
            file.readFully(buffer.array(), buffer.arrayOffset() + buffer.position(),
                    buffer.remaining());

            //read the PostScript
            //get length of PostScript
            int psLen = buffer.get(readSize - 1) & 0xff;
            HdfsFileType type = checkType(file, path, psLen, buffer, hadoopConf);

            return type;
        } catch (Exception e) {
            String message = String.format("检查文件[%s]类型失败，请检查您的文件是否合法。"
                    , filepath);
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, message);
        }
    }

    /**
     * Check the file type
     *
     * @param in     the file being read
     * @param path   the filename for error messages
     * @param psLen  the postscript length
     * @param buffer the tail of the file
     * @throws IOException
     */
    private HdfsFileType checkType(FSDataInputStream in,
                                   Path path,
                                   int psLen,
                                   ByteBuffer buffer,
                                   org.apache.hadoop.conf.Configuration hadoopConf) throws IOException {
        int len = OrcFile.MAGIC.length();
        if (psLen < len + 1) {
            String message = String.format("Malformed ORC file [%s]. Invalid postscript length [%s]"
                    , path, psLen);
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(
                    HdfsReaderErrorCode.MALFORMED_ORC_ERROR, message);
        }
        int offset = buffer.arrayOffset() + buffer.position() + buffer.limit() - 1
                - len;
        byte[] array = buffer.array();
        // now look for the magic string at the end of the postscript.
        if (Text.decode(array, offset, len).equals(OrcFile.MAGIC)) {
            return HdfsFileType.ORC;
        } else {
            // If it isn't there, this may be the 0.11.0 version of ORC.
            // Read the first 3 bytes of the file to check for the header
            in.seek(0);
            byte[] header = new byte[len];
            in.readFully(header, 0, len);
            // if it isn't there, this isn't an ORC file
            if (Text.decode(header, 0, len).equals(OrcFile.MAGIC)) {
                return HdfsFileType.ORC;
            } else {
                in.seek(0);
                switch (in.readShort()) {
                    case 0x5345:
                        if (in.readByte() == 'Q') {
                            return HdfsFileType.SEQ;
                        }
                    default:
                        in.seek(0);
                        CompressionCodecFactory compressionCodecFactory = new CompressionCodecFactory(hadoopConf);
                        CompressionCodec codec = compressionCodecFactory.getCodec(path);
                        if (null == codec)
                            return HdfsFileType.TEXT;
                        else {
                            return HdfsFileType.COMPRESSED_TEXT;
                        }
                }
            }

        }
    }

}