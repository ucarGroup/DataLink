package com.ucar.datalink.biz.auto.envfilter;

import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.domain.sync.SyncApplyStatus;
import com.ucar.datalink.util.SyncUtil;


/**
 * Created by yang.wang09 on 2018-05-07 17:41.
 */
public class ProdStrategy extends AbstractStrategy {
    @Override
    public boolean preInitFull(SyncApplyInfo info) {
        return false;
    }

    @Override
    public boolean postInitFull(SyncApplyInfo info) {
        info.setApplyStatus(SyncApplyStatus.FULL_EXECUTING);
        SyncUtil.updatSyncApplyInfo(info);
        return true;
    }

    @Override
    public boolean preProcessFull(SyncApplyInfo applyInfo) {
        return false;
    }

    @Override
    public boolean postProcessFull(SyncApplyInfo info) {
        return true;
    }



    @Override
    public boolean preInitIncrement(SyncApplyInfo info) {
        return false;
    }

    @Override
    public boolean postInitIncrement(SyncApplyInfo info) {
        return false;
    }

    @Override
    public boolean preProcessIncrement(SyncApplyInfo info) {
        return false;
    }

    @Override
    public boolean postProcessIncrement(SyncApplyInfo info) {
        return false;
    }

}
