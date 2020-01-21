package com.ucar.datalink.domain.plugin.writer.fq;

import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;

import java.util.Set;

/**
 * Created by sqq on 2017/5/10.
 */
public class FqWriterParameter extends PluginWriterParameter {

    private SerializeMode serializeMode = SerializeMode.Hessian; //Hessian, Json
    private PartitionMode partitionMode = PartitionMode.COLUMN;

    @Override
    public String initPluginName() {
        return "writer-fq";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.fq.FqTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.fq.FqTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.FLEXIBLEQ);
    }

    public SerializeMode getSerializeMode() {
        return serializeMode;
    }

    public void setSerializeMode(SerializeMode serializeMode) {
        this.serializeMode = serializeMode;
    }


    public PartitionMode getPartitionMode() {
        return partitionMode;
    }

    public void setPartitionMode(PartitionMode partitionMode) {
        this.partitionMode = partitionMode;
    }
}
