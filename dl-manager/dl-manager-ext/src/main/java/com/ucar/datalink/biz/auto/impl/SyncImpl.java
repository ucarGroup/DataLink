package com.ucar.datalink.biz.auto.impl;

import com.ucar.datalink.biz.auto.AbstractSync;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.util.SyncUtil;

/**
 * Created by qianqian.shi on 2018/8/13.
 */
public class SyncImpl extends AbstractSync {

    @Override
    public void initFull(SyncApplyInfo info) {
        //创建job
//        SyncUtil.createFullJob(info);
    }

    @Override
    public void processFull(SyncApplyInfo info) {
        //执行job
//        SyncUtil.executeJob(info);
    }

    @Override
    public void initIncrement(SyncApplyInfo info) {
        super.initIncrement(info);
    }

    @Override
    public void processIncrement(SyncApplyInfo info) {

    }
}
