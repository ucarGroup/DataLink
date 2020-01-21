package com.ucar.datalink.biz.auto.envfilter;

import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.domain.sync.SyncApplyStatus;
import com.ucar.datalink.util.SyncModifyUtil;
import com.ucar.datalink.util.SyncUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yang.wang09 on 2018-05-07 15:40.
 */
public abstract class AbstractStrategy implements IEnvStrategy{

    private static Logger logger = LoggerFactory.getLogger(AbstractStrategy.class);



    @Override
    public boolean preInitFull(SyncApplyInfo info) {
        return true;
    }

    @Override
    public boolean postInitFull(SyncApplyInfo info) {
        info.setApplyStatus(SyncApplyStatus.FULL_EXECUTING);
        SyncUtil.updatSyncApplyInfo(info);
        return true;
    }

    @Override
    public boolean preProcessFull(SyncApplyInfo applyInfo) {
        long id = applyInfo.getId();
        List<JobConfigInfo> list = SyncUtil.getJobConfigInfoList(id);
        for(JobConfigInfo jci : list) {
            try {
                SyncModifyUtil.checkModifyColumn(jci);
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                return false;
            }
        }
        return true;
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
