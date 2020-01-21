package com.ucar.datalink.worker.core.runtime.coordinate;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.biz.service.DoubleCenterService;
import com.ucar.datalink.biz.service.LabService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.WorkerService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.DatalinkProtocol;
import com.ucar.datalink.domain.ClusterConfigState;
import com.ucar.datalink.domain.lab.LabInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskSyncModeEnum;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.worker.core.runtime.TaskConfigManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
                //发送METADATA、GROUP_COORDINATOR消息
                ensureCoordinatorReady();
                now = time.milliseconds();
            }

            if (needRejoin()) {
                //发送JOIN_GROUP、SYNC_GROUP消息
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

    /**
     * 执行分配
     *
     * @param leaderId
     * @param protocol
     * @param allMemberMetadata
     * @return
     */
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

    /**
     * 分配任务
     *
     * @param leaderId
     * @param maxVersion
     * @param allConfigs 的key是虚拟机的worker id
     * @return
     */
    private Map<String, ByteBuffer> performTaskAssignment(String leaderId, long maxVersion, Map<String, DatalinkProtocol.WorkerState> allConfigs) {

        Map<String, List<String>> taskAssignments = new HashMap<>();

        //对worker分机房
        Map<Long, List<String>> labMap = new HashMap<Long, List<String>>();
        Iterator<Map.Entry<String,DatalinkProtocol.WorkerState>> iterator = allConfigs.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,DatalinkProtocol.WorkerState> entry = iterator.next();
            //获取真实worker id
            String virtualWorkerId = entry.getKey();
            String realWorkerId = virtualWorkerId.substring(0,virtualWorkerId.indexOf("-"));
            WorkerService service = DataLinkFactory.getObject(WorkerService.class);
            WorkerInfo workerInfo = service.getById(Long.valueOf(realWorkerId));
            List<String> workers = labMap.get(workerInfo.getLabId());
            if(workers == null){
                workers = new ArrayList<String>();
                labMap.put(workerInfo.getLabId(),workers);
            }
            workers.add(entry.getKey());
        }

        //对机房下的worker排序
        Iterator<Map.Entry<Long,List<String>>> iteratorLab = labMap.entrySet().iterator();
        Map<Long, CircularIterator<String>> labWorkerMap = new HashMap<Long, CircularIterator<String>>();
        while (iteratorLab.hasNext()){
            Map.Entry<Long,List<String>> entry = iteratorLab.next();
            labWorkerMap.put(entry.getKey(),new CircularIterator<>(sorted(entry.getValue())));
        }

        //中心机房
        String labName = DataLinkFactory.getObject(DoubleCenterService.class).getCenterLab(Constants.WHOLE_SYSTEM);
        LabService labService = DataLinkFactory.getObject(LabService.class);
        LabInfo labInfo = labService.getLabByName(labName);
        Long centerLabId = labInfo.getId();

        //跨机房的任务，所有的worker都要参与分配（专门为跨机房分配使用）
        CircularIterator<String> memberIt = new CircularIterator<>(sorted(allConfigs.keySet()));

        //跨机房同步和单机房同步不会属于同一个组
        String taskSyncMode = TaskSyncModeEnum.singleLabSync.getCode();
        if(CollectionUtils.isNotEmpty(configSnapshot.allTaskConfigs())){
            TaskInfo taskInfo = configSnapshot.allTaskConfigs().get(0);
            taskSyncMode = taskInfo.getTaskSyncMode();
        }

        //单机房分配
        if(StringUtils.equals(taskSyncMode,TaskSyncModeEnum.singleLabSync.getCode())){
            // Perform round-robin task assignment
            for (TaskInfo taskInfo : shuffle(configSnapshot.allTaskConfigs())) {

                //reader关联的数据源
                Long mediaSourceId = taskInfo.getTaskReaderParameterObj().getMediaSourceId();
                MediaSourceService service = DataLinkFactory.getObject(MediaSourceService.class);
                MediaSourceInfo mediaSourceInfo = service.getById(mediaSourceId);
                Boolean isVirtual = false;
                if(mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
                    isVirtual = true;
                }

                //获取虚拟数据源的当前中心机房
                Long dbCenterLabId = -1L;
                if(isVirtual){
                    String dbLabName = DataLinkFactory.getObject(DoubleCenterService.class).getCenterLab(mediaSourceId);
                    LabInfo dbLabInfo = labService.getLabByName(dbLabName);
                    dbCenterLabId = dbLabInfo.getId();
                }

                //如果Task配置了所属机房，将Task分配给对应机房下面的Worker即可
                if(taskInfo.getLabId() != null &&  CollectionUtils.isNotEmpty(labMap.get(taskInfo.getLabId()))){
                    String taskAssignedTo = labWorkerMap.get(taskInfo.getLabId()).next();
                    doPerform(String.valueOf(taskInfo.getId()),taskAssignedTo,taskAssignments);
                }
                //如果Task没有配置所有机房，但Reader数据源是【非虚拟介质】，将Task分配给Reader数据源所属的机房的worker
                else if(taskInfo.getLabId() == null && (!isVirtual) && CollectionUtils.isNotEmpty(labMap.get(mediaSourceInfo.getLabId()))){
                    String taskAssignedTo = labWorkerMap.get(mediaSourceInfo.getLabId()).next();
                    doPerform(String.valueOf(taskInfo.getId()),taskAssignedTo,taskAssignments);
                }
                //如果Task没有配置所有机房，Reader数据源是【虚拟介质】，根据Reader数据源上的中心机房,将Task分配给该机房的worker即可
                else if(taskInfo.getLabId() == null && isVirtual && CollectionUtils.isNotEmpty(labMap.get(dbCenterLabId))){
                    String taskAssignedTo = labWorkerMap.get(dbCenterLabId).next();
                    doPerform(String.valueOf(taskInfo.getId()),taskAssignedTo,taskAssignments);
                }
                //将Task分配给中心机房的worker
                else if(CollectionUtils.isNotEmpty(labMap.get(centerLabId))){
                    String taskAssignedTo = labWorkerMap.get(centerLabId).next();
                    doPerform(String.valueOf(taskInfo.getId()),taskAssignedTo,taskAssignments);
                }
                //如果上面条件都不满足，分配给非中心机房
                else{
                    Iterator<Map.Entry<Long,CircularIterator<String>>> it = labWorkerMap.entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry<Long,CircularIterator<String>> entry = it.next();
                        if(!entry.getKey().equals(centerLabId)){
                            String taskAssignedTo = entry.getValue().next();
                            doPerform(String.valueOf(taskInfo.getId()),taskAssignedTo,taskAssignments);
                            break;
                        }
                    }
                }
            }
        }
        //跨机房分配，组下的worker都参与分配
        else{
            for (TaskInfo taskInfo : shuffle(configSnapshot.allTaskConfigs())) {
                String taskAssignedTo = memberIt.next();
                doPerform(String.valueOf(taskInfo.getId()),taskAssignedTo,taskAssignments);
            }
        }


        log.debug("-------------任务分配结果是：" + JSON.toJSONString(taskAssignments) + " -------------");

        this.leaderState = new LeaderState(allConfigs, taskAssignments);

        return fillAssignmentsAndSerialize(allConfigs.keySet(), DatalinkProtocol.Assignment.NO_ERROR,
                leaderId, allConfigs.get(leaderId).url(), maxVersion, taskAssignments);

    }

    /**
     * 分配
     *
     * @param taskId
     * @param taskAssignedTo
     * @param taskAssignments
     */
    private void doPerform(String taskId,String taskAssignedTo,Map<String, List<String>> taskAssignments){
        log.trace("Assigning task {} to {}", taskId, taskAssignedTo);
        List<String> memberTasks = taskAssignments.get(taskAssignedTo);
        if (memberTasks == null) {
            memberTasks = new ArrayList<>();
            taskAssignments.put(taskAssignedTo, memberTasks);
        }
        memberTasks.add(taskId);
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
