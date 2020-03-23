package com.ucar.datalink.biz.utils.flinker.check.meta;

import java.util.Arrays;

/**
 * Created by yang.wang09 on 2018-04-23 18:59.
 */
public class SyncModifyTableInfo extends SyncTableInfo {

    public static final String SYNC_MODIFY = "modify";

    public static final String SYNC_ADD = "add";

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SyncModifyTableInfo{" +
                "type='" + type + '\'' +
                "database='" + database + '\'' +
                ", table='" + table + '\'' +
                ", dbType='" + dbType + '\'' +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }
}
