package com.ucar.datalink.manager.core.web.dto.task;

import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.lab.LabInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.reader.fq.FqReaderParameter;
import com.ucar.datalink.domain.plugin.writer.fq.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.fq.SerializeMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.CommitMode;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskSyncModeEnum;

import java.util.List;
import java.util.Map;

/**
 * Created by sqq on 2017/6/19.
 */
public class FqTaskModel extends TaskModel {
    private FqReaderParameter fqReaderParameter;
    private List<MediaSourceInfo> originalMediaSourceList;

    public FqTaskModel() {
    }

    public FqTaskModel(TaskModel.TaskBasicInfo taskBasicInfo,
                       Map<String, PluginWriterParameter> writerParameterMap,
                       List<GroupInfo> groupList,
                       List<TargetState> targetStateList,
                       List<MediaSourceInfo> mediaSourceList,
                       List<MediaSourceInfo> originalMediaSourceList,
                       List<PluginWriterParameter.RetryMode> retryModeList,
                       List<RdbmsWriterParameter.SyncMode> rdbSyncModeList,
                       FqReaderParameter fqReaderParameter,
                       List<CommitMode> commitModeList,
                       List<SerializeMode> serializeModeList,
                       List<PartitionMode> partitionModeList,
                       List<LabInfo> labInfoList,
                       List<TaskSyncModeEnum> taskSyncModes) {
        super(taskBasicInfo, writerParameterMap, groupList, targetStateList, mediaSourceList, retryModeList, rdbSyncModeList, commitModeList, serializeModeList,partitionModeList,labInfoList,taskSyncModes);
        this.fqReaderParameter = fqReaderParameter;
        this.originalMediaSourceList = originalMediaSourceList;
    }

    public FqReaderParameter getFqReaderParameter() {
        return fqReaderParameter;
    }

    public void setFqReaderParameter(FqReaderParameter fqReaderParameter) {
        this.fqReaderParameter = fqReaderParameter;
    }

    public List<MediaSourceInfo> getOriginalMediaSourceList() {
        return originalMediaSourceList;
    }

    public void setOriginalMediaSourceList(List<MediaSourceInfo> originalMediaSourceList) {
        this.originalMediaSourceList = originalMediaSourceList;
    }
}
