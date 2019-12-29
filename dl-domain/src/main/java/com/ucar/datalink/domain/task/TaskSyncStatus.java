package com.ucar.datalink.domain.task;

/**
 * Created by lubiao on 2019/2/18.
 */
public class TaskSyncStatus {

    /**
     * 定义任务同步状态
     * 注：当TaskStatus处于RUNNING状态时，TaskSyncStatus才有意义
     */
    public enum State {
        /**
         * 初始态：任务启动完成，还未开始进行同步时的状态
         */
        Init,
        /**
         * 空闲中:代表任务处于空闲状态(正在读数据或正在等待数据)
         */
        Idle,
        /**
         * 同步中:代表任务处于忙碌状态(正在写数据)
         */
        Busy
    }

    /**
     * 任务编号
     */
    private String id;
    /**
     * 同步状态
     */
    private State state;
    /**
     * 状态更新时间,单位ms
     */
    private long updateTime;

    public TaskSyncStatus() {
    }

    public TaskSyncStatus(String id, State state, long updateTime) {
        this.id = id;
        this.state = state;
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }


    @Override
    public String toString() {
        return "TaskSyncStatus{" +
                "id='" + id + '\'' +
                ", state=" + state +
                ", updateTime=" + updateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskSyncStatus that = (TaskSyncStatus) o;

        if (updateTime != that.updateTime) return false;
        if (!id.equals(that.id)) return false;
        return state == that.state;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + (int) (updateTime ^ (updateTime >>> 32));
        return result;
    }
}
