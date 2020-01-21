package com.ucar.datalink.manager.core.coordinator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * Test group state transitions and other GroupMetadata functionality
 * <p>
 * Created by lubiao on 2016/12/14.
 */
public class GroupMetadataTest extends TestCase {
    private final String protocolType = "datalink";
    private final String groupId = "groupId";
    private final String clientId = "clientId";
    private final String clientHost = "clientHost";
    private final int rebalanceTimeoutMs = 60000;
    private final int sessionTimeoutMs = 10000;

    private GroupMetadata group = null;

    @Before
    public void setUp() {
        group = new GroupMetadata("groupId");
    }

    @Test
    public void testCanRebalanceWhenStable() {
        assertTrue(group.canRebalance());
    }

    @Test
    public void testCanRebalanceWhenAwaitingSync() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.AwaitingSync);
        assertTrue(group.canRebalance());
    }

    @Test
    public void testCannotRebalanceWhenPreparingRebalance() {
        group.transitionTo(GroupState.PreparingRebalance);
        assertFalse(group.canRebalance());
    }

    @Test
    public void testCannotRebalanceWhenDead() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.Empty);
        group.transitionTo(GroupState.Dead);
        assertFalse(group.canRebalance());
    }

    @Test
    public void testStableToPreparingRebalanceTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        assertState(group, GroupState.PreparingRebalance);
    }

    @Test
    public void testStableToDeadTransition() {
        group.transitionTo(GroupState.Dead);
        assertState(group, GroupState.Dead);
    }

    @Test
    public void testAwaitingSyncToPreparingRebalanceTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.AwaitingSync);
        group.transitionTo(GroupState.PreparingRebalance);
        assertState(group, GroupState.PreparingRebalance);
    }

    @Test
    public void testPreparingRebalanceToDeadTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.Dead);
        assertState(group, GroupState.Dead);
    }

    @Test
    public void testPreparingRebalanceToEmptyTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.Empty);
        assertState(group, GroupState.Empty);
    }

    @Test
    public void testEmptyToDeadTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.Empty);
        group.transitionTo(GroupState.Dead);
        assertState(group, GroupState.Dead);
    }

    @Test
    public void testAwaitingSyncToStableTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.AwaitingSync);
        group.transitionTo(GroupState.Stable);
        assertState(group, GroupState.Stable);
    }

    @Test(expected = IllegalStateException.class)
    public void testStableToStableIllegalTransition() {
        group.transitionTo(GroupState.Stable);
    }

    @Test(expected = IllegalStateException.class)
    public void testStableToAwaitingSyncIllegalTransition() {
        group.transitionTo(GroupState.AwaitingSync);
    }

    @Test(expected = IllegalStateException.class)
    public void testPreparingRebalanceToPreparingRebalanceIllegalTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.PreparingRebalance);
    }

    @Test(expected = IllegalStateException.class)
    public void testPreparingRebalanceToStableIllegalTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.Stable);
    }

    @Test(expected = IllegalStateException.class)
    public void testAwaitingSyncToAwaitingSyncIllegalTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.AwaitingSync);
        group.transitionTo(GroupState.AwaitingSync);
    }

    public void testDeadToDeadIllegalTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.Dead);
        group.transitionTo(GroupState.Dead);
        assertState(group, GroupState.Dead);
    }

    @Test(expected = IllegalStateException.class)
    public void testDeadToStableIllegalTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.Dead);
        group.transitionTo(GroupState.Stable);
    }

    @Test(expected = IllegalStateException.class)
    public void testDeadToPreparingRebalanceIllegalTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.Dead);
        group.transitionTo(GroupState.PreparingRebalance);
    }

    @Test(expected = IllegalStateException.class)
    public void testDeadToAwaitingSyncIllegalTransition() {
        group.transitionTo(GroupState.PreparingRebalance);
        group.transitionTo(GroupState.Dead);
        group.transitionTo(GroupState.AwaitingSync);
    }

    @Test
    public void testSelectProtocol() {
        String memberId = "memberId";
        MemberMetadata member = new MemberMetadata(memberId, groupId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs,
                protocolType, Lists.newArrayList(new ProtocolEntry("range", new byte[0]), new ProtocolEntry("roundrobin", new byte[0])));

        group.add(memberId, member);
        assertEquals("range", group.selectProtocol());

        String otherMemberId = "otherMemberId";
        MemberMetadata otherMember = new MemberMetadata(otherMemberId, groupId, clientId, clientHost, rebalanceTimeoutMs,
                sessionTimeoutMs, protocolType, Lists.newArrayList(new ProtocolEntry("roundrobin", new byte[0]), new ProtocolEntry("range", new byte[0])));

        group.add(otherMemberId, otherMember);
        // now could be either range or robin since there is no majority preference
        assertTrue(Sets.newHashSet("range", "roundrobin").contains(group.selectProtocol()));

        String lastMemberId = "lastMemberId";
        MemberMetadata lastMember = new MemberMetadata(lastMemberId, groupId, clientId, clientHost, rebalanceTimeoutMs,
                sessionTimeoutMs, protocolType, Lists.newArrayList(new ProtocolEntry("roundrobin", new byte[0]), new ProtocolEntry("range", new byte[0])));

        group.add(lastMemberId, lastMember);
        // now we should prefer 'roundrobin'
        assertEquals("roundrobin", group.selectProtocol());
    }

    @Test(expected = IllegalStateException.class)
    public void testSelectProtocolRaisesIfNoMembers() {
        group.selectProtocol();
        fail();
    }

    @Test
    public void testSelectProtocolChoosesCompatibleProtocol() {
        String memberId = "memberId";
        MemberMetadata member = new MemberMetadata(memberId, groupId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs,
                protocolType, Lists.newArrayList(new ProtocolEntry("range", new byte[0]), new ProtocolEntry("roundrobin", new byte[0])));

        String otherMemberId = "otherMemberId";
        MemberMetadata otherMember = new MemberMetadata(otherMemberId, groupId, clientId, clientHost, rebalanceTimeoutMs,
                sessionTimeoutMs, protocolType,
                Lists.newArrayList(new ProtocolEntry("roundrobin", new byte[0]), new ProtocolEntry("blan", new byte[0])));

        group.add(memberId, member);
        group.add(otherMemberId, otherMember);
        assertEquals("roundrobin", group.selectProtocol());
    }

    @Test
    public void testSupportsProtocols() {
        // by default, the group supports everything
        assertTrue(group.supportsProtocols(Sets.newHashSet("roundrobin", "range")));

        String memberId = "memberId";
        MemberMetadata member = new MemberMetadata(memberId, groupId, clientId, clientHost, rebalanceTimeoutMs,
                sessionTimeoutMs, protocolType,
                Lists.newArrayList(new ProtocolEntry("range", new byte[0]), new ProtocolEntry("roundrobin", new byte[0])));

        group.add(memberId, member);
        assertTrue(group.supportsProtocols(Sets.newHashSet("roundrobin", "foo")));
        assertTrue(group.supportsProtocols(Sets.newHashSet("range", "foo")));
        assertFalse(group.supportsProtocols(Sets.newHashSet("foo", "bar")));

        String otherMemberId = "otherMemberId";
        MemberMetadata otherMember = new MemberMetadata(otherMemberId, groupId, clientId, clientHost, rebalanceTimeoutMs,
                sessionTimeoutMs, protocolType,
                Lists.newArrayList(new ProtocolEntry("roundrobin", new byte[0]), new ProtocolEntry("blan", new byte[0])));

        group.add(otherMemberId, otherMember);

        assertTrue(group.supportsProtocols(Sets.newHashSet("roundrobin", "foo")));
        assertFalse(group.supportsProtocols(Sets.newHashSet("range", "foo")));
    }

    @Test
    public void testInitNextGeneration() {
        String memberId = "memberId";
        MemberMetadata member = new MemberMetadata(memberId, groupId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs,
                protocolType, Lists.newArrayList(new ProtocolEntry("roundrobin", new byte[0])));

        group.transitionTo(GroupState.PreparingRebalance);
        member.setAwaitingJoinCallback(item -> {
        });
        group.add(memberId, member);

        assertEquals(0, group.getGenerationId());
        assertNull(group.getProtocol());

        group.initNextGeneration();

        assertEquals(1, group.getGenerationId());
        assertEquals("roundrobin", group.getProtocol());
    }

    @Test
    public void testInitNextGenerationEmptyGroup() {
        assertEquals(GroupState.Empty, group.getState());
        assertEquals(0, group.getGenerationId());
        assertNull(group.getProtocol());

        group.transitionTo(GroupState.PreparingRebalance);
        group.initNextGeneration();

        assertEquals(1, group.getGenerationId());
        assertNull(group.getProtocol());
    }

    private void assertState(GroupMetadata group, GroupState targetState) {
        Set<GroupState> states = Sets.<GroupState>newHashSet(GroupState.Stable, GroupState.PreparingRebalance, GroupState.AwaitingSync, GroupState.Dead);
        states.remove(targetState);
        states.forEach(otherState -> assertFalse(group.is(otherState)));
        assertTrue(group.is(targetState));
    }
}
