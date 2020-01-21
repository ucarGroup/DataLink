package com.ucar.datalink.biz.auto.impl;

import com.ucar.datalink.biz.auto.AbstractSync;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.util.SyncUtil;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by yang.wang09 on 2018-04-24 10:44.
 */
@Component
public class MySql2Hive extends AbstractSync {


    public MySql2Hive() {

    }

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
