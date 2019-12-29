package com.ucar.datalink.domain.plugin.writer.kudu;

import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;

import java.util.Set;

public class KuduWriterParameter  extends PluginWriterParameter {
    @Override
    public String initPluginName() {
        return "writer-kudu";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.kudu.KuduTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.kudu.KuduTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.KUDU);
    }

    
}
