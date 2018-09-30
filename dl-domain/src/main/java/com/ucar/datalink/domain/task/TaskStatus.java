package com.ucar.datalink.domain.task;

import java.io.Serializable;

/**
 * 任务状态定义类
 * <p>
 * Created by lubiao on 2016/12/5.
 */
public class TaskStatus implements Serializable {

    public enum State {
        UNASSIGNED,
        PREPARING,
        RUNNING,
        PAUSED,
        FAILED
    }

    private String id;
    private String executionId;
    private State state;
    private String trace;
    private String workerId;
    private int generation;
    private Long startTime;

    public TaskStatus() {
    }

    public TaskStatus(String id, String executionId, State state, String workerId, int generation,
                      Long startTime, String trace) {
        this.id = id;
        this.executionId = executionId;
        this.state = state;
        this.workerId = workerId;
        this.generation = generation;
        this.startTime = startTime;
        this.trace = trace;
    }

    public TaskStatus(String id, String executionId, State state, String workerId,
                      int generation, Long startTime) {
        this(id, executionId, state, workerId, generation, startTime, null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskStatus that = (TaskStatus) o;

        if (generation != that.generation) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (executionId != null ? !executionId.equals(that.executionId) : that.executionId != null) return false;
        if (state != that.state) return false;
        if (trace != null ? !trace.equals(that.trace) : that.trace != null) return false;
        if (workerId != null ? !workerId.equals(that.workerId) : that.workerId != null) return false;
        return !(startTime != null ? !startTime.equals(that.startTime) : that.startTime != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (executionId != null ? executionId.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (trace != null ? trace.hashCode() : 0);
        result = 31 * result + (workerId != null ? workerId.hashCode() : 0);
        result = 31 * result + generation;
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TaskStatus{" +
                "id='" + id + '\'' +
                ", executionId='" + executionId + '\'' +
                ", state=" + state +
                ", trace='" + trace + '\'' +
                ", workerId='" + workerId + '\'' +
                ", generation=" + generation +
                ", startTime=" + startTime +
                '}';
    }
}
