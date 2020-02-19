package com.ucar.datalink.flinker.plugin.reader.esreader;

import com.ucar.datalink.flinker.api.spi.ErrorCode;

/**
 * Created by hechaoyi on 16/8/23.
 */
public enum EsReaderErrorCode implements ErrorCode {

    REQUIRED_VALUE("EsReader-01", "您缺失了必须填写的参数值."),
    MAPPING_NOT_FOUND("EsReader-02", "找不到您指定的index/type映射.");

    private final String code;
    private final String description;

    EsReaderErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Description:[%s].", this.code,
                this.description);
    }
}
