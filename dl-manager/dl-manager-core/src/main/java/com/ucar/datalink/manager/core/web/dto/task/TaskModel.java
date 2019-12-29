package com.ucar.datalink.manager.core.web.dto.task;

import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.writer.kafka.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.kafka.SerializeMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.CommitMode;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.domain.task.TargetState;

import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2017/4/8.
 */
public class TaskModel {
    private TaskBasicInfo taskBasicInfo;
    private Map<String, PluginWriterParameter> writerParameterMap;

    private List<GroupInfo> groupList;
    private List<TargetState> targetStateList;
    private List<MediaSourceInfo> mediaSourceList;
    private List<PluginWriterParameter.RetryMode> retryModeList;
    private List<RdbmsWriterParameter.SyncMode> rdbSyncModeList;
    private List<CommitMode> commitModeList;
    private List<SerializeMode> serializeModeList;
    private List<PartitionMode> partitionModeList;
    private Map<String, String> currentWriters;

    public TaskModel() {
    }

    public TaskModel(TaskBasicInfo taskBasicInfo,
                     Map<String, PluginWriterParameter> writerParameterMap,
                     List<GroupInfo> groupList,
                     List<TargetState> targetStateList,
                     List<MediaSourceInfo> mediaSourceList,
                     List<PluginWriterParameter.RetryMode> retryModeList,
                     List<RdbmsWriterParameter.SyncMode> rdbSyncModeList,
                     List<CommitMode> commitModeList,
                     List<SerializeMode> serializeModeList,
                     List<PartitionMode> partitionModeList
					 ) {
        this.taskBasicInfo = taskBasicInfo;
        this.writerParameterMap = writerParameterMap;
        this.groupList = groupList;
        this.targetStateList = targetStateList;
        this.mediaSourceList = mediaSourceList;
        this.retryModeList = retryModeList;
        this.rdbSyncModeList = rdbSyncModeList;
        this.commitModeList = commitModeList;
        this.serializeModeList = serializeModeList;
        this.partitionModeList = partitionModeList;
    }

    public TaskBasicInfo getTaskBasicInfo() {
        return taskBasicInfo;
    }

    public void setTaskBasicInfo(TaskBasicInfo taskConfig) {
        this.taskBasicInfo = taskConfig;
    }

    public Map<String, PluginWriterParameter> getWriterParameterMap() {
        return writerParameterMap;
    }

    public void setWriterParameterMap(Map<String, PluginWriterParameter> writerParameterMap) {
        this.writerParameterMap = writerParameterMap;
    }

    public List<GroupInfo> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<GroupInfo> groupList) {
        this.groupList = groupList;
    }

    public List<TargetState> getTargetStateList() {
        return targetStateList;
    }

    public void setTargetStateList(List<TargetState> targetStateList) {
        this.targetStateList = targetStateList;
    }

    public List<MediaSourceInfo> getMediaSourceList() {
        return mediaSourceList;
    }

    public void setMediaSourceList(List<MediaSourceInfo> mediaSourceList) {
        this.mediaSourceList = mediaSourceList;
    }

    public List<PluginWriterParameter.RetryMode> getRetryModeList() {
        return retryModeList;
    }

    public void setRetryModeList(List<PluginWriterParameter.RetryMode> retryModeList) {
        this.retryModeList = retryModeList;
    }

    public List<RdbmsWriterParameter.SyncMode> getRdbSyncModeList() {
        return rdbSyncModeList;
    }

    public void setRdbSyncModeList(List<RdbmsWriterParameter.SyncMode> rdbSyncModeList) {
        this.rdbSyncModeList = rdbSyncModeList;
    }

    public List<CommitMode> getCommitModeList() {
        return commitModeList;
    }

    public void setCommitModeList(List<CommitMode> commitModeList) {
        this.commitModeList = commitModeList;
    }

    public List<SerializeMode> getSerializeModeList() {
        return serializeModeList;
    }

    public void setSerializeModeList(List<SerializeMode> serializeModeList) {
        this.serializeModeList = serializeModeList;
    }

    public Map<String, String> getCurrentWriters() {
        return currentWriters;
    }

    public void setCurrentWriters(Map<String, String> currentWriters) {
        this.currentWriters = currentWriters;
    }


    public List<PartitionMode> getPartitionModeList() {
        return partitionModeList;
    }

    public void setPartitionModeList(List<PartitionMode> partitionModeList) {
        this.partitionModeList = partitionModeList;
    }

    public static class TaskBasicInfo {
        private Long id;
        private String taskName;
        private String taskDesc;
        private TargetState targetState;
        private Long groupId;

        public TaskBasicInfo() {

        }

        public TaskBasicInfo(Long id, String taskName, String taskDesc, TargetState targetState, Long groupId) {
            this.id = id;
            this.taskName = taskName;
            this.taskDesc = taskDesc;
            this.targetState = targetState;
            this.groupId = groupId;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getTaskDesc() {
            return taskDesc;
        }

        public void setTaskDesc(String taskDesc) {
            this.taskDesc = taskDesc;
        }

        public TargetState getTargetState() {
            return targetState;
        }

        public void setTargetState(TargetState targetState) {
            this.targetState = targetState;
        }

        public Long getGroupId() {
            return groupId;
        }

        public void setGroupId(Long groupId) {
            this.groupId = groupId;
        }
    }
}
