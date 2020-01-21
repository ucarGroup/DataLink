package com.ucar.datalink.domain.task;

import org.apache.commons.lang.StringUtils;

import java.util.*;

public enum TaskSyncModeEnum {

    singleLabSync("SingleLab", "单机房同步"),
    acrossLabSync("AcrossLab", "跨机房同步");

    private String code;
    private String name;

    TaskSyncModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static List<TaskSyncModeEnum> toList(){
        List<TaskSyncModeEnum> list = new ArrayList<TaskSyncModeEnum>();
        TaskSyncModeEnum [] values = TaskSyncModeEnum.values();
        list = Arrays.asList(values);
        return list;
    }

    public static TaskSyncModeEnum getEnumByCode(String code){
        TaskSyncModeEnum [] values = TaskSyncModeEnum.values();
        for (TaskSyncModeEnum e : values){
            if(StringUtils.equals(e.getCode(),code)){
                return e;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}