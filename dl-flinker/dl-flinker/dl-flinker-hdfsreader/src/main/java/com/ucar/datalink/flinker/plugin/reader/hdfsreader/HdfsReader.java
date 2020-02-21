package com.ucar.datalink.flinker.plugin.reader.hdfsreader;

import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordSender;
import com.ucar.datalink.flinker.api.spi.Reader;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.UnstructuredStorageReaderUtil;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HdfsReader extends Reader {

    /**
     * Job 中的方法仅执行一次，Task 中方法会由框架启动多个 Task 线程并行执行。
     * <p/>
     * 整个 Reader 执行流程是：
     * <pre>
     * Job类init-->prepare-->split
     *
     * Task类init-->prepare-->startRead-->post-->destroy
     * Task类init-->prepare-->startRead-->post-->destroy
     *
     * Job类post-->destroy
     * </pre>
     */
    public static class Job extends Reader.Job {
        private static final Logger LOG = LoggerFactory
                .getLogger(Job.class);

        private Configuration readerOriginConfig = null;
        private String defaultFS = null;
        private String encoding = null;
        private HashSet<String> sourceFiles;
        private String specifiedFileType = null;
        private DFSUtil dfsUtil = null;
        private List<String> path = null;

        @Override
        public void init() {

            LOG.info("init() begin...");
            this.readerOriginConfig = super.getPluginJobConf();
            this.validate();
            dfsUtil = new DFSUtil(readerOriginConfig);
            LOG.info("init() ok and end...");

        }

        private void validate(){
            defaultFS = this.readerOriginConfig.getNecessaryValue(Key.DEFAULT_FS,
                    HdfsReaderErrorCode.DEFAULT_FS_NOT_FIND_ERROR);
            if (StringUtils.isBlank(defaultFS)) {
            	ErrorRecord.addError("您需要指定 defaultFS");
                throw DataXException.asDataXException(
                        HdfsReaderErrorCode.PATH_NOT_FIND_ERROR, "您需要指定 defaultFS");
            }

            // path check
            String pathInString = this.readerOriginConfig.getNecessaryValue(Key.PATH, HdfsReaderErrorCode.REQUIRED_VALUE);
            if (!pathInString.startsWith("[") && !pathInString.endsWith("]")) {
                path = new ArrayList<String>();
                path.add(pathInString);
            } else {
                path = this.readerOriginConfig.getList(Key.PATH, String.class);
                if (null == path || path.size() == 0) {
                    throw DataXException.asDataXException(HdfsReaderErrorCode.REQUIRED_VALUE, "您需要指定待读取的源目录或文件");
                }
                for (String eachPath : path) {
                    if(!eachPath.startsWith("/")){
                        String message = String.format("请检查参数path:[%s],需要配置为绝对路径", eachPath);
                        LOG.error(message);
                        ErrorRecord.addError(message);
                        throw DataXException.asDataXException(HdfsReaderErrorCode.ILLEGAL_VALUE, message);
                    }
                }
            }

            specifiedFileType = this.readerOriginConfig.getNecessaryValue(Key.FILETYPE, HdfsReaderErrorCode.REQUIRED_VALUE);
            if( !specifiedFileType.equalsIgnoreCase("ORC") &&
                    !specifiedFileType.equalsIgnoreCase("TEXT")){
                String message = "HdfsReader插件目前只支持ORC和TEXT两种格式的文件," +
                        "如果您需要指定读取的文件类型，请将filetype选项的值配置为ORC或者TEXT";
                ErrorRecord.addError(message);
                throw DataXException.asDataXException(
                        HdfsReaderErrorCode.FILE_TYPE_ERROR, message);
            }

            encoding = this.readerOriginConfig.getString(Key.ENCODING, "UTF-8");

            try {
                Charsets.toCharset(encoding);
            } catch (UnsupportedCharsetException uce) {
                ErrorRecord.addError(String.format("不支持的编码格式 : [%s]", encoding));
                throw DataXException.asDataXException(
                        HdfsReaderErrorCode.ILLEGAL_VALUE,
                        String.format("不支持的编码格式 : [%s]", encoding), uce);
            } catch (Exception e) {
                ErrorRecord.addError(String.format("运行配置异常 : %s", e.getMessage()));
                throw DataXException.asDataXException(
                        HdfsReaderErrorCode.ILLEGAL_VALUE,
                        String.format("运行配置异常 : %s", e.getMessage()), e);
            }

            // validate the Columns
            validateColumns();

        }

        private void validateColumns(){

            // 检测是column 是否为 ["*"] 若是则填为空
            List<Configuration> column = this.readerOriginConfig
                    .getListConfiguration(Key.COLUMN);
            if (null != column
                    && 1 == column.size()
                    && ("\"*\"".equals(column.get(0).toString()) || "'*'"
                    .equals(column.get(0).toString()))) {
                readerOriginConfig
                        .set(Key.COLUMN, new ArrayList<String>());
            } else {
                // column: 1. index type 2.value type 3.when type is Data, may have format
                List<Configuration> columns = this.readerOriginConfig
                        .getListConfiguration(Key.COLUMN);

                if (null == columns || columns.size() == 0) {
                	ErrorRecord.addError("您需要指定 columns");
                    throw DataXException.asDataXException(
                            HdfsReaderErrorCode.CONFIG_INVALID_EXCEPTION,
                            "您需要指定 columns");
                }

                if (null != columns && columns.size() != 0) {
                    for (Configuration eachColumnConf : columns) {
                        eachColumnConf.getNecessaryValue(Key.TYPE, HdfsReaderErrorCode.REQUIRED_VALUE);
                        Integer columnIndex = eachColumnConf.getInt(Key.INDEX);
                        String columnValue = eachColumnConf.getString(Key.VALUE);

                        if (null == columnIndex && null == columnValue) {
                        	ErrorRecord.addError("由于您配置了type, 则至少需要配置 index 或 value");
                            throw DataXException.asDataXException(
                                    HdfsReaderErrorCode.NO_INDEX_VALUE,
                                    "由于您配置了type, 则至少需要配置 index 或 value");
                        }

                        if (null != columnIndex && null != columnValue) {
                            ErrorRecord.addError("您混合配置了index, value, 每一列同时仅能选择其中一种");
                            throw DataXException.asDataXException(
                                    HdfsReaderErrorCode.MIXED_INDEX_VALUE,
                                    "您混合配置了index, value, 每一列同时仅能选择其中一种");
                        }

                    }
                }
            }
        }

        @Override
        public void prepare() {
            LOG.info("prepare()");
            boolean isIgnoreExcept = Boolean.parseBoolean( this.readerOriginConfig.getString(Key.IGNORE_EXCEPTION, "true") );
            String hadoopUserName = this.readerOriginConfig.getString(Key.HADOOP_USER_NAME);
            if(StringUtils.isNotBlank(hadoopUserName)) {
                System.setProperty("HADOOP_USER_NAME", hadoopUserName);
            }
            this.sourceFiles = dfsUtil.getAllFiles(path, specifiedFileType, isIgnoreExcept);
            LOG.info(String.format("您即将读取的文件数为: [%s]", this.sourceFiles.size()));
            LOG.info("待读取的所有文件绝对路径如下：");
            for(String filePath :sourceFiles){
                LOG.info(String.format("[%s]", filePath));
            }
        }

        @Override
        public List<Configuration> split(int adviceNumber) {

            LOG.info("split() begin...");
            List<Configuration> readerSplitConfigs = new ArrayList<Configuration>();
            // warn:每个slice拖且仅拖一个文件,
            // int splitNumber = adviceNumber;

            boolean isIgnoreExcept = Boolean.parseBoolean( this.readerOriginConfig.getString(Key.IGNORE_EXCEPTION, "true") );
            int splitNumber = this.sourceFiles.size();
            if (0 == splitNumber) {
                if(isIgnoreExcept) {
                    LOG.error(String.format("未能找到待读取的文件,请确认您的配置项path: %s", this.readerOriginConfig.getString(Key.PATH)));
                    ErrorRecord.addError(String.format("未能找到待读取的文件,请确认您的配置项path: %s", this.readerOriginConfig.getString(Key.PATH)));
                    return new ArrayList<Configuration>();
                } else {
                	ErrorRecord.addError(String.format("未能找到待读取的文件,请确认您的配置项path: %s", this.readerOriginConfig.getString(Key.PATH)));
                    throw DataXException.asDataXException(HdfsReaderErrorCode.EMPTY_DIR_EXCEPTION,
                            String.format("未能找到待读取的文件,请确认您的配置项path: %s", this.readerOriginConfig.getString(Key.PATH)));
                }
                //LOG.error(String.format("未能找到待读取的文件,请确认您的配置项path: %s", this.readerOriginConfig.getString(Key.PATH)));
                //return new ArrayList<Configuration>();
            }

            List<List<String>> splitedSourceFiles = this.splitSourceFiles(new ArrayList(this.sourceFiles), splitNumber);
            for (List<String> files : splitedSourceFiles) {
                Configuration splitedConfig = this.readerOriginConfig.clone();
                splitedConfig.set(Constant.SOURCE_FILES, files);
                readerSplitConfigs.add(splitedConfig);
            }

            return readerSplitConfigs;
        }


        private <T> List<List<T>> splitSourceFiles(final List<T> sourceList, int adviceNumber) {
            List<List<T>> splitedList = new ArrayList<List<T>>();
            int averageLength = sourceList.size() / adviceNumber;
            averageLength = averageLength == 0 ? 1 : averageLength;

            for (int begin = 0, end = 0; begin < sourceList.size(); begin = end) {
                end = begin + averageLength;
                if (end > sourceList.size()) {
                    end = sourceList.size();
                }
                splitedList.add(sourceList.subList(begin, end));
            }
            return splitedList;
        }


        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }

    }

    public static class Task extends Reader.Task {

        private static Logger LOG = LoggerFactory.getLogger(Reader.Task.class);
        private Configuration taskConfig;
        private List<String> sourceFiles;
        private String defaultFS;
        private HdfsFileType fileType;
        private String specifiedFileType;
        private String encoding;
        private DFSUtil dfsUtil = null;

        @Override
        public void init() {

            this.taskConfig = super.getPluginJobConf();
            this.sourceFiles = this.taskConfig.getList(Constant.SOURCE_FILES, String.class);
            this.defaultFS = this.taskConfig.getNecessaryValue(Key.DEFAULT_FS,
                    HdfsReaderErrorCode.DEFAULT_FS_NOT_FIND_ERROR);
            this.specifiedFileType = this.taskConfig.getNecessaryValue(Key.FILETYPE, HdfsReaderErrorCode.REQUIRED_VALUE);
            this.encoding = this.taskConfig.getString(Key.ENCODING, "UTF-8");
            this.dfsUtil = new DFSUtil(taskConfig);
        }

        @Override
        public void prepare() {

        }

        @Override
        public void startRead(RecordSender recordSender) {

            LOG.info("read start");
            for (String sourceFile : this.sourceFiles) {
                LOG.info(String.format("reading file : [%s]", sourceFile));
                fileType = dfsUtil.checkHdfsFileType(sourceFile);

                if((fileType.equals(HdfsFileType.TEXT) || fileType.equals(HdfsFileType.COMPRESSED_TEXT))
                        &&(this.specifiedFileType.equalsIgnoreCase(Constant.TEXT))) {

                    BufferedReader bufferedReader = dfsUtil.getBufferedReader(sourceFile, fileType, encoding);
                    UnstructuredStorageReaderUtil.doReadFromStream(bufferedReader, sourceFile,
                            this.taskConfig, recordSender, this.getTaskPluginCollector());
                }else if(fileType.equals(HdfsFileType.ORC)
                        && (this.specifiedFileType.equalsIgnoreCase(Constant.ORC))){

                    dfsUtil.orcFileStartRead(sourceFile, this.taskConfig,
                            recordSender, this.getTaskPluginCollector());
                }else {

                    String message = String.format("文件[%s]的类型与用户配置的fileType类型不一致，" +
                            "请确认您配置的目录下面所有文件的类型均为[%s]"
                            , sourceFile, this.specifiedFileType);
                    LOG.error(message);
                    ErrorRecord.addError(message);
                    throw DataXException.asDataXException(
                            HdfsReaderErrorCode.FILE_TYPE_UNSUPPORT, message);
                }

                if(recordSender != null){
                    recordSender.flush();
                }
            }

            LOG.info("end read source files...");
        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }

    }

}