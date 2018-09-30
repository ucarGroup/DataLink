package com.ucar.datalink.worker.core.runtime.coordinate;

import com.ucar.datalink.common.DatalinkProtocol;
import com.ucar.datalink.domain.ClusterConfigState;
import com.ucar.datalink.worker.core.runtime.TaskConfigManager;
import org.apache.kafka.clients.consumer.internals.AbstractCoordinator;
import org.apache.kafka.clients.consumer.internals.ConsumerNetworkClient;
import org.apache.kafka.common.metrics.Measurable;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.requests.JoinGroupRequest;
import org.apache.kafka.common.requests.JoinGroupRequest.ProtocolMetadata;
import org.apache.kafka.common.utils.CircularIterator;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * This class manages the coordination process with the Datalink group coordinator on the manager for managing assignments
 * to workers.
 * <p>
 * 参照[kafka-connect]的[WorkerCoordinator]进行的设计改造
 *
 * @author lubiao
 */
public final class WorkerCoordinator extends AbstractCoordinator implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(WorkerCoordinator.class);

    // Currently doesn't support multiple task assignment strategies, so we just fill in a default value
    public static final String DEFAULT_SUBPROTOCOL = "default";

    private final String restUrl;
    private final TaskConfigManager taskConfigManager;
    private DatalinkProtocol.Assignment assignmentSnapshot;
    private final WorkerCoordinatorMetrics sensors;
    private ClusterConfigState configSnapshot;
    private final WorkerRebalanceListener listener;
    private LeaderState leaderState;

    private boolean rejoinRequested;

    /**
     * Initialize the coordination manager.
     */
    public WorkerCoordinator(ConsumerNetworkClient client,
                             String groupId,
                             int rebalanceTimeoutMs,
                             int sessionTimeoutMs,
                             int heartbeatIntervalMs,
                             Metrics metrics,
                             String metricGrpPrefix,
                             Time time,
                             long retryBackoffMs,
                             String restUrl,
                             TaskConfigManager taskConfigManager,
                             WorkerRebalanceListener listener) {
        super(client,
                groupId,
                rebalanceTimeoutMs,
                sessionTimeoutMs,
                heartbeatIntervalMs,
                metrics,
                metricGrpPrefix,
                time,
                retryBackoffMs);
        this.restUrl = restUrl;
        this.taskConfigManager = taskConfigManager;
        this.assignmentSnapshot = null;
        this.sensors = new WorkerCoordinatorMetrics(metrics, metricGrpPrefix);
        this.listener = listener;
        this.rejoinRequested = false;
    }

    public void requestRejoin() {
        rejoinRequested = true;
    }

    @Override
    public String protocolType() {
        return "datalink";
    }

    public void poll(long timeout) {
        // poll for io until the timeout expires
        final long start = time.milliseconds();
        long now = start;
        long remaining;

        do {
            if (coordinatorUnknown()) {
                ensureCoordinatorReady();
                now = time.milliseconds();
            }

            if (needRejoin()) {
                ensureActiveGroup();
                now = time.milliseconds();
            }

            pollHeartbeat(now);

            long elapsed = now - start;
            remaining = timeout - elapsed;

            // Note that because the network client is shared with the background heartbeat thread,
            // we do not want to block in poll longer than the time to the next heartbeat.
            client.poll(Math.min(Math.max(0, remaining), timeToNextHeartbeat(now)));

            now = time.milliseconds();
            elapsed = now - start;
            remaining = timeout - elapsed;
        } while (remaining > 0);
    }

    @Override
    public List<ProtocolMetadata> metadata() {
        configSnapshot = taskConfigManager.snapshot();
        DatalinkProtocol.WorkerState workerState = new DatalinkProtocol.WorkerState(restUrl, configSnapshot.version());
        ByteBuffer metadata = DatalinkProtocol.serializeMetadata(workerState);
        return Collections.singletonList(new ProtocolMetadata(DEFAULT_SUBPROTOCOL, metadata));
    }

    @Override
    protected void onJoinComplete(int generation, String memberId, String protocol, ByteBuffer memberAssignment) {
        assignmentSnapshot = DatalinkProtocol.deserializeAssignment(memberAssignment);
        // At this point we always consider ourselves to be a member of the cluster, even if there was an assignment
        // error (the leader couldn't make the assignment) or we are behind the config and cannot yet work on our assigned
        // tasks. It's the responsibility of the code driving this process to decide how to react (e.g. trying to get
        // up to date, try to rejoin again, leaving the group and backing off, etc.).
        rejoinRequested = false;
        listener.onAssigned(assignmentSnapshot, generation);
    }

    @Override
    protected Map<String, ByteBuffer> performAssignment(String leaderId, String protocol, Map<String, ByteBuffer> allMemberMetadata) {
        log.debug("Performing task assignment");

        Map<String, DatalinkProtocol.WorkerState> allConfigs = new HashMap<>();
        for (Map.Entry<String, ByteBuffer> entry : allMemberMetadata.entrySet()) {
            allConfigs.put(entry.getKey(), DatalinkProtocol.deserializeMetadata(entry.getValue()));
        }

        long maxVersion = findMaxMemberConfigVersion(allConfigs);
        Long leaderOffset = ensureLeaderConfig(maxVersion);
        if (leaderOffset == null)
            return fillAssignmentsAndSerialize(allConfigs.keySet(), DatalinkProtocol.Assignment.CONFIG_MISMATCH,
                    leaderId, allConfigs.get(leaderId).url(), maxVersion, new HashMap<>());
        return performTaskAssignment(leaderId, leaderOffset, allConfigs);
    }

    private long findMaxMemberConfigVersion(Map<String, DatalinkProtocol.WorkerState> allConfigs) {
        // The new config version is the maximum seen by any member. We always perform assignment using this version,
        // even if some members have fallen behind. The config version used to generate the assignment is included in
        // the response so members that have fallen behind will not use the assignment until they have caught up.
        Long maxVersion = null;
        for (Map.Entry<String, DatalinkProtocol.WorkerState> stateEntry : allConfigs.entrySet()) {
            long memberRootVersion = stateEntry.getValue().version();
            if (maxVersion == null) {
                maxVersion = memberRootVersion;
            } else {
                maxVersion = Math.max(maxVersion, memberRootVersion);
            }
        }

        log.debug("Max config version root: {}, local snapshot config offsets root: {}",
                maxVersion, configSnapshot.version());
        return maxVersion;
    }

    private Long ensureLeaderConfig(long maxVersion) {
        // If this leader is behind some other members, we can't do assignment
        if (configSnapshot.version() < maxVersion) {
            // We might be able to take a new snapshot to catch up immediately and avoid another round of syncing here.
            // Alternatively, if this node has already passed the maximum reported by any other member of the group, it
            // is also safe to use this newer state.
            taskConfigManager.forceRefresh();
            ClusterConfigState updatedSnapshot = taskConfigManager.snapshot();
            if (updatedSnapshot.version() < maxVersion) {
                log.info("Was selected to perform assignments, but do not have latest config found in sync request. " +
                        "Returning an empty configuration to trigger re-sync.");
                return null;
            } else {
                configSnapshot = updatedSnapshot;
                return configSnapshot.version();
            }
        }

        return maxVersion;
    }

    private Map<String, ByteBuffer> performTaskAssignment(String leaderId, long maxVersion, Map<String, DatalinkProtocol.WorkerState> allConfigs) {
        Map<String, List<String>> taskAssignments = new HashMap<>();

        // Perform round-robin task assignment
        CircularIterator<String> memberIt = new CircularIterator<>(sorted(allConfigs.keySet()));

        for (String taskId : shuffle(configSnapshot.tasks())) {
            String taskAssignedTo = memberIt.next();
            log.trace("Assigning task {} to {}", taskId, taskAssignedTo);
            List<String> memberTasks = taskAssignments.get(taskAssignedTo);
            if (memberTasks == null) {
                memberTasks = new ArrayList<>();
                taskAssignments.put(taskAssignedTo, memberTasks);
            }
            memberTasks.add(taskId);
        }

        this.leaderState = new LeaderState(allConfigs, taskAssignments);

        return fillAssignmentsAndSerialize(allConfigs.keySet(), DatalinkProtocol.Assignment.NO_ERROR,
                leaderId, allConfigs.get(leaderId).url(), maxVersion, taskAssignments);
    }

    private Map<String, ByteBuffer> fillAssignmentsAndSerialize(Collection<String> members,
                                                                short error,
                                                                String leaderId,
                                                                String leaderUrl,
                                                                long maxVersion,
                                                                Map<String, List<String>> taskAssignments) {

        Map<String, ByteBuffer> groupAssignment = new HashMap<>();
        for (String member : members) {
            List<String> tasks = taskAssignments.get(member);
            if (tasks == null) {
                tasks = Collections.emptyList();
            }
            DatalinkProtocol.Assignment assignment = new DatalinkProtocol.Assignment(error, leaderId, leaderUrl, maxVersion, tasks);
            log.debug("Assignment: {} -> {}", member, assignment);
            groupAssignment.put(member, DatalinkProtocol.serializeAssignment(assignment));
        }
        log.debug("Finished assignment");
        return groupAssignment;
    }

    @Override
    protected void onJoinPrepare(int generation, String memberId) {
        this.leaderState = null;
        log.debug("Revoking previous assignment {}", assignmentSnapshot);
        if (assignmentSnapshot != null && !assignmentSnapshot.failed()) {
            listener.onRevoked(assignmentSnapshot.leader(), assignmentSnapshot.tasks());
        }
    }

    @Override
    protected boolean needRejoin() {
        return super.needRejoin() || (assignmentSnapshot == null || assignmentSnapshot.failed()) || rejoinRequested;
    }

    public String memberId() {
        Generation generation = generation();
        if (generation != null) {
            return generation.memberId;
        }
        return JoinGroupRequest.UNKNOWN_MEMBER_ID;
    }

    @Override
    public void close() {
        super.close();
    }

    private boolean isLeader() {
        return assignmentSnapshot != null && memberId().equals(assignmentSnapshot.leader());
    }


    public String ownerUrl(String taskId) {
        if (needRejoin() || !isLeader())
            return null;
        return leaderState.ownerUrl(taskId);
    }

    private class WorkerCoordinatorMetrics {
        public final Metrics metrics;
        public final String metricGrpName;

        public WorkerCoordinatorMetrics(Metrics metrics, String metricGrpPrefix) {
            this.metrics = metrics;
            this.metricGrpName = metricGrpPrefix + "-coordinator-metrics";

            Measurable numTasks = (config, now) -> assignmentSnapshot.tasks().size();
            metrics.addMetric(metrics.metricName("assigned-tasks",
                    this.metricGrpName,
                    "The number of tasks currently assigned to this consumer"), numTasks);
        }
    }

    private static <T extends Comparable<T>> List<T> sorted(Collection<T> members) {
        List<T> res = new ArrayList<>(members);
        Collections.sort(res);
        return res;
    }

    private static <T extends Comparable<T>> List<T> shuffle(Collection<T> tasks) {
        List<T> res = new ArrayList<>(tasks);
        Collections.shuffle(res);
        return res;
    }

    private static <K, V> Map<V, K> invertAssignment(Map<K, List<V>> assignment) {
        Map<V, K> inverted = new HashMap<>();
        for (Map.Entry<K, List<V>> assignmentEntry : assignment.entrySet()) {
            K key = assignmentEntry.getKey();
            for (V value : assignmentEntry.getValue())
                inverted.put(value, key);
        }
        return inverted;
    }

    private static class LeaderState {
        private final Map<String, DatalinkProtocol.WorkerState> allMembers;
        private final Map<String, String> taskOwners;

        public LeaderState(Map<String, DatalinkProtocol.WorkerState> allMembers,
                           Map<String, List<String>> taskAssignment) {
            this.allMembers = allMembers;
            this.taskOwners = invertAssignment(taskAssignment);
        }

        private String ownerUrl(String taskId) {
            String ownerId = taskOwners.get(taskId);
            if (ownerId == null)
                return null;
            return allMembers.get(ownerId).url();
        }
    }
}
