package com.ucar.datalink.flinker.plugin.writer.hdfswriter;

public enum SupportHiveDataType {
    TINYINT,
    SMALLINT,
    INT,
    BIGINT,
    FLOAT,
    DOUBLE,
    DECIMAL,//added by lubiao
    BINARY,//added by lubiao
    
    TIMESTAMP,
    DATE,

    STRING,
    VARCHAR,
    CHAR,

    BOOLEAN
}
