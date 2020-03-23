package com.ucar.datalink.biz.service;

import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.common.zookeeper.ZkClientX;

/**
 * Created by user on 2018/3/22.
 */
public abstract class JobDaemonService implements DaemonService{

    public void init() {

    }

    public void start() {
        ZkClientX zk = DLinkZkUtils.get().zkClient();
        initialized();
    }

    public void destroy() {
        destroyed();
    }


    public abstract void initialized();

    public abstract void destroyed();




}
