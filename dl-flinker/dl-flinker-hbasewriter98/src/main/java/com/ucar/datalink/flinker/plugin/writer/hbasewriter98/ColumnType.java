package com.ucar.datalink.flinker.plugin.writer.hbasewriter98;

import com.ucar.datalink.flinker.api.exception.DataXException;

import java.util.Arrays;

/**
 * 只对 normal 模式读取时有用，多版本读取时，不存在列类型的
 */
public enum ColumnType {
    STRING("string"),
    BINARY_STRING("binarystring"),
    BYTES("bytes"),
    BOOLEAN("boolean"),
    SHORT("short"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    DATE("date"),;

    private String typeName;

    ColumnType(String typeName) {
        this.typeName = typeName;
    }

    public static ColumnType getByTypeName(String typeName) {
        for (ColumnType columnType : values()) {
            if (columnType.typeName.equalsIgnoreCase(typeName)) {
                return columnType;
            }
        }

        throw DataXException.asDataXException(HBaseWriter98ErrorCode.ILLEGAL_VALUE,
                String.format("Hbasewriter98 不支持该类型:%s, 目前支持的类型是:%s", typeName, Arrays.asList(values())));
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
