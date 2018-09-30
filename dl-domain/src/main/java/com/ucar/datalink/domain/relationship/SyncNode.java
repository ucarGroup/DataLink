package com.ucar.datalink.domain.relationship;

import com.alibaba.fastjson.annotation.JSONField;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * 同步关系节点定义类.
 * Created by lubiao on 2017/5/23.
 */
public class SyncNode {
    private MediaSourceInfo mediaSource;
    private MediaMappingInfo mappingInfo;
    private String tableNameAlias;
    private List<SyncNode> children;

    public SyncNode(MediaSourceInfo mediaSource, MediaMappingInfo mappingInfo) {
        this.mediaSource = mediaSource;
        this.mappingInfo = mappingInfo;
        this.buildTableNameAlias();
    }

    @JSONField(serialize = false)
    public MediaSourceInfo getMediaSource() {
        return mediaSource;
    }

    @JSONField(serialize = false)
    public void setMediaSource(MediaSourceInfo mediaSource) {
        this.mediaSource = mediaSource;
    }

    @JSONField(serialize = false)
    public MediaMappingInfo getMappingInfo() {
        return mappingInfo;
    }

    @JSONField(serialize = false)
    public void setMappingInfo(MediaMappingInfo mappingInfo) {
        this.mappingInfo = mappingInfo;
        this.buildTableNameAlias();
    }

    public String getTableNameAlias() {
        return tableNameAlias;
    }

    public void setTableNameAlias(String tableNameAlias) {
        this.tableNameAlias = tableNameAlias;
    }

    public List<SyncNode> getChildren() {
        return children;
    }

    public void setChildren(List<SyncNode> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncNode syncNode = (SyncNode) o;

        return mediaSource.equals(syncNode.mediaSource);

    }

    @Override
    public int hashCode() {
        return mediaSource.hashCode();
    }

    public String getMediaSourceName() {
        return mediaSource.getName();
    }

    public MediaSourceType getMediaSourceType() {
        return mediaSource.getType();
    }

    public String getName() {
        return String.format("【%s】%s", mediaSource.getType(), mediaSource.getName());
    }

    private void buildTableNameAlias() {
        tableNameAlias = null;
        if (mappingInfo != null && StringUtils.isNotBlank(mappingInfo.getTargetMediaName()) &&
                !mappingInfo.getSourceMedia().getName().equals(mappingInfo.getTargetMediaName())) {
            tableNameAlias = mappingInfo.getTargetMediaName();
        }
    }
}
