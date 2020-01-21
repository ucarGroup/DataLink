package com.ucar.datalink.domain.plugin.writer.dove;

import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.writer.dove.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.dove.SerializeMode;

import java.util.Set;

public class DoveWriterParameter extends PluginWriterParameter {

    private SerializeMode serializeMode = SerializeMode.Hessian; //Hessian, Json
    private PartitionMode partitionMode = PartitionMode.COLUMN;

    @Override
    public String initPluginName() {
        return "writer-dove";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.dove.DoveTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.dove.DoveTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.DOVE);
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
