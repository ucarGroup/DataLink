package com.ucar.datalink.manager.core.coordinator;

/**
 * Created by lubiao on 2016/12/9.
 */
public class ProtocolEntry {
    private String name;//protocol name
    private byte[] metadata;//protocol metadata

    public ProtocolEntry(String name, byte[] metadata) {
        this.name = name;
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getMetadata() {
        return metadata;
    }

    public void setMetadata(byte[] metadata) {
        this.metadata = metadata;
    }
}
