package com.ucar.datalink.flinker.plugin.reader.hbasereader98;

import com.ucar.datalink.flinker.api.exception.DataXException;
import java.util.Arrays;

public enum ModeType {
    Normal("normal"),
    MultiVersionFixedColumn("multiVersionFixedColumn"),
    MultiVersionDynamicColumn("multiVersionDynamicColumn"),;

    private String mode;

    ModeType(String mode) {
        this.mode = mode.toLowerCase();
    }

    public static ModeType getByTypeName(String modeName) {
        for (ModeType modeType : values()) {
            if (modeType.mode.equalsIgnoreCase(modeName)) {
                return modeType;
            }
        }

        throw DataXException.asDataXException(HbaseReaderErrorCode.ILLEGAL_VALUE,
                String.format("Hbasereader 不支持该 mode 类型:%s, 目前支持的 mode 类型是:%s", modeName, Arrays.asList(values())));
    }
}
