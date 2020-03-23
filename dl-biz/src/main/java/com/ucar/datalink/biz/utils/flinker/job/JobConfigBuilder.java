package com.ucar.datalink.biz.utils.flinker.job;

import com.ucar.datalink.biz.meta.MetaManager;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
import com.ucar.datalink.biz.utils.flinker.module.AdvanceJobProperty;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.common.utils.DbConfigEncryption;
import com.ucar.datalink.domain.job.HostNodeInfo;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.media.ColumnMappingMode;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by user on 2017/7/22.
 */
public class JobConfigBuilder {


    private static final Logger LOGGER = LoggerFactory.getLogger(JobConfigBuilder.class);

    private static final Map<MediaSourceType,AbstractJobConfigService> map =new HashMap<MediaSourceType, AbstractJobConfigService>();


    static{
        map.put(MediaSourceType.HDFS,new HdfsJobConfigServiceImpl());
        map.put(MediaSourceType.HBASE,new HbaseJobConfigServiceImpl());
        map.put(MediaSourceType.MYSQL,new MysqlJobConfigServiceImpl());
        map.put(MediaSourceType.ELASTICSEARCH,new ESJobConfigServiceImpl());
        map.put(MediaSourceType.SQLSERVER,new SqlServerJobConfigServiceImpl());
        map.put(MediaSourceType.POSTGRESQL, new PostgreSQLJobConfigServiceImpl());
        map.put(MediaSourceType.SDDL, new SddlJobConfigServiceImpl());
        map.put(MediaSourceType.KUDU, new KuduJobConfigServiceImpl());
        map.put(MediaSourceType.ORACLE, new OracleJobConfigServiceImpl());
        map.put(MediaSourceType.HANA, new HANAJobConfigServiceImpl());
    }


    private static final String IS_ClEAN_TARGET = "isCleanTarget";

    private static final String CHANNEL = "channel";

    /**
     * 生成带有别名的json任务
     * @param src
     * @param dest
     * @param property
     * @param srcName
     * @param destName
     * @return
     * @throws Exception
     */
    public static String buildJson(MediaSourceInfo src, String srcName, MediaSourceInfo dest, String destName,JobExtendProperty property) throws Exception {
        if(StringUtils.isEmpty(destName)) {
            destName = srcName;
        }
        List<ColumnMeta> list = getSrcColumnMetas(src,srcName);
        String readerJson = parseReader(src,srcName,dest,destName,list,property);
        String writerJson = parseWriter(src,srcName,dest,destName,list,property);
        return mergeAndMopUp(src,dest,readerJson,writerJson,property);
    }


    public static String buildJson(MediaSourceInfo src, MediaSourceInfo dest, JobExtendProperty property, List<String> names) throws Exception {
        //目前先简单的拿到数组中第一个，作为media name使用，实际应该判断这些名字的前缀是否相等，是否是以 _01 这样的数字结尾的
        String mediaName = names.get(0);
        List<ColumnMeta> list = MetaManager.getColumns(src, mediaName);
        String readerJson = parseReaderForArrayNames(src,names,list,property);
        String writerJson = parseWriter(src,"",dest,mediaName,list,property);
        return mergeAndMopUp(src,dest,readerJson,writerJson,property);
    }

    public static String buildJson(MediaSourceInfo src, MediaSourceInfo dest, JobExtendProperty property,
    String srcName, List<String> srcColumns,String destName, List<String> targetColumns, ColumnMappingMode mode) throws Exception {
        if((srcColumns==null||srcColumns.size()==0) && (targetColumns==null||targetColumns.size()==0)) {
            return buildJson(src,srcName,dest,destName,property);
        }
        List<ColumnMeta> list = getSrcColumnMetas(src,srcName);
        //返回的List<ColumnMeta>包含了部分列(黑白名单定义的)，还有别名
        //返回的这个列表就是已经处理过黑名单，加上别名的列表
        list = partialContentForList(src,list,srcColumns,dest,targetColumns,mode);
        String readerJson = parseReader(src,srcName,dest,destName,list,property);
        String writerJson = parseWriter(src,srcName,dest,destName,list,property);
        return mergeAndMopUp(src,dest,readerJson,writerJson,property);
    }



    private static String parseReaderForArrayNames(MediaSourceInfo src,List<String> names,List<ColumnMeta> list,JobExtendProperty property) {
        AbstractJobConfigService readerService = map.get(src.getParameterObj().getMediaSourceType());
        String readerJson = readerService.createReaderJson(src,list,property,names);
        return readerJson;
    }

    private static String parseReader(MediaSourceInfo src, String srcName,MediaSourceInfo dest, String destName, List<ColumnMeta> list, JobExtendProperty property) {
        AbstractJobConfigService readerService = map.get(src.getParameterObj().getMediaSourceType());
        String readerJson = "";
        if(src.getParameterObj().getMediaSourceType()==MediaSourceType.HDFS && dest.getParameterObj().getMediaSourceType()!=MediaSourceType.HDFS) {
            MediaMeta meta = new MediaMeta();
            meta.setDbType( src.getParameterObj().getMediaSourceType() );
            meta.setName( destName );
            meta.setNameSpace( dest.getParameterObj().getNamespace() );
            meta.setColumn( list );
            MediaMeta hdfsMediaMeta = MetaMapping.transformToDataX(meta);
            readerJson = readerService.createReaderJson(src,hdfsMediaMeta.getColumn(),property,srcName);
        } else {
            readerJson = readerService.createReaderJson(src,list,property,srcName);
        }
        return readerJson;
    }

    private static String parseWriter(MediaSourceInfo src, String srcName, MediaSourceInfo dest, String destName, List<ColumnMeta> list, JobExtendProperty property) {
        if(StringUtils.isEmpty(destName)) {
            destName = srcName;
        }
        MediaMeta writerMeta = new MediaMeta();
        writerMeta.setDbType( src.getParameterObj().getMediaSourceType() );
        writerMeta.setName( destName );
        writerMeta.setNameSpace( dest.getParameterObj().getNamespace() );
        writerMeta.setColumn( list );
        AbstractJobConfigService writerService = map.get(dest.getParameterObj().getMediaSourceType());
        String writerJson = writerService.createWriterJson(src,dest,writerMeta,property,destName);
        return writerJson;
    }

    private static String mergeAndMopUp(MediaSourceInfo src, MediaSourceInfo dest, String readerJson, String writerJson, JobExtendProperty property) {
        String result = "";
        AbstractJobConfigService readerService = map.get(src.getParameterObj().getMediaSourceType());
        AbstractJobConfigService writerService = map.get(dest.getParameterObj().getMediaSourceType());
        if(MediaSourceType.HBASE == src.getParameterObj().getMediaSourceType() || MediaSourceType.HBASE == dest.getParameterObj().getMediaSourceType()){
            result = writerService.merge(readerJson,writerJson);
        }else{
            result = readerService.merge(readerJson,writerJson);
        }
        AdvanceJobProperty advenceProperty = property.getAdvance();
        result = processAdvanceProperty(result,advenceProperty);
        result = readerService.readerMopUp(result,src);
        result = writerService.writerMopUp(result,dest,property);
        result = settingMopUp(result);
        return result;
    }

    private static List<ColumnMeta> getSrcColumnMetas(MediaSourceInfo src, String srcName)throws Exception {
        List<ColumnMeta> list = new ArrayList<ColumnMeta>();
        if(JobContentParseUtil.isMutilTables(srcName)) {
            String tmp_name = JobContentParseUtil.getMutilTableFirt(srcName);
            list.addAll( MetaManager.getColumns(src, tmp_name) );
        } else {
            list.addAll( MetaManager.getColumns(src, srcName) );
        }
        return list;
    }


    public static String merge(String readerJobConfig,String writerJobConfig){
        try{
            DLConfig jobConfigReader = DLConfig.parseFrom(readerJobConfig);
            DLConfig jobConfigWriter = DLConfig.parseFrom(writerJobConfig);
            DLConfig config = jobConfigReader.merge(jobConfigWriter,false);
            return config.toJSON();
        }catch (Exception e){
            LOGGER.error("createJobConfig is error",e);
        }
        return "";
    }

    public static String processAdvanceProperty(String json, AdvanceJobProperty property) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            if( StringUtils.isNotBlank(property.getChannel()) ) {
                connConf.remove("job.setting.speed.channel");
                connConf.set("job.setting.speed.channel", property.getChannel());
            }
            if( StringUtils.isNotBlank(property.getAdaptModify())) {
                DLConfigRemove(connConf,"job.setting.adaptiveFieldModify");
                DLConfigSet(connConf,"job.setting.adaptiveFieldModify",property.getAdaptModify());
            }
            json = connConf.toJSON();
            return json;
        } catch(Exception e) {
            return json;
        }

    }




    /**
     * 重新加载这个job的数据源信息
     * 通过 src 和 dest 获取MediaSrouceInfo 的源端和目标端
     * 获得源端的ip,port,用户名，密码等信息
     * 获得目标端的ip，port，用户名，密码等信息
     * 通过Configuration对象，获取url，port等信息，并重新赋值， 最后返回json任务内容
     * @param info
     * @return
     */
    public static String reload(JobConfigInfo info, MediaSourceInfo src, MediaSourceInfo dest) {
        AbstractJobConfigService readerService = map.get(src.getParameterObj().getMediaSourceType());
        AbstractJobConfigService writerService = map.get(dest.getParameterObj().getMediaSourceType());
        String json = info.getJob_content();
        json = readerService.reloadReader(json,src);
        json = writerService.reloadWriter(json,dest);
        return json;
    }


    public static String replaceJsonResult(MediaSourceType type, MediaSourceInfo info, String json, Object obj) {
        AbstractJobConfigService service = map.get(type);
        return service.replaceJsonResult(json,obj,info);
    }



    public static String encryptPassword(String json) {
        json = encryptReaderPassword(json);
        json = encryptWriterPassword(json);
        return json;
    }

    public static String decryptPassword(String json) {
        json = decryptReaderPassword(json);
        json = decryptWriterPassword(json);
        return json;
    }



    /**
     * 如果是hbase，hdfs这种没有密码的，则执行抛出，那么会原封不动的返回
     * @param json
     * @return
     */
    private static String encryptReaderPassword(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            String password = (String) connConf.get("job.content[0].reader.parameter.password");
            if(StringUtils.isBlank(password)) {
                return json;
            }
            password = DbConfigEncryption.encrypt(password);
            connConf.set("job.content[0].reader.parameter.password",password);
            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }

    /**
     * 如果是hbase，hdfs这种没有密码的，则执行抛出，那么会原封不动的返回
     * @param json
     * @return
     */
    private static String decryptReaderPassword(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            String password = (String) connConf.get("job.content[0].reader.parameter.password");
            password = DbConfigEncryption.decrypt(password);
            connConf.set("job.content[0].reader.parameter.password",password);
            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }

    /**
     * 如果是hbase，hdfs这种没有密码的，则执行抛出，那么会原封不动的返回
     * @param json
     * @return
     */
    private static String encryptWriterPassword(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            String password = (String) connConf.get("job.content[0].writer.parameter.password");
            if(StringUtils.isBlank(password)) {
                return json;
            }
            password = DbConfigEncryption.encrypt(password);
            connConf.set("job.content[0].writer.parameter.password",password);
            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }

    /**
     * 如果是hbase，hdfs这种没有密码的，则执行抛出，那么会原封不动的返回
     * @param json
     * @return
     */
    private static String decryptWriterPassword(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            String password = (String) connConf.get("job.content[0].writer.parameter.password");
            password = DbConfigEncryption.decrypt(password);
            connConf.set("job.content[0].writer.parameter.password",password);
            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }


    private static void DLConfigRemove(DLConfig config,String path) {
        try {
            config.remove(path);
        } catch(Exception e) {
            //ignore
        }
    }

    public static void DLConfigSet(DLConfig config,String path,Object value) {
        try {
            config.set(path,value);
        } catch(Exception e) {
            //ignore
        }
    }


    public static String modifyWriterPath(String json, String value) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.set("job.content[0].writer.parameter.hosts",value);
            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }

    public static List<ColumnMeta> partialContentForList(List<ColumnMeta> columnMetas, List<String> list) {
        List<ColumnMeta> newColumns = new ArrayList<>();
        columnMetas.forEach(c -> {
            list.forEach(f -> {
                if( c.getName().equalsIgnoreCase(f) ) {
                    newColumns.add(c);
                }
            });
        });
        return newColumns;
    }


    public static List<ColumnMeta> partialContentForList(MediaSourceInfo src, List<ColumnMeta> columnMetas,
    List<String> srcColumns, MediaSourceInfo dest, List<String> targetColumns,ColumnMappingMode mode) {
        if(mode.isNone()) {
            return columnMetas;
        }

        Map<String,String> map = new HashMap<String,String>();
        for(int i=0;i<srcColumns.size();i++) {
            map.put(srcColumns.get(i), targetColumns.get(i));
        }
        if(mode.isInclude()) {
            List<ColumnMeta> newColumns = new ArrayList<>();
            columnMetas.forEach(c -> {
                if(map.containsKey(c.getName())) {
                    String alias = map.get(c.getName());
                    if(StringUtils.isNotBlank(alias)) {
                        c.setAliasName(alias);
                    }
                    newColumns.add(c);
                }
            });
            return newColumns;
        }
        if(mode.isExclude()) {
            List<ColumnMeta> newColumns = new CopyOnWriteArrayList<>(columnMetas);
            newColumns.forEach(c -> {
                if(map.containsKey(c.getName())) {
                    newColumns.remove(c);
                }
            });
            return newColumns;
        }
        return columnMetas;
    }

    public static List<HostNodeInfo> parseSrcMediaSourceInfo(MediaSourceInfo info) {
        AbstractJobConfigService service = map.get(info.getType());
        return service.parseSrcMediaSourceToHostNode(info);
    }

    public static List<HostNodeInfo> parseDestMediaSourceInfo(MediaSourceInfo info) {
        AbstractJobConfigService service = map.get(info.getType());
        return service.parseDestMediaSourceToHostNode(info);
    }


    public static boolean compareSrcHostNodeInfos(MediaSourceInfo info, List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts ) {
        AbstractJobConfigService service = map.get(info.getType());
        return service.compareSrcHostInfos(fromJobConfigHosts,fromMediaSourceHosts);
    }

    public static boolean compareDestHostNodeInfos(MediaSourceInfo info, List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts ) {
        AbstractJobConfigService service = map.get(info.getType());
        return service.compareDestHostInfos(fromJobConfigHosts,fromMediaSourceHosts);
    }

    private static String settingMopUp(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            String primaryIsString = (String) connConf.get("job.content[0].reader.parameter.primaryIsString");
            if(StringUtils.equals(primaryIsString,"true")) {
                connConf.set("job.setting.speed.channel",1);
                connConf.remove("job.content[0].reader.parameter.primaryIsString");
            }
            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }

    public static String getHDFSSpecifiedPreDate(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            String preDateNum = (String)connConf.get("job.content[0].reader.parameter.specifiePreDate");
            return preDateNum;
        } catch(Exception e) {
            return "";
        }
    }

    public static boolean isESWriterPreDel(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            String isPreDelStr = (String)connConf.get("job.content[0].writer.parameter.isPreDel");
            if(StringUtils.isBlank(isPreDelStr)) {
                return false;
            }
            return Boolean.parseBoolean(isPreDelStr);
        } catch(Exception e) {
            return false;
        }
    }

    public static String getESWriterIndex(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            String esIndex = (String)connConf.get("job.content[0].writer.parameter.esIndex");
            if(StringUtils.isBlank(esIndex)) {
                return "";
            }
            return esIndex;
        } catch(Exception e) {
            return "";
        }
    }

    public static String modifySyncApplyJobContentSpeed(MediaSourceInfo srcInfo,MediaSourceInfo destInfo,String json) {
//        try {
//            int speedLimited = 5000;
//            //hbase设置大一些
//            if(srcInfo.getType()==MediaSourceType.HBASE || destInfo.getType()==MediaSourceType.HBASE) {
//                speedLimited = 20000;
//            }
//            DLConfig connConf = DLConfig.parseFrom(json);
//            connConf.set("core.transport.channel.speed.record",new Integer(speedLimited));
//            return connConf.toJSON();
//        } catch(Exception e) {
//            return json;
//        }
        return json;
    }

    public static String removePreDateFromHDFS(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].reader.parameter.path");
            if(obj instanceof String) {

            }

            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }

    /**
     * 去掉转移字符
     * @param json
     * @return
     */
    public static String removeEscapeCharacter(String json) {
        if(StringUtils.isBlank(json)) {
            return json;
        }
        try {
            String tmp_json = new String(json);
            //检查是否包含了  $  特殊字符
            //前一天的标识
            if(tmp_json.contains("\\\\$DATAX_PRE_DATE")) {
                tmp_json = tmp_json.replace("\\\\$DATAX_PRE_DATE", "$DATAX_PRE_DATE");
            }
            if(tmp_json.contains("\\$DATAX_PRE_DATE")) {
                tmp_json = tmp_json.replace("\\$DATAX_PRE_DATE", "$DATAX_PRE_DATE");
            }

            //当前时间的标识
            if(tmp_json.contains("\\\\$DATAX_CURRENT_TIME")) {
                tmp_json = tmp_json.replace("\\\\$DATAX_CURRENT_TIME", "$DATAX_CURRENT_DATE");
            }
            if(tmp_json.contains("\\$DATAX_CURRENT_TIME")) {
                tmp_json = tmp_json.replace("\\$DATAX_CURRENT_TIME", "$DATAX_CURRENT_DATE");
            }

            //上一次执行成功的时间
            if(tmp_json.contains("\\\\$DATAX_LAST_EXECUTE_TIME")) {
                tmp_json = tmp_json.replace("\\\\$DATAX_LAST_EXECUTE_TIME", "$DATAX_LAST_EXECUTE_TIME");
            }
            if(tmp_json.contains("\\$DATAX_LAST_EXECUTE_TIME")) {
                tmp_json = tmp_json.replace("\\$DATAX_LAST_EXECUTE_TIME", "$DATAX_LAST_EXECUTE_TIME");
            }
            DLConfig connConf = DLConfig.parseFrom(json);
            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }

    public static String processExtend(String json, Map<String,String> map) {
        if(map==null || map.size()==0) {
            return json;
        }
        if( map.containsKey(IS_ClEAN_TARGET) ) {
            json = cleanTargeData(json,map);
        }
        if( map.containsKey(CHANNEL) ) {
            json = modifyConcurrency(json,map);
        }
        return json;
    }

    public static String cleanTargeData(String json, Map<String,String> map) {
        if( !map.containsKey(IS_ClEAN_TARGET) ) {
            return json;
        }
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            List<String> tables = (List<String>)connConf.get("job.content[0].writer.parameter.connection[0].table");
            if(tables==null || tables.size()==0) {
                return json;
            }
            List<String> arr = new ArrayList<String>();
            for(String s : tables) {
                String preSql = "TRUNCATE TABLE "+s;
                arr.add(preSql);
            }
            connConf.set("job.content[0].writer.parameter.preSql",arr);
            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }

    public static String modifyConcurrency(String json,Map<String,String> map) {
        if( !map.containsKey(CHANNEL) ) {
            return json;
        }
        String num_str = map.get(CHANNEL);
        int num = 0;
        try {
            num = Integer.parseInt(num_str);
            if(num<0 || num>30) {
                num = 10;
            }
        } catch(Exception e) {
            num = 10;
        }
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            connConf.remove("job.setting.speed.channel");
            connConf.set("job.setting.speed.channel", ""+num);
            return connConf.toJSON();
        } catch(Exception e) {
            return json;
        }
    }

    /**
     * 判断是否是多表
     * @param table_name
     * @return
     */
    private static boolean isEndWithMultiTables(String table_name) {
        if(StringUtils.isNotBlank(table_name) && table_name.endsWith("[0000-0031]")) {
            return true;
        }
        return false;
    }

    /**
     * 在真实解析一个表的表结构时，需要用到具体的表名，如果用test_[0000-0031]是解析不了表结构的
     * 所以需要将[0000-0031]这个表后缀去掉
     * 对于目标端来hdfs来说，写入的路径也不需要包含test_[0000-0031]这样的结构，直接用test 这个名字就可以了
     * @param table_name
     * @return
     */
    private static String filterMultiTables(String table_name) {
        if(StringUtils.isBlank(table_name) || !table_name.endsWith("[0000-0031]")) {
            return table_name;
        }
        table_name = table_name.substring(0,table_name.length()-"[0000-0031]".length()-1);
        return table_name;
    }

    /**
     * 对于解析一个具体的表，需要将test_[0000-0031]改成test_0000
     * @param table_name
     * @return
     */
    private static String appendMultiTableFormat(String table_name) {
        return table_name+"_0000";
    }

}
