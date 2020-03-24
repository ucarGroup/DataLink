package com.ucar.datalink.biz.utils.flinker.job;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobConfigConstant;
import com.ucar.datalink.biz.utils.flinker.module.HBaseJobExtendProperty;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.event.HBaseRange;
import com.ucar.datalink.domain.job.HostNodeInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by user on 2017/7/20.
 */
public class HbaseJobConfigServiceImpl extends AbstractJobConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HbaseJobConfigServiceImpl.class);

    private static final String ZOOKEEPER_QUORM = "hbase_zookeeper_quorum";
    private static final String ZOOKEEPER_QUORM_DOT = "hbase.zookeeper.quorum";

    private static final String ZOOKEEPER_CLIENTPORT = "hbase_zookeeper_property_clientPort";
    private static final String ZOOKEEPER_CLIENTPORT_DOT = "hbase.zookeeper.property.clientPort";

    private static final String ZNODE_PARENT = "zookeeper_znode_parent";
    private static final String ZNODE_PARENT_DOT = "zookeeper.znode.parent";

    private static final String SESSION_TIMEOUT = "zookeeper_session_timeout";
    private static final String SESSION_TIMEOUT_DOT = "zookeeper.session.timeout";

    private static final String KEYVALUE_MAXSIZE = "hbase_client_keyvalue_maxsize";
    private static final String KEYVALUE_MAXSIZE_DOT = "hbase.client.keyvalue.maxsize";



    @Override
    public void prepare(JobExtendProperty property) {
        Map<String, String> srcExtendJson = property.getReader();
        String extendJson = JSONObject.toJSONString(srcExtendJson);
        HBaseJobExtendProperty jobExtend = JSONObject.parseObject(extendJson, HBaseJobExtendProperty.class);
        if (StringUtils.isNotBlank(jobExtend.getHbaseSpecifiedNum())) {
            int specifiedNum = Integer.parseInt(jobExtend.getHbaseSpecifiedNum());
            HBaseUtil.howManyPieceOfDataReadToParse(specifiedNum);
        }
    }

    @Override
    public String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, String mediaName) {
        checkHbaseType(info.getParameterObj());
        HBaseMediaSrcParameter hbaseParameter = (HBaseMediaSrcParameter) info.getParameterObj();
        MediaSourceInfo zkInfo = getMediaSourceInfoById(hbaseParameter.getZkMediaSourceId());
        checkZkType(zkInfo.getParameterObj());
        ZkMediaSrcParameter zkParameter = (ZkMediaSrcParameter) zkInfo.getParameterObj();
        String hosts = zkParameter.parseServersToString();

        String json = "";
        try {
            ColumnMeta cm = new ColumnMeta();
            cm.setIsPrimaryKey(true);
            cm.setName("rowkey");
            LinkedList<ColumnMeta> list = new LinkedList<>(metas);
            list.addFirst(cm);
            metas.clear();
            metas.addAll(list);
            String columns = buildColumn(metas);
            String reader = loadJobConfig(FlinkerJobConfigConstant.HBASE_READER);
            json = replaceColumns(reader,columns);
            if (StringUtils.isNotBlank(mediaName)) {
                json = json.replaceAll(FlinkerJobConfigConstant.TABLE, mediaName);
            }
            json = json.replaceAll(FlinkerJobConfigConstant.HBASE_ZK_IP, hosts);
            json = json.replaceAll(FlinkerJobConfigConstant.HBASE_ZK_ZNODE, hbaseParameter.getZnodeParent());
        } catch (Exception e) {
            LOGGER.error("hbase createReaderJson error ", e);
            LOGGER.error("hbase createReader json -> " + json);
        }
        return json;
    }

    @Override
    public String createWriterJson(MediaSourceInfo srcInfo, MediaSourceInfo info, MediaMeta srcMediaMeta, JobExtendProperty property, String mediaName) {
        checkHbaseType(info.getParameterObj());
        Map<String, String> destExtendJson = property.getWriter();
        String extendJson = JSONObject.toJSONString(destExtendJson);
        HBaseJobExtendProperty jobExtend = JSONObject.parseObject(extendJson, HBaseJobExtendProperty.class);
        HBaseMediaSrcParameter hbaseParameter = (HBaseMediaSrcParameter) info.getParameterObj();
        MediaSourceInfo zkInfo = getMediaSourceInfoById(hbaseParameter.getZkMediaSourceId());
        checkZkType(zkInfo.getParameterObj());
        ZkMediaSrcParameter zkParameter = (ZkMediaSrcParameter) zkInfo.getParameterObj();
        String hosts = zkParameter.parseServersToString();

        String json = "";
        try {
            MediaMeta target = changeNameToAlias( MetaMapping.transformToHBase(srcMediaMeta) );
            String columns = "";
            if (StringUtils.isNotBlank(jobExtend.getColumnFamily())) {
                columns = buildColumnByColumnFamily(target.getColumn(), jobExtend.getColumnFamily());
            } else {
                columns = buildColumn(target.getColumn());
            }
            String writer = loadJobConfig(FlinkerJobConfigConstant.HBASE_WRITER);
            json = replaceColumns(writer,columns);
            json = json.replaceAll(FlinkerJobConfigConstant.TABLE, parseMediaName(mediaName));
            json = json.replaceAll(FlinkerJobConfigConstant.HBASE_ZK_IP, hosts);
            json = json.replaceAll(FlinkerJobConfigConstant.HBASE_ZK_ZNODE, hbaseParameter.getZnodeParent());
        } catch (Exception e) {
            LOGGER.error("hbase createWriterJson error ", e);
        }
        return json;
    }


    private void checkHbaseType(MediaSrcParameter parameter) {
        if (!(parameter instanceof HBaseMediaSrcParameter)) {
            throw new RuntimeException("media source type error " + parameter);
        }
    }

    private void checkZkType(MediaSrcParameter parameter) {
        if (!(parameter instanceof ZkMediaSrcParameter)) {
            throw new RuntimeException("media source type error " + parameter);
        }
    }


    private void createPrimaryKeyIfNotExist(List<ColumnMeta> list) {
        if (list == null && list.size() == 0) {
            return;
        }
        boolean containerPK = false;
        for (ColumnMeta cm : list) {
            if (cm.isPrimaryKey()) {
                containerPK = true;
            }
        }
        if (!containerPK) {
            ColumnMeta cm = list.get(0);
            cm.setIsPrimaryKey(true);
        }
    }

    private String buildColumn(List<ColumnMeta> list) {
        createPrimaryKeyIfNotExist(list);
        StringBuffer buf = new StringBuffer();
        boolean isContainPrimaryKey = false;
        for (int i = 0; i < list.size(); i++) {
            ColumnMeta cm = list.get(i);
            if (cm.isPrimaryKey()) {
                buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append("rowkey").append("\"").append(",");
                buf.append("\"").append("type").append("\"").append(":").append("\"").append("Bytes").append("\"").append("}").append(",");
                isContainPrimaryKey = true;
            } else {
                buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append(list.get(i).getColumnFamily()).append(":").append(StringUtils.isEmpty(list.get(i).getName())?"NULL_FLAG":list.get(i).getName()).append("\"").append(",");
                buf.append("\"").append("type").append("\"").append(":").append("\"").append("Bytes").append("\"").append("}").append(",");
            }
        }
        if (!isContainPrimaryKey) {
            //如果源端不包含主键则报错
            throw new RuntimeException("source type cannot contain primary key");
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }


    private String buildColumnByColumnFamily(List<ColumnMeta> list, String columnFamily) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            ColumnMeta cm = list.get(i);
            if (cm.isPrimaryKey()) {
                buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append("rowkey").append("\"").append(",");
                buf.append("\"").append("type").append("\"").append(":").append("\"").append("Bytes").append("\"").append("}").append(",");
            } else {
                buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append(columnFamily).append(":").append(list.get(i).getName()).append("\"").append(",");
                buf.append("\"").append("type").append("\"").append(":").append("\"").append("Bytes").append("\"").append("}").append(",");
            }
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    @Override
    public String readerMopUp(String json, MediaSourceInfo info) {
        if (StringUtils.isNotBlank(json)) {
            if (json.contains(ZOOKEEPER_QUORM)) {
                json = json.replaceAll(ZOOKEEPER_QUORM, ZOOKEEPER_QUORM_DOT);
            }
            if (json.contains(ZOOKEEPER_CLIENTPORT)) {
                json = json.replaceAll(ZOOKEEPER_CLIENTPORT, ZOOKEEPER_CLIENTPORT_DOT);
            }
            if (json.contains(ZNODE_PARENT)) {
                json = json.replaceAll(ZNODE_PARENT, ZNODE_PARENT_DOT);
            }
            if (json.contains(SESSION_TIMEOUT)) {
                json = json.replaceAll(SESSION_TIMEOUT, SESSION_TIMEOUT_DOT);
            }
            if (json.contains(KEYVALUE_MAXSIZE)) {
                json = json.replaceAll(KEYVALUE_MAXSIZE, KEYVALUE_MAXSIZE_DOT);
            }
        }
        return json;
    }

    @Override
    public String writerMopUp(String json, MediaSourceInfo info, JobExtendProperty property) {
        return readerMopUp(json, info);
    }


    private String replaceDot_2_Underline(String json) {
        if (StringUtils.isNotBlank(json)) {
            if (json.contains(ZOOKEEPER_QUORM_DOT)) {
                json = json.replaceAll(ZOOKEEPER_QUORM_DOT, ZOOKEEPER_QUORM);
            }
            if (json.contains(ZOOKEEPER_CLIENTPORT_DOT)) {
                json = json.replaceAll(ZOOKEEPER_CLIENTPORT_DOT, ZOOKEEPER_CLIENTPORT);
            }
            if (json.contains(ZNODE_PARENT_DOT)) {
                json = json.replaceAll(ZNODE_PARENT_DOT, ZNODE_PARENT);
            }
            if (json.contains(SESSION_TIMEOUT_DOT)) {
                json = json.replaceAll(SESSION_TIMEOUT_DOT, SESSION_TIMEOUT);
            }
            if (json.contains(KEYVALUE_MAXSIZE_DOT)) {
                json = json.replaceAll(KEYVALUE_MAXSIZE_DOT, KEYVALUE_MAXSIZE);
            }
        }
        return json;
    }

    public String reloadReader(String json, MediaSourceInfo info) {
        checkHbaseType(info.getParameterObj());
        HBaseMediaSrcParameter hbaseParameter = (HBaseMediaSrcParameter) info.getParameterObj();
        MediaSourceInfo zkInfo = getMediaSourceInfoById(hbaseParameter.getZkMediaSourceId());
        checkZkType(zkInfo.getParameterObj());
        ZkMediaSrcParameter zkParameter = (ZkMediaSrcParameter) zkInfo.getParameterObj();
        String hosts = zkParameter.parseServersToString();
        String znode = hbaseParameter.getZnodeParent();
        if (StringUtils.isNotBlank(hosts) && StringUtils.isNotBlank(znode)) {
            json = replaceDot_2_Underline(json);
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.remove("job.content[0].reader.parameter.hbaseConfig.hbase_zookeeper_quorum");
            connConf.remove("job.content[0].reader.parameter.hbaseConfig.zookeeper_znode_parent");
            connConf.set("job.content[0].reader.parameter.hbaseConfig.hbase_zookeeper_quorum", hosts);
            connConf.set("job.content[0].reader.parameter.hbaseConfig.zookeeper_znode_parent", znode);
            json = connConf.toJSON();
            json = readerMopUp(json, info);
        }
        return json;
    }

    public String reloadWriter(String json, MediaSourceInfo info) {
        checkHbaseType(info.getParameterObj());
        HBaseMediaSrcParameter hbaseParameter = (HBaseMediaSrcParameter) info.getParameterObj();
        MediaSourceInfo zkInfo = getMediaSourceInfoById(hbaseParameter.getZkMediaSourceId());
        checkZkType(zkInfo.getParameterObj());
        ZkMediaSrcParameter zkParameter = (ZkMediaSrcParameter) zkInfo.getParameterObj();
        String hosts = zkParameter.parseServersToString();
        String znode = hbaseParameter.getZnodeParent();
        if (StringUtils.isNotBlank(hosts) && StringUtils.isNotBlank(znode)) {
            json = replaceDot_2_Underline(json);
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.remove("job.content[0].writer.parameter.hbaseConfig.hbase_zookeeper_quorum");
            connConf.remove("job.content[0].writer.parameter.hbaseConfig.zookeeper_znode_parent");
            connConf.set("job.content[0].writer.parameter.hbaseConfig.hbase_zookeeper_quorum", hosts);
            connConf.set("job.content[0].writer.parameter.hbaseConfig.zookeeper_znode_parent", znode);
            json = connConf.toJSON();
            json = writerMopUp(json, info, null);
        }
        return json;
    }


    @Override
    public String replaceJsonResult(String json, Object obj, MediaSourceInfo srcInfo) {
        HBaseRange r = (HBaseRange) obj;
        String startKey = r.getStartRowkey();
        String endKey = r.getEndRowkey();
        json = replaceDot_2_Underline(json);

        DLConfig connConf = DLConfig.parseFrom(json);
        connConf.set("job.content[0].reader.parameter.range.startRowkey", startKey);
        connConf.set("job.content[0].reader.parameter.range.endRowkey", endKey);
        json = connConf.toJSON();
        json = readerMopUp(json, srcInfo);
        json = writerMopUp(json, srcInfo, null);
        return json;
    }

    public List<HostNodeInfo> parseSrcContent(String json) {
        String newJson = replaceDot_2_Underline(json);
        DLConfig connConf = DLConfig.parseFrom(newJson);
        String host = parseHostInfo(newJson,"job.content[0].reader.parameter.hbaseConfig.hbase_zookeeper_quorum");
        String port = parseHostInfo(newJson,"job.content[0].reader.parameter.hbaseConfig.hbase_zookeeper_property_clientPort");
        String other = parseHostInfo(newJson,"job.content[0].reader.parameter.hbaseConfig.zookeeper_znode_parent");
        List<HostNodeInfo> list = new ArrayList<>();
        HostNodeInfo info = new HostNodeInfo();
        info.setHost(host);
        info.setPort(port);
        info.setOther(other);
        list.add(info);
        return list;
    }

    public List<HostNodeInfo> parseDestContent(String json) {
        String newJson = replaceDot_2_Underline(json);
        DLConfig connConf = DLConfig.parseFrom(newJson);
        String host = parseHostInfo(newJson,"job.content[0].writer.parameter.hbaseConfig.hbase_zookeeper_quorum");
        String port = parseHostInfo(newJson, "job.content[0].writer.parameter.hbaseConfig.hbase_zookeeper_property_clientPort");
        String other = parseHostInfo(newJson,"job.content[0].writer.parameter.hbaseConfig.zookeeper_znode_parent");
        List<HostNodeInfo> list = new ArrayList<>();
        HostNodeInfo info = new HostNodeInfo();
        info.setHost(host);
        info.setPort(port);
        info.setOther(other);
        list.add(info);
        return list;
    }

    public List<HostNodeInfo> parseSrcMediaSourceToHostNode(MediaSourceInfo info) {
        return parseMediaSourceToHostNode(info);
    }

    public List<HostNodeInfo> parseDestMediaSourceToHostNode(MediaSourceInfo info) {
        return parseMediaSourceToHostNode(info);
    }

    public List<HostNodeInfo> parseMediaSourceToHostNode(MediaSourceInfo info) {
        checkHbaseType(info.getParameterObj());
        HBaseMediaSrcParameter hbaseParameter = (HBaseMediaSrcParameter) info.getParameterObj();
        MediaSourceInfo zkInfo = getMediaSourceInfoById(hbaseParameter.getZkMediaSourceId());
        checkZkType(zkInfo.getParameterObj());
        ZkMediaSrcParameter zkParameter = (ZkMediaSrcParameter) zkInfo.getParameterObj();
        String hosts = zkParameter.parseServersToString();
        String port = zkParameter.parsePort()+"";
        String other = hbaseParameter.getZnodeParent();
        List<HostNodeInfo> list = new ArrayList<>();
        HostNodeInfo hostInfo = new HostNodeInfo();
        hostInfo.setHost(hosts);
        hostInfo.setPort(port);
        hostInfo.setOther(other);
        list.add(hostInfo);
        return list;
    }

    public boolean compareSrcHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        if( (fromJobConfigHosts==null||fromJobConfigHosts.size()==0) || (fromMediaSourceHosts==null||fromMediaSourceHosts.size()==0)) {
            return false;
        }
        for(HostNodeInfo i : fromJobConfigHosts) {
            for(HostNodeInfo j : fromMediaSourceHosts) {
                if(i.compareTo(j) != 0) {
                    return false;
                }
            }
        }
        return true;
    }


    public boolean compareDestHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        if( (fromJobConfigHosts==null||fromJobConfigHosts.size()==0) || (fromMediaSourceHosts==null||fromMediaSourceHosts.size()==0)) {
            return false;
        }
        for(HostNodeInfo i : fromJobConfigHosts) {
            for(HostNodeInfo j : fromMediaSourceHosts) {
                if(i.compareTo(j) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

}
