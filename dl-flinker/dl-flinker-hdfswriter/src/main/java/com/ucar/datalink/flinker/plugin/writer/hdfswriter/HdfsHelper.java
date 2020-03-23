package com.ucar.datalink.flinker.plugin.writer.hdfswriter;

import com.ucar.datalink.flinker.api.element.Column;
import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordReceiver;
import com.ucar.datalink.flinker.api.plugin.TaskPluginCollector;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat;
import org.apache.hadoop.hive.ql.io.orc.OrcSerde;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobContext;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.RecordWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public  class HdfsHelper {
    public static final Logger LOG = LoggerFactory.getLogger(HdfsWriter.Job.class);
    public FileSystem fileSystem = null;
    public JobConf conf = null;
    public Integer errorRetryTimes = null;
	public Map<String, OrcWriterProxy> orcWriterCache = new HashMap<String, HdfsHelper.OrcWriterProxy>();
	public Map<String, TextWriterProxy> textWriterCache = new HashMap<String, HdfsHelper.TextWriterProxy>();

    public void getFileSystem(Configuration taskConfig){
        org.apache.hadoop.conf.Configuration hadoopConf = new org.apache.hadoop.conf.Configuration();

        String defaultFS = taskConfig.getString(Key.DEFAULT_FS);
        String hadoopUsername = taskConfig.getString(Key.HADOOP_USER_NAME);
        Configuration hadoopSiteParams = taskConfig.getConfiguration(Key.HADOOP_CONFIG);
        if(null != hadoopSiteParams) {
            JSONObject hadoopSiteParamsAsJsonObject = JSON.parseObject(taskConfig.getString("hadoopConfig"));
            Set paramKeys = hadoopSiteParams.getKeys();
            Iterator iter = paramKeys.iterator();
            while(iter.hasNext()) {
                String key = (String)iter.next();
                hadoopConf.set(key, hadoopSiteParamsAsJsonObject.getString(key));
            }
        }

        hadoopConf.set("fs.defaultFS", defaultFS);
		if (StringUtils.isNotEmpty( hadoopUsername )) {
			System.setProperty("HADOOP_USER_NAME", hadoopUsername);//added by lubiao
		}
        conf = new JobConf(hadoopConf);
        try {
            fileSystem = FileSystem.get(conf);
        } catch (IOException e) {
            String message = String.format("获取FileSystem时发生网络IO异常,请检查您的网络是否正常!HDFS地址：[%s]",
                    "message:defaultFS =" +defaultFS);
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }catch (Exception e) {
            String message = String.format("获取FileSystem失败,请检查HDFS地址是否正确: [%s]",
                    "message:defaultFS =" + defaultFS);
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }

        if(null == fileSystem || null == conf){
            String message = String.format("获取FileSystem失败,请检查HDFS地址是否正确: [%s]",
                    "message:defaultFS =" + defaultFS);
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, message);
        }
    }

    /**
     *获取指定目录先的文件列表
     * @param dir
     * @return
     * 拿到的是文件全路径，
     */
    public String[] hdfsDirList(String dir){
        Path path = new Path(dir);
        String[] files = null;
        try {
            FileStatus[] status = fileSystem.listStatus(path);
            files = new String[status.length];
            for(int i=0;i<status.length;i++){
                files[i] = status[i].getPath().toString();
            }
        } catch (IOException e) {
            String message = String.format("获取目录[%s]文件列表时发生网络IO异常,请检查您的网络是否正常！", dir);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        return files;
    }

    /**
     * 获取以fileName__ 开头的文件列表
     * @param dir
     * @param fileName
     * @return
     */
    public Path[] hdfsDirList(String dir,String fileName){
        Path path = new Path(dir);
        Path[] files = null;
        String filterFileName = fileName + "__*";
        try {
            PathFilter pathFilter = new GlobFilter(filterFileName);
            FileStatus[] status = fileSystem.listStatus(path,pathFilter);
            files = new Path[status.length];
            for(int i=0;i<status.length;i++){
                files[i] = status[i].getPath();
            }
        } catch (IOException e) {
            String message = String.format("获取目录[%s]下文件名以[%s]开头的文件列表时发生网络IO异常,请检查您的网络是否正常！",
                    dir,fileName);
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        return files;
    }

    public boolean isPathexists(String filePath) {
        Path path = new Path(filePath);
        boolean exist = false;
        try {
            exist = fileSystem.exists(path);
        } catch (IOException e) {
            String message = String.format("判断文件路径[%s]是否存在时发生网络IO异常,请检查您的网络是否正常！",
                    "message:filePath =" + filePath);
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        return exist;
    }

	public void createDir(String filePath) {
		Path path = new Path(filePath);
		try {
			fileSystem.mkdirs(path);
		} catch (IOException e) {
			String message = String.format("创建文件路径[%s]时发生网络IO异常,请检查您的网络是否正常！", "message:filePath =" + filePath);
			LOG.error(message);
			ErrorRecord.addError(message);
			throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
		}
	}

    public boolean isPathDir(String filePath) {
        Path path = new Path(filePath);
        boolean isDir = false;
        try {
            isDir = fileSystem.isDirectory(path);
        } catch (IOException e) {
            String message = String.format("判断路径[%s]是否是目录时发生网络IO异常,请检查您的网络是否正常！", filePath);
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        return isDir;
    }

    public void deleteFiles(Path[] paths){
        for(int i=0;i<paths.length;i++){
            LOG.info(String.format("delete file [%s].", paths[i].toString()));
            try {
                fileSystem.delete(paths[i],true);
            } catch (IOException e) {
                String message = String.format("删除文件[%s]时发生IO异常,请检查您的网络是否正常！",
                        paths[i].toString());
                LOG.error(message);
                ErrorRecord.addError(message);
                throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
            }
        }
    }

    public void deleteDir(Path path){
        LOG.info(String.format("start delete tmp dir [%s] .",path.toString()));
        try {
            if(isPathexists(path.toString())) {
                fileSystem.delete(path, true);
            }
        } catch (Exception e) {
            String message = String.format("删除临时目录[%s]时发生IO异常,请检查您的网络是否正常！", path.toString());
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        LOG.info(String.format("finish delete tmp dir [%s] .",path.toString()));
    }

    public void renameFile(HashSet<String> tmpFiles, HashSet<String> endFiles){
        Path tmpFilesParent = null;
        if(tmpFiles.size() != endFiles.size()){
            String message = String.format("临时目录下文件名个数与目标文件名个数不一致!");
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.HDFS_RENAME_FILE_ERROR, message);
        }else{
            try{
                for (Iterator it1=tmpFiles.iterator(),it2=endFiles.iterator();it1.hasNext()&&it2.hasNext();){
                    String srcFile = it1.next().toString();
                    String dstFile = it2.next().toString();
                    Path srcFilePah = new Path(srcFile);
                    Path dstFilePah = new Path(dstFile);
                    if(tmpFilesParent == null){
                        tmpFilesParent = srcFilePah.getParent();
                    }
                    LOG.info(String.format("start rename file [%s] to file [%s].", srcFile,dstFile));
                    boolean renameTag = false;
                    long fileLen = fileSystem.getFileStatus(srcFilePah).getLen();
                    if(fileLen>0){
                        renameTag = fileSystem.rename(srcFilePah,dstFilePah);
                        if(!renameTag){
                            String message = String.format("重命名文件[%s]失败,请检查您的网络是否正常！", srcFile);
                            LOG.error(message);
                            throw DataXException.asDataXException(HdfsWriterErrorCode.HDFS_RENAME_FILE_ERROR, message);
                        }
                        LOG.info(String.format("finish rename file [%s] to file [%s].", srcFile,dstFile));
                    }else{
                        LOG.info(String.format("文件［%s］内容为空,请检查写入是否正常！", srcFile));
                    }
                }
            }catch (Exception e) {
                String message = String.format("重命名文件时发生异常,请检查您的网络是否正常！");
                LOG.error(message);
                ErrorRecord.addError(message);
                throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
            }finally {
                deleteDir(tmpFilesParent);
            }
        }
    }

    public void moveFiles(HashSet<String> tmpFiles, HashSet<String> endFiles){
        Path tmpFilesParent = null;
        if(tmpFiles.size() != endFiles.size()){
            String message = String.format("临时目录下文件名个数与目标文件名个数不一致!");
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.HDFS_RENAME_FILE_ERROR, message);
        }else{
            try{
                for (Iterator it1=tmpFiles.iterator(),it2=endFiles.iterator();it1.hasNext()&&it2.hasNext();){
                    String srcFile = it1.next().toString();
                    String dstFile = it2.next().toString();
                    Path srcFilePah = new Path(srcFile);
                    Path dstFilePah = new Path(dstFile);
                    if(tmpFilesParent == null){
                        tmpFilesParent = srcFilePah.getParent();
                    }
                    LOG.info(String.format("start move file [%s] to file [%s].", srcFile,dstFile));
                    boolean renameTag = false;
                    long fileLen = fileSystem.getFileStatus(srcFilePah).getLen();
                    if(fileLen>0){
                        renameTag = fileSystem.rename(srcFilePah,dstFilePah);
                        if(!renameTag){
                            String message = String.format("移动文件[%s]失败,请检查您的网络是否正常！", srcFile);
                            LOG.error(message);
                            ErrorRecord.addError(message);
                            throw DataXException.asDataXException(HdfsWriterErrorCode.HDFS_RENAME_FILE_ERROR, message);
                        }
                        LOG.info(String.format("finish move file [%s] to file [%s].", srcFile,dstFile));
                    }else{
                        LOG.info(String.format("文件［%s］内容为空,请检查写入是否正常！", srcFile));
                    }
                }
            }catch (Exception e) {
                String message = String.format("移动文件时发生异常,请检查您的网络是否正常！");
                LOG.error(message);
                ErrorRecord.addError(message);
                throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
            }
        }
    }



    //关闭FileSystem
    public void closeFileSystem(){
        try {
            fileSystem.close();
        } catch (IOException e) {
            String message = String.format("关闭FileSystem时发生IO异常,请检查您的网络是否正常！");
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
    }


    //textfile格式文件
    public  FSDataOutputStream getOutputStream(String path){
        Path storePath = new Path(path);
        FSDataOutputStream fSDataOutputStream = null;
        try {
            fSDataOutputStream = fileSystem.create(storePath);
        } catch (IOException e) {
            String message = String.format("Create an FSDataOutputStream at the indicated Path[%s] failed: [%s]",
                    "message:path =" + path);
            LOG.error(message);
            ErrorRecord.addError(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.Write_FILE_IO_ERROR, e);
        }
        return fSDataOutputStream;
    }

    /**
     * 写textfile类型文件
     * @param lineReceiver
     * @param config
     * @param fileName
     * @param taskPluginCollector
     */
    public void textFileStartWrite(RecordReceiver lineReceiver, Configuration config, String fileName,
                                   TaskPluginCollector taskPluginCollector){
        try {
            TextWriterProxy writer =getTextWriterProxy(fileName, config);
            Record record = null;
			while ((record = lineReceiver.getFromReader()) != null) {
				int times = 0;
				while (true) {//增加重试机制，added by lubiao
					try {
						MutablePair<Text, Boolean> transportResult = transportOneRecord(record, writer.fieldDelimiter, writer.columns, taskPluginCollector);
						if (!transportResult.getRight()) {
							writer.write(transportResult.getLeft());
						}
						break;
					} catch (Exception e) {
						if (errorRetryTimes == null) {
							ErrorRecord.addError(e);
							throw e;
						}
						String errorMessage = MessageFormat.format("failed to transfer data to hdfs after retry {0} times.data content is:{1}",
								times, record.toString());
						LOG.error(errorMessage, e);
						times++;
						Thread.sleep(50);
						if (times > errorRetryTimes) {
							ErrorRecord.addError(e);
							throw e;
						}
					}
				}
			}
        } catch (Exception e) {
            String message = String.format("写文件文件[%s]时发生IO异常,请检查您的网络是否正常！", fileName);
            LOG.error(message);
            Path path = new Path(fileName);
            deleteDir(path.getParent());
            throw DataXException.asDataXException(HdfsWriterErrorCode.Write_FILE_IO_ERROR, e);
        }
    }

    public static MutablePair<Text, Boolean> transportOneRecord(
            Record record, char fieldDelimiter, List<Configuration> columnsConfiguration, TaskPluginCollector taskPluginCollector) {
        MutablePair<List<Object>, Boolean> transportResultList =  transportOneRecord(record,columnsConfiguration,taskPluginCollector);
        //保存<转换后的数据,是否是脏数据>
        MutablePair<Text, Boolean> transportResult = new MutablePair<Text, Boolean>();
        transportResult.setRight(false);
        if(null != transportResultList){
            Text recordResult = new Text(StringUtils.join(transportResultList.getLeft(), fieldDelimiter));
            transportResult.setRight(transportResultList.getRight());
            transportResult.setLeft(recordResult);
        }
        return transportResult;
    }

    public Class<? extends CompressionCodec>  getCompressCodec(String compress){
        Class<? extends CompressionCodec> codecClass = null;
        if(null == compress){
            codecClass = null;
        }else if("GZIP".equalsIgnoreCase(compress)){
            codecClass = org.apache.hadoop.io.compress.GzipCodec.class;
        }else if ("BZIP2".equalsIgnoreCase(compress)) {
            codecClass = org.apache.hadoop.io.compress.BZip2Codec.class;
        }else if("SNAPPY".equalsIgnoreCase(compress)){
            //todo 等需求明确后支持 需要用户安装SnappyCodec
            codecClass = org.apache.hadoop.io.compress.SnappyCodec.class;
            // org.apache.hadoop.hive.ql.io.orc.ZlibCodec.class  not public
            //codecClass = org.apache.hadoop.hive.ql.io.orc.ZlibCodec.class;
        }else {
        	ErrorRecord.addError("目前不支持您配置的 compress 模式 : "+compress);
            throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                    String.format("目前不支持您配置的 compress 模式 : [%s]", compress));
        }
        return codecClass;
    }

    /**
     * 写orcfile类型文件
     * @param lineReceiver
     * @param config
     * @param fileName
     * @param taskPluginCollector
     */
    public void orcFileStartWrite(RecordReceiver lineReceiver, Configuration config, String fileName,
                                  TaskPluginCollector taskPluginCollector){
        try {
			OrcWriterProxy writer = getOrcWriterProxy(fileName, config);
            Record record = null;
            while ((record = lineReceiver.getFromReader()) != null) {
            	int times = 0;
            	while (true) {//增加重试机制，added by lubiao
            		try {
            			MutablePair<List<Object>, Boolean> transportResult =  transportOneRecord(record,writer.columns,taskPluginCollector);
                        if (!transportResult.getRight()) {
                            writer.write(transportResult.getLeft());
                        }
						break;
					} catch (Exception e) {
						if (errorRetryTimes == null) {
							ErrorRecord.addError(e);
							throw e;
						}
						String errorMessage = MessageFormat.format("failed to transfer data to hdfs after retry {0} times.data content is:{1}",
								times, record.toString());
						LOG.error(errorMessage, e);
						ErrorRecord.addError(e);
						times++;
						Thread.sleep(50);
						if (times > errorRetryTimes) {
							ErrorRecord.addError(e);
							throw e;
						}
					}
            	}
            }
        } catch (Exception e) {
            String message = String.format("写文件文件[%s]时发生IO异常,请检查您的网络是否正常！", fileName);
            LOG.error(message,e);
            Path path = new Path(fileName);
            deleteDir(path.getParent());
            ErrorRecord.addError(message+e.getMessage());
            throw DataXException.asDataXException(HdfsWriterErrorCode.Write_FILE_IO_ERROR, e);
        }
    }

    public List<String> getColumnNames(List<Configuration> columns){
        List<String> columnNames = Lists.newArrayList();
        for (Configuration eachColumnConf : columns) {
            columnNames.add(eachColumnConf.getString(Key.NAME));
        }
        return columnNames;
    }

    /**
     * 根据writer配置的字段类型，构建inspector
     * @param columns
     * @return
     */
    public List<ObjectInspector>  getColumnTypeInspectors(List<Configuration> columns){
        List<ObjectInspector>  columnTypeInspectors = Lists.newArrayList();
        for (Configuration eachColumnConf : columns) {
            SupportHiveDataType columnType = SupportHiveDataType.valueOf(eachColumnConf.getString(Key.TYPE).toUpperCase());
            ObjectInspector objectInspector = null;
            switch (columnType) {
                case TINYINT:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Byte.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                case SMALLINT:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Short.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                case INT:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Integer.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                case BIGINT:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Long.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                case FLOAT:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Float.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                case DOUBLE:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Double.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                case DECIMAL://decimal,added by lubiao
                	objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(HiveDecimal.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                	break;
                case BINARY://binary,added by luibao
                	objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(BytesWritable.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                	break;
                case TIMESTAMP:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(java.sql.Timestamp.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                case DATE:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(java.sql.Date.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                case STRING:
                case VARCHAR:
                case CHAR:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(String.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                case BOOLEAN:
                    objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Boolean.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                    break;
                default:
                    ErrorRecord.addError(String.format(
                            "您的配置文件中的列配置信息有误. 因为DataX 不支持数据库写入这种字段类型. 字段名:[%s], 字段类型:[%d]. 请修改表中该字段的类型或者不同步该字段.",
                            eachColumnConf.getString(Key.NAME),
                            eachColumnConf.getString(Key.TYPE)));
                    throw DataXException
                            .asDataXException(
                                    HdfsWriterErrorCode.ILLEGAL_VALUE,
                                    String.format(
                                            "您的配置文件中的列配置信息有误. 因为DataX 不支持数据库写入这种字段类型. 字段名:[%s], 字段类型:[%d]. 请修改表中该字段的类型或者不同步该字段.",
                                            eachColumnConf.getString(Key.NAME),
                                            eachColumnConf.getString(Key.TYPE)));
            }

            columnTypeInspectors.add(objectInspector);
        }
        return columnTypeInspectors;
    }

    public OrcSerde getOrcSerde(Configuration config){
        String fieldDelimiter = config.getString(Key.FIELD_DELIMITER);
        String compress = config.getString(Key.COMPRESS);
        String encoding = config.getString(Key.ENCODING);

        OrcSerde orcSerde = new OrcSerde();
        Properties properties = new Properties();
        properties.setProperty("orc.bloom.filter.columns", fieldDelimiter);
        properties.setProperty("orc.compress", compress);
        properties.setProperty("orc.encoding.strategy", encoding);

        orcSerde.initialize(conf, properties);
        return orcSerde;
    }

    public static MutablePair<List<Object>, Boolean> transportOneRecord(
            Record record,List<Configuration> columnsConfiguration,
            TaskPluginCollector taskPluginCollector){

        MutablePair<List<Object>, Boolean> transportResult = new MutablePair<List<Object>, Boolean>();
        transportResult.setRight(false);
        List<Object> recordList = Lists.newArrayList();
        int recordLength = record.getColumnNumber();
        if (0 != recordLength) {
            Column column;
            for (int i = 0; i < recordLength; i++) {
                column = record.getColumn(i);
                //todo as method
				String rowData = column.getRawData() == null ? null : column.getRawData().toString();
                if (null != column.getRawData() && StringUtils.isNotBlank(rowData)) {
                    SupportHiveDataType columnType = SupportHiveDataType.valueOf(
                            columnsConfiguration.get(i).getString(Key.TYPE).toUpperCase());
                    //根据writer端类型配置做类型转换
                    try {
                        switch (columnType) {
                            case TINYINT:
                                recordList.add(Byte.valueOf(rowData));
                                break;
                            case SMALLINT:
                                recordList.add(Short.valueOf(rowData));
                                break;
                            case INT:
                                recordList.add(Integer.valueOf(rowData));
                                break;
                            case BIGINT:
                                recordList.add(column.asLong());
                                break;
                            case FLOAT:
                                recordList.add(Float.valueOf(rowData));
                                break;
                            case DOUBLE:
                                recordList.add(column.asDouble());
                                break;
                            case DECIMAL://decimal,added by lubiao
                            	recordList.add(HiveDecimal.create(column.asBigDecimal()));
                            	break;
                            case BINARY://binary,added by lubiao
                            	recordList.add(new BytesWritable(column.asBytes()));
                            	break;
                            case STRING:
                            case VARCHAR:
                            case CHAR:
                                recordList.add(column.asString());
                                break;
                            case BOOLEAN:
                                recordList.add(column.asBoolean());
                                break;
                            case DATE:
                                recordList.add(new java.sql.Date(column.asDate().getTime()));
                                break;
                            case TIMESTAMP:
                                recordList.add(new java.sql.Timestamp(column.asDate().getTime()));
                                break;
                            default:
                                ErrorRecord.addError(String.format(
                                        "您的配置文件中的列配置信息有误. 因为DataX 不支持数据库写入这种字段类型. 字段名:[%s], 字段类型:[%d]. 请修改表中该字段的类型或者不同步该字段.",
                                        columnsConfiguration.get(i).getString(Key.NAME),
                                        columnsConfiguration.get(i).getString(Key.TYPE)));
                                throw DataXException
                                        .asDataXException(
                                                HdfsWriterErrorCode.ILLEGAL_VALUE,
                                                String.format(
                                                        "您的配置文件中的列配置信息有误. 因为DataX 不支持数据库写入这种字段类型. 字段名:[%s], 字段类型:[%d]. 请修改表中该字段的类型或者不同步该字段.",
                                                        columnsConfiguration.get(i).getString(Key.NAME),
                                                        columnsConfiguration.get(i).getString(Key.TYPE)));
                        }
                    } catch (Exception e) {
                        // warn: 此处认为脏数据
                        String message = String.format(
                                "字段类型转换错误：你目标字段为[%s]类型，实际字段值为[%s].",
                                columnsConfiguration.get(i).getString(Key.TYPE), column.getRawData().toString());
                        taskPluginCollector.collectDirtyRecord(record, message);
                        transportResult.setRight(true);
                        break;
                    }
                }else {
                    // warn: it's all ok if nullFormat is null
                    recordList.add(null);
                }
            }
        }
        transportResult.setLeft(recordList);
        return transportResult;
    }

    public OrcWriterProxy getOrcWriterProxy(String fileName,Configuration config) throws IOException{
    	if (orcWriterCache.get(fileName) == null) {
			synchronized (this) {
				if (orcWriterCache.get(fileName) == null) {
					orcWriterCache.put(fileName, new OrcWriterProxy(config,fileName));
				}
			}
		}
		return orcWriterCache.get(fileName);

    }

    public TextWriterProxy getTextWriterProxy(String fileName,Configuration config) throws IOException{
    	if (textWriterCache.get(fileName) == null) {
			synchronized (this) {
				if (textWriterCache.get(fileName) == null) {
					textWriterCache.put(fileName, new TextWriterProxy(config,fileName));
				}
			}
		}
		return textWriterCache.get(fileName);

    }

	public void closeWriters() {
		for (OrcWriterProxy proxy : orcWriterCache.values()) {
			proxy.close();
		}
		for (TextWriterProxy proxy : textWriterCache.values()) {
			proxy.close();
		}
	}

    class OrcWriterProxy{
    	RecordWriter writer;
    	OrcSerde orcSerde;
    	StructObjectInspector inspector;
    	List<Configuration>  columns;

    	OrcWriterProxy(Configuration config, String fileName) throws IOException{
    		// initial columns
            columns = config.getListConfiguration(Key.COLUMN);

            // initial inspector
            List<String> columnNames = getColumnNames(columns);
            List<ObjectInspector> columnTypeInspectors = getColumnTypeInspectors(columns);
            inspector = (StructObjectInspector)ObjectInspectorFactory
                    .getStandardStructObjectInspector(columnNames, columnTypeInspectors);

            // initial writer
            String compress = config.getString(Key.COMPRESS, null);
            FileOutputFormat outFormat = new OrcOutputFormat();
            if(!"NONE".equalsIgnoreCase(compress) && null != compress ) {
                Class<? extends CompressionCodec> codecClass = getCompressCodec(compress);
                if (null != codecClass) {
                    outFormat.setOutputCompressorClass(conf, codecClass);
                }
            }
            writer = outFormat.getRecordWriter(fileSystem, conf, fileName, Reporter.NULL);

            //initial orcSerde
            orcSerde = new OrcSerde();
    	}

    	void write(List<Object> list) throws IOException{
    		writer.write(NullWritable.get(), orcSerde.serialize(list, inspector));
    	}

    	void close(){
        	try {
				writer.close(Reporter.NULL);
			} catch (IOException e) {
				LOG.error("writer close failed.",e);
				ErrorRecord.addError("writer close failed."+e.getMessage());
			}
        }
    }

    class TextWriterProxy{
    	RecordWriter writer;
    	char fieldDelimiter;
    	List<Configuration>  columns;

    	TextWriterProxy(Configuration config, String fileName) throws IOException{
    		fieldDelimiter = config.getChar(Key.FIELD_DELIMITER);
            columns = config.getListConfiguration(Key.COLUMN);
            
            String compress = config.getString(Key.COMPRESS,null);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            String attempt = "attempt_"+dateFormat.format(new Date())+"_0001_m_000000_0";
            Path outputPath = new Path(fileName);
            //todo 需要进一步确定TASK_ATTEMPT_ID
            conf.set(JobContext.TASK_ATTEMPT_ID, attempt);
            FileOutputFormat outFormat = new TextOutputFormat();
            outFormat.setOutputPath(conf, outputPath);
            outFormat.setWorkOutputPath(conf, outputPath);
            if(null != compress) {
                Class<? extends CompressionCodec> codecClass = getCompressCodec(compress);
                if (null != codecClass) {
                    outFormat.setOutputCompressorClass(conf, codecClass);
                }
            }
            
            writer = outFormat.getRecordWriter(fileSystem, conf, outputPath.toString(), Reporter.NULL);
    	}

        void write(Text text) throws IOException{
    		writer.write(NullWritable.get(), text);
        }
        
        void close(){
        	try {
				writer.close(Reporter.NULL);
			} catch (IOException e) {
				LOG.error("writer close failed.",e);
				ErrorRecord.addError("writer close failed."+e.getMessage());
			}
        }
    }
}
