package com.ucar.datalink.flinker.core.admin.bean;

/**
 * Created by user on 2018/2/1.
 * CREATE TABLE `t_dl_user` (
 `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
 `user_name` varchar(50) NOT NULL COMMENT '用户名称',
 `zuche_email` varchar(50) DEFAULT NULL COMMENT '租车邮箱',
 `ucar_email` varchar(50) DEFAULT NULL COMMENT '集团邮箱',
 `phone` varchar(20) NOT NULL COMMENT '手机号',
 `role_id` bigint(20) NOT NULL COMMENT '角色id',
 `create_time` datetime NOT NULL COMMENT '新建时间',
 `modify_time` datetime NOT NULL COMMENT '修改时间',
 `is_alarm` varchar(5) NOT NULL DEFAULT 'false' COMMENT '是否发送报警',
 `isReceiveDataxMail` varchar(5) DEFAULT 'false' COMMENT '是否接收邮件,1-是,0-否',
 PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8 COMMENT='用户表'

 */
public class UserBean {

    private Long id;

    private String userName;

    private String zucheEmail;

    private String ucarEmail;


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

    public String getZucheEmail() {
        return zucheEmail;
    }

    public void setZucheEmail(String zucheEmail) {
        this.zucheEmail = zucheEmail;
    }

    public String getUcarEmail() {
        return ucarEmail;
    }

    public void setUcarEmail(String ucarEmail) {
        this.ucarEmail = ucarEmail;
    }


}
