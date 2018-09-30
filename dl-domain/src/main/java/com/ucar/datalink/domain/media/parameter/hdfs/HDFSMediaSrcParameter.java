package com.ucar.datalink.domain.media.parameter.hdfs;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;

/**
 * Created by sqq on 2017/6/16.
 */
public class HDFSMediaSrcParameter extends MediaSrcParameter {
    private String nameServices;
    private String nameNode1;
    private String nameNode2;
    private String hadoopUser = "increment";
    private Long zkMediaSourceId;



    /**
     * spare cube的元数据地址，通过这个地址获取元数据(所有表，所有列)的信息
     */
    private String sparkcubeAddress;

    public String getNameServices() {
        return nameServices;
    }

    public void setNameServices(String nameServices) {
        this.nameServices = nameServices;
    }

    public String getNameNode1() {
        return nameNode1;
    }

    public void setNameNode1(String nameNode1) {
        this.nameNode1 = nameNode1;
    }

    public String getNameNode2() {
        return nameNode2;
    }

    public void setNameNode2(String nameNode2) {
        this.nameNode2 = nameNode2;
    }

    public String getHadoopUser() {
        return hadoopUser;
    }

    public void setHadoopUser(String hadoopUser) {
        this.hadoopUser = hadoopUser;
    }

    public Long getZkMediaSourceId() {
        return zkMediaSourceId;
    }

    public void setZkMediaSourceId(Long zkMediaSourceId) {
        this.zkMediaSourceId = zkMediaSourceId;
    }

    public String getSparkcubeAddress() {
        return sparkcubeAddress;
    }

    public void setSparkcubeAddress(String sparkcubeAddress) {
        this.sparkcubeAddress = sparkcubeAddress;
    }


}
