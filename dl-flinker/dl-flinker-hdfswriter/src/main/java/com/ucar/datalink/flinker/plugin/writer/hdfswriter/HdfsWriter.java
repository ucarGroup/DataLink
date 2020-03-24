package com.ucar.datalink.flinker.plugin.writer.hdfswriter;

import com.google.common.collect.Sets;
import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordReceiver;
import com.ucar.datalink.flinker.api.spi.Writer;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class HdfsWriter extends Writer {
	public static class Job extends Writer.Job {
		private static final Logger LOG = LoggerFactory.getLogger(Job.class);

		private Configuration writerSliceConfig = null;

		private String hadoopUserName;// added by lubiao
		private Boolean createPathIfNotExist;// added by lubiao
		private Integer parallelSize;// added by lubiao
		private String defaultFS;
		private String path;
		private String fileType;
		private String fileName;
		private List<Configuration> columns;
		private String writeMode;
		private String fieldDelimiter;
		private String compress;
		private String encoding;
		private List<HashSet<String>> tmpFiles = new ArrayList<HashSet<String>>();// 临时文件全路径
		private List<HashSet<String>> endFiles = new ArrayList<HashSet<String>>();// 最终文件全路径

		private boolean isPreDel = false;
		private static final boolean IS_PRE_DEL_DEFAULT = true;

		private HdfsHelper hdfsHelper = null;

		private static final String DATE_FORMAT = "yyyy-MM-dd_kk-mm-ss";

		@Override
		public void init() {
			this.writerSliceConfig = this.getPluginJobConf();
			this.validateParameter();

			// 创建textfile存储
			hdfsHelper = new HdfsHelper();

			hdfsHelper.getFileSystem(writerSliceConfig);
		}


		@Override
		public void postHandler(Configuration jobConfiguration){

		}

		private void validateParameter() {
			this.defaultFS = this.writerSliceConfig.getNecessaryValue(Key.DEFAULT_FS,
					HdfsWriterErrorCode.REQUIRED_VALUE);

			this.hadoopUserName = this.writerSliceConfig.getString(Key.HADOOP_USER_NAME);
			this.createPathIfNotExist = this.writerSliceConfig.getBool(Key.CREATE_PATH_IF_NOT_EXIST, false);
			this.parallelSize = this.writerSliceConfig.getInt(Key.PARALLEL_SIZE, 1);
			// fileType check
			this.fileType = this.writerSliceConfig.getNecessaryValue(Key.FILE_TYPE, HdfsWriterErrorCode.REQUIRED_VALUE);
			if (!fileType.equalsIgnoreCase("ORC") && !fileType.equalsIgnoreCase("TEXT")) {
				String message = "HdfsWriter插件目前只支持ORC和TEXT两种格式的文件,请将filetype选项的值配置为ORC或者TEXT";
				throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE, message);
			}
			// path
			this.path = this.writerSliceConfig.getNecessaryValue(Key.PATH, HdfsWriterErrorCode.REQUIRED_VALUE);
			if (!path.startsWith("/")) {
				String message = String.format("请检查参数path:[%s],需要配置为绝对路径", path);
				LOG.error(message);
				ErrorRecord.addError(message);
				throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE, message);
			} else if (path.contains("*") || path.contains("?")) {
				String message = String.format("请检查参数path:[%s],不能包含*,?等特殊字符", path);
				LOG.error(message);
				ErrorRecord.addError(message);
				throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE, message);
			}
			// fileName
			this.fileName = this.writerSliceConfig.getNecessaryValue(Key.FILE_NAME, HdfsWriterErrorCode.REQUIRED_VALUE);
			// columns check
			this.columns = this.writerSliceConfig.getListConfiguration(Key.COLUMN);
			if (null == columns || columns.size() == 0) {
				throw DataXException.asDataXException(HdfsWriterErrorCode.REQUIRED_VALUE, "您需要指定 columns");
			} else {
				for (Configuration eachColumnConf : columns) {
					eachColumnConf.getNecessaryValue(Key.NAME, HdfsWriterErrorCode.COLUMN_REQUIRED_VALUE);
					eachColumnConf.getNecessaryValue(Key.TYPE, HdfsWriterErrorCode.COLUMN_REQUIRED_VALUE);
				}
			}
			// writeMode check
			this.writeMode = this.writerSliceConfig.getNecessaryValue(Key.WRITE_MODE,
					HdfsWriterErrorCode.REQUIRED_VALUE);
			writeMode = writeMode.toLowerCase().trim();
			Set<String> supportedWriteModes = Sets.newHashSet("append", "nonconflict");
			if (!supportedWriteModes.contains(writeMode)) {
				throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
						String.format("仅支持append, nonConflict两种模式, 不支持您配置的 writeMode 模式 : [%s]", writeMode));
			}
			this.writerSliceConfig.set(Key.WRITE_MODE, writeMode);
			// fieldDelimiter check
			this.fieldDelimiter = this.writerSliceConfig.getString(Key.FIELD_DELIMITER, null);
			if (null == fieldDelimiter) {
				ErrorRecord.addError(String.format("仅支持append, nonConflict两种模式, 不支持您配置的 writeMode 模式 : [%s]", writeMode));
				throw DataXException.asDataXException(HdfsWriterErrorCode.REQUIRED_VALUE,
						String.format("您提供配置文件有误，[%s]是必填参数.", Key.FIELD_DELIMITER));
			} else if (1 != fieldDelimiter.length()) {
				// warn: if have, length must be one
				ErrorRecord.addError(String.format("您提供配置文件有误，[%s]是必填参数.", Key.FIELD_DELIMITER));
				throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
						String.format("仅仅支持单字符切分, 您配置的切分为 : [%s]", fieldDelimiter));
			}
			// compress check
			this.compress = this.writerSliceConfig.getString(Key.COMPRESS, null);
			if (fileType.equalsIgnoreCase("TEXT")) {
				Set<String> textSupportedCompress = Sets.newHashSet("GZIP", "BZIP2");
				if (null == compress) {
					this.writerSliceConfig.set(Key.COMPRESS, null);
				} else {
					compress = compress.toUpperCase().trim();
					if (!textSupportedCompress.contains(compress)) {
						ErrorRecord.addError(String.format("目前TEXT FILE仅支持GZIP、BZIP2 两种压缩, 不支持您配置的 compress 模式 : [%s]", compress));
						throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
								String.format("目前TEXT FILE仅支持GZIP、BZIP2 两种压缩, 不支持您配置的 compress 模式 : [%s]", compress));
					}
				}
			} else if (fileType.equalsIgnoreCase("ORC")) {
				Set<String> orcSupportedCompress = Sets.newHashSet("NONE", "SNAPPY");
				if (null == compress) {
					this.writerSliceConfig.set(Key.COMPRESS, "NONE");
				} else {
					compress = compress.toUpperCase().trim();
					if (!orcSupportedCompress.contains(compress)) {
						ErrorRecord.addError(String.format("目前ORC FILE仅支持SNAPPY压缩, 不支持您配置的 compress 模式 : [%s]", compress));
						throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
								String.format("目前ORC FILE仅支持SNAPPY压缩, 不支持您配置的 compress 模式 : [%s]", compress));
					}
				}

			}
			// encoding check
			this.encoding = this.writerSliceConfig.getString(Key.ENCODING, Constant.DEFAULT_ENCODING);
			try {
				encoding = encoding.trim();
				this.writerSliceConfig.set(Key.ENCODING, encoding);
				Charsets.toCharset(encoding);
			} catch (Exception e) {
				ErrorRecord.addError(String.format("不支持您配置的编码格式:[%s]", encoding));
				throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
						String.format("不支持您配置的编码格式:[%s]", encoding), e);
			}

			// 执行前是否先清空目录
			this.isPreDel = this.writerSliceConfig.getBool(Key.PRE_DEL,IS_PRE_DEL_DEFAULT);
			//this.writerSliceConfig.getBool()
		}


		private void preDel() {
			try {
				String[] oldPaths = hdfsHelper.hdfsDirList(path);
				if(oldPaths==null && oldPaths.length==0) {
					return;
				}

				//不带 /  结尾的路径
				String pathWithoutSlash = path;
				if( path.indexOf("/") > 0) {
					pathWithoutSlash = path.substring(0,path.indexOf("/"));
				}
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
				//根据原始路径，加上新创建的目录，生成一个备份的目录路径，路径是在原路径基础上加上 _2019-04-01_10-11-12 后缀
				String backupPath = pathWithoutSlash+"_"+ sdf.format(new Date()) +"/";
				//创建一个备份目录，
				hdfsHelper.createDir(backupPath);
				//拼接出备份的hdfs路径
				String backupHdfsPath = this.defaultFS +"/" +backupPath;
				HashSet<String> old = new HashSet<String>();
				HashSet<String> backup = new HashSet<String>();
				for(String s : oldPaths) {
					old.add(s);
					//  hdfs://hadoop2cluster/user/sqlserverhistory/BI_ODS/Dim_City_tmp/20190417180316/Dim_Date__2329479e_9e9c_4bbe_aaa6_71f7f1902f96
					// orginalFileName -> Dim_Date__2329479e_9e9c_4bbe_aaa6_71f7f1902f96
					int len = s.lastIndexOf("/");
					String orginalFileName = s.substring(len+1);
					String backupHdfsFileFullPath = backupHdfsPath + "/" + orginalFileName;
					backup.add(backupHdfsFileFullPath);
				}
				hdfsHelper.moveFiles(old,backup);
			} catch(Exception e) {
				ErrorRecord.addError(e);
				LOG.error(e.getMessage(),e);
			}

		}

		@Override
		public void prepare() {
			// 如果path不存在，并且外部指定为自动创建，则进行创建操作，added by lubiao
			if (!hdfsHelper.isPathexists(path) && createPathIfNotExist) {
				hdfsHelper.createDir(path);

			}

			// 若路径已经存在，检查path是否是目录
			if (hdfsHelper.isPathexists(path)) {
				if (!hdfsHelper.isPathDir(path)) {
					throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
							String.format("您配置的path: [%s] 不是一个合法的目录, 请您注意文件重名, 不合法目录名等情况.", path));
				}
				//遍历目录下的文件，将旧的文件移动到一个备份目录下
				if(isPreDel) {
					preDel();
				}

				// 根据writeMode对目录下文件进行处理
				Path[] existFilePaths = hdfsHelper.hdfsDirList(path, fileName);
				boolean isExistFile = false;
				if (existFilePaths.length > 0) {
					isExistFile = true;
				}
				/**
				 * if ("truncate".equals(writeMode) && isExistFile ) {
				 * LOG.info(String .format(
				 * "由于您配置了writeMode truncate, 开始清理 [%s] 下面以 [%s] 开头的内容", path,
				 * fileName)); hdfsHelper.deleteFiles(existFilePaths); } else
				 */
				if ("append".equalsIgnoreCase(writeMode)) {
					LOG.info(String.format("由于您配置了writeMode append, 写入前不做清理工作, [%s] 目录下写入相应文件名前缀  [%s] 的文件", path,
							fileName));
				} else if ("nonconflict".equalsIgnoreCase(writeMode) && isExistFile) {
					LOG.info(String.format("由于您配置了writeMode nonConflict, 开始检查 [%s] 下面的内容", path));
					List<String> allFiles = new ArrayList<String>();
					for (Path eachFile : existFilePaths) {
						allFiles.add(eachFile.toString());
					}
					LOG.error(String.format("冲突文件列表为: [%s]", StringUtils.join(allFiles, ",")));
					ErrorRecord.addError(String.format("由于您配置了writeMode nonConflict,但您配置的path: [%s] 目录不为空, 下面存在其他文件或文件夹.", path));
					throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
							String.format("由于您配置了writeMode nonConflict,但您配置的path: [%s] 目录不为空, 下面存在其他文件或文件夹.", path));
				}
			} else {
				ErrorRecord.addError(String.format("您配置的path: [%s] 不存在, 请先在hive端创建对应的数据库和表.", path));
				throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
						String.format("您配置的path: [%s] 不存在, 请先在hive端创建对应的数据库和表.", path));
			}
		}

		@Override
		public void post() {
			HashSet<String> mergedTmpFiles = new HashSet<String>();
			HashSet<String> mergedEndFiles = new HashSet<String>();

			for (int i = 0; i < tmpFiles.size(); i++) {
				mergedTmpFiles.addAll(tmpFiles.get(i));
				mergedEndFiles.addAll(endFiles.get(i));
			}

			hdfsHelper.renameFile(mergedTmpFiles, mergedEndFiles);
		}

		@Override
		public void destroy() {
			hdfsHelper.closeFileSystem();
		}

		@Override
		public List<Configuration> split(int mandatoryNumber) {
			LOG.info("begin do split...");
			List<Configuration> writerSplitConfigs = new ArrayList<Configuration>();
			String filePrefix = fileName;

			Set<String> allFiles = new HashSet<String>();

			// 获取该路径下的所有已有文件列表
			if (hdfsHelper.isPathexists(path)) {
				allFiles.addAll(Arrays.asList(hdfsHelper.hdfsDirList(path)));
			}

			String fileSuffix;
			// 临时存放路径
			String storePath = buildTmpFilePath(this.path);
			// 最终存放路径
			String endStorePath = buildFilePath();
			this.path = endStorePath;
			for (int i = 0; i < mandatoryNumber; i++) {
				HashSet<String> taskTmpFiles = new HashSet<String>();
				HashSet<String> taskEndFiles = new HashSet<String>();
				Configuration splitedTaskConfig = this.writerSliceConfig.clone();

				for (int j = 0; j < parallelSize; j++) {
					String fullFileName = null;
					String endFullFileName = null;

					fileSuffix = UUID.randomUUID().toString().replace('-', '_');

					fullFileName = String.format("%s%s%s__%s", defaultFS, storePath, filePrefix, fileSuffix);
					endFullFileName = String.format("%s%s%s__%s", defaultFS, endStorePath, filePrefix, fileSuffix);

					// handle same file name
					while (allFiles.contains(endFullFileName)) {
						fileSuffix = UUID.randomUUID().toString().replace('-', '_');
						fullFileName = String.format("%s%s%s__%s", defaultFS, storePath, filePrefix, fileSuffix);
						endFullFileName = String.format("%s%s%s__%s", defaultFS, endStorePath, filePrefix, fileSuffix);
					}
					allFiles.add(endFullFileName);

					// 设置临时文件全路径和最终文件全路径
					if ("GZIP".equalsIgnoreCase(this.compress)) {
						taskTmpFiles.add(fullFileName + ".gz");
						taskEndFiles.add(endFullFileName + ".gz");
					} else if ("BZIP2".equalsIgnoreCase(compress)) {
						taskTmpFiles.add(fullFileName + ".bz2");
						taskEndFiles.add(endFullFileName + ".bz2");
					} else {
						taskTmpFiles.add(fullFileName);
						taskEndFiles.add(endFullFileName);
					}
				}

				this.tmpFiles.add(taskTmpFiles);
				this.endFiles.add(taskEndFiles);
				splitedTaskConfig.set(com.ucar.datalink.flinker.plugin.unstructuredstorage.writer.Key.FILE_NAME,
						new ArrayList<String>(taskTmpFiles));
				LOG.info(String.format("splited write file name:[%s]", taskTmpFiles));
				writerSplitConfigs.add(splitedTaskConfig);
			}
			LOG.info("end do split.");
			return writerSplitConfigs;
		}

		private String buildFilePath() {
			boolean isEndWithSeparator = false;
			switch (IOUtils.DIR_SEPARATOR) {
			case IOUtils.DIR_SEPARATOR_UNIX:
				isEndWithSeparator = this.path.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR));
				break;
			case IOUtils.DIR_SEPARATOR_WINDOWS:
				isEndWithSeparator = this.path.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_WINDOWS));
				break;
			default:
				break;
			}
			if (!isEndWithSeparator) {
				this.path = this.path + IOUtils.DIR_SEPARATOR;
			}
			return this.path;
		}

		/**
		 * 创建临时目录
		 *
		 * @param userPath
		 * @return
		 */
		private String buildTmpFilePath(String userPath) {
			String tmpFilePath;
			boolean isEndWithSeparator = false;
			switch (IOUtils.DIR_SEPARATOR) {
			case IOUtils.DIR_SEPARATOR_UNIX:
				isEndWithSeparator = userPath.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR));
				break;
			case IOUtils.DIR_SEPARATOR_WINDOWS:
				isEndWithSeparator = userPath.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_WINDOWS));
				break;
			default:
				break;
			}
			String tmpSuffix;
			tmpSuffix = UUID.randomUUID().toString().replace('-', '_');
			if (!isEndWithSeparator) {
				tmpFilePath = String.format("%s__%s%s", userPath, tmpSuffix, IOUtils.DIR_SEPARATOR);
			} else if ("/".equals(userPath)) {
				tmpFilePath = String.format("%s__%s%s", userPath, tmpSuffix, IOUtils.DIR_SEPARATOR);
			} else {
				tmpFilePath = String.format("%s__%s%s", userPath.substring(0, userPath.length() - 1), tmpSuffix,
						IOUtils.DIR_SEPARATOR);
			}
			while (hdfsHelper.isPathexists(tmpFilePath)) {
				tmpSuffix = UUID.randomUUID().toString().replace('-', '_');
				if (!isEndWithSeparator) {
					tmpFilePath = String.format("%s__%s%s", userPath, tmpSuffix, IOUtils.DIR_SEPARATOR);
				} else if ("/".equals(userPath)) {
					tmpFilePath = String.format("%s__%s%s", userPath, tmpSuffix, IOUtils.DIR_SEPARATOR);
				} else {
					tmpFilePath = String.format("%s__%s%s", userPath.substring(0, userPath.length() - 1), tmpSuffix,
							IOUtils.DIR_SEPARATOR);
				}
			}
			return tmpFilePath;
		}
	}

	public static class Task extends Writer.Task {
		private static final Logger LOG = LoggerFactory.getLogger(Task.class);

		private Configuration writerSliceConfig;

		private String defaultFS;
		private String hadoopUserName;// added by lubiao
		private Integer taskRecordSize;// added by lubiao
		private ExecutorService taskExecutor;// added by lubiao
		private String fileType;
		private List<String> fileName;
		private Integer errorRetryTimes;


		private HdfsHelper hdfsHelper = null;

		@Override
		public void init() {
			this.writerSliceConfig = this.getPluginJobConf();

			this.defaultFS = this.writerSliceConfig.getString(Key.DEFAULT_FS);
			this.hadoopUserName = this.writerSliceConfig.getString(Key.HADOOP_USER_NAME);
			this.taskRecordSize = this.writerSliceConfig.getInt(Key.TASK_RECORD_SIZE, 1000);
			this.fileType = this.writerSliceConfig.getString(Key.FILE_TYPE);
			this.fileName = this.writerSliceConfig.getList(Key.FILE_NAME, String.class);
			this.errorRetryTimes = this.writerSliceConfig.getInt(Key.ERROR_RETRY_TIMES);

			hdfsHelper = new HdfsHelper();
			hdfsHelper.errorRetryTimes = errorRetryTimes;
			hdfsHelper.getFileSystem(writerSliceConfig);
		}

		@Override
		public void prepare() {

		}

		@Override
		public void startWrite(RecordReceiver lineReceiver) {
			LOG.info("begin do write...");
			LOG.info(String.format("write to file : [%s]", this.fileName));

			if (this.fileName.size() == 1) {
				writeInternal(lineReceiver, fileName.get(0));
			} else {
				taskExecutor = Executors.newFixedThreadPool(this.fileName.size());
				int totalRecordSize = fileName.size() * taskRecordSize;
				ArrayList<Record> recordList = new ArrayList<Record>();
				Record record = null;
				while ((record = lineReceiver.getFromReader()) != null) {
					recordList.add(record);
					if (recordList.size() % totalRecordSize == 0) {
						parallelWrite(recordList);
						recordList.clear();
					}
				}
				if (!recordList.isEmpty()) {
					writeInternal(new TaskRecordReceiver(new LinkedList<Record>(recordList)), fileName.get(0));
				}
			}

			hdfsHelper.closeWriters();
			
			LOG.info("end do write");
		}

		@Override
		public void post() {

		}

		@Override
		public void destroy() {

		}

		private void parallelWrite(ArrayList<Record> recordList) {
			List<FutureTask<Exception>> tasks = new ArrayList<FutureTask<Exception>>();
			List<LinkedList<Record>> splittedList = split(recordList, taskRecordSize);
			for (int i = 0; i < fileName.size(); i++) {
				final FutureTask<Exception> task = createTask(new TaskRecordReceiver(splittedList.get(i)),
						fileName.get(i));
				taskExecutor.execute(task);
				tasks.add(task);
			}

			for (Future<Exception> task : tasks) {
				try {
					Exception ex = task.get();
					if (ex != null) {
						taskExecutor.shutdownNow();
						throw ex;
					}
				} catch (DataXException de) {
					ErrorRecord.addError(de);
					throw de;
				} catch (Exception e) {
					ErrorRecord.addError("something goew wrong when write record in parallel mode."+e.getMessage());
					throw new RuntimeException("something goew wrong when write record in parallel mode.", e);
				}
			}
		}

		public static <T> List<LinkedList<T>> split(ArrayList<T> list, int taskRecordSize) {
			List<LinkedList<T>> lists = new ArrayList<LinkedList<T>>();
			if (list != null && taskRecordSize > 0) {
				int splitSize = list.size() / taskRecordSize;

				for (int i = 0; i < splitSize; i++) {
					int fromIndex = i * taskRecordSize;
					int toIndex = fromIndex + taskRecordSize;
					lists.add(new LinkedList<T>(list.subList(fromIndex, toIndex)));
				}
			}
			return lists;
		}

		private FutureTask<Exception> createTask(final RecordReceiver lineReceiver, final String fileName) {
			return new FutureTask<Exception>(new Callable<Exception>() {
				@Override
				public Exception call() throws Exception {
					try {
						writeInternal(lineReceiver, fileName);
						return null;
					} catch (Exception e) {
						ErrorRecord.addError(e);
						return e;
					}
				}
			});
		}

		private void writeInternal(RecordReceiver lineReceiver, String fileName) {
			if (fileType.equalsIgnoreCase("TEXT")) {
				// 写TEXT FILE
				hdfsHelper.textFileStartWrite(lineReceiver, this.writerSliceConfig, fileName,
						this.getTaskPluginCollector());
			} else if (fileType.equalsIgnoreCase("ORC")) {
				// 写ORC FILE
				hdfsHelper.orcFileStartWrite(lineReceiver, this.writerSliceConfig, fileName,
						this.getTaskPluginCollector());
			}
		}
	}

	public static class TaskRecordReceiver implements RecordReceiver {
		private LinkedList<Record> recordList;

		public TaskRecordReceiver(LinkedList<Record> recordList) {
			this.recordList = recordList;
		}

		@Override
		public Record getFromReader() {
			if (recordList.isEmpty()) {
				return null;
			} else {
				return recordList.removeFirst();
			}
		}

		@Override
		public void shutdown() {

		}
	}
}
