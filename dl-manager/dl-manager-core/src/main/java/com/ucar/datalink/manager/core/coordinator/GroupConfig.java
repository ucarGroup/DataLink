package com.ucar.datalink.manager.core.coordinator;

/**
 * Created by lubiao on 2016/12/1.
 */
public class GroupConfig {
    private int groupMinSessionTimeoutMs;
    private int groupMaxSessionTimeoutMs;

    public GroupConfig(int groupMinSessionTimeoutMs, int groupMaxSessionTimeoutMs) {
        this.groupMinSessionTimeoutMs = groupMinSessionTimeoutMs;
        this.groupMaxSessionTimeoutMs = groupMaxSessionTimeoutMs;
    }

    public int getGroupMinSessionTimeoutMs() {
        return groupMinSessionTimeoutMs;
    }

    public void setGroupMinSessionTimeoutMs(int groupMinSessionTimeoutMs) {
        this.groupMinSessionTimeoutMs = groupMinSessionTimeoutMs;
    }

    public int getGroupMaxSessionTimeoutMs() {
        return groupMaxSessionTimeoutMs;
    }

    public void setGroupMaxSessionTimeoutMs(int groupMaxSessionTimeoutMs) {
        this.groupMaxSessionTimeoutMs = groupMaxSessionTimeoutMs;
    }
}
