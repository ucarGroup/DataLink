package com.ucar.datalink.biz.utils.flinker.job;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.ElasticSearchUtil;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobConfigConstant;
import com.ucar.datalink.biz.utils.flinker.module.ElasticSearchJobExtendProperty;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.HostNodeInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.es.EsMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2017/7/20.
 */
public class ESJobConfigServiceImpl extends AbstractJobConfigService{

    private static final Logger LOGGER = LoggerFactory.getLogger(ESJobConfigServiceImpl.class);

    @Override
    public String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, String mediaName) {
        checkType( info.getParameterObj() );
        EsMediaSrcParameter parameter = (EsMediaSrcParameter)info.getParameterObj();
        Map<String,String> srcExtendJson = property.getReader();
        String hosts = parameter.getClusterHosts();
        String tcpPort = parameter.getTcpPort()+"";
        String httpPort = parameter.getHttpPort()+"";
        String userName = parameter.getUserName();
        String password = parameter.getPassword();

        String json = "";
        try{
            String columns = buildColumnParm( metas );
            String reader = loadJobConfig(FlinkerJobConfigConstant.ES_READER);
            if(StringUtils.isNotBlank(columns)){
                //json = writer.replaceAll(FlinkerJobConfigConstant.COLUMN, columns);
                json = replaceColumns(reader,columns);
            } else {
                json = replaceColumns(reader,"");
            }
            if(StringUtils.isNotBlank(hosts)){
                json = json.replaceAll(FlinkerJobConfigConstant.HOSTS, hosts);
            }
            if(StringUtils.isNotBlank(password)){
                json = json.replaceAll(FlinkerJobConfigConstant.PASSWORD, password);
            }
            if(StringUtils.isNotBlank(userName)){
                json = json.replaceAll(FlinkerJobConfigConstant.USERNAME, userName);
            }
            if(StringUtils.isNotBlank(mediaName)){
                json = json.replaceAll(FlinkerJobConfigConstant.TABLE, mediaName);
                String[] arr = mediaName.split("\\.");
                if(arr!=null && arr.length==2) {
                    json = json.replaceAll(FlinkerJobConfigConstant.ES_INDEX, arr[0]);
                    json = json.replaceAll(FlinkerJobConfigConstant.ES_TYPE, arr[1]);
                } else {
                    json = json.replaceAll(FlinkerJobConfigConstant.ES_INDEX, mediaName);
                    json = json.replaceAll(FlinkerJobConfigConstant.ES_TYPE, mediaName);
                }
            }
            json = json.replaceAll("\""+ FlinkerJobConfigConstant.HTTP_PORT+"\"", String.valueOf(httpPort));
            json = json.replaceAll("\""+ FlinkerJobConfigConstant.TCP_PORT+"\"", String.valueOf(tcpPort));
            json = processReaderExtendJson(json,srcExtendJson);
        }catch (Exception e){
            LOGGER.error("es createReaderJson error ",e);
        }
        return json;
    }


    @Override
    public String createWriterJson(MediaSourceInfo srcInfo, MediaSourceInfo info, MediaMeta srcMediaMeta, JobExtendProperty property, String mediaName) {
        checkType( info.getParameterObj() );
        EsMediaSrcParameter parameter = (EsMediaSrcParameter)info.getParameterObj();
        Map<String,String> destExtendJson = property.getWriter();
        String hosts = parameter.getClusterHosts();
        String tcpPort = parameter.getTcpPort()+"";
        String httpPort = parameter.getHttpPort()+"";
        String userName = parameter.getUserName();
        String password = parameter.getPassword();

        String json = "";
        String orginalMediaName = mediaName;
        try{
            MediaMeta target = changeNameToAlias( MetaMapping.transformToES(srcMediaMeta) );
            String columns = buildColumnParm( target.getColumn() );
            String writer = loadJobConfig(FlinkerJobConfigConstant.ES_WRITER);
            if(StringUtils.isNotBlank(columns)){
                //json = writer.replaceAll(FlinkerJobConfigConstant.COLUMN, columns);
                json = replaceColumns(writer,columns);
            } else {
                json = writer.replaceAll(FlinkerJobConfigConstant.COLUMN, "");
            }

            if(StringUtils.isNotBlank(hosts)){
                json = json.replaceAll(FlinkerJobConfigConstant.HOSTS, hosts);
            }
            if(StringUtils.isNotBlank(password)){
                json = json.replaceAll(FlinkerJobConfigConstant.PASSWORD, password);
            }
            if(StringUtils.isNotBlank(userName)){
                json = json.replaceAll(FlinkerJobConfigConstant.USERNAME, userName);
            }
            String type = "";
            if(StringUtils.isNotBlank(mediaName)){
                try {
                    mediaName = parseMediaName(mediaName);
                    type = parseType(mediaName);
                    //如果源端是 关系型数据库，则 ES的 index为namespace，type为表名
                    if(srcInfo.getParameterObj().getMediaSourceType()==MediaSourceType.MYSQL || srcInfo.getParameterObj().getMediaSourceType()==MediaSourceType.SQLSERVER) {
                        json = json.replaceAll(FlinkerJobConfigConstant.ES_INDEX, mediaName);
                        json = json.replaceAll(FlinkerJobConfigConstant.ES_TYPE, type);
                        json = json.replaceAll(FlinkerJobConfigConstant.TABLE, mediaName);
                    }
                    else {
                        json = json.replaceAll(FlinkerJobConfigConstant.TABLE, mediaName);
                        //String[] names = mediaName.split("\\.");
                        json = json.replaceAll(FlinkerJobConfigConstant.ES_INDEX, mediaName);
                        json = json.replaceAll(FlinkerJobConfigConstant.ES_TYPE, type);
                    }
                } catch(Exception e) {
                    json = json.replaceAll(FlinkerJobConfigConstant.ES_INDEX, mediaName);
                    json = json.replaceAll(FlinkerJobConfigConstant.ES_TYPE, type);
                }
            }
            json = json.replaceAll("\""+ FlinkerJobConfigConstant.HTTP_PORT+"\"", String.valueOf(httpPort));
            json = json.replaceAll("\""+ FlinkerJobConfigConstant.TCP_PORT+"\"", String.valueOf(tcpPort));
            json = processIsPrefixTable(json,srcInfo,info,orginalMediaName);

            //es routing支持
            json = setEsRoutingInfo(json,info,destExtendJson,mediaName);

            json = processWriterExtendJson(json,destExtendJson,srcMediaMeta,info,mediaName);

        }catch (Exception e){
            LOGGER.error("es createWriterJson error ",e);
            //抛异常，让前端感知到
            throw e;
        }
        return json;
    }

    private String parseType(String mediaName) {
        if(StringUtils.isNotBlank(mediaName)) {
            String[] names = mediaName.split("\\.");
            if(names.length == 2) {
                return names[1];
            }
        }
        return mediaName;
    }

    private String processIsPrefixTable(String json, MediaSourceInfo srcInfo, MediaSourceInfo destInfo, String mediName) {
        //如果用户没设置prefix，查询es的索引获取其索引结构，然后看看第一个字段是否是 [表名]|[字段名]
        //如果是这样的结果就将 is_table_prefix设置为true，否则设置为false
        DLConfig connConf = DLConfig.parseFrom(json);
        try {
            if(mediName.contains("\\.")) {
                if(srcInfo.getType()==MediaSourceType.HDFS) {
                    mediName = mediName.split(".")[1];
                } else if(srcInfo.getType()==MediaSourceType.MYSQL || srcInfo.getType()==MediaSourceType.SQLSERVER || srcInfo.getType()==MediaSourceType.HBASE){
                    mediName = mediName.split(".")[0];
                }
            }
            List<ColumnMeta> columnMetas = ElasticSearchUtil.getColumns(destInfo,mediName);
            if(columnMetas!=null && columnMetas.size()>0) {
                String esName = columnMetas.get(0).getName();
                if(esName.indexOf("|")>0) {
                    connConf.set("job.content[0].writer.parameter.isAddTablePrefix", "true");
                }else {
                    connConf.set("job.content[0].writer.parameter.isAddTablePrefix", "false");
                }
            }
        }catch(Exception e) {
            //es index not exist
            //ignore except
        }
        return connConf.toJSON();
    }

    /**
     * 获取es routing信息
     *
     */
    private String setEsRoutingInfo(String json, MediaSourceInfo destInfo, Map<String, String> destExtendJson,String orginalMediaName) {
        DLConfig connConf = DLConfig.parseFrom(json);

        Map<String,String> resultMap = new HashMap<String,String>();
        MediaSourceInfo mediaSourceInfo = destInfo;

        //取es ip
        EsMediaSrcParameter parameter = mediaSourceInfo.getParameterObj();
        String ipList = parameter.getClusterHosts();
        String ip = ipList.split(",")[0];

        //es index
        String extendJson = JSONObject.toJSONString(destExtendJson);
        ElasticSearchJobExtendProperty jobExtend = JSONObject.parseObject(extendJson,ElasticSearchJobExtendProperty.class);
        String esWriterIndexType = jobExtend.getEsWriterIndexType();

        String index;
        if(StringUtils.isNotBlank(esWriterIndexType)){
            index = esWriterIndexType.split("\\.")[0];
        }
        //索引为空，给默认值源端表名
        else{
            index = orginalMediaName;
        }

        //获取es routing信息，开源版本暂不实现
        //resultMap = elasticSearchService.getEsRoutingInfo(ip,index);

        //设置esRouting
        if(StringUtils.isNotBlank(resultMap.get("esRouting"))){
            connConf.set("job.content[0].writer.parameter.esRouting", resultMap.get("esRouting"));
            connConf.set("job.content[0].writer.parameter.esRoutingIgnore", String.valueOf(resultMap.get("esRoutingIgnore")));
        }else{
            connConf.set("job.content[0].writer.parameter.esRouting", "");
            connConf.set("job.content[0].writer.parameter.esRoutingIgnore", "");
        }

        return connConf.toJSON();
    }

    private String processWriterExtendJson(String json, Map<String, String> destExtendJson, MediaMeta srcMediaMeta,
    MediaSourceInfo srcInfo,String mediName) {
        if(destExtendJson==null || destExtendJson.size()==0) {
            return json;
        }
        String extendJson = JSONObject.toJSONString(destExtendJson);
        ElasticSearchJobExtendProperty jobExtend = JSONObject.parseObject(extendJson,ElasticSearchJobExtendProperty.class);
        DLConfig connConf = DLConfig.parseFrom(json);
        if(StringUtils.isNotBlank( jobExtend.getJoinColumn() )) {
            List<String> list = (List<String>)connConf.get("job.content[0].writer.parameter.column");
            if(list!=null && list.size()>0) {
                int index = -1;
                for(int i=0;i<list.size();i++) {
                    if( list.get(i).equals(jobExtend.getJoinColumn()) ) {
                        index = i;
                        break;
                    }
                }
                if( index > -1) {
                    String tmp = list.get(index);
                    list.remove(index);
                    List<String> arr = new ArrayList<>();
                    arr.add(tmp);
                    arr.addAll(list);
                    connConf.set("job.content[0].writer.parameter.column", arr);
                }
            }

        }

        if(StringUtils.isNotBlank( jobExtend.getEsWriterIndexType()) ) {
            String indexType = jobExtend.getEsWriterIndexType();
            String[] arr = indexType.split("\\.");
            if(arr!=null && arr.length==2) {
                connConf.set("job.content[0].writer.parameter.esIndex", arr[0]);
                connConf.set("job.content[0].writer.parameter.esType", arr[1]);
            }
            else if(arr!=null && arr.length==1) {
                connConf.set("job.content[0].writer.parameter.esIndex", arr[0]);
                connConf.set("job.content[0].writer.parameter.esType", arr[0]);
            }
        }

        if(StringUtils.isNotBlank(jobExtend.getEsIsTablePrefix())) {
            boolean isPrefix = Boolean.parseBoolean(jobExtend.getEsIsTablePrefix());
            String isPrefixStr = Boolean.toString(isPrefix);
            connConf.set("job.content[0].writer.parameter.isAddTablePrefix", isPrefixStr);
        }else {

        }

        if(StringUtils.isNotBlank(jobExtend.getEsWriterPreDel())) {
            boolean isPreDel = Boolean.parseBoolean(jobExtend.getEsWriterPreDel());
            String isPreDelStr = Boolean.toString(isPreDel);
            connConf.set("job.content[0].writer.parameter.isPreDel", isPreDelStr);
        }
        return connConf.toJSON();
    }


    private String processReaderExtendJson(String json, Map<String, String> srcExtendJson) {
        if(srcExtendJson==null || srcExtendJson.size()==0) {
            return json;
        }
        String extendJson = JSONObject.toJSONString(srcExtendJson);
        ElasticSearchJobExtendProperty jobExtend = JSONObject.parseObject(extendJson,ElasticSearchJobExtendProperty.class);
        DLConfig connConf = DLConfig.parseFrom(json);
        if(StringUtils.isNotBlank( jobExtend.getEsReaderIndexType()) ) {
            String indexType = jobExtend.getEsReaderIndexType();
            String[] arr = indexType.split("\\.");
            if(arr!=null && arr.length==2) {
                connConf.set("job.content[0].reader.parameter.esIndex", arr[0]);
                connConf.set("job.content[0].reader.parameter.esType", arr[1]);
            }
            else if(arr!=null && arr.length==1) {
                connConf.set("job.content[0].reader.parameter.esIndex", arr[0]);
                connConf.set("job.content[0].reader.parameter.esType", arr[0]);
            }
        }

        if(StringUtils.isNotBlank( jobExtend.getEsReaderQuery()) ) {
            String query = jobExtend.getEsReaderQuery();
            Object jsonQuery = JSONObject.parse(query);
            connConf.set("job.content[0].reader.parameter.esQuery",jsonQuery);
        }
        return json = connConf.toJSON();
    }





    private void checkType(MediaSrcParameter parameter) {
        if( !(parameter instanceof EsMediaSrcParameter) ) {
            throw new RuntimeException("media source type error "+parameter);
        }
    }


    @Override
    public String reloadReader(String json,MediaSourceInfo info) {
        checkType( info.getParameterObj() );
        EsMediaSrcParameter parameter = (EsMediaSrcParameter)info.getParameterObj();
        String hosts = parameter.getClusterHosts();
        String tcpPort = parameter.getTcpPort()+"";
        String httpPort = parameter.getHttpPort()+"";
        String userName = parameter.getUserName();
        String password = parameter.getPassword();
        if(StringUtils.isNotBlank(hosts) && StringUtils.isNotBlank(tcpPort) && StringUtils.isNotBlank(httpPort) &&
                StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.remove("job.content[0].reader.parameter.hosts");
            connConf.remove("job.content[0].reader.parameter.httpPort");
            connConf.remove("job.content[0].reader.parameter.tcpPort");
            connConf.remove("job.content[0].reader.parameter.username");
            connConf.remove("job.content[0].reader.parameter.password");
            connConf.set("job.content[0].reader.parameter.hosts",hosts);
            connConf.set("job.content[0].reader.parameter.httpPort",httpPort);
            connConf.set("job.content[0].reader.parameter.tcpPort",tcpPort);
            connConf.set("job.content[0].reader.parameter.username",userName);
            connConf.set("job.content[0].reader.parameter.password",password);
            json = connConf.toJSON();
        }
        return json;
    }

    @Override
    public String reloadWriter(String json, MediaSourceInfo info) {
        checkType( info.getParameterObj() );
        EsMediaSrcParameter parameter = (EsMediaSrcParameter)info.getParameterObj();
        String hosts = parameter.getClusterHosts();
        String tcpPort = parameter.getTcpPort()+"";
        String httpPort = parameter.getHttpPort()+"";
        String userName = parameter.getUserName();
        String password = parameter.getPassword();
        if(StringUtils.isNotBlank(hosts) && StringUtils.isNotBlank(tcpPort) && StringUtils.isNotBlank(httpPort) &&
                StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.remove("job.content[0].writer.parameter.hosts");
            connConf.remove("job.content[0].writer.parameter.httpPort");
            connConf.remove("job.content[0].writer.parameter.tcpPort");
            connConf.remove("job.content[0].writer.parameter.username");
            connConf.remove("job.content[0].writer.parameter.password");
            connConf.set("job.content[0].writer.parameter.hosts",hosts);
            connConf.set("job.content[0].writer.parameter.httpPort",httpPort);
            connConf.set("job.content[0].writer.parameter.tcpPort",tcpPort);
            connConf.set("job.content[0].writer.parameter.username",userName);
            connConf.set("job.content[0].writer.parameter.password",password);
            json = connConf.toJSON();
        }
        return json;
    }

    @Override
    public String writerMopUp(String json, MediaSourceInfo info, JobExtendProperty property) {
        if(property==null) {
            return json;
        }
        Map<String,String> destExtendJson = property.getWriter();
        String extendJson = JSONObject.toJSONString(destExtendJson);
        ElasticSearchJobExtendProperty jobExtend = JSONObject.parseObject(extendJson,ElasticSearchJobExtendProperty.class);
        if(StringUtils.isNotBlank( jobExtend.getJoinColumn() )) {
            DLConfig connConf = DLConfig.parseFrom(json);
            List<String> list = (List<String>)connConf.get("job.content[0].reader.parameter.column");
            if(list!=null && list.size()>0) {
                int index = -1;
                for(int i=0;i<list.size();i++) {
                    if( list.get(i).equals(jobExtend.getJoinColumn()) ) {
                        index = i;
                        break;
                    }
                }
                if( index > -1) {
                    String tmp = list.get(index);
                    list.remove(index);
                    List<String> arr = new ArrayList<>();
                    arr.add(tmp);
                    arr.addAll(list);
                    connConf.set("job.content[0].reader.parameter.column", arr);
                }
            }
            json = connConf.toJSON();
        }
        return json;
    }

    @Override
    public List<HostNodeInfo> parseSrcContent(String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        //String host = (String)connConf.get("job.content[0].reader.parameter.hosts");
        //String port = Integer.toString((Integer)connConf.get("job.content[0].reader.parameter.httpPort"));
        //String other = Integer.toString((Integer)connConf.get("job.content[0].reader.parameter.tcpPort"));
        String host = parseHostInfo(json, "job.content[0].reader.parameter.hosts");
        String port = parseHostInfo(json, "job.content[0].reader.parameter.httpPort");
        String other = parseHostInfo(json, "job.content[0].reader.parameter.tcpPort");
        HostNodeInfo info = new HostNodeInfo();
        info.setHost(host);
        info.setPort(port);
        info.setOther(other);
        List<HostNodeInfo> list = new ArrayList<>();
        list.add(info);
        return list;
    }

    @Override
    public List<HostNodeInfo> parseDestContent(String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        //String host = (String)connConf.get();
        //String port = Integer.toString((Integer)connConf.get());
        //String other = Integer.toString((Integer)connConf.get());
        String host = parseHostInfo(json, "job.content[0].writer.parameter.hosts");
        String port = parseHostInfo(json, "job.content[0].writer.parameter.httpPort");
        String other = parseHostInfo(json, "job.content[0].writer.parameter.tcpPort");
        HostNodeInfo info = new HostNodeInfo();
        info.setHost(host);
        info.setPort(port);
        info.setOther(other);
        List<HostNodeInfo> list = new ArrayList<>();
        list.add(info);
        return list;
    }

    @Override
    public List<HostNodeInfo> parseSrcMediaSourceToHostNode(MediaSourceInfo info) {
        return parseMediaSourceInfo(info);
    }

    @Override
    public List<HostNodeInfo> parseDestMediaSourceToHostNode(MediaSourceInfo info) {
        return parseMediaSourceInfo(info);
    }


    @Override
    public boolean compareSrcHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        return compareHost(fromJobConfigHosts,fromMediaSourceHosts);
    }


    @Override
    public boolean compareDestHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        return compareHost(fromJobConfigHosts,fromMediaSourceHosts);
    }

    public List<HostNodeInfo> parseMediaSourceInfo(MediaSourceInfo info) {
        checkType( info.getParameterObj() );
        EsMediaSrcParameter parameter = (EsMediaSrcParameter)info.getParameterObj();
        String hosts = parameter.getClusterHosts();
        String tcpPort = parameter.getTcpPort()+"";
        String httpPort = parameter.getHttpPort()+"";
        String userName = parameter.getUserName();
        String password = parameter.getPassword();
        HostNodeInfo host = new HostNodeInfo();
        host.setHost(hosts);
        host.setPort(httpPort);
        host.setOther(tcpPort);
        host.setUserName(userName);
        host.setPassword(password);
        List<HostNodeInfo> list = new ArrayList<>();
        list.add(host);
        return list;
    }

    private boolean compareHost(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
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
