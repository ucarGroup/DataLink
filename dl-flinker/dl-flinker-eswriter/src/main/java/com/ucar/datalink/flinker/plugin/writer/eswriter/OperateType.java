package com.ucar.datalink.flinker.plugin.writer.eswriter;

/**
 * Created by yw.zhang02 on 2016/7/27.
 */
public enum OperateType {
    INSERT("INSERT"), UPDATE("UPDATE"),UPSERT("UPSERT");

    private String value;

    OperateType(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
