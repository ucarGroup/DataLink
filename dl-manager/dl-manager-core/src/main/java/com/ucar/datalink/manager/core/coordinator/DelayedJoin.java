package com.ucar.datalink.manager.core.coordinator;

import com.ucar.datalink.manager.core.server.DelayedOperation;

/**
 * Delayed rebalance operations that are added to the purgatory when group is preparing for rebalance
 * <p>
 * Whenever a join-group request is received, check if all known group members have requested
 * to re-join the group; if yes, complete this operation to proceed rebalance.
 * <p>
 * When the operation has expired, any known members that have not requested to re-join
 * the group are marked as failed, and complete this operation to proceed rebalance with
 * the rest of the group.
 * <p>
 * Created by lubiao on 2016/12/13.
 */
public class DelayedJoin extends DelayedOperation {
    private final GroupCoordinator coordinator;
    private final GroupMetadata group;
    private final Long sessionTimeout;

    public DelayedJoin(GroupCoordinator coordinator, GroupMetadata group, Long sessionTimeout) {
        super(sessionTimeout);
        this.coordinator = coordinator;
        this.group = group;
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public void onExpiration() {
        coordinator.onExpireJoin(group);
    }

    @Override
    public void onComplete() {
        coordinator.onCompleteJoin(group);
    }

    @Override
    public boolean tryComplete() {
        return coordinator.tryCompleteJoin(group, () -> forceComplete());
    }
}
