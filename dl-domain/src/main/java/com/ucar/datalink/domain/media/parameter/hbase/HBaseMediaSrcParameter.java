package com.ucar.datalink.domain.media.parameter.hbase;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;

/**
 * Created by user on 2017/6/19.
 */

public class HBaseMediaSrcParameter extends MediaSrcParameter {

    /**
     * HDFS集群的url
     */
    private String nameServices;

    /**
     * zookeeper集群的id
     */
    private Long zkMediaSourceId;

    /**
     * 当前HBase在zookeeper集群中的znode路径
     */
    private String znodeParent;

    /**
     * HBase中的一条底层记录KeyValue的最大长度
     */
    private Integer keyvalueMaxsize;

    /**
     * kerberos认证 krb5.conf
     */
    private String realm;
    private String kdc;
    /**
     * 登录用户
     */
    private String loginPrincipal;
    /**
     * 登录用户Keytab文件绝对路径
     */
    private String loginKeytabPath;
    /**
     * hbase-site.xml 绝对路径
     */
    private String hbaseSitePath;

    public String getNameServices() {
        return nameServices;
    }

    public void setNameServices(String nameServices) {
        this.nameServices = nameServices;
    }

    public Long getZkMediaSourceId() {
        return zkMediaSourceId;
    }

    public void setZkMediaSourceId(Long zkMediaSourceId) {
        this.zkMediaSourceId = zkMediaSourceId;
    }

    public String getZnodeParent() {
        return znodeParent;
    }

    public void setZnodeParent(String znodeParent) {
        this.znodeParent = znodeParent;
    }

    public Integer getKeyvalueMaxsize() {
        return keyvalueMaxsize;
    }

    public void setKeyvalueMaxsize(Integer keyvalueMaxsize) {
        this.keyvalueMaxsize = keyvalueMaxsize;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getKdc() {
        return kdc;
    }

    public void setKdc(String kdc) {
        this.kdc = kdc;
    }

    public String getLoginPrincipal() {
        return loginPrincipal;
    }

    public void setLoginPrincipal(String loginPrincipal) {
        this.loginPrincipal = loginPrincipal;
    }

    public String getLoginKeytabPath() {
        return loginKeytabPath;
    }

    public void setLoginKeytabPath(String loginKeytabPath) {
        this.loginKeytabPath = loginKeytabPath;
    }

    public String getHbaseSitePath() {
        return hbaseSitePath;
    }

    public void setHbaseSitePath(String hbaseSitePath) {
        this.hbaseSitePath = hbaseSitePath;
    }
}
