package com.ucar.datalink.manager.core.web.dto.task;

import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.reader.hbase.HBaseReaderParameter;
import com.ucar.datalink.domain.plugin.writer.hdfs.CommitMode;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.domain.task.TargetState;

import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2017/6/19.
 */
public class HBaseTaskModel extends TaskModel {
    private HBaseReaderParameter hbaseReaderParameter;
    private List<MediaSourceInfo> zkMediaSourceList;

    public HBaseTaskModel() {
    }

    public HBaseTaskModel(TaskBasicInfo taskBasicInfo,
                          Map<String, PluginWriterParameter> writerParameterMap,
                          List<GroupInfo> groupList,
                          List<TargetState> targetStateList,
                          List<MediaSourceInfo> mediaSourceList,
                          List<MediaSourceInfo> zkMediaSourceList,
                          List<PluginWriterParameter.RetryMode> retryModeList,
                          List<RdbmsWriterParameter.SyncMode> rdbSyncModeList,
                          HBaseReaderParameter hbaseReaderParameter,
                          List<CommitMode> commitModeList) {
        super(taskBasicInfo, writerParameterMap, groupList, targetStateList, mediaSourceList, retryModeList, rdbSyncModeList, commitModeList);
        this.hbaseReaderParameter = hbaseReaderParameter;
        this.zkMediaSourceList = zkMediaSourceList;
    }

    public HBaseReaderParameter getHbaseReaderParameter() {
        return hbaseReaderParameter;
    }

    public void setHbaseReaderParameter(HBaseReaderParameter hbaseReaderParameter) {
        this.hbaseReaderParameter = hbaseReaderParameter;
    }

    public List<MediaSourceInfo> getZkMediaSourceList() {
        return zkMediaSourceList;
    }

    public void setZkMediaSourceList(List<MediaSourceInfo> zkMediaSourceList) {
        this.zkMediaSourceList = zkMediaSourceList;
    }
}
