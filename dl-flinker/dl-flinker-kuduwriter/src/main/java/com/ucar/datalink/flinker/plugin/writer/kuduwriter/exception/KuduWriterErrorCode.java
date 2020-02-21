package com.ucar.datalink.flinker.plugin.writer.kuduwriter.exception;

import com.ucar.datalink.flinker.api.spi.ErrorCode;

public enum KuduWriterErrorCode implements ErrorCode {

    CONFIG_INVALID_EXCEPTION("KuduWriter-00", "您的参数配置错误."),
    REQUIRED_VALUE("KuduWriter-01", "您缺失了必须填写的参数值."),
    ILLEGAL_VALUE("KuduWriter-02", "您填写的参数值不合法."),
    WRITER_FILE_WITH_CHARSET_ERROR("KuduWriter-03", "您配置的编码未能正常写入."),
    Write_FILE_IO_ERROR("KuduWriter-04", "您配置的文件在写入时出现IO异常."),
    WRITER_RUNTIME_EXCEPTION("KuduWriter-05", "出现运行时异常, 请联系我们."),
    CONNECT_HDFS_IO_ERROR("KuduWriter-06", "与Kudu建立连接时出现IO异常."),
    COLUMN_REQUIRED_VALUE("KuduWriter-07", "您column配置中缺失了必须填写的参数值."),
    COLUMN_TYPE_PARSE_ERROR("KuduWriter-08", "您原端数据类型与目标端数据类型不匹配."),
    COLUMN_WRITER_ERROR("KuduWriter-09", "写入数据异常.");

    private String code;
    private String description;

    KuduWriterErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
