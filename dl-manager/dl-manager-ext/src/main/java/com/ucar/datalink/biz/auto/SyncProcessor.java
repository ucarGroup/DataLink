package com.ucar.datalink.biz.auto;

import com.ucar.datalink.domain.sync.SyncApplyInfo;

/**
 * Created by yang.wang09 on 2018-04-24 10:42.
 */
public interface SyncProcessor {

    public void first(SyncApplyInfo info);

    public void second(SyncApplyInfo info);
}
