package com.ucar.datalink.manager.core.coordinator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ucar.datalink.common.zookeeper.ManagerMetaData;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.manager.core.server.DelayedOperationPurgatory;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.server.ServerStatusMonitor;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.protocol.Errors;
import org.apache.kafka.common.requests.JoinGroupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 分组协调管理器
 * 其设计参考自kafka的GroupCoordinator
 * <p>
 * 1.协议验证相关的逻辑，只实现了协议类型的验证，协议版本的验证暂未实现，短期时间内应该不会有实现的必要
 * <p>
 * Created by lubiao on 2016/11/30.
 */
public class GroupCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(GroupCoordinator.class);

    private static final String NoProtocol = "";
    private static final String NoLeader = "";

    private final DLinkZkUtils zkUtils;
    private final ServerStatusMonitor serverStatusMonitor;
    private final GroupConfig groupConfig;
    private final GroupMetadataManager groupManager;
    private final AtomicBoolean isActive;
    private final DelayedOperationPurgatory<DelayedHeartbeat, MemberKey> heartbeatPurgatory;
    private final DelayedOperationPurgatory<DelayedJoin, GroupKey> joinPurgatory;

    public GroupCoordinator(ManagerConfig config, DLinkZkUtils zkUtils, ServerStatusMonitor serverStatusMonitor) {
        this.zkUtils = zkUtils;
        this.groupConfig = new GroupConfig(config.getGroupMinSessionTimeoutMs(), config.getGroupMaxSessionTimeoutMsProp());
        this.serverStatusMonitor = serverStatusMonitor;
        this.groupManager = new GroupMetadataManager();
        this.isActive = new AtomicBoolean(false);
        this.heartbeatPurgatory = new DelayedOperationPurgatory<>("Heartbeat", null, null, null);
        this.joinPurgatory = new DelayedOperationPurgatory<>("Rebalance", null, null, null);
    }

    public void startup() {
        isActive.set(true);

        logger.info(" ##Group Coordinator is started!");
    }

    public void shutdown() {
        isActive.set(false);
        heartbeatPurgatory.shutdown();
        joinPurgatory.shutdown();

        logger.info(" ##Group Coordinator is shutdown!");
    }

    public void clearGroupMetaInfo() {
        groupManager.removeGroups();
    }

    public GroupMetadataManager getGroupManager() {
        return this.groupManager;
    }

    public Node getActiveGroupCoordinator() {
        ManagerMetaData activeManager = this.serverStatusMonitor.getActiveManagerMetaData();
        if (activeManager != null) {
            return new Node(-1, activeManager.getAddress(), activeManager.getPort());
        }
        return null;
    }

    public List<Node> getAllGroupCoordinators() {
        List<Node> result = Lists.newArrayList();
        int nodeId = 1;
        for (ManagerMetaData mi : serverStatusMonitor.getAllAliveManagers()) {
            result.add(new Node(nodeId++, mi.getAddress(), mi.getPort()));
        }
        return result;
    }

    void onExpireJoin(GroupMetadata group) {
        // currently do nothing
        logger.info("Join Expired for generation " + group.getGenerationId());
    }

    boolean tryCompleteJoin(GroupMetadata group, Supplier<Boolean> forceComplete) {
        synchronized (group) {
            if (group.notYetRejoinedMembers().isEmpty()) {
                return forceComplete.get();
            } else {
                return false;
            }
        }
    }


    void onCompleteJoin(GroupMetadata group) {
        synchronized (group) {
            // remove any members who haven't joined the group yet
            group.notYetRejoinedMembers().forEach(failedMember -> group.remove(failedMember.getMemberId()));
            // TODO: cut the socket connection to the client
            // TODO: delay store,need deeply research

            if (!group.is(GroupState.Dead)) {
                group.initNextGeneration();
                if (group.is(GroupState.Empty)) {
                    logger.info("Group {} with generation {} is now empty", group.getGroupId(), group.getGenerationId());
                } else {
                    logger.info("Stabilized group {} generation {}", group.getGroupId(), group.getGenerationId());

                    // trigger the awaiting join group response callback for all the members after rebalancing
                    for (MemberMetadata member : group.allMemberMetadata()) {
                        assert member.getAwaitingJoinCallback() != null;
                        JoinGroupResult joinResult = new JoinGroupResult(
                                member.getMemberId().equals(group.getLeaderId()) ? group.currentMemberMetadata() : Maps.<String, byte[]>newHashMap(),
                                member.getMemberId(),
                                group.getGenerationId(),
                                group.getProtocol(),
                                group.getLeaderId(),
                                Errors.NONE.code());

                        member.getAwaitingJoinCallback().response(joinResult);
                        member.setAwaitingJoinCallback(null);
                        completeAndScheduleNextHeartbeatExpiration(group, member);
                    }
                }
            }
        }
    }

    void onCompleteHeartbeat() {
        // currently do nothing
    }

    void onExpireHeartbeat(GroupMetadata group, MemberMetadata member, Long heartbeatDeadline) {
        synchronized (group) {
            if (!shouldKeepMemberAlive(member, heartbeatDeadline))
                onMemberFailure(group, member);
        }
    }

    boolean tryCompleteHeartbeat(GroupMetadata group, MemberMetadata member, Long heartbeatDeadline, Supplier<Boolean> forceComplete) {
        synchronized (group) {
            if (shouldKeepMemberAlive(member, heartbeatDeadline) || member.isLeaving()) {
                return forceComplete.get();
            } else {
                return false;
            }
        }
    }

    public void handleJoinGroup(String groupId,
                                String memberId,
                                String clientId,
                                String clientHost,
                                int rebalanceTimeoutMs,
                                int sessionTimeoutMs,
                                String protocolType,
                                List<ProtocolEntry> protocols,
                                Callbacks.JoinCallback responseCallback) {
        if (!isActive.get()) {
            responseCallback.response(joinError(memberId, Errors.GROUP_COORDINATOR_NOT_AVAILABLE.code()));
        } else if (!validGroupId(groupId)) {
            responseCallback.response(joinError(memberId, Errors.INVALID_GROUP_ID.code()));
        } else if (!isCoordinatorForGroup(groupId)) {
            responseCallback.response(joinError(memberId, Errors.NOT_COORDINATOR_FOR_GROUP.code()));
        } else if (isCoordinatorLoadingInProgress(groupId)) {
            responseCallback.response(joinError(memberId, Errors.GROUP_LOAD_IN_PROGRESS.code()));
        } else if (sessionTimeoutMs < groupConfig.getGroupMinSessionTimeoutMs() ||
                sessionTimeoutMs > groupConfig.getGroupMaxSessionTimeoutMs()) {
            responseCallback.response(joinError(memberId, Errors.INVALID_SESSION_TIMEOUT.code()));
        } else {
            // only try to create the group if the group is not unknown AND
            // the member id is UNKNOWN, if member is specified but group does not
            // exist we should reject the request
            GroupMetadata currentGroup = groupManager.getGroup(groupId);
            if (currentGroup == null) {
                if (!JoinGroupRequest.UNKNOWN_MEMBER_ID.equals(memberId)) {
                    responseCallback.response(joinError(memberId, Errors.UNKNOWN_MEMBER_ID.code()));
                } else {
                    //if the group already exists,we use the existing group,so the operation is thread safe.
                    GroupMetadata group = groupManager.tryAddGroup(new GroupMetadata(groupId));
                    doJoinGroup(group, memberId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs, protocolType, protocols, responseCallback);
                }
            } else {
                doJoinGroup(currentGroup, memberId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs, protocolType, protocols, responseCallback);
            }
        }
    }

    private void doJoinGroup(GroupMetadata group,
                             String memberId,
                             String clientId,
                             String clientHost,
                             int rebalanceTimeoutMs,
                             int sessionTimeoutMs,
                             String protocolType,
                             List<ProtocolEntry> protocols,
                             Callbacks.JoinCallback responseCallback) {
        synchronized (group) {
            if (!group.is(GroupState.Empty) &&
                    (!protocolType.equals(group.getProtocolType()) ||
                            !group.supportsProtocols(protocols.stream().map(i -> i.getName()).collect(Collectors.toSet())))) {
                // if the new member does not support the group protocol, reject it
                responseCallback.response(joinError(memberId, Errors.INCONSISTENT_GROUP_PROTOCOL.code()));
            } else if (!JoinGroupRequest.UNKNOWN_MEMBER_ID.equals(memberId) && !group.has(memberId)) {
                // if the member trying to register with a un-recognized id, send the response to let
                // it reset its member id and retry
                responseCallback.response(joinError(memberId, Errors.UNKNOWN_MEMBER_ID.code()));
            } else {
                switch (group.getState()) {
                    case Dead:
                        // if the group is marked as dead, it means some other thread has just removed the group
                        // parseFrom the coordinator metadata; this is likely that the group has migrated to some other
                        // coordinator OR the group is in a transient unstable phase. Let the member retry
                        // joining without the specified member id,
                        responseCallback.response(joinError(memberId, Errors.UNKNOWN_MEMBER_ID.code()));
                        break;
                    case PreparingRebalance:
                        if (JoinGroupRequest.UNKNOWN_MEMBER_ID.equals(memberId)) {
                            addMemberAndRebalance(rebalanceTimeoutMs, sessionTimeoutMs, clientId, clientHost, protocolType, protocols, group, responseCallback);
                        } else {
                            MemberMetadata member = group.get(memberId);
                            updateMemberAndRebalance(group, member, protocols, responseCallback);
                        }
                        break;
                    case AwaitingSync:
                        if (JoinGroupRequest.UNKNOWN_MEMBER_ID.equals(memberId)) {
                            addMemberAndRebalance(rebalanceTimeoutMs, sessionTimeoutMs, clientId, clientHost, protocolType, protocols, group, responseCallback);
                        } else {
                            MemberMetadata member = group.get(memberId);
                            if (member.matches(protocols)) {
                                // member is joining with the same metadata (which could be because it failed to
                                // receive the initial JoinGroup response), so just return current group information
                                // for the current generation.
                                responseCallback.response(new JoinGroupResult(
                                        memberId.equals(group.getLeaderId()) ? group.currentMemberMetadata() : Maps.<String, byte[]>newHashMap(),
                                        memberId,
                                        group.getGenerationId(),
                                        group.getProtocol(),
                                        group.getLeaderId(),
                                        Errors.NONE.code()
                                ));
                            } else {
                                // member has changed metadata, so force a rebalance
                                updateMemberAndRebalance(group, member, protocols, responseCallback);
                            }
                        }
                        break;
                    case Empty:
                    case Stable:
                        if (JoinGroupRequest.UNKNOWN_MEMBER_ID.equals(memberId)) {
                            // if the member id is unknown, register the member to the group
                            addMemberAndRebalance(rebalanceTimeoutMs, sessionTimeoutMs, clientId, clientHost, protocolType, protocols, group, responseCallback);
                        } else {
                            MemberMetadata member = group.get(memberId);
                            if (memberId.equals(group.getLeaderId()) || !member.matches(protocols)) {
                                // force a rebalance if a member has changed metadata or if the leader sends JoinGroup.
                                // The latter allows the leader to trigger rebalances for changes affecting assignment
                                // which do not affect the member metadata (such as topic metadata changes for the consumer)
                                updateMemberAndRebalance(group, member, protocols, responseCallback);
                            } else {
                                // for followers with no actual change to their metadata, just return group information
                                // for the current generation which will allow them to issue SyncGroup
                                responseCallback.response(
                                        new JoinGroupResult(
                                                Maps.<String, byte[]>newHashMap(),
                                                memberId,
                                                group.getGenerationId(),
                                                group.getProtocol(),
                                                group.getLeaderId(),
                                                Errors.NONE.code()));
                            }
                        }
                        break;
                }

                if (group.is(GroupState.PreparingRebalance)) {
                    joinPurgatory.checkAndComplete(new GroupKey(group.getGroupId()));
                }
            }
        }
    }

    public void handleSyncGroup(String groupId,
                                Integer generation,
                                String memberId,
                                Map<String, byte[]> groupAssignment,
                                Callbacks.SyncCallback responseCallback) {
        if (!isActive.get()) {
            responseCallback.response(new byte[0], Errors.GROUP_COORDINATOR_NOT_AVAILABLE.code());
        } else if (!isCoordinatorForGroup(groupId)) {
            responseCallback.response(new byte[0], Errors.NOT_COORDINATOR_FOR_GROUP.code());
        } else {
            GroupMetadata currentGroup = groupManager.getGroup(groupId);
            if (currentGroup == null) {
                responseCallback.response(new byte[0], Errors.UNKNOWN_MEMBER_ID.code());
            } else {
                doSyncGroup(currentGroup, generation, memberId, groupAssignment, responseCallback);
            }
        }
    }

    private void doSyncGroup(GroupMetadata group,
                             Integer generationId,
                             String memberId,
                             Map<String, byte[]> groupAssignment,
                             Callbacks.SyncCallback responseCallback) {
        synchronized (group) {
            if (!group.has(memberId)) {
                responseCallback.response(new byte[0], Errors.UNKNOWN_MEMBER_ID.code());
            } else if (generationId != group.getGenerationId()) {
                responseCallback.response(new byte[0], Errors.ILLEGAL_GENERATION.code());
            } else {
                switch (group.getState()) {
                    case Empty:
                    case Dead:
                        responseCallback.response(new byte[0], Errors.UNKNOWN_MEMBER_ID.code());
                        break;
                    case PreparingRebalance:
                        responseCallback.response(new byte[0], Errors.REBALANCE_IN_PROGRESS.code());
                        break;
                    case AwaitingSync:
                        group.get(memberId).setAwaitingSyncCallback(responseCallback);
                        //TODO: delaystore,暂不实现
                        if (memberId.equals(group.getLeaderId())) {
                            logger.info("Assignment received parseFrom leader for group {} for generation {}", group.getGroupId(), group.getGenerationId());
                            Set<String> missing = Sets.difference(group.allMembers(), groupAssignment.keySet());
                            groupAssignment.putAll(missing.stream().collect(Collectors.toMap(k -> k, k -> new byte[0])));

                            if (group.is(GroupState.AwaitingSync) && generationId.equals(group.getGenerationId())) {
                                setAndPropagateAssignment(group, groupAssignment);
                                group.transitionTo(GroupState.Stable);
                                group.setLastReblanceTime(new Date());
                            }
                        }
                        break;
                    case Stable:
                        // if the group is stable, we just return the current assignment
                        MemberMetadata memberMetadata = group.get(memberId);
                        responseCallback.response(memberMetadata.getAssignment(), Errors.NONE.code());
                        completeAndScheduleNextHeartbeatExpiration(group, group.get(memberId));
                }
            }
        }
    }

    public void handleHeartbeat(String groupId,
                                String memberId,
                                int generationId,
                                Callbacks.HeartbeatCallback responseCallback) {
        if (!isActive.get()) {
            responseCallback.response(Errors.GROUP_COORDINATOR_NOT_AVAILABLE.code());
        } else if (!isCoordinatorForGroup(groupId)) {
            responseCallback.response(Errors.NOT_COORDINATOR_FOR_GROUP.code());
        } else if (isCoordinatorLoadingInProgress(groupId)) {
            // the group is still loading, so respond just blindly
            responseCallback.response(Errors.NONE.code());
        } else {
            GroupMetadata group = groupManager.getGroup(groupId);
            if (group == null) {
                responseCallback.response(Errors.UNKNOWN_MEMBER_ID.code());
            } else {
                synchronized (group) {
                    switch (group.getState()) {
                        case Dead:
                            // if the group is marked as dead, it means some other thread has just removed the group
                            // parseFrom the coordinator metadata; this is likely that the group has migrated to some other
                            // coordinator OR the group is in a transient unstable phase. Let the member retry
                            // joining without the specified member id,
                            responseCallback.response(Errors.UNKNOWN_MEMBER_ID.code());
                            break;
                        case Empty:
                            responseCallback.response(Errors.UNKNOWN_MEMBER_ID.code());
                            break;
                        case AwaitingSync:
                            if (!group.has(memberId)) {
                                responseCallback.response(Errors.UNKNOWN_MEMBER_ID.code());
                            } else {
                                responseCallback.response(Errors.REBALANCE_IN_PROGRESS.code());
                            }
                            break;
                        case PreparingRebalance:
                            if (!group.has(memberId)) {
                                responseCallback.response(Errors.UNKNOWN_MEMBER_ID.code());
                            } else if (generationId != group.getGenerationId()) {
                                responseCallback.response(Errors.ILLEGAL_GENERATION.code());
                            } else {
                                MemberMetadata member = group.get(memberId);
                                completeAndScheduleNextHeartbeatExpiration(group, member);
                                responseCallback.response(Errors.REBALANCE_IN_PROGRESS.code());
                            }
                            break;
                        case Stable:
                            if (!group.has(memberId)) {
                                responseCallback.response(Errors.UNKNOWN_MEMBER_ID.code());
                            } else if (generationId != group.getGenerationId()) {
                                responseCallback.response(Errors.ILLEGAL_GENERATION.code());
                            } else {
                                MemberMetadata member = group.get(memberId);
                                completeAndScheduleNextHeartbeatExpiration(group, member);
                                responseCallback.response(Errors.NONE.code());
                            }
                    }
                }
            }
        }
    }

    public void handleLeaveGroup(
            String groupId,
            String memberId,
            Callbacks.LeaveGroupCallback responseCallback
    ) {
        if (!isActive.get()) {
            responseCallback.response(Errors.GROUP_COORDINATOR_NOT_AVAILABLE.code());
        } else if (!isCoordinatorForGroup(groupId)) {
            responseCallback.response(Errors.NOT_COORDINATOR_FOR_GROUP.code());
        } else if (isCoordinatorLoadingInProgress(groupId)) {
            // the group is still loading, so respond just blindly
            responseCallback.response(Errors.GROUP_LOAD_IN_PROGRESS.code());
        } else {
            GroupMetadata group = groupManager.getGroup(groupId);
            if (group == null) {
                // if the group is marked as dead, it means some other thread has just removed the group
                // parseFrom the coordinator metadata; this is likely that the group has migrated to some other
                // coordinator OR the group is in a transient unstable phase. Let the consumer to retry
                // joining without specified consumer id,
                responseCallback.response(Errors.UNKNOWN_MEMBER_ID.code());
            } else {
                synchronized (group) {
                    if (group.is(GroupState.Dead) || !group.has(memberId)) {
                        responseCallback.response(Errors.UNKNOWN_MEMBER_ID.code());
                    } else {
                        MemberMetadata member = group.get(memberId);
                        removeHeartbeatForLeavingMember(group, member);
                        onMemberFailure(group, member);
                        responseCallback.response(Errors.NONE.code());
                    }
                }
            }
        }
    }

    private MemberMetadata addMemberAndRebalance(int rebalanceTimeoutMs,
                                                 int sessionTimeoutMs,
                                                 String clientId,
                                                 String clientHost,
                                                 String protocolType,
                                                 List<ProtocolEntry> protocols,
                                                 GroupMetadata group,
                                                 Callbacks.JoinCallback responseCallback) {
        // use the client-id with a random id suffix as the member-id
        String memberId = clientId + "-" + group.generateMemberIdSuffix();
        MemberMetadata member = new MemberMetadata(memberId, group.getGroupId(), clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs, protocolType, protocols);
        member.setAwaitingJoinCallback(responseCallback);
        group.add(memberId, member);
        maybePrepareRebalance(group);
        return member;
    }

    private void updateMemberAndRebalance(GroupMetadata group, MemberMetadata member, List<ProtocolEntry> protocols, Callbacks.JoinCallback responseCallback) {
        member.setSupportedProtocols(protocols);
        member.setAwaitingJoinCallback(responseCallback);
        maybePrepareRebalance(group);
    }

    private void maybePrepareRebalance(GroupMetadata group) {
        synchronized (group) {
            if (group.canRebalance()) {
                prepareRebalance(group);
            }
        }
    }

    private void prepareRebalance(GroupMetadata group) {
        // if any members are awaiting sync, cancel their request and have them rejoin
        if (group.is(GroupState.AwaitingSync)) {
            resetAndPropagateAssignmentError(group, Errors.REBALANCE_IN_PROGRESS);
        }

        group.transitionTo(GroupState.PreparingRebalance);
        logger.info(String.format("Preparing to restabilize group %s with old generation %s", group.getGroupId(), group.getGenerationId()));

        int rebalanceTimeout = group.rebalanceTimeoutMs();
        DelayedJoin delayedRebalance = new DelayedJoin(this, group, Long.valueOf(rebalanceTimeout));
        joinPurgatory.tryCompleteElseWatch(delayedRebalance, Lists.newArrayList(new GroupKey(group.getGroupId())));
    }

    private void resetAndPropagateAssignmentError(GroupMetadata group, Errors error) {
        assert group.is(GroupState.AwaitingSync);
        group.allMemberMetadata().forEach(item -> item.setAssignment(new byte[0]));
        propagateAssignment(group, error);
    }

    private void setAndPropagateAssignment(GroupMetadata group, Map<String, byte[]> assignment) {
        assert group.is(GroupState.AwaitingSync);
        group.allMemberMetadata().forEach(member -> member.setAssignment(assignment.get(member.getMemberId())));
        propagateAssignment(group, Errors.NONE);
    }

    private void propagateAssignment(GroupMetadata group, Errors error) {
        group.allMemberMetadata().forEach(member -> {
            if (member.getAwaitingSyncCallback() != null) {
                member.getAwaitingSyncCallback().response(member.getAssignment(), error.code());
                member.setAwaitingSyncCallback(null);

                // reset the session timeout for members after propagating the member's assignment.
                // This is because if any member's session expired while we were still awaiting either
                // the leader sync group or the storage callback, its expiration will be ignored and no
                // future heartbeat expectations will not be scheduled.
                completeAndScheduleNextHeartbeatExpiration(group, member);
            }
        });
    }


    private void completeAndScheduleNextHeartbeatExpiration(GroupMetadata group, MemberMetadata member) {
        // complete current heartbeat expectation
        member.setLatestHeartbeat(System.currentTimeMillis());
        MemberKey memberKey = new MemberKey(member.getGroupId(), member.getMemberId());
        heartbeatPurgatory.checkAndComplete(memberKey);

        // reschedule the next heartbeat expiration deadline
        Long newHeartbeatDeadline = member.getLatestHeartbeat() + member.getSessionTimeoutMs();
        DelayedHeartbeat delayedHeartbeat = new DelayedHeartbeat(this, group, member, newHeartbeatDeadline, (long) member.getSessionTimeoutMs());
        heartbeatPurgatory.tryCompleteElseWatch(delayedHeartbeat, Lists.newArrayList(memberKey));
    }

    private boolean validGroupId(String groupId) {
        return StringUtils.isNotBlank(groupId);
    }

    private boolean isCoordinatorForGroup(String groupId) {
        return this.serverStatusMonitor.activeIsMine();
    }

    private boolean isCoordinatorLoadingInProgress(String groupId) {
        //if active manager is null,means that the manager is initializing
        return this.serverStatusMonitor.getActiveManagerMetaData() == null;
    }

    private JoinGroupResult joinError(String memberId, short errorCode) {
        return new JoinGroupResult(Collections.EMPTY_MAP, memberId, 0, NoProtocol, NoLeader, errorCode);
    }

    private boolean shouldKeepMemberAlive(MemberMetadata member, Long heartbeatDeadline) {
        return member.getAwaitingJoinCallback() != null ||
                member.getAwaitingSyncCallback() != null ||
                member.getLatestHeartbeat() + member.getSessionTimeoutMs() > heartbeatDeadline;
    }

    private void onMemberFailure(GroupMetadata group, MemberMetadata member) {
        logger.trace(String.format("Member %s in group %s has failed", member.getMemberId(), group.getGroupId()));
        group.remove(member.getMemberId());
        switch (group.getState()) {
            case Dead:
            case Empty:
                break;
            case Stable:
            case AwaitingSync:
                maybePrepareRebalance(group);
                break;
            case PreparingRebalance:
                joinPurgatory.checkAndComplete(new GroupKey(group.getGroupId()));
                break;
        }

    }

    private void removeHeartbeatForLeavingMember(GroupMetadata group, MemberMetadata member) {
        member.setIsLeaving(true);
        MemberKey memberKey = new MemberKey(member.getGroupId(), member.getMemberId());
        heartbeatPurgatory.checkAndComplete(memberKey);
    }

    class GroupKey {
        private final String keyLabel;

        public GroupKey(String keyLabel) {
            this.keyLabel = keyLabel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GroupKey groupKey = (GroupKey) o;

            return keyLabel.equals(groupKey.keyLabel);

        }

        @Override
        public int hashCode() {
            return keyLabel.hashCode();
        }
    }

    class MemberKey {
        private final String keyLabel;

        public MemberKey(String groupId, String memberId) {
            this.keyLabel = String.format("%s-%s", groupId, memberId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MemberKey memberKey = (MemberKey) o;

            return keyLabel.equals(memberKey.keyLabel);

        }

        @Override
        public int hashCode() {
            return keyLabel.hashCode();
        }
    }
}
