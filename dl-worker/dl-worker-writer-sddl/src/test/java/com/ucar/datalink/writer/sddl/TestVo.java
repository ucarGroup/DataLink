package com.ucar.datalink.writer.sddl;


import java.io.Serializable;

public class TestVo implements Serializable {

    private String id;
    private final String fiId;
    private String trace;
    private String workerId;
    private int generation;

    public TestVo(String fiId) {
        this.fiId = fiId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }
}
