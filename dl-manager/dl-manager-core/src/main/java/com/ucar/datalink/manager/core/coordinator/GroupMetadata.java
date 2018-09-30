package com.ucar.datalink.manager.core.coordinator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * Created by lubiao on 2016/12/1.
 */
class GroupMetadata {
    private static final Map<GroupState, Set<GroupState>> validPreviousStates;

    private final String groupId;
    private volatile GroupState state;
    private String protocol;
    private String protocolType = "";
    private String leaderId;
    private int generationId = 0;
    private final Map<String, MemberMetadata> members = new HashMap<String, MemberMetadata>();
    private Date lastReblanceTime;//分组最近一次的Reblance时间

    static {
        validPreviousStates = ImmutableMap.of(
                GroupState.Dead,
                (Set<GroupState>) ImmutableSet.of(GroupState.Stable, GroupState.PreparingRebalance, GroupState.AwaitingSync, GroupState.Empty, GroupState.Dead),
                GroupState.AwaitingSync,
                (Set<GroupState>) ImmutableSet.of(GroupState.PreparingRebalance),
                GroupState.Stable,
                (Set<GroupState>) ImmutableSet.of(GroupState.AwaitingSync),
                GroupState.PreparingRebalance,
                (Set<GroupState>) ImmutableSet.of(GroupState.Stable, GroupState.AwaitingSync, GroupState.Empty),
                GroupState.Empty,
                (Set<GroupState>) ImmutableSet.of(GroupState.PreparingRebalance)
        );
    }

    GroupMetadata(String groupId) {
        this.groupId = groupId;
        this.state = GroupState.Empty;
        this.protocolType = "";
    }

    String getGroupId() {
        return groupId;
    }

    GroupState getState() {
        return state;
    }

    String getProtocol() {
        return protocol;
    }

    String getLeaderId() {
        return leaderId;
    }

    int getGenerationId() {
        return generationId;
    }

    int rebalanceTimeoutMs() {
        return members.values().isEmpty() ? 0 : Collections.max(members.values(), (MemberMetadata m1, MemberMetadata m2) -> {
            if (m1.getRebalanceTimeoutMs() < m2.getRebalanceTimeoutMs()) {
                return -1;
            } else if (m1.getRebalanceTimeoutMs() > m2.getRebalanceTimeoutMs()) {
                return 1;
            }
            return 0;
        }).getRebalanceTimeoutMs();
    }


    Map<String, byte[]> currentMemberMetadata() {
        if (is(GroupState.Dead) || is(GroupState.PreparingRebalance)) {
            throw new IllegalStateException(String.format("Cannot obtain member metadata for group in state %s", state));
        }
        return members.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), k -> k.getValue().metadata(protocol)));
    }

    boolean supportsProtocols(Set<String> memberProtocols) {
        return members.isEmpty() || !Sets.intersection(candidateProtocols(), memberProtocols).isEmpty();
    }

    private Set<String> candidateProtocols() {
        // get the set of protocols that are commonly supported by all members
        List<Set<String>> allProtocols = allMemberMetadata().stream().map(item -> item.protocols()).collect(Collectors.toList());
        return allProtocols.stream().skip(1).collect(() -> new HashSet<String>(allProtocols.get(0)), Set::retainAll, Set::retainAll);
    }

    void add(String memberId, MemberMetadata member) {
        if (members.isEmpty()) {
            this.protocolType = member.getProtocolType();
        }

        assert groupId.equals(member.getGroupId());
        assert protocolType.equals(member.getProtocolType());

        if (leaderId == null) {
            leaderId = memberId;
        }
        members.put(memberId, member);
    }

    String selectProtocol() {
        if (members.isEmpty())
            throw new IllegalStateException("Cannot select protocol for empty group");

        // select the protocol for this group which is supported by all members
        Set<String> candidates = candidateProtocols();

        // let each member vote for one of the protocols and choose the one with the most votes
        return allMemberMetadata().stream()
                .map(item -> item.vote(candidates))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().max(Comparator.comparing(i -> i.getValue())).get().getKey();
    }

    void remove(String memberId) {
        members.remove(memberId);
        if (memberId.equals(leaderId)) {
            if (members.isEmpty()) {
                leaderId = null;
            } else {
                leaderId = members.keySet().stream().findFirst().get();
            }
        }
    }

    void initNextGeneration() {
        assert notYetRejoinedMembers().isEmpty();
        if (!members.isEmpty()) {
            generationId += 1;
            protocol = selectProtocol();
            transitionTo(GroupState.AwaitingSync);
        } else {
            generationId += 1;
            protocol = null;
            transitionTo(GroupState.Empty);
        }
    }

    boolean canRebalance() {
        return validPreviousStates.get(GroupState.PreparingRebalance).contains(this.state);
    }

    List<MemberMetadata> notYetRejoinedMembers() {
        return members.values().stream().filter(m -> m.getAwaitingJoinCallback() == null).collect(Collectors.toList());
    }

    void transitionTo(GroupState groupState) {
        assertValidTransition(groupState);
        this.state = groupState;
    }

    boolean is(GroupState state) {
        return this.state == state;
    }

    boolean has(String memberId) {
        return this.members.containsKey(memberId);
    }

    MemberMetadata get(String memberId) {
        return members.get(memberId);
    }

    List<MemberMetadata> allMemberMetadata() {
        return Lists.newArrayList(members.values());
    }

    String generateMemberIdSuffix() {
        return UUID.randomUUID().toString();
    }

    Set<String> allMembers(){
        return members.keySet();
    }

    private void assertValidTransition(GroupState targetState) {
        if (!validPreviousStates.get(targetState).contains(state))
            throw new IllegalStateException(String.format("Group %s should be in the %s states before moving to %s state. Instead it is in %s state"
                    , groupId, validPreviousStates.get(targetState).toString(), targetState, state));
    }

    public String getProtocolType() {
        return protocolType;
    }

    public Date getLastReblanceTime() {
        return lastReblanceTime;
    }

    public void setLastReblanceTime(Date lastReblanceTime) {
        this.lastReblanceTime = lastReblanceTime;
    }
}
