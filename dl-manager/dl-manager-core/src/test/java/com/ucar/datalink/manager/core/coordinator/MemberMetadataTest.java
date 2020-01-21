package com.ucar.datalink.manager.core.coordinator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 *
 * Created by lubiao on 2016/12/14.
 */
public class MemberMetadataTest extends TestCase {
    private final String groupId = "groupId";
    private final String clientId = "clientId";
    private final String clientHost = "clientHost";
    private final String memberId = "memberId";
    private final String protocolType = "datalink";
    private final int rebalanceTimeoutMs = 60000;
    private final int sessionTimeoutMs = 10000;

    @Test
    public void testMatchesSupportedProtocols() {
        List<ProtocolEntry> protocols = Lists.newArrayList(new ProtocolEntry("range", new byte[0]));
        MemberMetadata member = new MemberMetadata(memberId, groupId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs,
                protocolType, protocols);
        assertTrue(member.matches(protocols));
        assertFalse(member.matches(Lists.<ProtocolEntry>newArrayList(new ProtocolEntry("range", new byte[]{1}))));
        assertFalse(member.matches(Lists.<ProtocolEntry>newArrayList(new ProtocolEntry("roundrobin", new byte[0]))));
        assertFalse(member.matches(Lists.<ProtocolEntry>newArrayList(new ProtocolEntry("range", new byte[0]), new ProtocolEntry("roundrobin", new byte[0]))));
    }

    @Test
    public void testVoteForPreferredProtocol() {
        List<ProtocolEntry> protocols = Lists.newArrayList(
                new ProtocolEntry("range", new byte[0]),
                new ProtocolEntry("roundrobin", new byte[0]));

        MemberMetadata member = new MemberMetadata(memberId, groupId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs,
                protocolType, protocols);
        assertEquals("range", member.vote(Sets.newHashSet("range", "roundrobin")));
        assertEquals("roundrobin", member.vote(Sets.newHashSet("blah", "roundrobin")));
    }

    @Test
    public void testMetadata() {
        List<ProtocolEntry> protocols = Lists.newArrayList(
                new ProtocolEntry("range", new byte[]{0}),
                new ProtocolEntry("roundrobin", new byte[]{1}));

        MemberMetadata member = new MemberMetadata(memberId, groupId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs,
                protocolType, protocols);
        assertTrue(Arrays.equals(new byte[]{0}, member.metadata("range")));
        assertTrue(Arrays.equals(new byte[]{1}, member.metadata("roundrobin")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMetadataRaisesOnUnsupportedProtocol() {
        List<ProtocolEntry> protocols = Lists.newArrayList(
                new ProtocolEntry("range", new byte[0]),
                new ProtocolEntry("roundrobin", new byte[0]));

        MemberMetadata member = new MemberMetadata(memberId, groupId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs,
                protocolType, protocols);
        member.metadata("blah");
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVoteRaisesOnNoSupportedProtocols() {
        List<ProtocolEntry> protocols = Lists.newArrayList(
                new ProtocolEntry("range", new byte[]{0}),
                new ProtocolEntry("roundrobin", new byte[]{1}));

        MemberMetadata member = new MemberMetadata(memberId, groupId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs,
                protocolType, protocols);
        member.vote(Sets.newHashSet("blah"));
        fail();
    }
}
