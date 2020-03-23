package com.ucar.datalink.biz.utils.flinker.module;

/**
 * Created by user on 2017/7/26.
 */
public class HDFSJobExtendProperty extends AbstractJobExtendProperty {

    private String path;

    private String compress;

    private String ignoreException;

    private String hdfsPathType;

    private String hsdfUser;

    private String specifiedPreDate;

    private String hdfsPaths;

    private String hdfsPreDel;



    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCompress() {
        return compress;
    }

    public void setCompress(String compress) {
        this.compress = compress;
    }

    public String getIgnoreException() {
        return ignoreException;
    }

    public void setIgnoreException(String ignoreException) {
        this.ignoreException = ignoreException;
    }

    public String getHdfsPathType() {
        return hdfsPathType;
    }

    public void setHdfsPathType(String hdfsPathType) {
        this.hdfsPathType = hdfsPathType;
    }

    public String getHsdfUser() {
        return hsdfUser;
    }

    public void setHsdfUser(String hsdfUser) {
        this.hsdfUser = hsdfUser;
    }

    public String getSpecifiedPreDate() {
        return specifiedPreDate;
    }

    public void setSpecifiedPreDate(String specifiedPreDate) {
        this.specifiedPreDate = specifiedPreDate;
    }

    public String getHdfsPaths() {
        return hdfsPaths;
    }

    public void setHdfsPaths(String hdfsPaths) {
        this.hdfsPaths = hdfsPaths;
    }

    public String getHdfsPreDel() {
        return hdfsPreDel;
    }

    public void setHdfsPreDel(String hdfsPreDel) {
        this.hdfsPreDel = hdfsPreDel;
    }
}
