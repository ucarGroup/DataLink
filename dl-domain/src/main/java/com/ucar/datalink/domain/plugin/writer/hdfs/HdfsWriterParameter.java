package com.ucar.datalink.domain.plugin.writer.hdfs;

import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;

import java.util.Set;

/**
 * Created by sqq on 2017/7/12.
 */
public class HdfsWriterParameter extends PluginWriterParameter {

    private CommitMode commitMode = CommitMode.Hflush;// Hflush, Hsync
    private Integer streamLeisureLimit = 600000;// 单位：毫秒
    private Integer hdfsPacketSize = 20971520;// 20*1024*1024,packet-size设置为20M，保证数据一次性发送到hdfs-server端，避免被截断
    private Long hsyncInterval = 30000L;//单位：毫秒，用于控制当CommitMode为Hflush的时候，多长时间进行一次hsync操作
    private String hbasePath = "user/hbase";
    private String mysqlBinlogPath = "user/mysql/binlog";

    @Override
    public String initPluginName() {
        return "writer-hdfs";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.hdfs.HdfsTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.hdfs.HdfsTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.HDFS);
    }

    public CommitMode getCommitMode() {
        return commitMode;
    }

    public void setCommitMode(CommitMode commitMode) {
        this.commitMode = commitMode;
    }

    public Integer getStreamLeisureLimit() {
        return streamLeisureLimit;
    }

    public void setStreamLeisureLimit(Integer streamLeisureLimit) {
        this.streamLeisureLimit = streamLeisureLimit;
    }

    public int getHdfsPacketSize() {
        return hdfsPacketSize;
    }

    public void setHdfsPacketSize(Integer hdfsPacketSize) {
        this.hdfsPacketSize = hdfsPacketSize;
    }

    public Long getHsyncInterval() {
        return hsyncInterval;
    }

    public void setHsyncInterval(Long hsyncInterval) {
        this.hsyncInterval = hsyncInterval;
    }

    public String getHbasePath() {
        return hbasePath;
    }

    public void setHbasePath(String hbasePath) {
        this.hbasePath = hbasePath;
    }

    public String getMysqlBinlogPath() {
        return mysqlBinlogPath;
    }

    public void setMysqlBinlogPath(String mysqlBinlogPath) {
        this.mysqlBinlogPath = mysqlBinlogPath;
    }
}
