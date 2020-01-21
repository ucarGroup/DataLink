package com.ucar.datalink.biz.auto.envfilter;

import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.domain.sync.SyncApplyStatus;

/**
 * Created by yang.wang09 on 2018-05-07 15:39.
 */
public interface IEnvStrategy {

    // ---  预处理操作 full
    public boolean preInitFull(SyncApplyInfo info);

    public boolean postInitFull(SyncApplyInfo info);

    public boolean preProcessFull(SyncApplyInfo info);

    public boolean postProcessFull(SyncApplyInfo info);


    // -- 预处理操作 increment
    public boolean preInitIncrement(SyncApplyInfo info);

    public boolean postInitIncrement(SyncApplyInfo info);

    public boolean preProcessIncrement(SyncApplyInfo info);

    public boolean postProcessIncrement(SyncApplyInfo info);

}
