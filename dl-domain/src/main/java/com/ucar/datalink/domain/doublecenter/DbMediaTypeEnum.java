package com.ucar.datalink.domain.doublecenter;

import com.ucar.datalink.domain.media.MediaSourceType;
import org.apache.commons.lang.StringUtils;

public enum DbMediaTypeEnum {

    MySQL("MySQL", MediaSourceType.MYSQL.name()),
    SQLServer("SQL Server", MediaSourceType.SQLSERVER.name());

    private String code;
    private String name;

    DbMediaTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
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

    public static DbMediaTypeEnum getEnumByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }

        DbMediaTypeEnum [] values = DbMediaTypeEnum.values();
        for (DbMediaTypeEnum e : values){
            if(StringUtils.equals(code, e.getCode())){
                return e;
            }
        }
        return null;
    }

}
