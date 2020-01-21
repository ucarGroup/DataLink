package com.ucar.datalink.biz.auto.impl;

import com.ucar.datalink.biz.auto.AbstractSync;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.util.SyncUtil;
import org.springframework.stereotype.Component;

/**
 * Created by yang.wang09 on 2018-05-18 14:46.
 */
@Component
public class Hive2HBase extends AbstractSync {

    @Override
    public boolean isFullFirst() {
        return false;
    }

    @Override
    public void initFull(SyncApplyInfo info) {
        //创建job
        SyncUtil.createFullJob(info);
    }

    @Override
    public void processFull(SyncApplyInfo info) {
        //执行job
        SyncUtil.executeJob(info);
    }

    @Override
    public void initIncrement(SyncApplyInfo info) {


    }

    @Override
    public void processIncrement(SyncApplyInfo info) {

    }

}
