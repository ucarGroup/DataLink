package com.ucar.datalink.biz.utils.flinker.module;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2017/7/27.
 */
public class JobExtendProperty {

    private long srcID;

    private long destID;

    private String mediaName;

    private AdvanceJobProperty advance = new AdvanceJobProperty();

    private Map<String,String> reader = new HashMap<>();

    private Map<String,String> writer = new HashMap<>();

    private TimingJobExtendPorperty timing = new TimingJobExtendPorperty();



    public long getSrcID() {
        return srcID;
    }

    public void setSrcID(long srcID) {
        this.srcID = srcID;
    }

    public long getDestID() {
        return destID;
    }

    public void setDestID(long destID) {
        this.destID = destID;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public AdvanceJobProperty getAdvance() {
        return advance;
    }

    public void setAdvance(AdvanceJobProperty advance) {
        this.advance = advance;
    }

    public Map<String, String> getReader() {
        return reader;
    }

    public void setReader(Map<String, String> reader) {
        this.reader = reader;
    }

    public Map<String, String> getWriter() {
        return writer;
    }

    public void setWriter(Map<String, String> writer) {
        this.writer = writer;
    }

    public TimingJobExtendPorperty getTiming() {
        return timing;
    }

    public void setTiming(TimingJobExtendPorperty timing) {
        this.timing = timing;
    }

}
