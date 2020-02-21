package com.ucar.datalink.flinker.plugin.writer.mysqlwriter;

import com.ucar.datalink.flinker.api.spi.ErrorCode;

public enum MysqlWriterErrorCode implements ErrorCode {
   ;

    private final String code;
    private final String describe;

    private MysqlWriterErrorCode(String code, String describe) {
        this.code = code;
        this.describe = describe;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.describe;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Describe:[%s]. ", this.code,
                this.describe);
    }
}
