package com.ucar.datalink.domain.user;

import com.ucar.datalink.domain.Storable;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2017/4/19.
 */
@Alias("user")
public class UserInfo implements Serializable, Storable {
    private Long id;
    private String userName;
    private String ucarEmail;
    private String phone;
    private int userType;
    private Date createTime;
    private Date modifyTime;
    private Boolean isAlarm = Boolean.FALSE;
    private Boolean isReceiveDataxMail = Boolean.FALSE;
    private List<RoleInfo> roleInfoList;
    private String roleIdStr;

    public Boolean getIsAlarm() {
        return isAlarm;
    }

    public void setIsAlarm(Boolean isAlarm) {
        this.isAlarm = isAlarm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUcarEmail() {
        return ucarEmail;
    }

    public void setUcarEmail(String ucarEmail) {
        this.ucarEmail = ucarEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Boolean getIsReceiveDataxMail() {
        return isReceiveDataxMail;
    }

    public void setIsReceiveDataxMail(Boolean isReceiveDataxMail) {
        this.isReceiveDataxMail = isReceiveDataxMail;
    }

    public List<RoleInfo> getRoleInfoList() {
        return roleInfoList;
    }

    public void setRoleInfoList(List<RoleInfo> roleInfoList) {
        this.roleInfoList = roleInfoList;
    }

    public String getRoleIdStr() {
        return roleIdStr;
    }

    public void setRoleIdStr(String roleIdStr) {
        this.roleIdStr = roleIdStr;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }


   public enum UserType{
        UCARINC("ucarinc",0),LUCKY("lucky",1),LUCKYNUM("luckynum",1);
        //,BORGWARD("borgwardEmail",2)
        private String name;
        private int value;

        UserType(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public static boolean contain(String name){
            for(UserType lut : UserType.values()){
                if(lut.getName().equals(name)){
                    return true;
                }
            }
            return false;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}



