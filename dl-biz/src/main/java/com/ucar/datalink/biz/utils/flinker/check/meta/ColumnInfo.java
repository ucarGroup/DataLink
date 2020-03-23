package com.ucar.datalink.biz.utils.flinker.check.meta;

/**
 * Created by yang.wang09 on 2018-04-23 11:35.
 */
public class ColumnInfo {

    private String name;

    private String type;

    private String typeLength;

    private String typePrecision;

    protected String comment;

    protected String hiveType;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeLength() {
        return typeLength;
    }

    public void setTypeLength(String typeLength) {
        this.typeLength = typeLength;
    }

    public String getTypePrecision() {
        return typePrecision;
    }

    public void setTypePrecision(String typePrecision) {
        this.typePrecision = typePrecision;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getHiveType() {
        return hiveType;
    }

    public void setHiveType(String hiveType) {
        this.hiveType = hiveType;
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", typeLength='" + typeLength + '\'' +
                ", typePrecision='" + typePrecision + '\'' +
                ", comment='" + comment + '\'' +
                ", hiveType='" + hiveType + '\'' +
                '}';
    }
}




