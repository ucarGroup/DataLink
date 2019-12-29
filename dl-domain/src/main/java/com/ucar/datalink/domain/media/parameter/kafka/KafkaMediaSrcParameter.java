package com.ucar.datalink.domain.media.parameter.kafka;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class KafkaMediaSrcParameter extends MediaSrcParameter {
    private String topic;
    private String paramters;
    private String bootstrapServers;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getParamters() {
        return paramters;
    }

    public void setParamters(String paramters) {
        this.paramters = paramters;
    }

    public Map<String, String> getMapparamters() {
        Map<String, String> pKVS = new HashMap<String, String>();
        if (StringUtils.isBlank(paramters)) {
            return pKVS;
        }

        String[] kvs = paramters.split(",");
        if (kvs.length < 1) {
            return pKVS;
        }

        for (String kv : kvs) {
            String[] kvt = kv.split(":");
            if (kvt.length != 2) {
                continue;
            }
            pKVS.put(kvt[0], kvt[1]);
        }

        return pKVS;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof KafkaMediaSrcParameter) {
            KafkaMediaSrcParameter kafka = (KafkaMediaSrcParameter) obj;
            if (kafka.bootstrapServers.equals(this.bootstrapServers) && kafka.topic.equals(this.topic)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "KafkaMediaSrcParameter{" +
                "topic='" + topic + '\'' +
                ", paramters='" + paramters + '\'' +
                ", bootstrapServers='" + bootstrapServers + '\'' +
                '}';
    }
}



