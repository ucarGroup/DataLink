package com.ucar.datalink.flinker.plugin.reader.hbasereader98;

import com.ucar.datalink.flinker.api.spi.ErrorCode;

public enum HbaseReaderErrorCode implements ErrorCode {
    REQUIRED_VALUE("HbaseReader98-00", "您缺失了必须填写的参数值."),
    ILLEGAL_VALUE("HbaseReader98-01", "您配置的值不合法."),
    PREPAR_READ_ERROR("HbaseReader98-02", "准备读取 Hbase 时出错."),
    SPLIT_ERROR("HbaseReader98-03", "切分 Hbase 表时出错."),
    INIT_TABLE_ERROR("HbaseReader98-04", "初始化 Hbase 抽取表时出错."),

    ;

    private final String code;
    private final String description;

    private HbaseReaderErrorCode(String code, String description) {
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
        return String.format("Code:[%s], Description:[%s]. ", this.code,
                this.description);
    }
}
