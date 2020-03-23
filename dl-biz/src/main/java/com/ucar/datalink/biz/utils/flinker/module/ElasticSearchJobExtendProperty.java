package com.ucar.datalink.biz.utils.flinker.module;

/**
 * Created by user on 2017/7/27.
 */
public class ElasticSearchJobExtendProperty extends AbstractJobExtendProperty {

    private String joinColumn;

    private String esReaderIndexType;

    private String esWriterIndexType;

    private String esReaderQuery;

    private String esIsTablePrefix;

    private String esWriterPreDel;

    public String getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(String joinColumn) {
        this.joinColumn = joinColumn;
    }

    public String getEsReaderIndexType() {
        return esReaderIndexType;
    }

    public void setEsReaderIndexType(String esReaderIndexType) {
        this.esReaderIndexType = esReaderIndexType;
    }

    public String getEsWriterIndexType() {
        return esWriterIndexType;
    }

    public void setEsWriterIndexType(String esWriterIndexType) {
        this.esWriterIndexType = esWriterIndexType;
    }

    public String getEsReaderQuery() {
        return esReaderQuery;
    }

    public void setEsReaderQuery(String esReaderQuery) {
        this.esReaderQuery = esReaderQuery;
    }

    public String getEsIsTablePrefix() {
        return esIsTablePrefix;
    }

    public void setEsIsTablePrefix(String esIsTablePrefix) {
        this.esIsTablePrefix = esIsTablePrefix;
    }

    public String getEsWriterPreDel() {
        return esWriterPreDel;
    }

    public void setEsWriterPreDel(String esWriterPreDel) {
        this.esWriterPreDel = esWriterPreDel;
    }
}
