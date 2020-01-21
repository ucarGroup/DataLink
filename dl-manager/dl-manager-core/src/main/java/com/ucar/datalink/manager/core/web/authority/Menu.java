package com.ucar.datalink.manager.core.web.authority;

import java.util.Set;

/**
 * Created by yw.zhang02 on 2016/9/16.
 */
public class Menu {
    private String code;
    private String name;
    private String parentCode;
    private String type;
    private String url;
    private String icon;
    private Set<String> roles;

    public boolean hasRole(String role){
        return getRoles().contains(role);
    }

    public boolean isNode(){
        return "node".equals(type);
    }

    public boolean isLeaf(){
        return "leaf".equals(type);
    }

    public boolean isAction(){
        return "action".equals(type);
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

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
