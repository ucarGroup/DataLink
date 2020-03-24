package com.ucar.datalink.biz.utils.flinker.job;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobConfigConstant;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
import com.ucar.datalink.biz.utils.flinker.module.MySqlJobExtendProperty;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by user on 2017/9/12.
 */
public class SddlJobConfigServiceImpl extends AbstractJobConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SddlJobConfigServiceImpl.class);

    @Override
    public String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, String mediaName) {
        checkType(info.getParameterObj());
        SddlMediaSrcParameter sddlParameter = (SddlMediaSrcParameter)info.getParameterObj();
        sddlParameter.getProxyDbId();
        RdbMediaSrcParameter parameter = getMediaSourceInfoById(sddlParameter.getProxyDbId()).getParameterObj();
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
            json = replaceSingleTable(json,mediaName);
            //json = createSharingStrateg(dataxJobConfig,json);
            json = processSplitPrimaryKey(metas,json);
            json = processReaderExtendJson(json, srcExtendJson,url);
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
            MediaMeta target = MetaMapping.transformToRDBMS(srcMediaMeta);
            String url = MessageFormat.format(FlinkerJobConfigConstant.MYSQL_URL, ip, port, schema);
            String columns = buildColumnParm( target.getColumn() );
            String reader = loadJobConfig(FlinkerJobConfigConstant.MYSQL_WRITER);
            json = replace(reader,url,username,password,columns);
            json = replaceSingleTable(json, parseMediaName(mediaName) );
            json = processWriterExtendJson(json,destExtendJson);
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
        if( !(parameter instanceof SddlMediaSrcParameter)) {
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

    @Override
    public String replaceJsonResult(String json, Object object, MediaSourceInfo srcInfo) {
        String dbName = (String)object;
        DLConfig connConf = DLConfig.parseFrom(json);
        Object obj = connConf.get("job.content[0].reader.parameter.connection[0].jdbcUrl");
        if(obj instanceof String) {
            String url = (String)connConf.get("job.content[0].reader.parameter.connection[0].jdbcUrl");
            url = replaceJdbcUrl(url,dbName);
            connConf.remove("job.content[0].reader.parameter.connection[0].jdbcUrl");
            connConf.set("job.content[0].reader.parameter.connection[0].jdbcUrl", url);
        }
        else {
            List<String> list = (List<String>)connConf.get("job.content[0].reader.parameter.connection[0].jdbcUrl");
            List<String> newUrl = new ArrayList<>();
            for(String url : list) {
                String tmp = replaceJdbcUrl(url,dbName);
                newUrl.add(tmp);
            }
            connConf.remove("job.content[0].reader.parameter.connection[0].jdbcUrl");
            connConf.set("job.content[0].reader.parameter.connection[0].jdbcUrl", newUrl);
        }
        json = connConf.toJSON();
        return json;
    }

    private String replaceJdbcUrl(String url, String dbName) {
        if(url!=null && url.lastIndexOf("/")!=-1) {
            int index = url.lastIndexOf("/");
            String prefix = url.substring(0,index);
            url = prefix + "/" + dbName;
        }
        return url;
    }


}
