package com.ucar.datalink.biz.utils.flinker.job;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobConfigConstant;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
import com.ucar.datalink.biz.utils.flinker.module.MySqlJobExtendProperty;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.HostNodeInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created by user on 2017/7/19.
 */
public class MysqlJobConfigServiceImpl extends AbstractJobConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlJobConfigServiceImpl.class);

    @Override
    public String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, String mediaName) {
        checkType(info.getParameterObj());
        RdbMediaSrcParameter parameter = (RdbMediaSrcParameter)info.getParameterObj();
        Map<String,String> srcExtendJson = property.getReader();
        Random rand = new Random();
        String ip = null;
        if(parameter.getReadConfig().getHosts()!=null && parameter.getReadConfig().getHosts().size()>0) {
            ip = parameter.getReadConfig().getHosts().get( rand.nextInt(parameter.getReadConfig().getHosts().size()) );
        } else {
            throw new RuntimeException("mysql read ip is emtpy");
        }
        String etlHost = parameter.getReadConfig().getEtlHost();
        if(StringUtils.isBlank(etlHost)) {
            etlHost = ip;
        }
        String port = parameter.getPort()+"";
        String schema = info.getParameterObj().getNamespace();
        String username = parameter.getReadConfig().getUsername();
        String password = parameter.getReadConfig().getDecryptPassword();

        String json = "";
        try{
            String etlUrl = MessageFormat.format(FlinkerJobConfigConstant.MYSQL_URL, etlHost, port, schema);
            String url = MessageFormat.format(FlinkerJobConfigConstant.MYSQL_URL, ip, port, schema);
            String columns = buildColumnParm( metas );
            String reader = loadJobConfig(FlinkerJobConfigConstant.MYSQL_READER);
            json = replace(reader,etlUrl,username,password,columns);
            //json = replaceSingleTable(json,mediaName);
            //json = createSharingStrateg(dataxJobConfig,json);
            json = processSplitPrimaryKey(metas,json);
            json = processReaderExtendJson(json, srcExtendJson,url);
            json = primaryKeyIsString(metas,json);
            json = parseReaderTables(json,mediaName);
        }catch (Exception e){
            LOGGER.error("mysql createReaderJson error ",e);
        }
        return json;
    }

    @Override
    public String createWriterJson(MediaSourceInfo srcInfo, MediaSourceInfo info, MediaMeta srcMediaMeta, JobExtendProperty property, String mediaName) {
        checkType(info.getParameterObj());
        RdbMediaSrcParameter parameter = (RdbMediaSrcParameter)info.getParameterObj();
        Map<String,String> destExtendJson = property.getWriter();
        String ip = parameter.getWriteConfig().getWriteHost();
        String port = parameter.getPort()+"";
        String schema = info.getParameterObj().getNamespace();
        String username = parameter.getWriteConfig().getUsername();
        String password = parameter.getWriteConfig().getDecryptPassword();

        String json = "";
        try{
            MediaMeta target = changeNameToAlias( MetaMapping.transformToRDBMS(srcMediaMeta) );
            String url = MessageFormat.format(FlinkerJobConfigConstant.MYSQL_URL, ip, port, schema);
            String columns = buildColumnParm( target.getColumn() );
            String reader = loadJobConfig(FlinkerJobConfigConstant.MYSQL_WRITER);
            json = replace(reader,url,username,password,columns);
            json = replaceSingleTable(json, parseMediaName(mediaName) );
            json = processWriterExtendJson(json,destExtendJson);
            json = parseMutilWriterTables(json,mediaName);
        }catch (Exception e){
            LOGGER.error("mysql createWriterJson error ",e);
        }
        return json;
    }

    @Override
    public String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, List<String> names) {
        checkType(info.getParameterObj());
        RdbMediaSrcParameter parameter = (RdbMediaSrcParameter)info.getParameterObj();
        Map<String,String> srcExtendJson = property.getReader();
        Random rand = new Random();
        String ip = null;
        if(parameter.getReadConfig().getHosts()!=null && parameter.getReadConfig().getHosts().size()>0) {
            ip = parameter.getReadConfig().getHosts().get( rand.nextInt(parameter.getReadConfig().getHosts().size()) );
        } else {
            ip = parameter.getWriteConfig().getWriteHost();
        }
        String etlHost = parameter.getReadConfig().getEtlHost();
        if(StringUtils.isBlank(etlHost)) {
            etlHost = ip;
        }
        String port = parameter.getPort()+"";
        String schema = info.getParameterObj().getNamespace();
        String username = parameter.getReadConfig().getUsername();
        String password = parameter.getReadConfig().getDecryptPassword();

        String json = "";
        try{
            String etlUrl = MessageFormat.format(FlinkerJobConfigConstant.MYSQL_URL, etlHost, port, schema);
            String url = MessageFormat.format(FlinkerJobConfigConstant.MYSQL_URL, ip, port, schema);
            String columns = buildColumnParm( metas );
            String reader = loadJobConfig(FlinkerJobConfigConstant.MYSQL_READER);
            json = replace(reader,etlUrl,username,password,columns );
            json = replaceMultiTable(json,names);
            json = processSplitPrimaryKey(metas,json);
            json = processReaderExtendJson(json, srcExtendJson,url);
            json = primaryKeyIsString(metas,json);
        }catch (Exception e){
            LOGGER.error("mysql createReaderJson error ",e);
        }
        return json;
    }


    private String processReaderExtendJson(String json, Map<String, String> srcExtendJson,String url) {
        if(srcExtendJson==null || srcExtendJson.size()==0) {
            return json;
        }
        String extendJson = JSONObject.toJSONString(srcExtendJson);
        MySqlJobExtendProperty jobExtend = JSONObject.parseObject(extendJson, MySqlJobExtendProperty.class);
        filterSpace(jobExtend);
        DLConfig connConf = DLConfig.parseFrom(json);
        if( StringUtils.isNotBlank(jobExtend.getWhere()) ) {
            connConf.set("job.content[0].reader.parameter.where", jobExtend.getWhere());
        }
        if( StringUtils.isNotBlank(jobExtend.getQuerySql()) ) {
            //connConf.remove("job.content[0].reader.parameter.splitPk");
            connConf.remove("job.content[0].reader.parameter.connection[0].table");
            connConf.remove("job.content[0].reader.parameter.where");
            List<String> list = new ArrayList<>();
            list.add(jobExtend.getQuerySql());
            connConf.set("job.content[0].reader.parameter.connection[0].querySql", list);
        }
        if( StringUtils.isNotBlank(jobExtend.getJdbcReaderUrl()) ) {
            List<String> list = (List<String>)connConf.get("job.content[0].reader.parameter.connection[0].jdbcUrl");
            list.add(jobExtend.getJdbcReaderUrl());
            connConf.remove("job.content[0].reader.parameter.connection[0].jdbcUrl");
            connConf.set("job.content[0].reader.parameter.connection[0].jdbcUrl", list);
        } else {
            List<String> list = (List<String>)connConf.get("job.content[0].reader.parameter.connection[0].jdbcUrl");
            list.add(url);
            if(StringUtils.isNotBlank(jobExtend.getJdbcReaderUrl())) {
                list.add(jobExtend.getJdbcReaderUrl());
            }
            connConf.remove("job.content[0].reader.parameter.connection[0].jdbcUrl");
            connConf.set("job.content[0].reader.parameter.connection[0].jdbcUrl", list);
        }
        json = connConf.toJSON();
        return json;
    }

    private String processWriterExtendJson(String json, Map<String, String> destExtendJson) {
        if(destExtendJson==null || destExtendJson.size()==0) {
            return json;
        }
        String extendJson = JSONObject.toJSONString(destExtendJson);
        MySqlJobExtendProperty jobExtend = JSONObject.parseObject(extendJson, MySqlJobExtendProperty.class);
        filterSpace(jobExtend);
        DLConfig connConf = DLConfig.parseFrom(json);
        if( StringUtils.isNotBlank(jobExtend.getPreSql()) ) {
            List<String> list = new ArrayList<>();
            list.add(jobExtend.getPreSql());
            connConf.set("job.content[0].writer.parameter.preSql", list);
        }
        if( StringUtils.isNotBlank(jobExtend.getPostSql()) ) {
            List<String> list = new ArrayList<>();
            list.add(jobExtend.getPostSql());
            connConf.set("job.content[0].writer.parameter.postSql", list);
        }
        json = connConf.toJSON();
        return json;
    }



    private void checkType(MediaSrcParameter parameter) {
        if( !(parameter instanceof RdbMediaSrcParameter)) {
            throw new RuntimeException("media source type error "+parameter);
        }
    }


    /**
     * 处理job配置中的 splitPk 参数，这个值不再强制指定为 id，而是根据读取到的列元信息自动选择
     * @param list
     */
    private String processSplitPrimaryKey(List<ColumnMeta> list, String json) {
        for(ColumnMeta cm : list) {
            if(cm.isPrimaryKey()) {
                json = json.replaceAll(FlinkerJobConfigConstant.RMDBS_SPLIT_PK,cm.getName());
                break;
            }
        }
        //如果当前表没有配置主键信息则将 splitPk这个字段设置为空字符串
        if( json.contains(FlinkerJobConfigConstant.RMDBS_SPLIT_PK) ) {
            json = json.replaceAll(FlinkerJobConfigConstant.RMDBS_SPLIT_PK,"");
        }
        return json;
    }

    private String replace(String json,String url,String userName,String passWord,String column){
        if(StringUtils.isNotBlank(url)){
            json = json.replaceAll(FlinkerJobConfigConstant.JDBCURL, url);
        }
        if(StringUtils.isNotBlank(userName)){
            json = json.replaceAll(FlinkerJobConfigConstant.USERNAME,userName);
        }
        if(StringUtils.isNotBlank(passWord)){
            json = json.replaceAll(FlinkerJobConfigConstant.PASSWORD,passWord);
        }
        if(StringUtils.isNotBlank(column)){
            //json = json.replaceAll(FlinkerJobConfigConstant.COLUMN,column);
            json = replaceColumns(json,column);
        }
        if(json.contains(FlinkerJobConfigConstant.COLUMN)) {
            json.replaceAll(FlinkerJobConfigConstant.COLUMN,"");
        }
        return json;
    }

    private String replaceSingleTable(String json, String name) {
        if(StringUtils.isNotBlank(name)){
            json = json.replaceAll(FlinkerJobConfigConstant.TABLE,name);
        }
        return json;
    }

    private String replaceMultiTable(String json,List<String> names) {
        if(names!=null && names.size()>0) {
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.remove("job.content[0].reader.parameter.connection[0].table");
            connConf.set("job.content[0].reader.parameter.connection[0].table", names);
            json = connConf.toJSON();
        }
        return json;
    }


    private void filterSpace(MySqlJobExtendProperty property) {
        if(property == null) {
            return;
        }
//        if(StringUtils.isNotBlank( property.getJdbcReaderUrl() )) {
//            String jdbcReader_url = removeEnter(property.getJdbcReaderUrl());
//            property.setJdbcReaderUrl(jdbcReader_url);
//        }
        if(StringUtils.isNotBlank(property.getPostSql())) {
            String postSql = removeEnter(property.getPostSql());
            property.setPostSql(postSql);
        }
        if(StringUtils.isNotBlank(property.getPreSql())) {
            String preSql = removeEnter(property.getPreSql());
            property.setPreSql(preSql);
        }
        if(StringUtils.isNotBlank(property.getQuerySql())) {
            String querySql = removeEnter(property.getQuerySql());
            property.setQuerySql(querySql);
        }
        if(StringUtils.isNotBlank(property.getWhere())) {
            String where = removeEnter(property.getWhere());
            property.setWhere(where);
        }
    }

    private String removeEnter(String content) {
        if(content.contains("\n")) {
            content = content.replaceAll("\n"," ");
            return content;
        }
        return content;
    }


    @Override
    public String reloadReader(String json,MediaSourceInfo info) {
        try {
            checkType(info.getParameterObj());
            RdbMediaSrcParameter parameter = (RdbMediaSrcParameter)info.getParameterObj();
            Random rand = new Random();
            String ip = null;
            if(parameter.getReadConfig().getHosts()!=null && parameter.getReadConfig().getHosts().size()>0) {
                ip = parameter.getReadConfig().getHosts().get( rand.nextInt(parameter.getReadConfig().getHosts().size()) );
            } else {
                throw new RuntimeException("mysql read ip is emtpy");
            }
            String etlHost = parameter.getReadConfig().getEtlHost();
            if(StringUtils.isBlank(etlHost)) {
                etlHost = ip;
            }
            String port = parameter.getPort()+"";
            String schema = info.getParameterObj().getNamespace();
            String username = parameter.getReadConfig().getUsername();
            String password = parameter.getReadConfig().getDecryptPassword();
            String etlUrl = MessageFormat.format(FlinkerJobConfigConstant.MYSQL_URL, etlHost, port, schema);
            String url = MessageFormat.format(FlinkerJobConfigConstant.MYSQL_URL, ip, port, schema);

            DLConfig connConf = DLConfig.parseFrom(json);
            List<String> list = new ArrayList<>();
            list.add(etlUrl);
            list.add(url);
            connConf.remove("job.content[0].reader.parameter.connection[0].jdbcUrl");
            connConf.set("job.content[0].reader.parameter.connection[0].jdbcUrl", list);
            connConf.remove("job.content[0].reader.parameter.username");
            connConf.remove("job.content[0].reader.parameter.password");
            connConf.set("job.content[0].reader.parameter.username",username);
            connConf.set("job.content[0].reader.parameter.password",password);
            json = connConf.toJSON();
        } catch(Exception e) {
            LOGGER.error("reload reader json failure,",e);
        }
        return json;
    }

    @Override
    public String reloadWriter(String json, MediaSourceInfo info) {
        try {
            checkType(info.getParameterObj());
            RdbMediaSrcParameter parameter = (RdbMediaSrcParameter)info.getParameterObj();
            String ip = parameter.getWriteConfig().getWriteHost();
            String port = parameter.getPort()+"";
            String schema = info.getParameterObj().getNamespace();
            String username = parameter.getWriteConfig().getUsername();
            String password = parameter.getWriteConfig().getDecryptPassword();
            String url = MessageFormat.format(FlinkerJobConfigConstant.MYSQL_URL, ip, port, schema);
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.remove("job.content[0].writer.parameter.connection[0].jdbcUrl");
            connConf.set("job.content[0].writer.parameter.connection[0].jdbcUrl", url);
            connConf.remove("job.content[0].writer.parameter.username");
            connConf.remove("job.content[0].writer.parameter.password");
            connConf.set("job.content[0].writer.parameter.username",username);
            connConf.set("job.content[0].writer.parameter.password",password);
            json = connConf.toJSON();
        } catch(Exception e) {
            LOGGER.error("reload writer json failure.",e);
        }
        return json;
    }

    /**
     * the url like jdbc:mysql://10.212.17.5:3306/ucar_order
     * @param json
     * @return
     */
    @Override
    public List<HostNodeInfo> parseSrcContent(String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        List<String> list = parseHostInfoToList(json,"job.content[0].reader.parameter.connection[0].jdbcUrl");
        String username = parseHostInfo(json,"job.content[0].reader.parameter.username");
        String password = parseHostInfo(json,"job.content[0].reader.parameter.password");
        List<HostNodeInfo> hosts = new ArrayList<>();
        list.forEach(i->{
            String url = i.split("/")[2];
            HostNodeInfo h = new HostNodeInfo();
            h.setHost(url.split(":")[0]);
            h.setPort(url.split(":")[1]);
            hosts.add(h);
        });
        return hosts;
    }

    @Override
    public List<HostNodeInfo> parseDestContent(String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        List<String> list = parseHostInfoToList(json,"job.content[0].writer.parameter.connection[0].jdbcUrl");
        String username = parseHostInfo(json,"job.content[0].writer.parameter.username");
        String password = parseHostInfo(json,"job.content[0].writer.parameter.password");
        List<HostNodeInfo> hosts = new ArrayList<>();
        list.forEach(i->{
            String url = i.split("/")[2];
            HostNodeInfo h = new HostNodeInfo();
            h.setHost(url.split(":")[0]);
            h.setPort(url.split(":")[1]);
            hosts.add(h);
        });
        return hosts;
    }

    @Override
    public List<HostNodeInfo> parseSrcMediaSourceToHostNode(MediaSourceInfo info) {
        checkType(info.getParameterObj());
        RdbMediaSrcParameter parameter = (RdbMediaSrcParameter)info.getParameterObj();
        String dataxHost = parameter.getReadConfig().getEtlHost();
        String port = parameter.getPort()+"";
        Set<String> hosts = new HashSet<>();
        hosts.add(dataxHost);

        List<HostNodeInfo> hostList = new ArrayList<>();
        hosts.forEach(i->{
            HostNodeInfo h = new HostNodeInfo();
            h.setHost(i);
            h.setPort(port);
            hostList.add(h);
        });
        return hostList;
    }

    @Override
     public List<HostNodeInfo> parseDestMediaSourceToHostNode(MediaSourceInfo info) {
        checkType(info.getParameterObj());
        RdbMediaSrcParameter parameter = (RdbMediaSrcParameter)info.getParameterObj();
        String writeHost = parameter.getWriteConfig().getWriteHost();
        String port = parameter.getPort()+"";
        Set<String> hosts = new HashSet<>();
        hosts.add(writeHost);

        List<HostNodeInfo> hostList = new ArrayList<>();
        hosts.forEach(i->{
            HostNodeInfo h = new HostNodeInfo();
            h.setHost(i);
            h.setPort(port);
            hostList.add(h);
        });
        return hostList;
    }


    @Override
    public boolean compareSrcHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        return compareHost(fromJobConfigHosts,fromMediaSourceHosts);
    }

    @Override
    public boolean compareDestHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        return compareHost(fromJobConfigHosts,fromMediaSourceHosts);
    }

    private boolean compareHost(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        if( (fromJobConfigHosts==null||fromJobConfigHosts.size()==0) || (fromMediaSourceHosts==null||fromMediaSourceHosts.size()==0)) {
            return false;
        }
        Set<HostNodeInfo> jobconfigSet = new HashSet<>(fromJobConfigHosts);
        Set<HostNodeInfo> mediaSet = new HashSet<>(fromMediaSourceHosts);
        for(HostNodeInfo i : jobconfigSet) {
            for(HostNodeInfo j : mediaSet) {
                if(i.compareTo(j) != 0) {
                    return false;
                }
            }
        }
        return true;
    }


    private static final Set<String> INTEGER_PRIMARY_KEY_SET = new HashSet<>();
    static {
        INTEGER_PRIMARY_KEY_SET.add("bigint");
        INTEGER_PRIMARY_KEY_SET.add("tinyint");
        INTEGER_PRIMARY_KEY_SET.add("smallint");
        INTEGER_PRIMARY_KEY_SET.add("int");
        INTEGER_PRIMARY_KEY_SET.add("bigint identity");
        INTEGER_PRIMARY_KEY_SET.add("bigint unsigned");
        INTEGER_PRIMARY_KEY_SET.add("int identity");
        INTEGER_PRIMARY_KEY_SET.add("tinyint unsigned");
        INTEGER_PRIMARY_KEY_SET.add("int unsigned");
        INTEGER_PRIMARY_KEY_SET.add("integer");
        INTEGER_PRIMARY_KEY_SET.add("int4");
        INTEGER_PRIMARY_KEY_SET.add("smallint identity");

    }
    private String primaryKeyIsString(List<ColumnMeta> metas, String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        for(ColumnMeta cm : metas) {
            if( cm.isPrimaryKey() ) {
                String type = cm.getType().toLowerCase();
                if( !INTEGER_PRIMARY_KEY_SET.contains(type) ) {
                    connConf.set("job.content[0].reader.parameter.primaryIsString","true");
                    break;
                }
            }
        }
        return connConf.toJSON();
    }

    private static String parseReaderTables(String json, String table_name) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            List<String> tables = JobContentParseUtil.parseMutilTables(table_name);
            connConf.remove("job.content[0].reader.parameter.connection[0].table");
            connConf.set("job.content[0].reader.parameter.connection[0].table",tables);
            json = connConf.toJSON();
        } catch(Exception e) {
            LOGGER.error("process multi tables failure.",e);
        }
        return json;
    }

    private static String parseMutilWriterTables(String json, String table_name) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            List<String> tables = JobContentParseUtil.parseMutilTables(table_name);
            connConf.remove("job.content[0].writer.parameter.connection[0].table");
            connConf.set("job.content[0].writer.parameter.connection[0].table",tables);
            json = connConf.toJSON();
        } catch(Exception e) {
            LOGGER.error("process multi tables failure.",e);
        }
        return json;
    }



}
