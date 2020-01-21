package com.ucar.datalink.domain.sync;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by sqq on 2017/9/20.
 */
public enum SyncApplyStatus {
    SUBMITTED, CANCELED, APPROVED, FULL_EXECUTING, INCREMENT_EXECUTING, FULL_FINISH, FULL_FAILED, INCREMENT_FINISH, INCREMENT_FAILED, REJECTED, SUCCEEDED, FAILED, ABANDONED;

    public static List<SyncApplyStatus> getAllSyncApplyStatus() {
        return Lists.newArrayList(SUBMITTED, CANCELED, APPROVED, FULL_EXECUTING, INCREMENT_EXECUTING, FULL_FINISH, FULL_FAILED, INCREMENT_FINISH, INCREMENT_FAILED, REJECTED, SUCCEEDED, FAILED, ABANDONED);
    }

    public static List<SyncApplyStatus> getProcessSyncApplyStatus() {
        return Lists.newArrayList(SUCCEEDED, FAILED, ABANDONED);
    }
}
