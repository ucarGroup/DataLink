package com.ucar.datalink.biz.utils.flinker.job;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobConfigConstant;
import com.ucar.datalink.biz.utils.flinker.module.HDFSJobExtendProperty;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
import com.ucar.datalink.biz.utils.flinker.module.TimingJobExtendPorperty;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.HostNodeInfo;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.hdfs.HDFSMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by user on 2017/7/19.
 */
public class HdfsJobConfigServiceImpl extends AbstractJobConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HdfsJobConfigServiceImpl.class);

    private static final String HIVE_CREATE_DB_SUFFIX = ".db";

    private static final String DFS_NAMESERVICES_UNDERLINE = "dfs_nameservices";
    private static final String DFS_NAMESERVICES_DOT = "dfs.nameservices";

    private static final String DFS_N1_N2_READER_UNDERLINE = "dfs_ha_namenodes_{0}";
    private static final String DFS_N1_N2_READER_DOT = "dfs.ha.namenodes.{0}";

    private static final String DFS_N1_READER_UNDERLINE = "dfs_namenode_rpc-address_{0}_n1";
    private static final String DFS_N1_READER_DOT = "dfs.namenode.rpc-address.{0}.n1";

    private static final String DFS_N2_READER_UNDERLINE = "dfs_namenode_rpc-address_{0}_n2";
    private static final String DFS_N2_READER_DOT = "dfs.namenode.rpc-address.{0}.n2";

    private static final String DFS_PROVIDER_READER_UNDERLINE = "dfs_client_failover_proxy_provider_{0}";
    private static final String DFS_PROVIDER_READER_DOT = "dfs.client.failover.proxy.provider.{0}";

    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    private static final String FULL = JobConfigInfo.TIMING_TRANSFER_TYPE_FULL;

    private static final String INCREMENT = JobConfigInfo.TIMING_TRANSFER_TYPE_INCREMENT;

    @Value("${biz.flinker.hdfs.read.prefix.path}")
    private String hdfsReadPrefix;

    @Override
    public String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, String mediaName) {
        checkType(info.getParameterObj());
        HDFSMediaSrcParameter parameter = (HDFSMediaSrcParameter)info.getParameterObj();
        String url = parameter.getNameServices();
        Map<String,String> srcExtendJson = property.getReader();
        TimingJobExtendPorperty timing = property.getTiming();
        String json = "";
        try {
            if(metas.size() > 0) {
                ColumnMeta tmp = metas.get(0);
                ColumnMeta cm = new ColumnMeta();
                cm.setName(tmp.getName());
                cm.setType(tmp.getType());
                cm.setIsPrimaryKey(true);
                metas.set(0, cm);
            }
            LinkedList<ColumnMeta> list = new LinkedList<>(metas);
            metas.clear();
            metas.addAll(list);

            //生成columns时候用的是 toDataX的那个函数
            String columns = buildColumnByRead(metas);
            String path = "";
            String[] names = mediaName.split("\\.");
            if(names==null || names.length<2) {
                throw new RuntimeException("elastic search table error");
            }
            if(names[0].equals("default")) {
                if( isContainSpecifiedPreDate(srcExtendJson) ) {
                    path = hdfsReadPrefix +  names[1] +"/dt="+  FlinkerJobConfigConstant.DATAX_SPECIFIED_PRE_DATE_ESCAPE +"/*";
                } else {
                    if(timing.isOpen() && INCREMENT.equals(timing.getType())) {
                        path = hdfsReadPrefix +  names[1] +"/dt="+  FlinkerJobConfigConstant.DATAX_PRE_DATE_ESCAPE +"/*";
                    } else {
                        path = hdfsReadPrefix +  names[1] +"/*";
                    }
                }
            } else {
                if( isContainSpecifiedPreDate(srcExtendJson) ) {
                    path = hdfsReadPrefix +  names[0] + HIVE_CREATE_DB_SUFFIX +"/" +names[1] +"/dt="+  FlinkerJobConfigConstant.DATAX_SPECIFIED_PRE_DATE_ESCAPE +"/*";
                } else {
                    if(timing.isOpen() && INCREMENT.equals(timing.getType())) {
                        path = hdfsReadPrefix + names[0] + HIVE_CREATE_DB_SUFFIX +"/"+ names[1] +"/dt="+ FlinkerJobConfigConstant.DATAX_PRE_DATE_ESCAPE +"/*";
                    } else {
                        path = hdfsReadPrefix + names[0] + HIVE_CREATE_DB_SUFFIX +"/"+ names[1] +"/*";
                    }
                }

            }

            if(srcExtendJson.get("path") == null) {
                srcExtendJson.put("path", path);
            }
            String reader = loadJobConfig(FlinkerJobConfigConstant.HDFS_READER);
            json = reader.replaceAll(FlinkerJobConfigConstant.TABLE, mediaName);
            json = json.replaceAll(FlinkerJobConfigConstant.PATH, path);
            if (StringUtils.isNotBlank(url)) {
                json = json.replaceAll(FlinkerJobConfigConstant.HDFSURL, url);
            }
            if (StringUtils.isNotBlank(columns)) {
                json = replaceColumns(json,columns);
            }
            json = processExtendReaderJson(parameter,json,srcExtendJson);
            json = processReaderFileNmae(json,names);
        } catch (Exception e) {
            LOGGER.error("hdfs createReaderJson error ", e);
        }
        return json;
    }


    @Override
    public String createWriterJson(MediaSourceInfo srcInfo, MediaSourceInfo info, MediaMeta srcMediaMeta, JobExtendProperty property, String mediaName) {
        checkType(info.getParameterObj());
        HDFSMediaSrcParameter parameter = (HDFSMediaSrcParameter) info.getParameterObj();
        String url = parameter.getNameServices();
        Map<String, String> destExtendJson = property.getWriter();
        String extendJson = JSONObject.toJSONString(destExtendJson);
        HDFSJobExtendProperty jobExtend = JSONObject.parseObject(extendJson, HDFSJobExtendProperty.class);
        boolean isTimeMode = Boolean.parseBoolean(jobExtend.getHdfsPathType());
        TimingJobExtendPorperty timing = property.getTiming();

        String json = "";
        try {
            MediaMeta target = changeNameToAlias(MetaMapping.transformToHDFS(srcMediaMeta));
            String columns = buildColumnByWrite(target.getColumn());
            String path = "";
            mediaName = parseMediaName(mediaName);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            String formatDateString = sdf.format(new Date());
            if (MediaSourceType.HBASE == srcInfo.getParameterObj().getMediaSourceType()) {
                columns = buildColumnFamily(target.getColumn());
                if (timing.isOpen()) {
                    path = "/user/hbase/" + mediaName + "/" + FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE;
                } else {
                    path = "/user/hbasehistory/" + mediaName + "/" + formatDateString;
                }
            } else if (MediaSourceType.MYSQL == srcInfo.getParameterObj().getMediaSourceType()) {
                RdbMediaSrcParameter rdbParameter = (RdbMediaSrcParameter) srcInfo.getParameterObj();
                String schema = srcInfo.getParameterObj().getNamespace();
                mediaName = filterMultiTables(mediaName);
                if (timing.isOpen()) {
                    path = "/user/mysql/" + schema + "/" + mediaName + "/" + FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE;
                } else {
                    path = "/user/mysqlhistory/" + schema + "/" + mediaName + "/" + formatDateString;
                }
                columns = buildColumnByWrite(target.getColumn());
            } else if (MediaSourceType.SQLSERVER == srcInfo.getParameterObj().getMediaSourceType()) {
                RdbMediaSrcParameter rdbParameter = (RdbMediaSrcParameter) srcInfo.getParameterObj();
                String schema = srcInfo.getParameterObj().getNamespace();
                if (timing.isOpen()) {
                    if (isTimeMode) {
                        path = "/user/sqlserver/" + schema + "/" + mediaName + "/" + FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE + "-" + FlinkerJobConfigConstant.DATAX_CURRENT_TIME_ESCAPE;
                        ;
                    } else {
                        path = "/user/sqlserver/" + schema + "/" + mediaName + "/" + FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE;
                    }
                } else {
                    path = "/user/sqlserverhistory/" + schema + "/" + mediaName + "/" + formatDateString;
                }
                columns = buildColumnByWrite(target.getColumn());
            } else if (MediaSourceType.HANA == srcInfo.getParameterObj().getMediaSourceType()) {
                RdbMediaSrcParameter rdbParameter = (RdbMediaSrcParameter) srcInfo.getParameterObj();
                String schema = srcInfo.getParameterObj().getNamespace();
                if (timing.isOpen()) {
                    if (isTimeMode) {
                        path = "/user/hana/" + schema + "/" + mediaName + "/" + FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE + "-" + FlinkerJobConfigConstant.DATAX_CURRENT_TIME_ESCAPE;
                        ;
                    } else {
                        path = "/user/hana/" + schema + "/" + mediaName + "/" + FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE;
                    }
                } else {
                    path = "/user/hanahistory/" + schema + "/" + mediaName + "/" + formatDateString;
                }
                columns = buildColumnByWrite(target.getColumn());
            } else if (MediaSourceType.ORACLE == srcInfo.getParameterObj().getMediaSourceType()) {
                RdbMediaSrcParameter rdbParameter = (RdbMediaSrcParameter) srcInfo.getParameterObj();
                String schema = srcInfo.getParameterObj().getNamespace();
                if (timing.isOpen()) {
                    if (isTimeMode) {
                        path = "/user/oracle/" + schema + "/" + mediaName + "/" + FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE + "-" + FlinkerJobConfigConstant.DATAX_CURRENT_TIME_ESCAPE;
                        ;
                    } else {
                        path = "/user/oracle/" + schema + "/" + mediaName + "/" + FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE;
                    }
                } else {
                    path = "/user/oraclehistory/" + schema + "/" + mediaName + "/" + formatDateString;
                }
                columns = buildColumnByWrite(target.getColumn());
            } else {

            }

            String writer = loadJobConfig(FlinkerJobConfigConstant.HDFS_WRITER);
            json = writer.replaceAll(FlinkerJobConfigConstant.TABLE, mediaName);
            json = json.replaceAll(FlinkerJobConfigConstant.PATH, path);
            if (StringUtils.isNotBlank(url)) {
                json = json.replaceAll(FlinkerJobConfigConstant.HDFSURL, url);
            }
            if (StringUtils.isNotBlank(columns)) {
                json = replaceColumns(json, columns);
            }
            json = processExtendWriterJson(parameter, json, destExtendJson);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("hdfs createWriterJson error ", e);
        }
        return json;
    }

    private boolean isContainSpecifiedPreDate(Map<String,String> srcExtendJson ) {
        String extendJson = JSONObject.toJSONString(srcExtendJson);
        HDFSJobExtendProperty jobExtend = JSONObject.parseObject(extendJson, HDFSJobExtendProperty.class);
        if(StringUtils.isNotBlank(jobExtend.getSpecifiedPreDate())) {
            return true;
        }
        return false;
    }


    private String processReaderFileNmae(String json,String[] names) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            if(names==null || names.length!=2) {
                return json;
            }
            connConf.set("job.content[0].reader.parameter.fileName", names[1]);
            return connConf.toJSON();
        }catch(Exception e) {
            return json;
        }
    }

    private String filterMultiTables(String table_name) {
        if(StringUtils.isBlank(table_name)) {
            return table_name;
        }
        if(JobContentParseUtil.isMutilTables(table_name)) {
            String tmp_name = JobContentParseUtil.getMutilTableFirt(table_name);
            table_name = tmp_name.substring(0,tmp_name.length()-5);

        }
        return table_name;
    }

    private String processExtendReaderJson(HDFSMediaSrcParameter parameter, String json, Map<String, String> srcExtendJson) {
        String extendJson = JSONObject.toJSONString(srcExtendJson);
        HDFSJobExtendProperty jobExtend = JSONObject.parseObject(extendJson, HDFSJobExtendProperty.class);
        DLConfig connConf = DLConfig.parseFrom(json);
        if( StringUtils.isNotBlank(jobExtend.getPath()) ) {
            connConf.set("job.content[0].reader.parameter.path", jobExtend.getPath());
        }
        if( StringUtils.isNotBlank(jobExtend.getCompress()) ) {
            connConf.set("job.content[0].reader.parameter.compress", jobExtend.getCompress());
        }
        if( StringUtils.isNotBlank(jobExtend.getIgnoreException()) ) {
            connConf.set("job.content[0].reader.parameter.ignoreException", jobExtend.getIgnoreException());
        }
        if( StringUtils.isNotBlank(jobExtend.getHsdfUser()) ) {
            connConf.set("job.content[0].reader.parameter.hadoopUserName", jobExtend.getHsdfUser());
        }
            if( StringUtils.isNotBlank(jobExtend.getSpecifiedPreDate()) ){
                connConf.set("job.content[0].reader.parameter.specifiedPreDate", jobExtend.getSpecifiedPreDate());
            }
            if( StringUtils.isNotBlank(jobExtend.getHdfsPaths()) ) {
                String paths = jobExtend.getHdfsPaths();
                String[] pathArr = paths.split("\n");
            List<String> pathList = new ArrayList<>();
            for(String s : pathArr) {
                if(StringUtils.isNotBlank(s)) {
                    s = s.trim();
                    pathList.add(s);
                }
            }
            try {
                connConf.remove("job.content[0].reader.parameter.path");
            }catch(Exception e){}
            connConf.set("job.content[0].reader.parameter.path", pathList);
        }

        //设置高可用
        String name_service = parameter.getNameServices();
        if(name_service.startsWith("hdfs://")) {
            name_service = name_service.substring(7,name_service.length());
        }
        String dfs_name_serivce = MessageFormat.format(DFS_NAMESERVICES_UNDERLINE,name_service);
        String n1_n2 = MessageFormat.format(DFS_N1_N2_READER_UNDERLINE, name_service);
        String n1 = MessageFormat.format(DFS_N1_READER_UNDERLINE, name_service);
        String n2 = MessageFormat.format(DFS_N2_READER_UNDERLINE, name_service);
        String provider = MessageFormat.format(DFS_PROVIDER_READER_UNDERLINE, name_service);
        connConf.set("job.content[0].reader.parameter.hadoopConfig."+ dfs_name_serivce, name_service);
        connConf.set("job.content[0].reader.parameter.hadoopConfig."+n1_n2, "n1,n2");
        connConf.set("job.content[0].reader.parameter.hadoopConfig."+n1, parameter.getNameNode1());
        connConf.set("job.content[0].reader.parameter.hadoopConfig."+n2, parameter.getNameNode2());
        connConf.set("job.content[0].reader.parameter.hadoopConfig."+provider, "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        return connConf.toJSON();
    }


    private String processExtendWriterJson(HDFSMediaSrcParameter parameter, String json, Map<String, String> destExtendJson) {
        String extendJson = JSONObject.toJSONString(destExtendJson);
        HDFSJobExtendProperty jobExtend = JSONObject.parseObject(extendJson, HDFSJobExtendProperty.class);
        DLConfig connConf = DLConfig.parseFrom(json);
        //设置高可用
        String name_service = parameter.getNameServices();
        if(name_service.startsWith("hdfs://")) {
            name_service = name_service.substring(7,name_service.length());
        }
        String dfs_name_serivce = MessageFormat.format(DFS_NAMESERVICES_UNDERLINE,name_service);
        String n1_n2 = MessageFormat.format(DFS_N1_N2_READER_UNDERLINE, name_service);
        String n1 = MessageFormat.format(DFS_N1_READER_UNDERLINE, name_service);
        String n2 = MessageFormat.format(DFS_N2_READER_UNDERLINE, name_service);
        String provider = MessageFormat.format(DFS_PROVIDER_READER_UNDERLINE, name_service);
        connConf.set("job.content[0].writer.parameter.hadoopConfig."+ dfs_name_serivce, name_service);
        connConf.set("job.content[0].writer.parameter.hadoopConfig."+n1_n2, "n1,n2");
        connConf.set("job.content[0].writer.parameter.hadoopConfig."+n1, parameter.getNameNode1());
        connConf.set("job.content[0].writer.parameter.hadoopConfig."+n2, parameter.getNameNode2());
        connConf.set("job.content[0].writer.parameter.hadoopConfig."+provider, "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");

        if(StringUtils.isNotBlank(jobExtend.getHdfsPreDel())) {
            boolean isPreDel = Boolean.parseBoolean(jobExtend.getHdfsPreDel());
            String isPreDelStr = Boolean.toString(isPreDel);
            connConf.set("job.content[0].writer.parameter.preDel",isPreDelStr);
        }

        return connConf.toJSON();
    }


    private void buildHdfsPath(MediaSourceInfo srcInfo, MediaMeta target, TimingJobExtendPorperty timing, String mediaName) {
        String columns = "";
        String path = "";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String formatDateString = sdf.format(new Date());
        if (MediaSourceType.HBASE == srcInfo.getParameterObj().getMediaSourceType()) {
            columns = buildColumnFamily( target.getColumn() );
            if(timing.isOpen()) {
                path = "/user/hbase/" + mediaName +"/"+ FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE;
            } else {
                path = "/user/hbasehistory/" + mediaName +"/"+ formatDateString;
            }
        } else if (MediaSourceType.MYSQL == srcInfo.getParameterObj().getMediaSourceType()) {
            RdbMediaSrcParameter  rdbParameter = (RdbMediaSrcParameter)srcInfo.getParameterObj();
            String schema = srcInfo.getParameterObj().getNamespace();
            if(timing.isOpen()) {
                path = "/user/mysql/" + schema + "/" + mediaName +"/"+ FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE;
            } else {
                path = "/user/mysqlhistory/" + schema + "/" + mediaName +"/"+ formatDateString;
            }
            columns = buildColumnByWrite(target.getColumn());
        } else if (MediaSourceType.SQLSERVER == srcInfo.getParameterObj().getMediaSourceType()) {
            RdbMediaSrcParameter  rdbParameter = (RdbMediaSrcParameter)srcInfo.getParameterObj();
            String schema = srcInfo.getParameterObj().getNamespace();
            if(timing.isOpen()) {
                path = "/user/sqlserver/" + schema + "/" + mediaName +"/"+ FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE;
            } else {
                path = "/user/sqlserverhistory/" + schema + "/" + mediaName +"/"+ formatDateString;
            }
            columns = buildColumnByWrite(target.getColumn());
        } else if( MediaSourceType.ORACLE == srcInfo.getParameterObj().getMediaSourceType()) {
            RdbMediaSrcParameter  rdbParameter = (RdbMediaSrcParameter)srcInfo.getParameterObj();
            String schema = srcInfo.getParameterObj().getNamespace();
            if(timing.isOpen()) {
                if(timing.isOpen()) {
                    path = "/user/oracle/" + schema + "/" + mediaName +"/"+ FlinkerJobConfigConstant.DATAX_CURRENT_DATE_ESCAPE;
                } else {
                    path = "/user/oraclehistory/" + schema + "/" + mediaName +"/"+ formatDateString;
                }
            }
        }
    }


    private void checkType(MediaSrcParameter parameter) {
        if( !(parameter instanceof HDFSMediaSrcParameter) ) {
            throw new RuntimeException("media source type error "+parameter);
        }
    }

    private void checkRDBMSType(MediaSrcParameter parameter) {
        if( !(parameter instanceof RdbMediaSrcParameter)) {
            throw new RuntimeException("media source type error "+parameter);
        }
    }


    private String buildColumnByRead(List<ColumnMeta> list) {
        StringBuffer buf = new StringBuffer();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                String type = list.get(i).getType();
                if (i == list.size() - 1) {
                    buf.append("{").append("\"").append("index").append("\"").append(":").append("\"").append(i).append("\"").append(",");
                    buf.append("\"").append("type").append("\"").append(":").append("\"").append(type).append("\"").append("}");
                } else {
                    buf.append("{").append("\"").append("index").append("\"").append(":").append("\"").append(i).append("\"").append(",");
                    buf.append("\"").append("type").append("\"").append(":").append("\"").append(type).append("\"").append("}").append(",");
                }
            }
        }
        return buf.toString();
    }

    private String buildColumnByWrite(List<ColumnMeta> list) {
        StringBuffer buf = new StringBuffer();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                String type = list.get(i).getType();
                if (i == list.size() - 1) {
                    buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append(list.get(i).getName()).append("\"").append(",");
                    buf.append("\"").append("type").append("\"").append(":").append("\"").append(type).append("\"").append("}");
                } else {
                    buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append(list.get(i).getName()).append("\"").append(",");
                    buf.append("\"").append("type").append("\"").append(":").append("\"").append(type).append("\"").append("}").append(",");
                }
            }
        }
        return buf.toString();
    }


    private String buildColumnFamily(List<ColumnMeta> list) {
        StringBuffer buf = new StringBuffer();
        if(list!=null && list.size()==1) {
            buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append("rowkey").append("\"").append(",");
            buf.append("\"").append("type").append("\"").append(":").append("\"").append("string").append("\"").append("}");
        }
        if(list!=null && list.size()>1) {
            buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append("rowkey").append("\"").append(",");
            buf.append("\"").append("type").append("\"").append(":").append("\"").append("string").append("\"").append("}").append(",");
            for (int i = 1; i < list.size(); i++) {
                String family = list.get(i).getColumnFamily();
                String columnName = list.get(i).getName();
                if (i == list.size() - 1) {
                    buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append(StringUtils.isEmpty(family)?"":family.replaceAll("\\.","_")).append(":").append(StringUtils.isEmpty(columnName)?"":columnName.replaceAll("\\.","_")).append("\"").append(",");
                    buf.append("\"").append("type").append("\"").append(":").append("\"").append("string").append("\"").append("}");
                } else {
                    buf.append("{").append("\"").append("name").append("\"").append(":").append("\"").append(StringUtils.isEmpty(family)?"":family.replaceAll("\\.","_")).append(":").append(StringUtils.isEmpty(columnName)?"":columnName.replaceAll("\\.","_")).append("\"").append(",");
                    buf.append("\"").append("type").append("\"").append(":").append("\"").append("string").append("\"").append("}").append(",");
                }
            }
        }
        return buf.toString();
    }

    private String parseAndGetJsonNode(DLConfig config, String node) {
        String str = null;
        try {
            Object obj = config.get(node);
            str = JSONObject.toJSONString(obj);
        } catch(Exception e) {
            //ignore
        }
        return str;
    }

    @Override
    public String readerMopUp(String json, MediaSourceInfo info) {
        if(StringUtils.isNotBlank(json)) {
            //替换reader相关
            DLConfig connConf = DLConfig.parseFrom(json);
            String str = null;
            str = parseAndGetJsonNode(connConf, "job.content[0].reader.parameter.hadoopConfig");
            if(StringUtils.isNotBlank(str)) {
                str = str.replaceAll("_","\\.");
                Object jsonObj = JSONObject.parseObject(str);
                connConf.set("job.content[0].reader.parameter.hadoopConfig",jsonObj);
            }
            json = connConf.toJSON();
        }
        return json;
    }

    @Override
    public String writerMopUp(String json, MediaSourceInfo info, JobExtendProperty property) {
        if(StringUtils.isNotBlank(json)) {
            //替换reader相关
            DLConfig connConf = DLConfig.parseFrom(json);
            String str = null;
            str = parseAndGetJsonNode(connConf, "job.content[0].writer.parameter.hadoopConfig");
            if(StringUtils.isNotBlank(str)) {
                str = str.replaceAll("_","\\.");
                Object jsonObj = JSONObject.parseObject(str);
                connConf.set("job.content[0].writer.parameter.hadoopConfig",jsonObj);
            }
            json = connConf.toJSON();
        }
        return json;
    }


    private boolean checkExist(DLConfig config, String node) {
        try {
            Object obj = config.get(node);
            if(obj != null) {
                return true;
            }
        } catch(Exception e) {
            //ignore
        }
        return false;
    }

    private String replaceDot_2_Underline(String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        if( checkExist(connConf, "job.content[0].reader.parameter.hadoopConfig") ) {
            //替换reader相关
            String str = parseAndGetJsonNode(connConf, "job.content[0].reader.parameter.hadoopConfig");
            if(StringUtils.isNotBlank(str)) {
                str = str.replaceAll("\\.","_");
                Object jsonObj = JSONObject.parseObject(str);
                connConf.set("job.content[0].reader.parameter.hadoopConfig",jsonObj);
            }
            json = connConf.toJSON();
        }
        if( checkExist(connConf, "job.content[0].writer.parameter.hadoopConfig") ) {
            //替换reader相关
            String str = parseAndGetJsonNode(connConf, "job.content[0].writer.parameter.hadoopConfig");
            if(StringUtils.isNotBlank(str)) {
                str = str.replaceAll("\\.","_");
                Object jsonObj = JSONObject.parseObject(str);
                connConf.set("job.content[0].writer.parameter.hadoopConfig",jsonObj);
            }
            json = connConf.toJSON();
        }
        return json;
    }



    public String reloadReader(String json,MediaSourceInfo info) {
        checkType(info.getParameterObj());
        HDFSMediaSrcParameter parameter = (HDFSMediaSrcParameter)info.getParameterObj();
        String url = parameter.getNameServices();
        if (StringUtils.isNotBlank(url)) {
            json = replaceDot_2_Underline(json);
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.remove("job.content[0].reader.parameter.defaultFS");
            connConf.set("job.content[0].reader.parameter.defaultFS",url);

            //设置高可用
            String name_service = parameter.getNameServices();
            if(name_service.startsWith("hdfs://")) {
                name_service = name_service.substring(7,name_service.length());
            }
            String dfs_name_serivce = MessageFormat.format(DFS_NAMESERVICES_UNDERLINE,name_service);
            String n1_n2 = MessageFormat.format(DFS_N1_N2_READER_UNDERLINE, name_service);
            String n1 = MessageFormat.format(DFS_N1_READER_UNDERLINE, name_service);
            String n2 = MessageFormat.format(DFS_N2_READER_UNDERLINE, name_service);
            String provider = MessageFormat.format(DFS_PROVIDER_READER_UNDERLINE, name_service);
            connConf.set("job.content[0].reader.parameter.hadoopConfig."+ dfs_name_serivce, name_service);
            connConf.set("job.content[0].reader.parameter.hadoopConfig."+n1_n2, "n1,n2");
            connConf.set("job.content[0].reader.parameter.hadoopConfig."+n1, parameter.getNameNode1());
            connConf.set("job.content[0].reader.parameter.hadoopConfig."+n2, parameter.getNameNode2());
            connConf.set("job.content[0].reader.parameter.hadoopConfig."+provider, "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
            json = connConf.toJSON();
            json = readerMopUp(json, info);
        }
        return json;
    }

    public String reloadWriter(String json, MediaSourceInfo info) {
        checkType(info.getParameterObj());
        HDFSMediaSrcParameter parameter = (HDFSMediaSrcParameter)info.getParameterObj();
        String url = parameter.getNameServices();
        if (StringUtils.isNotBlank(url)) {
            json = replaceDot_2_Underline(json);
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.remove("job.content[0].writer.parameter.defaultFS");
            connConf.set("job.content[0].writer.parameter.defaultFS",url);

            //设置高可用
            String name_service = parameter.getNameServices();
            if(name_service.startsWith("hdfs://")) {
                name_service = name_service.substring(7,name_service.length());
            }
            String dfs_name_serivce = MessageFormat.format(DFS_NAMESERVICES_UNDERLINE,name_service);
            String n1_n2 = MessageFormat.format(DFS_N1_N2_READER_UNDERLINE, name_service);
            String n1 = MessageFormat.format(DFS_N1_READER_UNDERLINE, name_service);
            String n2 = MessageFormat.format(DFS_N2_READER_UNDERLINE, name_service);
            String provider = MessageFormat.format(DFS_PROVIDER_READER_UNDERLINE, name_service);
            connConf.set("job.content[0].writer.parameter.hadoopConfig."+ dfs_name_serivce, name_service);
            connConf.set("job.content[0].writer.parameter.hadoopConfig."+n1_n2, "n1,n2");
            connConf.set("job.content[0].writer.parameter.hadoopConfig."+n1, parameter.getNameNode1());
            connConf.set("job.content[0].writer.parameter.hadoopConfig."+n2, parameter.getNameNode2());
            connConf.set("job.content[0].writer.parameter.hadoopConfig."+provider, "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
            json = connConf.toJSON();
            json = writerMopUp(json, info, null);
        }
        return json;
    }

    public List<HostNodeInfo> parseSrcContent(String json) {
        json = replaceDot_2_Underline(json);
        DLConfig connConf = DLConfig.parseFrom(json);
        String nameService = (String)connConf.get("job.content[0].reader.parameter.hadoopConfig.dfs_nameservices");
        String n1 = "job.content[0].reader.parameter.hadoopConfig.dfs_namenode_rpc-address_"+nameService+"_n1";
        String n2 = "job.content[0].reader.parameter.hadoopConfig.dfs_namenode_rpc-address_"+nameService+"_n2";
        String n1Str = (String)connConf.get(n1);
        String n2Str = (String)connConf.get(n2);
        HostNodeInfo info1 = new HostNodeInfo();
        info1.setHost(n1Str.split(":")[0].replaceAll("_","\\."));
        info1.setPort(n1Str.split(":")[1]);
        HostNodeInfo info2 = new HostNodeInfo();
        info2.setHost(n2Str.split(":")[0].replaceAll("_","\\."));
        info2.setPort(n2Str.split(":")[1]);

        List<HostNodeInfo> list = new ArrayList<>();
        list.add(info1);
        list.add(info2);
        return list;
    }

    public List<HostNodeInfo> parseDestContent(String json) {
        json = replaceDot_2_Underline(json);
        DLConfig connConf = DLConfig.parseFrom(json);
        String nameService = (String)connConf.get("job.content[0].writer.parameter.hadoopConfig.dfs_nameservices");
        String n1 = "job.content[0].writer.parameter.hadoopConfig.dfs_namenode_rpc-address_"+nameService+"_n1";
        String n2 = "job.content[0].writer.parameter.hadoopConfig.dfs_namenode_rpc-address_"+nameService+"_n2";
        String n1Str = (String)connConf.get(n1);
        String n2Str = (String)connConf.get(n2);
        HostNodeInfo info1 = new HostNodeInfo();
        info1.setHost(n1Str.split(":")[0].replaceAll("_", "\\."));
        info1.setPort(n1Str.split(":")[1]);
        HostNodeInfo info2 = new HostNodeInfo();
        info2.setHost(n2Str.split(":")[0].replaceAll("_","\\."));
        info2.setPort(n2Str.split(":")[1]);

        List<HostNodeInfo> list = new ArrayList<>();
        list.add(info1);
        list.add(info2);
        return list;
    }

    public List<HostNodeInfo> parseSrcMediaSourceToHostNode(MediaSourceInfo info) {
        return parseMediaSourceToHostNode(info);
    }

    public List<HostNodeInfo> parseDestMediaSourceToHostNode(MediaSourceInfo info) {
        return parseMediaSourceToHostNode(info);
    }

    private List<HostNodeInfo> parseMediaSourceToHostNode(MediaSourceInfo info) {
        checkType(info.getParameterObj());
        HDFSMediaSrcParameter parameter = (HDFSMediaSrcParameter)info.getParameterObj();
        String url = parameter.getNameServices();
        String n1Str = parameter.getNameNode1();
        String n2Str = parameter.getNameNode2();

        HostNodeInfo info1 = new HostNodeInfo();
        info1.setHost(n1Str.split(":")[0]);
        info1.setPort(n1Str.split(":")[1]);
        HostNodeInfo info2 = new HostNodeInfo();
        info2.setHost(n2Str.split(":")[0]);
        info2.setPort(n2Str.split(":")[1]);

        List<HostNodeInfo> list = new ArrayList<>();
        list.add(info1);
        list.add(info2);
        return list;
    }


    public boolean compareSrcHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        return compareHostInfos(fromJobConfigHosts,fromMediaSourceHosts);
    }


    public boolean compareDestHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        return compareHostInfos(fromJobConfigHosts,fromMediaSourceHosts);
    }

    private boolean compareHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        if( (fromJobConfigHosts==null||fromJobConfigHosts.size()==0) || (fromMediaSourceHosts==null||fromMediaSourceHosts.size()==0)) {
            return false;
        }
        int compare = 0;
        if(fromJobConfigHosts.size()==1 && fromMediaSourceHosts.size()==1) {
            HostNodeInfo h1 = fromJobConfigHosts.get(0);
            HostNodeInfo h2 = fromMediaSourceHosts.get(0);
            compare = h1.compareTo(h2);
            if(compare != 0) {
                return false;
            }
        }
        else if(fromJobConfigHosts.size()==2 && fromMediaSourceHosts.size()==2) {
            HostNodeInfo n1_jobconfig = fromJobConfigHosts.get(0);
            HostNodeInfo n2_jobconfig = fromJobConfigHosts.get(1);
            HostNodeInfo n1_mediasour = fromMediaSourceHosts.get(0);
            HostNodeInfo n2_mediasour = fromMediaSourceHosts.get(1);
            compare = n1_jobconfig.compareTo(n1_mediasour);
            if(compare != 0) {
                return false;
            }
            compare = n2_jobconfig.compareTo(n2_mediasour);
            if(compare != 0) {
                return false;
            }
        }
        else {

        }
        return true;
    }
}
