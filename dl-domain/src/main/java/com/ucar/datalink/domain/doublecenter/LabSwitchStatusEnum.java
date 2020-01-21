package com.ucar.datalink.domain.doublecenter;

public enum LabSwitchStatusEnum {

    未开始(0, "未开始"),
    开始切换(1, "开始切换"),
    切换完成(2, "切换成功"),
    切换失败(3, "切换失败");

    private Integer code;
    private String name;

    LabSwitchStatusEnum(Integer code, String name) {
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

    public static LabSwitchStatusEnum getEnumByCode(Integer code){
        LabSwitchStatusEnum [] values = LabSwitchStatusEnum.values();
        for (LabSwitchStatusEnum e : values){
            if(code.intValue() == e.getCode().intValue()){
                return e;
            }
        }
        return null;
    }
}