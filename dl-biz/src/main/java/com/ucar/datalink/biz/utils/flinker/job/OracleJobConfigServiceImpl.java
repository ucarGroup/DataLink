package com.ucar.datalink.biz.utils.flinker.job;

import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobConfigConstant;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
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
 * Created by yang.wang09 on 2019-04-30 16:23.
 */
public class OracleJobConfigServiceImpl extends AbstractJobConfigService{

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleJobConfigServiceImpl.class);

    @Override
    public String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, String mediaName) {
        checkType(info.getParameterObj());
        //metas = removeDuplicateColumn(metas);
        RdbMediaSrcParameter parameter = (RdbMediaSrcParameter)info.getParameterObj();
        Map<String,String> srcExtendJson = property.getReader();
        Random rand = new Random();
        String ip = null;
        if(parameter.getReadConfig().getHosts()!=null && parameter.getReadConfig().getHosts().size()>0) {
            ip = parameter.getReadConfig().getHosts().get( rand.nextInt(parameter.getReadConfig().getHosts().size()) );
        } else {
            throw new RuntimeException("oracle read ip is emtpy");
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
            String etlUrl = MessageFormat.format(FlinkerJobConfigConstant.ORACLE_URL, etlHost, port, schema);
            String url = MessageFormat.format(FlinkerJobConfigConstant.ORACLE_URL, ip, port, schema);
            String columns = buildColumnParm( metas );
            String reader = loadJobConfig(FlinkerJobConfigConstant.ORACLE_READER);
            json = replace(reader,etlUrl,username,password,columns);
            json = replaceSingleTable(json,mediaName,info);
            //json = createSharingStrateg(dataxJobConfig,json);
            json = processSplitPrimaryKey(metas,json);
        }catch (Exception e){
            LOGGER.error("oracle createReaderJson error ",e);
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
            String url = MessageFormat.format(FlinkerJobConfigConstant.ORACLE_URL, ip, port, schema);
            String columns = buildColumnParm( target.getColumn() );
            String reader = loadJobConfig(FlinkerJobConfigConstant.ORACLE_WRITER);
            json = replace(reader,url,username,password,columns);
            json = replaceSingleTable(json, parseMediaName(mediaName),info );
        }catch (Exception e){
            LOGGER.error("oracle createWriterJson error ",e);
        }
        return json;
    }





    private void checkType(MediaSrcParameter parameter) {
        if( !(parameter instanceof RdbMediaSrcParameter)) {
            throw new RuntimeException("media source type error "+parameter);
        }
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

    private String replaceSingleTable(String json, String name,MediaSourceInfo info) {
        if(StringUtils.isNotBlank(name)){
            String nameSpace = info.getParameterObj().getNamespace();
            String infoName = info.getName();
            String dbName = infoName.replace(nameSpace+"#","");
            json = json.replaceAll(FlinkerJobConfigConstant.TABLE,dbName+"."+name);
        }
        return json;
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

    private List<ColumnMeta> removeDuplicateColumn(List<ColumnMeta> metas) {
        List<ColumnMeta> columnMetas = new ArrayList<>();
        Map<String,ColumnMeta> map = new HashMap<>();
        for(ColumnMeta cm : metas) {
            if( !map.containsKey(cm.getName()) ) {
                map.put(cm.getName(),cm);
            }
        }
        map.values().forEach(i->{
            columnMetas.add(i);
        });
        return columnMetas;
    }

}
