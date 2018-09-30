package com.ucar.datalink.domain.plugin.reader.hbase;

import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginReaderParameter;

import java.util.Set;

/**
 * Created by lubiao on 17-6-29.
 */
public class HBaseReaderParameter extends PluginReaderParameter {
    private String replZnodeParent;
    private Long replZkMediaSourceId;

    @Override
    public String initPluginName() {
        return "reader-hbase";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.reader.hbase.HBaseTaskReader";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.reader.hbase.HBaseTaskReaderListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.HBASE);
    }

    public String getReplZnodeParent() {
        return replZnodeParent;
    }

    public void setReplZnodeParent(String replZnodeParent) {
        this.replZnodeParent = replZnodeParent;
    }

    public Long getReplZkMediaSourceId() {
        return replZkMediaSourceId;
    }

    public void setReplZkMediaSourceId(Long replZkMediaSourceId) {
        this.replZkMediaSourceId = replZkMediaSourceId;
    }
}
