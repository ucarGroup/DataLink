package com.ucar.datalink.domain.plugin.writer.es;

import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;

import java.util.Set;

/**
 * Created by lubiao on 2017/6/15.
 */
public class EsWriterParameter extends PluginWriterParameter {

    @Override
    public String initPluginName() {
        return "writer-es";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.es.EsTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.es.EsTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.ELASTICSEARCH);
    }
}
