package com.ucar.datalink.domain.doublecenter;

import org.apache.commons.lang.StringUtils;

public enum LabEnum {

    logicA("logicA", "A机房"),
    logicB("logicB", "B机房");

    private String code;
    private String name;


    LabEnum(String code, String name) {
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

    public static LabEnum getEnumByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }

        LabEnum [] values = LabEnum.values();
        for (LabEnum e : values){
            if(StringUtils.equals(code, e.getCode())){
                return e;
            }
        }
        return null;
    }

}
