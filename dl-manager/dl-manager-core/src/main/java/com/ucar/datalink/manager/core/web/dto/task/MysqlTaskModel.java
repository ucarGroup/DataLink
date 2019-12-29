package com.ucar.datalink.manager.core.web.dto.task;

import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.reader.mysql.GroupSinkMode;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderParameter;
import com.ucar.datalink.domain.plugin.writer.kafka.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.kafka.SerializeMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.CommitMode;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskParameter;


import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2017/4/7.
 */
public class MysqlTaskModel extends TaskModel {
    private List<EventType> filterEventTypeList;
    private List<GroupSinkMode> groupSinkModeList;
    private MysqlReaderParameter mysqlReaderParameter;
    private TaskParameter taskParameter;


    public MysqlTaskModel() {

    }

    public MysqlTaskModel(TaskBasicInfo taskBasicInfo,
                          Map<String, PluginWriterParameter> writerParameterMap,
                          List<GroupInfo> groupList,
                          List<TargetState> targetStateList,
                          List<MediaSourceInfo> mediaSourceList,
                          List<PluginWriterParameter.RetryMode> retryModeList,
                          List<RdbmsWriterParameter.SyncMode> rdbSyncModeList,
                          List<CommitMode> commitModeList,
                          List<EventType> filterEventTypeList,
                          List<GroupSinkMode> groupSinkModeList,
                          List<SerializeMode> serializeModeList,
                          List<PartitionMode> partitionModeList,
                          MysqlReaderParameter mysqlReaderParameter,
                          TaskParameter taskParameter) {
        super(taskBasicInfo, writerParameterMap, groupList, targetStateList, mediaSourceList, retryModeList, rdbSyncModeList, commitModeList, serializeModeList,partitionModeList);
        this.filterEventTypeList = filterEventTypeList;
        this.groupSinkModeList = groupSinkModeList;
        this.mysqlReaderParameter = mysqlReaderParameter;
        this.taskParameter = taskParameter;
    }


    public List<EventType> getFilterEventTypeList() {
        return filterEventTypeList;
    }

    public void setFilterEventTypeList(List<EventType> filterEventTypeList) {
        this.filterEventTypeList = filterEventTypeList;
    }

    public MysqlReaderParameter getMysqlReaderParameter() {
        return mysqlReaderParameter;
    }

    public void setMysqlReaderParameter(MysqlReaderParameter mysqlReaderParameter) {
        this.mysqlReaderParameter = mysqlReaderParameter;
    }

    public List<GroupSinkMode> getGroupSinkModeList() {
        return groupSinkModeList;
    }

    public void setGroupSinkModeList(List<GroupSinkMode> groupSinkModeList) {
        this.groupSinkModeList = groupSinkModeList;
    }

    public TaskParameter getTaskParameter() {
        return taskParameter;
    }

    public void setTaskParameter(TaskParameter taskParameter) {
        this.taskParameter = taskParameter;
    }

}
