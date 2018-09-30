package com.ucar.datalink.manager.core.coordinator;

/**
 * Created by lubiao on 2016/12/1.
 */
public class Callbacks {
    public static interface JoinCallback {
        void response(JoinGroupResult joinResult);
    }

    public static interface SyncCallback {
        void response(byte[] assignment, Short error);
    }

    public static interface HeartbeatCallback{
        void response(short errorCode);
    }

    public static interface LeaveGroupCallback{
        void response(short errorCode);
    }
}
