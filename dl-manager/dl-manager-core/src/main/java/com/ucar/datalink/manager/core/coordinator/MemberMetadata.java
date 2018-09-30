package com.ucar.datalink.manager.core.coordinator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Member metadata contains the following metadata:
 *
 * Heartbeat metadata:
 * 1. negotiated heartbeat session timeout
 * 2. timestamp of the latest heartbeat
 *
 * Protocol metadata:
 * 1. the list of supported protocols (ordered by preference)
 * 2. the metadata associated with each protocol
 *
 * In addition, it also contains the following state information:
 *
 * 1. Awaiting rebalance callback: when the group is in the prepare-rebalance state,
 *                                 its rebalance callback will be kept in the metadata if the
 *                                 member has sent the join group request
 * 2. Awaiting sync callback: when the group is in the awaiting-sync state, its sync callback
 *                            is kept in metadata until the leader provides the group assignment
 *                            and the group transitions to stable
 *
 * Created by lubiao on 2016/12/1.
 */
class MemberMetadata {
    private final String memberId;
    private final String groupId;
    private final String clientId;
    private final String clientHost;
    private final int rebalanceTimeoutMs;
    private final int sessionTimeoutMs;
    private final String protocolType;
    private List<ProtocolEntry> supportedProtocols;

    private byte[] assignment = new byte[0];
    private Long latestHeartbeat = -1L;
    private boolean isLeaving = false;
    private volatile Callbacks.JoinCallback awaitingJoinCallback;
    private volatile Callbacks.SyncCallback awaitingSyncCallback;

    MemberMetadata(String memberId, String groupId, String clientId, String clientHost, int rebalanceTimeoutMs, int sessionTimeoutMs, String protocolType, List<ProtocolEntry> protocols) {
        this.memberId = memberId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.clientHost = clientHost;
        this.rebalanceTimeoutMs = rebalanceTimeoutMs;
        this.sessionTimeoutMs = sessionTimeoutMs;
        this.protocolType = protocolType;
        this.supportedProtocols = protocols;
    }

    public boolean matches(List<ProtocolEntry> protocols) {
        if (protocols.size() != this.supportedProtocols.size()) {
            return false;
        }
        for (int i = 0; i < protocols.size(); i++) {
            ProtocolEntry p1 = protocols.get(i);
            ProtocolEntry p2 = supportedProtocols.get(i);
            if (!p1.getName().equals(p2.getName()) || !Arrays.equals(p1.getMetadata(), p2.getMetadata())) {
                return false;
            }
        }
        return true;
    }

    public Set<String> protocols() {
        return this.supportedProtocols.stream().map(item -> item.getName()).collect(Collectors.toSet());
    }

    /**
     * Vote for one of the potential group protocols. This takes into account the protocol preference as
     * indicated by the order of supported protocols and returns the first one also contained in the set
     */
    String vote(Set<String> candidates) {
        Optional<ProtocolEntry> protocol = supportedProtocols.stream().filter(item -> candidates.contains(item.getName())).findFirst();
        if (protocol.isPresent()) {
            return protocol.get().getName();
        } else {
            throw new IllegalArgumentException("Member does not support any of the candidate protocols");
        }
    }

    /**
     * Get metadata corresponding to the provided protocol.
     */
    byte[] metadata(String protocol) {
        Optional<ProtocolEntry> entry = supportedProtocols.stream().filter(item -> item.getName().equals(protocol)).findFirst();
        if (entry.isPresent()) {
            return entry.get().getMetadata();
        } else {
            throw new IllegalArgumentException("Member does not support protocol");
        }
    }

    public String getMemberId() {
        return memberId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientHost() {
        return clientHost;
    }

    public int getRebalanceTimeoutMs() {
        return rebalanceTimeoutMs;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public byte[] getAssignment() {
        return assignment;
    }

    public void setAssignment(byte[] assignment) {
        this.assignment = assignment;
    }

    public Callbacks.JoinCallback getAwaitingJoinCallback() {
        return awaitingJoinCallback;
    }

    public void setAwaitingJoinCallback(Callbacks.JoinCallback awaitingJoinCallback) {
        this.awaitingJoinCallback = awaitingJoinCallback;
    }

    public Callbacks.SyncCallback getAwaitingSyncCallback() {
        return awaitingSyncCallback;
    }

    public void setAwaitingSyncCallback(Callbacks.SyncCallback awaitingSyncCallback) {
        this.awaitingSyncCallback = awaitingSyncCallback;
    }

    public Long getLatestHeartbeat() {
        return latestHeartbeat;
    }

    public void setLatestHeartbeat(Long latestHeartbeat) {
        this.latestHeartbeat = latestHeartbeat;
    }

    public boolean isLeaving() {
        return isLeaving;
    }

    public void setIsLeaving(boolean isLeaving) {
        this.isLeaving = isLeaving;
    }

    public List<ProtocolEntry> getSupportedProtocols() {
        return supportedProtocols;
    }

    public void setSupportedProtocols(List<ProtocolEntry> supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }
}
