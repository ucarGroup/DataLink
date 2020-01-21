package com.ucar.datalink.domain.doublecenter;

public enum IdLabEnum {

    logicA(1, LabEnum.logicA.getCode()),
    logicB(2, LabEnum.logicB.getCode());

    private Integer code;
    private String name;


    IdLabEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static IdLabEnum getEnumByCode(Integer code){
        if(code == null){
            return null;
        }

        IdLabEnum [] values = IdLabEnum.values();
        for (IdLabEnum e : values){
            if(code.equals(e.getCode())){
                return e;
            }
        }
        return null;
    }

}
