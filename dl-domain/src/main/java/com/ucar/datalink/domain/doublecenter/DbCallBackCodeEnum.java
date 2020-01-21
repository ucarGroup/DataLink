package com.ucar.datalink.domain.doublecenter;

public enum DbCallBackCodeEnum {

    DATALINK_OPS_INPROGRESS(3001, "切机房进行中"),
    DATALINK_OPS_SUCCESS(3002, "切机房成功"),
    DATALINK_OPS_FAILURE(3003, "切机房失败");

    private Integer code;
    private String name;


    DbCallBackCodeEnum(Integer code, String name) {
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

    public static DbCallBackCodeEnum getEnumByCode(Integer code){
        if(code == null){
            return null;
        }

        DbCallBackCodeEnum[] values = DbCallBackCodeEnum.values();
        for (DbCallBackCodeEnum e : values){
            if(code.equals(e.getCode())){
                return e;
            }
        }
        return null;
    }

}
