package com.ucar.datalink.flinker.plugin.unstructuredstorage.reader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/*import org.anarres.lzo.LzoDecompressor1z_safe;
import org.anarres.lzo.LzoInputStream;
import org.anarres.lzo.LzopInputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;*/
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
/*//import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
//import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;*/
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucar.datalink.flinker.api.element.BoolColumn;
import com.ucar.datalink.flinker.api.element.Column;
import com.ucar.datalink.flinker.api.element.DateColumn;
import com.ucar.datalink.flinker.api.element.DoubleColumn;
import com.ucar.datalink.flinker.api.element.LongColumn;
import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.element.StringColumn;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordSender;
import com.ucar.datalink.flinker.api.plugin.TaskPluginCollector;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.csvreader.CsvReader;

public class UnstructuredStorageReaderUtil {
	private static final Logger LOG = LoggerFactory
			.getLogger(UnstructuredStorageReaderUtil.class);

	private UnstructuredStorageReaderUtil() {

	}

	/**
	 * @param inputLine
	 *            输入待分隔字符串
	 * @param delimiter
	 *            字符串分割符
	 * @return 分隔符分隔后的字符串数组，出现异常时返回为null 支持转义，即数据中可包含分隔符
	 * */
	public static String[] splitOneLine(String inputLine, char delimiter) {
		String[] splitedResult = null;
		if (null != inputLine) {
			try {
				CsvReader csvReader = new CsvReader(new StringReader(inputLine));
				csvReader.setDelimiter(delimiter);
				if (csvReader.readRecord()) {
					splitedResult = csvReader.getValues();
				}
			} catch (IOException e) {
				// nothing to do
			}
		}
		return splitedResult;
	}

	/**
	 * 不支持转义
	 * 
	 * @return 分隔符分隔后的字符串数，
	 * */
	public static String[] splitOneLine(String inputLine, String delimiter) {
		String[] splitedResult = StringUtils.split(inputLine, delimiter);
		return splitedResult;
	}

	public static void readFromStream(InputStream inputStream, String context,
			Configuration readerSliceConfig, RecordSender recordSender,
			TaskPluginCollector taskPluginCollector) {
		String compress = readerSliceConfig.getString(Key.COMPRESS, null);
		if (StringUtils.isBlank(compress)) {
			compress = null;
		}
		String encoding = readerSliceConfig.getString(Key.ENCODING,
				Constant.DEFAULT_ENCODING);
		// handle blank encoding
		if (StringUtils.isBlank(encoding)) {
			encoding = Constant.DEFAULT_ENCODING;
			LOG.warn(String.format("您配置的encoding为[%s], 使用默认值[%s]", encoding,
					Constant.DEFAULT_ENCODING));
		}

		List<Configuration> column = readerSliceConfig
				.getListConfiguration(Key.COLUMN);
		// handle ["*"] -> [], null
		if (null != column && 1 == column.size()
				&& "\"*\"".equals(column.get(0).toString())) {
			readerSliceConfig.set(Key.COLUMN, null);
			column = null;
		}

		BufferedReader reader = null;
		// compress logic
		try {
			if (null == compress) {
				reader = new BufferedReader(new InputStreamReader(inputStream,
						encoding));
			} else {
				// TODO compress
				/*if ("lzo".equalsIgnoreCase(compress)) {
					LzoInputStream lzoInputStream = new LzoInputStream(
							inputStream, new LzoDecompressor1z_safe());
					reader = new BufferedReader(new InputStreamReader(
							lzoInputStream, encoding));
				} else if ("lzop".equalsIgnoreCase(compress)) {
					LzoInputStream lzopInputStream = new LzopInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							lzopInputStream, encoding));
				} else */if ("gzip".equalsIgnoreCase(compress)) {
					CompressorInputStream compressorInputStream = new GzipCompressorInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							compressorInputStream, encoding));
				} else if ("bzip2".equalsIgnoreCase(compress)) {
					CompressorInputStream compressorInputStream = new BZip2CompressorInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							compressorInputStream, encoding));
				} /*else if ("lzma".equalsIgnoreCase(compress)) {
					CompressorInputStream compressorInputStream = new LZMACompressorInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							compressorInputStream, encoding));
				} *//*else if ("pack200".equalsIgnoreCase(compress)) {
					CompressorInputStream compressorInputStream = new Pack200CompressorInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							compressorInputStream, encoding));
				} *//*else if ("snappy".equalsIgnoreCase(compress)) {
					CompressorInputStream compressorInputStream = new SnappyCompressorInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							compressorInputStream, encoding));
				} *//*else if ("xz".equalsIgnoreCase(compress)) {
					CompressorInputStream compressorInputStream = new XZCompressorInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							compressorInputStream, encoding));
				} else if ("ar".equalsIgnoreCase(compress)) {
					ArArchiveInputStream arArchiveInputStream = new ArArchiveInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							arArchiveInputStream, encoding));
				} else if ("arj".equalsIgnoreCase(compress)) {
					ArjArchiveInputStream arjArchiveInputStream = new ArjArchiveInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							arjArchiveInputStream, encoding));
				} else if ("cpio".equalsIgnoreCase(compress)) {
					CpioArchiveInputStream cpioArchiveInputStream = new CpioArchiveInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							cpioArchiveInputStream, encoding));
				} else if ("dump".equalsIgnoreCase(compress)) {
					DumpArchiveInputStream dumpArchiveInputStream = new DumpArchiveInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							dumpArchiveInputStream, encoding));
				} else if ("jar".equalsIgnoreCase(compress)) {
					JarArchiveInputStream jarArchiveInputStream = new JarArchiveInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							jarArchiveInputStream, encoding));
				} else if ("tar".equalsIgnoreCase(compress)) {
					TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							tarArchiveInputStream, encoding));
				} else if ("zip".equalsIgnoreCase(compress)) {
					ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(
							inputStream);
					reader = new BufferedReader(new InputStreamReader(
							zipArchiveInputStream, encoding));
				}*/ else {
					throw DataXException
							.asDataXException(
									UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
									String.format(
											"仅支持 gzip, bzip2 文件压缩格式 , 不支持您配置的文件压缩格式: [%s]",
											compress));
				}
			}
			UnstructuredStorageReaderUtil.doReadFromStream(reader, context,
					readerSliceConfig, recordSender, taskPluginCollector);
		} catch (UnsupportedEncodingException uee) {
			throw DataXException
					.asDataXException(
							UnstructuredStorageReaderErrorCode.OPEN_FILE_WITH_CHARSET_ERROR,
							String.format("不支持的编码格式 : [%]", encoding), uee);
		} catch (NullPointerException e) {
			throw DataXException.asDataXException(
					UnstructuredStorageReaderErrorCode.RUNTIME_EXCEPTION,
					"运行时错误, 请联系我们", e);
		}/* catch (ArchiveException e) {
			throw DataXException.asDataXException(
					UnstructuredStorageReaderErrorCode.READ_FILE_IO_ERROR,
					String.format("压缩文件流读取错误 : [%]", context), e);
		} */catch (IOException e) {
			throw DataXException.asDataXException(
					UnstructuredStorageReaderErrorCode.READ_FILE_IO_ERROR,
					String.format("流读取错误 : [%]", context), e);
		} finally {
			IOUtils.closeQuietly(reader);
		}

	}

	public static void doReadFromStream(BufferedReader reader, String context,
			Configuration readerSliceConfig, RecordSender recordSender,
			TaskPluginCollector taskPluginCollector) {
		List<Configuration> column = readerSliceConfig
				.getListConfiguration(Key.COLUMN);
		String encoding = readerSliceConfig.getString(Key.ENCODING,
				Constant.DEFAULT_ENCODING);
		Character fieldDelimiter = null;
		String delimiterInStr = readerSliceConfig
				.getString(Key.FIELD_DELIMITER);
		if (null != delimiterInStr && 1 != delimiterInStr.length()) {
			throw DataXException.asDataXException(
					UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
					String.format("仅仅支持单字符切分, 您配置的切分为 : [%s]", delimiterInStr));
		}
		if (null == delimiterInStr) {
			LOG.warn(String.format("您没有配置列分隔符, 使用默认值[%s]",
					Constant.DEFAULT_FIELD_DELIMITER));
		}

		// warn: default value ',', fieldDelimiter could be \n(lineDelimiter)
		// for no fieldDelimiter
		fieldDelimiter = readerSliceConfig.getChar(Key.FIELD_DELIMITER,
				Constant.DEFAULT_FIELD_DELIMITER);
		Boolean skipHeader = readerSliceConfig.getBool(Key.SKIP_HEADER,
				Constant.DEFAULT_SKIP_HEADER);
		// warn: no default value '\N'
		String nullFormat = readerSliceConfig.getString(Key.NULL_FORMAT);
		// every line logic
		try {
			String fetchLine = null;
			// TODO lineDelimiter
			if (skipHeader) {
				fetchLine = reader.readLine();
				LOG.info("Header line has been skiped.");
			}
			while ((fetchLine = reader.readLine()) != null) {
				String[] splitedStrs = null;
				if (null == fieldDelimiter) {
					splitedStrs = new String[] { fetchLine };
				} else {
					splitedStrs = UnstructuredStorageReaderUtil.splitOneLine(
							fetchLine, fieldDelimiter);
				}
				UnstructuredStorageReaderUtil.transportOneRecord(recordSender,
						column, splitedStrs, nullFormat, taskPluginCollector);
			}
		} catch (UnsupportedEncodingException uee) {
			throw DataXException
					.asDataXException(
							UnstructuredStorageReaderErrorCode.OPEN_FILE_WITH_CHARSET_ERROR,
							String.format("不支持的编码格式 : [%]", encoding), uee);
		} catch (FileNotFoundException fnfe) {
			throw DataXException.asDataXException(
					UnstructuredStorageReaderErrorCode.FILE_NOT_EXISTS,
					String.format("无法找到文件 : [%s]", context), fnfe);
		} catch (IOException ioe) {
			throw DataXException.asDataXException(
					UnstructuredStorageReaderErrorCode.READ_FILE_IO_ERROR,
					String.format("读取文件错误 : [%s]", context), ioe);
		} catch (Exception e) {
			throw DataXException.asDataXException(
					UnstructuredStorageReaderErrorCode.RUNTIME_EXCEPTION,
					String.format("运行时异常 : %s", e.getMessage()), e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private static Record transportOneRecord(RecordSender recordSender,
			List<Configuration> columnConfigs, String[] sourceLine,
			String nullFormat, TaskPluginCollector taskPluginCollector) {
		Record record = recordSender.createRecord();
		Column columnGenerated = null;

		// 创建都为String类型column的record
		if (null == columnConfigs || columnConfigs.size() == 0) {
			for (String columnValue : sourceLine) {
				// not equalsIgnoreCase, it's all ok if nullFormat is null
				if (columnValue.equals(nullFormat)) {
					columnGenerated = new StringColumn(null);
				} else {
					columnGenerated = new StringColumn(columnValue);
				}
				record.addColumn(columnGenerated);
			}
			recordSender.sendToWriter(record);
		} else {
			try {
				for (Configuration columnConfig : columnConfigs) {
					String columnType = columnConfig
							.getNecessaryValue(
									Key.TYPE,
									UnstructuredStorageReaderErrorCode.CONFIG_INVALID_EXCEPTION);
					Integer columnIndex = columnConfig.getInt(Key.INDEX);
					String columnConst = columnConfig.getString(Key.VALUE);

					String columnValue = null;

					if (null == columnIndex && null == columnConst) {
						throw DataXException
								.asDataXException(
										UnstructuredStorageReaderErrorCode.NO_INDEX_VALUE,
										"由于您配置了type, 则至少需要配置 index 或 value");
					}

					if (null != columnIndex && null != columnConst) {
						throw DataXException
								.asDataXException(
										UnstructuredStorageReaderErrorCode.MIXED_INDEX_VALUE,
										"您混合配置了index, value, 每一列同时仅能选择其中一种");
					}

					if (null != columnIndex) {
						if (columnIndex >= sourceLine.length) {
							String message = String
									.format("您尝试读取的列越界,源文件该行有 [%s] 列,您尝试读取第 [%s] 列, 数据详情[%s]",
											sourceLine.length, columnIndex + 1,
											sourceLine);
							LOG.warn(message);
							throw new IndexOutOfBoundsException(message);
						}

						columnValue = sourceLine[columnIndex];
					} else {
						columnValue = columnConst;
					}
					Type type = Type.valueOf(columnType.toUpperCase());
					// it's all ok if nullFormat is null
					if (columnValue.equals(nullFormat)) {
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
								String formatString = columnConfig
										.getString(Key.FORMAT);
								//if (null != formatString) {
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
						throw DataXException
								.asDataXException(
										UnstructuredStorageReaderErrorCode.NOT_SUPPORT_TYPE,
										errorMessage);
					}

					record.addColumn(columnGenerated);

				}
				recordSender.sendToWriter(record);
			} catch (IllegalArgumentException iae) {
				taskPluginCollector
						.collectDirtyRecord(record, iae.getMessage());
			} catch (IndexOutOfBoundsException ioe) {
				taskPluginCollector
						.collectDirtyRecord(record, ioe.getMessage());
			} catch (Exception e) {
				if (e instanceof DataXException) {
					throw (DataXException) e;
				}
				// 每一种转换失败都是脏数据处理,包括数字格式 & 日期格式
				taskPluginCollector.collectDirtyRecord(record, e.getMessage());
			}
		}
		
		return record;
	}

	private enum Type {
		STRING, LONG, BOOLEAN, DOUBLE, DATE, ;
	}
	
	/**
     * check parameter:encoding, compress, filedDelimiter
     * */
	public static void validateParameter(Configuration readerConfiguration) {
	
		// encoding check
		 String encoding = readerConfiguration.getUnnecessaryValue(
					com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.ENCODING,
					com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Constant.DEFAULT_ENCODING,null);
		 try {
             encoding = encoding.trim();
             readerConfiguration.set(Key.ENCODING, encoding);
             Charsets.toCharset(encoding);
         } catch (UnsupportedCharsetException uce) {
				throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
						String.format("不支持您配置的编码格式 : [%s]", encoding), uce);
		} catch (Exception e) {
				throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.CONFIG_INVALID_EXCEPTION,
						String.format("编码配置异常, 请联系我们: %s", e.getMessage()), e);
		}
		 
		 //only support compress types
		 String compress =readerConfiguration
					.getUnnecessaryValue(com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.COMPRESS,null,null);
			if(compress != null){
				compress = compress.toLowerCase().trim();
				boolean compressTag = "gzip".equals(compress) || "bzip2".equals(compress);
				if (!compressTag) {
					throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
							String.format("仅支持 gzip, bzip2 文件压缩格式 , 不支持您配置的文件压缩格式: [%s]", compress));
				}
			}		
			readerConfiguration.set(com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.COMPRESS, compress);
			
			//fieldDelimiter check
			String delimiterInStr = readerConfiguration.getString(com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.FIELD_DELIMITER,null);
			if(null == delimiterInStr){
				throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.REQUIRED_VALUE,
						String.format("您提供配置文件有误，[%s]是必填参数.",
								com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.FIELD_DELIMITER));
			}else if(1 != delimiterInStr.length()){
				// warn: if have, length must be one
				throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
						String.format("仅仅支持单字符切分, 您配置的切分为 : [%s]", delimiterInStr));
			}

			// column: 1. index type 2.value type 3.when type is Date, may have
			// format
			List<Configuration> columns = readerConfiguration
					.getListConfiguration(com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.COLUMN);
			if (null == columns || columns.size() == 0) {
				throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.REQUIRED_VALUE, "您需要指定 columns");
			}
			// handle ["*"]
			if (null != columns && 1 == columns.size()) {
				String columnsInStr = columns.get(0).toString();
				if ("\"*\"".equals(columnsInStr) || "'*'".equals(columnsInStr)) {
					readerConfiguration.set(com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.COLUMN, null);
					columns = null;
				}
			}
	
			if (null != columns && columns.size() != 0) {
				for (Configuration eachColumnConf : columns) {
					eachColumnConf.getNecessaryValue(com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.TYPE,
							UnstructuredStorageReaderErrorCode.REQUIRED_VALUE);
					Integer columnIndex = eachColumnConf
							.getInt(com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.INDEX);
					String columnValue = eachColumnConf
							.getString(com.ucar.datalink.flinker.plugin.unstructuredstorage.reader.Key.VALUE);
	
					if (null == columnIndex && null == columnValue) {
						throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.NO_INDEX_VALUE,
								"由于您配置了type, 则至少需要配置 index 或 value");
					}
	
					if (null != columnIndex && null != columnValue) {
						throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.MIXED_INDEX_VALUE,
								"您混合配置了index, value, 每一列同时仅能选择其中一种");
					}
					if (null != columnIndex && columnIndex < 0) {
						throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
								String.format("index需要大于等于0, 您配置的index为[%s]", columnIndex));
					}
				}
			}

	}
	
	/**
	 * 
	* @Title: getRegexPathParent 
	* @Description: 获取正则表达式目录的父目录
	* @param @param regexPath
	* @param @return     
	* @return String 
	* @throws
	 */
	public static String getRegexPathParent(String regexPath){
		int endMark;
		for (endMark = 0; endMark < regexPath.length(); endMark++) {
			if ('*' != regexPath.charAt(endMark) && '?' != regexPath.charAt(endMark)) {
				continue;
			} else {
				break;
			}
		}
		int lastDirSeparator = regexPath.substring(0, endMark).lastIndexOf(IOUtils.DIR_SEPARATOR);
		String parentPath  = regexPath.substring(0,lastDirSeparator + 1);
		
		return  parentPath;	
	}
	/**
	 * 
	* @Title: getRegexPathParentPath 
	* @Description: 获取含有通配符路径的父目录，目前只支持在最后一级目录使用通配符*或者?.
	* (API jcraft.jsch.ChannelSftp.ls(String path)函数限制)  http://epaul.github.io/jsch-documentation/javadoc/
	* @param @param regexPath
	* @param @return     
	* @return String 
	* @throws
	 */
	public static String getRegexPathParentPath(String regexPath){
		int lastDirSeparator = regexPath.lastIndexOf(IOUtils.DIR_SEPARATOR);
		String parentPath = "";
		parentPath = regexPath.substring(0,lastDirSeparator + 1);
		if(parentPath.contains("*") || parentPath.contains("?")){
			throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
					String.format("配置项目path中：[%s]不合法，目前只支持在最后一级目录使用通配符*或者?", regexPath));
		}
		return parentPath;
	}
	
	

	public static void main(String args[]) {
		while (true) {
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(System.in);
			String inputString = sc.nextLine();
			String delemiter = sc.nextLine();
			if (delemiter.length() == 0) {
				break;
			}
			if (!inputString.equals("exit")) {
				String[] result = UnstructuredStorageReaderUtil.splitOneLine(
						inputString, delemiter.charAt(0));
				for (String str : result) {
					System.out.print(str + " ");
				}
				System.out.println();
			} else {
				break;
			}
		}
	}
}
