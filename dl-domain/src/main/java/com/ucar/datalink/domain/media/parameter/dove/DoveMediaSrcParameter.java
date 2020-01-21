package com.ucar.datalink.domain.media.parameter.dove;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;

/**
 * Created by sqq on 2017/5/11.
 */
public class DoveMediaSrcParameter extends MediaSrcParameter {
    private Long zkMediaSourceId;
    private String clusterPrefix;
    private String topic;

    public Long getZkMediaSourceId() {
        return zkMediaSourceId;
    }

    public void setZkMediaSourceId(Long zkMediaSourceId) {
        this.zkMediaSourceId = zkMediaSourceId;
    }

    public String getClusterPrefix() {
        return clusterPrefix;
    }

    public void setClusterPrefix(String clusterPrefix) {
        this.clusterPrefix = clusterPrefix;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean equals(Object obj) {
        if(obj==null){
            return false;
        }
        if(this==obj){
            return true;
        }
        if(obj instanceof DoveMediaSrcParameter){
            DoveMediaSrcParameter fq=(DoveMediaSrcParameter)obj;
            if(fq.zkMediaSourceId.equals(this.zkMediaSourceId) && fq.clusterPrefix.equals(this.clusterPrefix) && fq.topic.equals(this.topic)){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }
}
