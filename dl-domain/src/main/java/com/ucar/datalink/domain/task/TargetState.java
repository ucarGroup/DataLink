package com.ucar.datalink.domain.task;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * The target state of a job is its desired state as indicated by the user
 * through interaction with the REST API. When a job is first created, its
 * target state is "STARTED." This does not mean it has actually started, just that
 * the Datalink framework will attempt to start it after its tasks have been assigned.
 * After the job has been paused, the target state will change to PAUSED,
 * and all the tasks will stop doing work.
 * <p>
 * Target states are persisted in the backing stor, which is read by all of the
 * workers in the group. When a worker sees a new target state for a job which
 * is running, it will transition any tasks which it owns (i.e. which have been
 * assigned to it by the leader) into the desired target state. Upon completion of
 * a task rebalance, the worker will start the task in the last known target state.
 * <p>
 * <p>
 * Created by lubiao on 2016/12/5.
 */
public enum TargetState {
    STARTED,
    PAUSED;

    public static List<TargetState> getAllStates() {
        return Lists.newArrayList(PAUSED, STARTED);
    }
}
