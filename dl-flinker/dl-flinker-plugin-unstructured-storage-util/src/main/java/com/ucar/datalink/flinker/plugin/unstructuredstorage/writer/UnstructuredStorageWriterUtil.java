package com.ucar.datalink.flinker.plugin.unstructuredstorage.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.anarres.lzo.LzoCompressor1x_1;
import org.anarres.lzo.LzoOutputStream;
import org.anarres.lzo.LzopOutputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucar.datalink.flinker.api.element.Column;
import com.ucar.datalink.flinker.api.element.DateColumn;
import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordReceiver;
import com.ucar.datalink.flinker.api.plugin.TaskPluginCollector;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.csvreader.CsvWriter;
import com.google.common.collect.Sets;

public class UnstructuredStorageWriterUtil {
    private UnstructuredStorageWriterUtil() {

    }

    private static final Logger LOG = LoggerFactory
            .getLogger(UnstructuredStorageWriterUtil.class);

    /**
     * check parameter: writeMode, encoding, compress, filedDelimiter
     * */
    public static void validateParameter(Configuration writerConfiguration) {
        // writeMode check
        String writeMode = writerConfiguration.getNecessaryValue(
                Key.WRITE_MODE,
                UnstructuredStorageWriterErrorCode.REQUIRED_VALUE);
        writeMode = writeMode.trim();
        Set<String> supportedWriteModes = Sets.newHashSet("truncate", "append",
                "nonConflict");
        if (!supportedWriteModes.contains(writeMode)) {
            throw DataXException
                    .asDataXException(
                            UnstructuredStorageWriterErrorCode.ILLEGAL_VALUE,
                            String.format(
                                    "仅支持 truncate, append, nonConflict 三种模式, 不支持您配置的 writeMode 模式 : [%s]",
                                    writeMode));
        }
        writerConfiguration.set(Key.WRITE_MODE, writeMode);

        // encoding check
        String encoding = writerConfiguration.getString(Key.ENCODING);
        if (StringUtils.isBlank(encoding)) {
            // like "  ", null
            LOG.warn(String.format("您的encoding配置为空, 将使用默认值[%s]",
                    Constant.DEFAULT_ENCODING));
            writerConfiguration.set(Key.ENCODING, Constant.DEFAULT_ENCODING);
        } else {
            try {
                encoding = encoding.trim();
                writerConfiguration.set(Key.ENCODING, encoding);
                Charsets.toCharset(encoding);
            } catch (Exception e) {
                throw DataXException.asDataXException(
                        UnstructuredStorageWriterErrorCode.ILLEGAL_VALUE,
                        String.format("不支持您配置的编码格式:[%s]", encoding), e);
            }
        }

        // only support compress types
        String compress = writerConfiguration.getString(Key.COMPRESS);
        if (StringUtils.isBlank(compress)) {
            writerConfiguration.set(Key.COMPRESS, null);
        } else {
            Set<String> supportedCompress = Sets.newHashSet("gzip", "bzip2");
            if (!supportedCompress.contains(compress.toLowerCase().trim())) {
                String message = String.format(
                        "仅支持 [%s] 文件压缩格式 , 不支持您配置的文件压缩格式: [%s]",
                        StringUtils.join(supportedCompress, ","));
                throw DataXException.asDataXException(
                        UnstructuredStorageWriterErrorCode.ILLEGAL_VALUE,
                        String.format(message, compress));
            }
        }

        // fieldDelimiter check
        String delimiterInStr = writerConfiguration
                .getString(Key.FIELD_DELIMITER);
        // warn: if have, length must be one
        if (null != delimiterInStr && 1 != delimiterInStr.length()) {
            throw DataXException.asDataXException(
                    UnstructuredStorageWriterErrorCode.ILLEGAL_VALUE,
                    String.format("仅仅支持单字符切分, 您配置的切分为 : [%s]", delimiterInStr));
        }
        if (null == delimiterInStr) {
            LOG.warn(String.format("您没有配置列分隔符, 使用默认值[%s]",
                    Constant.DEFAULT_FIELD_DELIMITER));
            writerConfiguration.set(Key.FIELD_DELIMITER,
                    Constant.DEFAULT_FIELD_DELIMITER);
        }

        // fileFormat check
        String fileFormat = writerConfiguration.getString(Key.FILE_FORMAT,
                Constant.FILE_FORMAT_TEXT);
        if (!Constant.FILE_FORMAT_CSV.equals(fileFormat)
                && !Constant.FILE_FORMAT_TEXT.equals(fileFormat)) {
            throw DataXException.asDataXException(
                    UnstructuredStorageWriterErrorCode.ILLEGAL_VALUE,
                    String.format("您配置的fileFormat [%s]错误, 支持csv, plainText两种.",
                            fileFormat));
        }
    }

    public static void writeToStream(RecordReceiver lineReceiver,
            OutputStream outputStream, Configuration config, String context,
            TaskPluginCollector taskPluginCollector) {
        String encoding = config.getString(Key.ENCODING,
                Constant.DEFAULT_ENCODING);
        // handle blank encoding
        if (StringUtils.isBlank(encoding)) {
            LOG.warn(String.format("您配置的encoding为[%s], 使用默认值[%s]", encoding,
                    Constant.DEFAULT_ENCODING));
            encoding = Constant.DEFAULT_ENCODING;
        }
        String compress = config.getString(Key.COMPRESS);

        BufferedWriter writer = null;
        // compress logic
        try {
            if (null == compress) {
                writer = new BufferedWriter(new OutputStreamWriter(
                        outputStream, encoding));
            } else {
                // TODO compress
                if ("lzo".equalsIgnoreCase(compress)) {

                    LzoOutputStream lzoOutputStream = new LzoOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            lzoOutputStream, encoding));
                } else if ("lzop".equalsIgnoreCase(compress)) {
                    LzoOutputStream lzopOutputStream = new LzopOutputStream(
                            outputStream, new LzoCompressor1x_1());
                    writer = new BufferedWriter(new OutputStreamWriter(
                            lzopOutputStream, encoding));
                } else if ("gzip".equalsIgnoreCase(compress)) {
                    CompressorOutputStream compressorOutputStream = new GzipCompressorOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            compressorOutputStream, encoding));
                } else if ("bzip2".equalsIgnoreCase(compress)) {
                    CompressorOutputStream compressorOutputStream = new BZip2CompressorOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            compressorOutputStream, encoding));
                } else if ("pack200".equalsIgnoreCase(compress)) {
                    CompressorOutputStream compressorOutputStream = new Pack200CompressorOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            compressorOutputStream, encoding));
                } else if ("xz".equalsIgnoreCase(compress)) {
                    CompressorOutputStream compressorOutputStream = new XZCompressorOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            compressorOutputStream, encoding));
                } else if ("ar".equalsIgnoreCase(compress)) {
                    ArArchiveOutputStream arArchiveOutputStream = new ArArchiveOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            arArchiveOutputStream, encoding));
                } else if ("cpio".equalsIgnoreCase(compress)) {
                    CpioArchiveOutputStream cpioArchiveOutputStream = new CpioArchiveOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            cpioArchiveOutputStream, encoding));
                } else if ("jar".equalsIgnoreCase(compress)) {
                    JarArchiveOutputStream jarArchiveOutputStream = new JarArchiveOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            jarArchiveOutputStream, encoding));
                } else if ("tar".equalsIgnoreCase(compress)) {
                    TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            tarArchiveOutputStream, encoding));
                } else if ("zip".equalsIgnoreCase(compress)) {
                    ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(
                            outputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(
                            zipArchiveOutputStream, encoding));
                } else {
                    throw DataXException
                            .asDataXException(
                                    UnstructuredStorageWriterErrorCode.ILLEGAL_VALUE,
                                    String.format(
                                            "仅支持 lzo, lzop, gzip, bzip2, pack200, xz, ar, cpio, jar, tar, zip 文件压缩格式 , 不支持您配置的文件压缩格式: [%s]",
                                            compress));
                }
            }
            UnstructuredStorageWriterUtil.doWriteToStream(lineReceiver, writer,
                    context, config, taskPluginCollector);
        } catch (UnsupportedEncodingException uee) {
            throw DataXException
                    .asDataXException(
                            UnstructuredStorageWriterErrorCode.Write_FILE_WITH_CHARSET_ERROR,
                            String.format("不支持的编码格式 : [%]", encoding), uee);
        } catch (NullPointerException e) {
            throw DataXException.asDataXException(
                    UnstructuredStorageWriterErrorCode.RUNTIME_EXCEPTION,
                    "运行时错误, 请联系我们", e);
        } catch (IOException e) {
            throw DataXException.asDataXException(
                    UnstructuredStorageWriterErrorCode.Write_FILE_IO_ERROR,
                    String.format("流写入错误 : [%]", context), e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private static void doWriteToStream(RecordReceiver lineReceiver,
            BufferedWriter writer, String contex, Configuration config,
            TaskPluginCollector taskPluginCollector) throws IOException {

        String nullFormat = config.getString(Key.NULL_FORMAT);

        // 兼容format & dataFormat
        String dateFormat = config.getString(Key.DATE_FORMAT);

        // warn: default false
        String fileFormat = config.getString(Key.FILE_FORMAT,
                Constant.FILE_FORMAT_TEXT);

        String delimiterInStr = config.getString(Key.FIELD_DELIMITER);
        if (null != delimiterInStr && 1 != delimiterInStr.length()) {
            throw DataXException.asDataXException(
                    UnstructuredStorageWriterErrorCode.ILLEGAL_VALUE,
                    String.format("仅仅支持单字符切分, 您配置的切分为 : [%]", delimiterInStr));
        }
        if (null == delimiterInStr) {
            LOG.warn(String.format("您没有配置列分隔符, 使用默认值[%s]",
                    Constant.DEFAULT_FIELD_DELIMITER));
        }

        // warn: fieldDelimiter could not be '' for no fieldDelimiter
        char fieldDelimiter = config.getChar(Key.FIELD_DELIMITER,
                Constant.DEFAULT_FIELD_DELIMITER);

        List<String> headers = config.getList(Key.HEADER, String.class);
        if (null != headers && !headers.isEmpty()) {
            writer.write(UnstructuredStorageWriterUtil.doTransportOneRecord(
                    headers, fieldDelimiter, fileFormat));
        }

        Record record = null;
        while ((record = lineReceiver.getFromReader()) != null) {
            MutablePair<String, Boolean> transportResult = UnstructuredStorageWriterUtil
                    .transportOneRecord(record, nullFormat, dateFormat,
                            fieldDelimiter, fileFormat, taskPluginCollector);
            if (!transportResult.getRight()) {
                writer.write(transportResult.getLeft());
            }
        }
    }

    /**
     * @return MutablePair<String, Boolean> left: formated data line; right: is
     *         dirty data or not, true means meeting dirty data
     * */
    public static MutablePair<String, Boolean> transportOneRecord(
            Record record, String nullFormat, String dateFormat,
            char fieldDelimiter, String fileFormat,
            TaskPluginCollector taskPluginCollector) {
        // warn: default is null
        if (null == nullFormat) {
            nullFormat = "null";
        }
        MutablePair<String, Boolean> transportResult = new MutablePair<String, Boolean>();
        transportResult.setRight(false);
        List<String> splitedRows = new ArrayList<String>();
        int recordLength = record.getColumnNumber();
        if (0 != recordLength) {
            Column column;
            for (int i = 0; i < recordLength; i++) {
                column = record.getColumn(i);
                if (null != column.getRawData()) {
                    boolean isDateColumn = column instanceof DateColumn;
                    if (!isDateColumn) {
                        splitedRows.add(column.asString());
                    } else {
                        // if (null != dateFormat) {
                        if (StringUtils.isNotBlank(dateFormat)) {
                            try {
                                SimpleDateFormat dateParse = new SimpleDateFormat(
                                        dateFormat);
                                splitedRows.add(dateParse.format(column
                                        .asDate()));
                            } catch (Exception e) {
                                // warn: 此处认为似乎脏数据
                                String message = String.format(
                                        "使用您配置的格式 [%s] 转换 [%s] 错误.",
                                        dateFormat, column.asString());
                                taskPluginCollector.collectDirtyRecord(record,
                                        message);
                                transportResult.setRight(true);
                                break;
                            }
                        } else {
                            splitedRows.add(column.asString());
                        }
                    }
                } else {
                    // warn: it's all ok if nullFormat is null
                    splitedRows.add(nullFormat);
                }
            }
        }

        transportResult.setLeft(UnstructuredStorageWriterUtil
                .doTransportOneRecord(splitedRows, fieldDelimiter, fileFormat));
        return transportResult;
    }

    public static String doTransportOneRecord(List<String> splitedRows,
            char fieldDelimiter, String fileFormat) {
        if (splitedRows.isEmpty()) {
            LOG.info("Found one record line which is empty.");
        }
        // warn: false means plain text(old way), true means strict csv format
        if (Constant.FILE_FORMAT_TEXT.equals(fileFormat)) {
            return StringUtils.join(splitedRows, fieldDelimiter)
                    + IOUtils.LINE_SEPARATOR;
        } else {
            StringWriter sw = new StringWriter();
            CsvWriter csvWriter = new CsvWriter(sw, fieldDelimiter);
            csvWriter.setTextQualifier('"');
            csvWriter.setUseTextQualifier(true);
            // warn: in linux is \n , in windows is \r\n
            csvWriter.setRecordDelimiter(IOUtils.LINE_SEPARATOR.charAt(0));
            UnstructuredStorageWriterUtil.csvWriteSlience(csvWriter,
                    splitedRows);
            return sw.toString();
            // sw.close(); //no need do this
        }
    }

    private static void csvWriteSlience(CsvWriter csvWriter,
            List<String> splitedRows) {
        try {
            csvWriter
                    .writeRecord((String[]) splitedRows.toArray(new String[0]));
        } catch (IOException e) {
            // shall not happen
            throw DataXException.asDataXException(
                    UnstructuredStorageWriterErrorCode.RUNTIME_EXCEPTION,
                    String.format("转换CSV格式失败[%s]",
                            StringUtils.join(splitedRows, " ")));
        }
    }
}
