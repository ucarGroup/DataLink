package com.ucar.datalink.manager.core.coordinator;

import com.ucar.datalink.common.DatalinkProtocol;

import java.util.*;

/**
 * 获取整个集群的运行状态，主要包括：
 * 1.分组状态
 * 2.每个分组下的Worker状态
 * 3.每个Worker的任务分配状态.
 * <p>
 * Created by lubiao on 2017/2/3.
 */
public class ClusterState {
    private final Map<String, GroupData> groupDataMap;

    ClusterState(Map<String, GroupData> groupDataMap) {
        this.groupDataMap = groupDataMap;
    }

    /**
     * 获取task-worker的对应关系，key：taskid，value：workerid
     *
     * @return
     */
    public Map<Long, Long> getTaskWorkerMapping() {
        Map<Long, Long> result = new HashMap<>();
        for (GroupData s : groupDataMap.values()) {
            s.getMembers().forEach(m -> {
                if (m.getAssignment() != null) {
                    m.getAssignment().tasks().forEach(t -> {
                        result.put(Long.valueOf(t), Long.valueOf(m.getClientId()));
                    });
                }
            });
        }
        return result;
    }

    /**
     * 通过GroupId获取GroupData
     *
     * @param groupId
     * @return
     */
    public GroupData getGroupData(Long groupId) {
        return groupDataMap == null ? null : groupDataMap.get(groupId.toString());
    }

    /**
     * 通过taskId获取该Task所属的Member的信息
     *
     * @param taskId
     * @return
     */
    public MemberData getMemberData(Long taskId) {
        for (GroupData s : groupDataMap.values()) {
            for (MemberData m : s.getMembers()) {
                if (m.getAssignment() != null && m.getAssignment().tasks().contains(String.valueOf(taskId))) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有Member信息
     *
     * @return
     */
    public List<MemberData> getAllMemberData() {
        List<MemberData> memberLists = new ArrayList<>();
        for (GroupData gd : groupDataMap.values()) {
            memberLists.addAll(gd.getMembers());
        }
        return memberLists;
    }

    public static class GroupData {
        private final String groupId;
        private final GroupState state;
        private final String protocol;
        private final Integer generationId;
        private final Date lastReblanceTime;
        private final List<MemberData> members = new ArrayList<>();

        public GroupData(String groupId, GroupState state, String protocol, Integer generationId,Date lastReblanceTime) {
            this.groupId = groupId;
            this.state = state;
            this.protocol = protocol;
            this.generationId = generationId;
            this.lastReblanceTime = lastReblanceTime;
        }

        void addMember(MemberData memberData) {
            this.members.add(memberData);
        }

        public String getGroupId() {
            return groupId;
        }

        public GroupState getState() {
            return state;
        }

        public String getProtocol() {
            return protocol;
        }

        public Integer getGenerationId() {
            return generationId;
        }

        public List<MemberData> getMembers() {
            return members;
        }

        public Date getLastReblanceTime() {
            return lastReblanceTime;
        }
    }

    public static class MemberData {
        private final String groupId;
        private final String clientId;
        private final String clientHost;
        private final DatalinkProtocol.Assignment assignment;
        private final DatalinkProtocol.WorkerState workerState;

        public MemberData(String groupId, String clientId, String clientHost, DatalinkProtocol.Assignment assignment, DatalinkProtocol.WorkerState workerState) {
            this.groupId = groupId;
            this.clientId = clientId;
            this.clientHost = clientHost;
            this.assignment = assignment;
            this.workerState = workerState;
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

        public DatalinkProtocol.Assignment getAssignment() {
            return assignment;
        }

        public DatalinkProtocol.WorkerState getWorkerState() {
            return workerState;
        }
    }
}
