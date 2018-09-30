package com.ucar.datalink.manager.core.coordinator;

import com.ucar.datalink.common.DatalinkProtocol;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by lubiao on 2016/12/1.
 */
public class GroupMetadataManager {

    private ConcurrentHashMap<String, GroupMetadata> groupMetadataCache = new ConcurrentHashMap<String, GroupMetadata>();

    GroupMetadata getGroup(String groupId) {
        return groupMetadataCache.get(groupId);
    }

    void startup() {
        //do nothing now.
    }

    void shutdown() {
        //do nothing now.
    }

    /**
     * 获取整个集群的运行状态，该方法不建议高频率调用
     *
     * @return
     */
    public ClusterState getClusterState() {
        Map<String, ClusterState.GroupData> groupDataMap = new HashMap<>();
        for (Map.Entry<String, GroupMetadata> entry : groupMetadataCache.entrySet()) {
            GroupMetadata gm = entry.getValue();
            ClusterState.GroupData gd = new ClusterState.GroupData(
                    gm.getGroupId(),
                    gm.getState(),
                    gm.getProtocol(),
                    gm.getGenerationId(),
                    gm.getLastReblanceTime()
            );
            gm.allMemberMetadata().forEach(m -> {
                DatalinkProtocol.Assignment assignment = m.getAssignment().length == 0 ? null : DatalinkProtocol.deserializeAssignment(ByteBuffer.wrap(m.getAssignment()));
                DatalinkProtocol.WorkerState workerState = DatalinkProtocol.deserializeMetadata(ByteBuffer.wrap(m.metadata(gm.getProtocol())));
                ClusterState.MemberData md = new ClusterState.MemberData(gm.getGroupId(), m.getClientId(), m.getClientHost(), assignment, workerState);
                gd.addMember(md);
            });
            groupDataMap.put(gd.getGroupId(), gd);
        }
        return new ClusterState(groupDataMap);
    }

    /**
     * Add a group or get the group associated with the given groupId if it already exists
     */
    synchronized GroupMetadata tryAddGroup(GroupMetadata group) {
        GroupMetadata currentGroup = groupMetadataCache.putIfAbsent(group.getGroupId(), group);
        if (currentGroup != null) {
            return currentGroup;
        } else {
            return group;
        }
    }

    /**
     * clear all the group metadatas.
     */
    synchronized void removeGroups() {
        groupMetadataCache.clear();
    }
}
