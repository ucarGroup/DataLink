package com.ucar.datalink.flinker.plugin.writer.eswriter;

import com.ucar.datalink.flinker.api.spi.ErrorCode;

/**
 * Created by yw.zhang02 on 2016/7/27.
 */
public enum EsWriterErrorCode implements ErrorCode {

    CONFIG_INVALID_EXCEPTION("EsWriter-00", "您的参数配置错误."),
    REQUIRED_VALUE("EsWriter-01", "您缺失了必须填写的参数值."),
    ILLEGAL_VALUE("EsWriter-02", "您填写的参数值不合法."),
    WRITER_FILE_WITH_CHARSET_ERROR("EsWriter-03", "您配置的编码未能正常写入."),
    Write_FILE_IO_ERROR("EsWriter-04", "您配置的文件在写入时出现IO异常."),
    WRITER_RUNTIME_EXCEPTION("EsWriter-05", "出现运行时异常, 请联系我们."),
    CONNECT_HDFS_IO_ERROR("EsWriter-06", "与ES建立连接时出现IO异常."),
    COLUMN_REQUIRED_VALUE("EsWriter-07", "您column配置中缺失了必须填写的参数值."),
    ES_ROUTING_EXCEPTION("EsWriter-08", "走es routing逻辑，routing字段没有值，且不能忽略，需要人为介入.");

    private final String code;
    private final String description;

    EsWriterErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Description:[%s].", this.code,
                this.description);
    }

}
