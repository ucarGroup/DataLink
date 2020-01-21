package com.ucar.datalink.biz.auto;

import com.ucar.datalink.domain.sync.SyncApplyInfo;

/**
 * Created by yang.wang09 on 2018-04-24 10:42.
 */
public interface SyncExecutor extends SyncProcessor {

    public void initFull(SyncApplyInfo info);

    public void processFull(SyncApplyInfo info);

    public void initIncrement(SyncApplyInfo info);

    public void processIncrement(SyncApplyInfo info);

    public boolean checkFinish(SyncApplyInfo info);

    public boolean isSuccess(SyncApplyInfo info);

    public boolean canAutoProcess(SyncApplyInfo info);

}
