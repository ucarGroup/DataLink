package com.ucar.datalink.manager.core.coordinator;

import com.ucar.datalink.manager.core.server.DelayedOperation;

/**
 * Delayed heartbeat operations that are added to the purgatory for session timeout checking.
 * Heartbeats are paused during rebalance.
 * <p>
 * Created by lubiao on 2016/12/13.
 */
public class DelayedHeartbeat extends DelayedOperation {
    private final GroupCoordinator coordinator;
    private final GroupMetadata group;
    private final MemberMetadata member;
    private final Long heartbeatDeadline;
    private final Long sessionTimeout;

    public DelayedHeartbeat(GroupCoordinator coordinator, GroupMetadata group, MemberMetadata member, Long heartbeatDeadline, Long sessionTimeout) {
        super(sessionTimeout);
        this.coordinator = coordinator;
        this.group = group;
        this.member = member;
        this.heartbeatDeadline = heartbeatDeadline;
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public boolean tryComplete() {
        return coordinator.tryCompleteHeartbeat(group, member, heartbeatDeadline, () -> forceComplete());
    }

    @Override
    public void onComplete() {
        coordinator.onCompleteHeartbeat();
    }

    @Override
    public void onExpiration() {
        coordinator.onExpireHeartbeat(group, member, heartbeatDeadline);
    }
}
