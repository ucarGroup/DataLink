package com.ucar.datalink.flinker.plugin.reader.esreader;

/**
 * Created by hechaoyi on 16/8/23.
 */
public enum DataType {
    // Elasticsearch支持的数据类型:
    // https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html

    STRING, // String
    LONG, INTEGER, SHORT, BYTE, DOUBLE, FLOAT, // Numeric
    DATE, // Date
    BOOLEAN, // Boolean
    BINARY, // Binary
    ;

    public static DataType parse(String code) {
        for (DataType dType : values()) {
            if (dType.name().equalsIgnoreCase(code))
                return dType;
        }
        return STRING; // 默认字符串
    }

}
