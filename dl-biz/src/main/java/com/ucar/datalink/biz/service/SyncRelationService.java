package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.relationship.SqlCheckResult;
import com.ucar.datalink.domain.relationship.SyncNode;

import java.util.List;

/**
 * Created by lubiao on 2017/5/23.
 */
public interface SyncRelationService {

    List<SyncNode> getSyncRelationTrees(Long mediaSourceId, String mediaName);
    List<SqlCheckResult> checkSqls(Long mediaSourceId, String sqls);
    void clearSyncRelationCache();
}
