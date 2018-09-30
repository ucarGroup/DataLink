package com.ucar.datalink.writer.hdfs.handle.config;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.hdfs.HDFSMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lubiao on 2017/11/21.
 */
public class HdfsConfig {
    private volatile String hdfsAddress;
    private volatile URI hdfsUri;
    private volatile String zkUrl;
    private volatile String zkPort;
    private volatile String hadoopUser;
    private volatile String haNameNode1;
    private volatile String haNameNode2;
    private volatile Configuration configuration;

    HdfsConfig(MediaSourceInfo mediaSourceInfo, HdfsWriterParameter hdfsWriterParameter) {
        this.setHdfsParameter(mediaSourceInfo);
        this.buildConfiguration(mediaSourceInfo, hdfsWriterParameter);
    }

    private void setHdfsParameter(MediaSourceInfo mediaSourceInfo) {
        HDFSMediaSrcParameter hdfsMediaSrcParameter = mediaSourceInfo.getParameterObj();
        this.hdfsAddress = hdfsMediaSrcParameter.getNameServices();
        this.hdfsUri = URI.create(this.hdfsAddress);
        this.hadoopUser = hdfsMediaSrcParameter.getHadoopUser();
        this.haNameNode1 = hdfsMediaSrcParameter.getNameNode1();
        this.haNameNode2 = hdfsMediaSrcParameter.getNameNode2();
        MediaSourceInfo zkMediaSrcInfo = DataLinkFactory.getObject(MediaSourceService.class).getById(hdfsMediaSrcParameter.getZkMediaSourceId());
        String zkServers = ((ZkMediaSrcParameter) zkMediaSrcInfo.getParameterObj()).getServers();
        String[] zkArray = zkServers.split(",");
        List<String> zkList = new ArrayList<>();
        Collections.addAll(zkList, zkArray);
        List<String> ipList = new ArrayList<>();
        for (String zk : zkList) {
            String[] zkServer = zk.split(":");
            ipList.add(zkServer[0]);
            this.zkPort = zkServer[1];
        }
        this.zkUrl = String.join(",", ipList);
    }

    private void buildConfiguration(MediaSourceInfo mappingInfo, HdfsWriterParameter hdfsWriterParameter) {
        this.configuration = HBaseConfiguration.create();
        this.configuration.set("fs.defaultFS", this.hdfsAddress);
        this.configuration.set("dfs.support.append", "true");
        this.configuration.set("hbase.zookeeper.quorum", this.zkUrl);
        this.configuration.set("hbase.zookeeper.property.clientPort", this.zkPort);
        this.configuration.set("dfs.client-write-packet-size", String.valueOf(hdfsWriterParameter.getHdfsPacketSize()));

        // 高可用设置
        String key = hdfsUri.getAuthority();
        this.configuration.set("dfs.nameservices", key);
        this.configuration.set(String.format("dfs.ha.namenodes.%s", key), "nn1,nn2");
        this.configuration.set(String.format("dfs.namenode.rpc-address.%s.nn1", key), this.haNameNode1);
        this.configuration.set(String.format("dfs.namenode.rpc-address.%s.nn2", key), this.haNameNode2);
        this.configuration.set(String.format("dfs.client.failover.proxy.provider.%s", key), "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");

        //System.setProperty("HADOOP_USER_NAME", this.hadoopUser);//为了支持在同一个进程内访问多个hadoop集群，不能再通过系统变量设置用户名
    }

    public String getHdfsAddress() {
        return hdfsAddress;
    }

    public URI getHdfsUri() {
        return hdfsUri;
    }

    public String getZkUrl() {
        return zkUrl;
    }

    public String getZkPort() {
        return zkPort;
    }

    public String getHadoopUser() {
        return hadoopUser;
    }

    public String getHaNameNode1() {
        return haNameNode1;
    }

    public String getHaNameNode2() {
        return haNameNode2;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
