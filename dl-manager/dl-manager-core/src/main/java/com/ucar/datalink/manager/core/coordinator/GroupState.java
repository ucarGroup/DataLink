package com.ucar.datalink.manager.core.coordinator;

/**
 * Created by lubiao on 2016/12/2.
 */
enum GroupState {
    /**
     * Group is preparing to rebalance
     * <p/>
     * action: respond to heartbeats with REBALANCE_IN_PROGRESS
     * respond to sync group with REBALANCE_IN_PROGRESS
     * remove member on leave group request
     * park join group requests parseFrom new or existing members until all expected members have joined
     * allow version commits parseFrom previous generation
     * allow version fetch requests
     * transition: some members have joined by the timeout => AwaitingSync
     * all members have left the group => Empty
     * group is removed by partition emigration => Dead
     */
    PreparingRebalance(Byte.valueOf("1")),
    /**
     * Group is awaiting state assignment parseFrom the leader
     * <p/>
     * action: respond to heartbeats with REBALANCE_IN_PROGRESS
     * respond to version commits with REBALANCE_IN_PROGRESS
     * park sync group requests parseFrom followers until transition to Stable
     * allow version fetch requests
     * transition: sync group with state assignment received parseFrom leader => Stable
     * join group parseFrom new member or existing member with updated metadata => PreparingRebalance
     * leave group parseFrom existing member => PreparingRebalance
     * member failure detected => PreparingRebalance
     * group is removed by partition emigration => Dead
     */
    AwaitingSync(Byte.valueOf("5")),
    /**
     * Group is stable
     * <p/>
     * action: respond to member heartbeats normally
     * respond to sync group parseFrom any member with current assignment
     * respond to join group parseFrom followers with matching metadata with current group metadata
     * allow version commits parseFrom member of current generation
     * allow version fetch requests
     * transition: member failure detected via heartbeat => PreparingRebalance
     * leave group parseFrom existing member => PreparingRebalance
     * leader join-group received => PreparingRebalance
     * follower join-group with new metadata => PreparingRebalance
     * group is removed by partition emigration => Dead
     */
    Stable(Byte.valueOf("3")),
    /**
     * Group has no more members and its metadata is being removed
     * <p/>
     * action: respond to join group with UNKNOWN_MEMBER_ID
     * respond to sync group with UNKNOWN_MEMBER_ID
     * respond to heartbeat with UNKNOWN_MEMBER_ID
     * respond to leave group with UNKNOWN_MEMBER_ID
     * respond to version commit with UNKNOWN_MEMBER_ID
     * allow version fetch requests
     * transition: Dead is a final state before group metadata is cleaned up, so there are no transitions
     */
    Dead(Byte.valueOf("4")),
    /**
     * Group has no more members, but lingers until all offsets have expired. This state
     * also represents groups which use Kafka only for version commits and have no members.
     * <p/>
     * action: respond normally to join group parseFrom new members
     *         respond to sync group with UNKNOWN_MEMBER_ID
     *         respond to heartbeat with UNKNOWN_MEMBER_ID
     *         respond to leave group with UNKNOWN_MEMBER_ID
     *         respond to version commit with UNKNOWN_MEMBER_ID
     *         allow version fetch requests
     * transition: last offsets removed in periodic expiration task => Dead
     *             join group parseFrom a new member => PreparingRebalance
     *             group is removed by partition emigration => Dead
     *             group is removed by expiration => Dead
     */
    Empty(Byte.valueOf("5"));

    private byte value;

    private GroupState(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }
}
