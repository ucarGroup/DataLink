package com.ucar.datalink.domain.plugin.writer.kafka;

import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;

import java.util.Set;

public class KafkaWriterParameter extends PluginWriterParameter {

    private SerializeMode serializeMode = SerializeMode.Hessian; //Hessian, Json
    private PartitionMode partitionMode = PartitionMode.COLUMN;

    @Override
    public String initPluginName() {
        return "writer-kafka";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.kafka.KafkaTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.kafka.KafkaTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.KAFKA);
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
