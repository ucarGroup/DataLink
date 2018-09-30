package com.ucar.datalink.domain.plugin.writer.hbase;

import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;

import java.util.Set;

/**
 * Created by sqq on 2017/11/29.
 */
public class HBaseWriterParameter extends PluginWriterParameter {
    @Override
    public String initPluginName() {
        return "writer-hbase";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.hbase.HBaseTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.hbase.HBaseTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.HBASE);
    }
}
