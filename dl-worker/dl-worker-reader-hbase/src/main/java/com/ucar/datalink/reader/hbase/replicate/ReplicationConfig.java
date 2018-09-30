package com.ucar.datalink.reader.hbase.replicate;

import com.ucar.datalink.common.zookeeper.ZkConfig;

/**
 * Created by lubiao on 2017/11/16.
 */
public class ReplicationConfig {

    private String hbaseName;
    private ZkConfig zkConfig;
    private String znodeParent;
    private int handlers = 10;

    public String getHbaseName() {
        return hbaseName;
    }

    public void setHbaseName(String hbaseName) {
        this.hbaseName = hbaseName;
    }

    public ZkConfig getZkConfig() {
        return zkConfig;
    }

    public void setZkConfig(ZkConfig zkConfig) {
        this.zkConfig = zkConfig;
    }

    public String getZnodeParent() {
        return znodeParent;
    }

    public void setZnodeParent(String znodeParent) {
        this.znodeParent = znodeParent;
    }

    public int getHandlers() {
        return handlers;
    }

    public void setHandlers(int handlers) {
        this.handlers = handlers;
    }

}
