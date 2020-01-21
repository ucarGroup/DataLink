package com.ucar.datalink.writer.es.util;

import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ucar.datalink.biz.mapping.RDBMSMapping;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.ddl.DdlSqlUtils;
import com.ucar.datalink.biz.utils.ddl.SQLStatementHolder;
import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.domain.event.EsColumnSyncEvent;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.ModeUtils;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.relationship.SqlCheckColumnInfo;
import com.ucar.datalink.domain.relationship.SqlCheckItem;
import com.ucar.datalink.domain.relationship.SqlType;
import com.ucar.datalink.domain.vo.ResponseVo;
import com.ucar.datalink.writer.es.client.rest.client.EsClient;
import com.ucar.datalink.writer.es.client.rest.loadBalance.ESConfigVo;
import com.ucar.datalink.writer.es.client.rest.vo.MappingIndexVo;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * created by swb on 2018/12/10
 * 解决问题
 * 1，es为虚拟集群
 */
public class EsColumnSyncManager {
    private static final Logger logger = LoggerFactory.getLogger(EsColumnSyncManager.class);
    private static final String baseVal = "2.3.99";
    private static final String WILD_PATTERN = "(.*)_(\\[(\\d+)\\-(\\d+)\\])$"; // 匹配类似"offer[0000-0031]"的分库分表模式
    private static final String NUMBER_PATTERN = "(.*)_(\\d{1,4})$";

    public static ResponseVo syncColumnDefinition(EsColumnSyncEvent event){
        ResponseVo responseVo = new ResponseVo();
        Long mediaSourceId = event.getMediaSourceId();
        String sql = event.getSql();
        Long mappingId = event.getMappingId();
        try {
            boolean flag = true;
            StringBuilder stringBuilder = new StringBuilder();
            MediaMappingInfo mappingInfo = DataLinkFactory.getObject(MediaService.class).findMediaMappingsById(mappingId);
            if (mappingInfo == null) {
                throw new ErrorException(CodeContext.NOTFIND_MAPPING_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.NOTFIND_MAPPING_ERROR_CODE));
            }
            MediaSourceInfo targetMediaSource = DataLinkFactory.getObject(MediaSourceService.class).getById(mediaSourceId);
            //获取es集群配置信息，如果是双中心则是两个
            List<ESConfigVo> esConfigVoList = EsConfigManager.getESConfigList(targetMediaSource);
            //获取es集群地址，如果是双中心则是两个
            Map<String,ESConfigVo> esConfigMap = getEsAddres(mappingInfo, esConfigVoList);
            if (esConfigMap == null||esConfigMap.size()<1) {
                throw new ErrorException(CodeContext.NOTFIND_ADDRESS_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.NOTFIND_ADDRESS_ERROR_CODE));
            }
            for (Map.Entry<String, ESConfigVo> entry : esConfigMap.entrySet()) {
                //获取索引对象
                MappingIndexVo indexVo = getMappingIndexVo(mappingInfo, entry.getKey(), entry.getValue());
                MediaSourceType mediaSourceType = getMediaSourceType(mappingInfo);
                //sql脚本检测解析
                SQLStatementHolder holder = getSQLStatementHolder(mediaSourceType, sql);
                //获取添加的字段及类型
                Map<String,ColumnOperationInfo> columnsMap = getAddColumnMap(holder);
                //获取es版本号
                String version = getEsVersion(indexVo);
                //幂等性判断,并返回请求数据
                Map<String, Object> requestMap = idempotency(mappingInfo, indexVo, columnsMap,version);
                //执行字段同步，考虑双中心同步
                if(!syncToManyEs(indexVo,requestMap,stringBuilder)){
                    flag = false;
                }
            }
            if(!flag){
                throw new ErrorException(CodeContext.ES_EXECUTE_ERROR_CODE,stringBuilder.deleteCharAt(stringBuilder.length()-1).toString());
            }
            return responseVo;
        }  catch (ErrorException e) {
            logger.info("[ {} ]语句执行失败！", sql, e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }
        return responseVo;
    }

    private static String getEsVersion(MappingIndexVo indexVo) {
        MappingIndexVo versionVo = new MappingIndexVo();
        versionVo.setHost(indexVo.getHost());
        versionVo.setClusterName(indexVo.getClusterName());
        versionVo.setUser(indexVo.getUser());
        versionVo.setPass(indexVo.getPass());
        versionVo.setMetaType("");
        versionVo.setType("");
        versionVo.setIndex("");
        return EsClient.getEsVersion(versionVo);
    }

    private static boolean syncToManyEs(MappingIndexVo indexVo, Map<String, Object> requestMap,StringBuilder stringBuilder) {
        boolean flag = true;
        if (requestMap.size() < 1) {
            stringBuilder.append("集群为:").append(indexVo.getClusterName()).append(",ip为:").append(indexVo.getHost()).append("的机器执行失败,执行结果 [ ").append("没有找到要新增的字段").append(" ]").append("\n");
            return true;
       }
        try {
            String response = EsClient.updateMappingIndex(indexVo, JSON.toJSONString(requestMap).getBytes("utf-8"));
            if (StringUtils.isEmpty(response) || !"true".equals(JSON.parseObject(response).getString("acknowledged"))) {
                flag = false;
                logger.info("es执行失败，结果为:{}", response);
                stringBuilder.append("集群为:").append(indexVo.getClusterName()).append(",ip为:").append(indexVo.getHost()).append("的机器执行失败,执行结果 [ ").append(response).append(" ]").append("\n");
            }
        } catch (UnsupportedEncodingException e) {
            flag = false;
            logger.info("es执行失败，结果为:{}", e);
            stringBuilder.append("集群为:").append(indexVo.getClusterName()).append(",ip为:").append(indexVo.getHost()).append("的机器执行失败,执行结果 [ ").append(e.getMessage()).append(" ]").append("\n");
        }
        return flag;
    }

    private static Map<String,ColumnOperationInfo> getAddColumnMap(SQLStatementHolder holder) throws ErrorException {
        if(holder.getSqlType()!=SqlType.AlterTable){
            throw  new ErrorException(CodeContext.SQL_NOTADD_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.SQL_NOTADD_ERROR_CODE));
        }
        List<SqlCheckItem> items = holder.getSqlCheckItems();
        if(items.size()!=1){
            throw  new ErrorException(CodeContext.SQL_COUNT_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.SQL_COUNT_ERROR_CODE));
        }
        SqlCheckItem sqlItem = items.get(0);

        List<SqlCheckColumnInfo> columnsAddInfo = sqlItem.getColumnsAddInfo();
        List<SqlCheckColumnInfo> columnModifyInfo = sqlItem.getColumnsModifyInfo();
        Map<String,ColumnOperationInfo> map = Maps.newHashMap();
        columnsAddInfo.forEach(e->{
            ColumnOperationInfo operationInfo = new ColumnOperationInfo();
            operationInfo.setName(e.getName());
            operationInfo.setType(e.getDataType());
            map.put(e.getName(),operationInfo);
        });
        columnModifyInfo.forEach(e->{
            ColumnOperationInfo operationInfo = new ColumnOperationInfo();
            operationInfo.setName(e.getName());
            operationInfo.setType(e.getDataType());
            operationInfo.setModify(true);
            map.put(e.getName(),operationInfo);
        });
        return  map;
    }


    public static SQLStatementHolder getSQLStatementHolder(MediaSourceType mediaSourceType, String sqls) throws ErrorException{
        try{
            List<SQLStatementHolder> holders = DdlSqlUtils.buildSQLStatement(mediaSourceType, sqls,false);
            if (holders.isEmpty()) {
                throw new ErrorException(CodeContext.SQL_COUNT_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.SQL_COUNT_ERROR_CODE));
            }
            if (holders.size() > 1) {
                throw new ErrorException(CodeContext.SQL_COUNT_ERROR_CODE, CodeContext.getErrorDesc(CodeContext.SQL_COUNT_ERROR_CODE));
            }
            SQLStatementHolder holder = holders.get(0);
            holder.check();
            if (holder.getSqlType()!= SqlType.AlterTable) {
                throw new ErrorException(CodeContext.SQL_VALIDATE_ERROR_CODE, CodeContext.getErrorDesc(CodeContext.SQL_VALIDATE_ERROR_CODE));
            }
            return holder;
        }catch (ParserException e){
           throw new ErrorException(CodeContext.SQL_SYNTAX_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.SQL_SYNTAX_ERROR_CODE));
        }
    }

    private static boolean containsModifyColumn(SQLStatementHolder holder) {
        List<SqlCheckItem> items = holder.getSqlCheckItems();
        for (SqlCheckItem item : items) {
            if(item.isContainsColumnModify()){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取es请求的所需要的元数据
     * @param mappingInfo
     * @return
     */
    private static MappingIndexVo getMappingIndexVo(MediaMappingInfo mappingInfo,String esAddr,ESConfigVo esConfigVo){
        MappingIndexVo indexVo = new MappingIndexVo();
        String targetMediaName = mappingInfo.getTargetMediaName();
        String[] array = targetMediaName.split("\\.");
        indexVo.setIndex(array[0]);
        indexVo.setType(array[1]);
        indexVo.setHost(esAddr);
        indexVo.setClusterName(esConfigVo.getClusterName());
        indexVo.setUser(esConfigVo.getUser());
        indexVo.setPass(esConfigVo.getPass());
        indexVo.setMetaType("_mapping");
        return indexVo;
    }

    /**
     * 获取地址列表
     * @param mappingInfo
     * @return
     */
    private static Map<String,ESConfigVo> getEsAddres(MediaMappingInfo mappingInfo,List<ESConfigVo> esConfigVoList) throws ErrorException {
        if (esConfigVoList == null||esConfigVoList.size()<1) {
            throw new ErrorException(CodeContext.NOTFIND_ESCONFIG_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.NOTFIND_ESCONFIG_ERROR_CODE));
        }
        Map<String,ESConfigVo> esConfigVoMap = Maps.newHashMap();
        for(ESConfigVo esConfigVo:esConfigVoList){
            String hosts = esConfigVo.getHosts();
            if(!StringUtils.isEmpty(hosts)){
                String[] hostArray = hosts.split(",");
                esConfigVoMap.put(hostArray[0]+":"+esConfigVo.getHttp_port(),esConfigVo);
            }
        }
        return esConfigVoMap;
    }

    /**
     * 根据幂等性过滤掉已经添加的参数，并返回添加或修改的列集合
     * @param mappingInfo
     * @param indexVo
     * @param columnsMap
     * @return
     */
    private static Map<String, Object> idempotency( MediaMappingInfo mappingInfo,MappingIndexVo indexVo,  Map<String,ColumnOperationInfo> columnsMap,String version) throws ErrorException {
        Map<String, Object> columnPro = Maps.newHashMap();
        Map<String, Object> columnObject = Maps.newHashMap();
        RDBMSMapping rdbmsMapping = new RDBMSMapping();
        if (columnsMap == null || columnsMap.size() < 1) {
            return columnPro;
        }
        String response = EsClient.viewMappingIndex(indexVo);
        if (StringUtils.isEmpty(response)) {
            return columnPro;
        }
        JSONObject json = JSON.parseObject(response);
        Map<String, String> propMap = Maps.newHashMap();
        toIndexPropMap(json, propMap);
        for (Map.Entry<String,ColumnOperationInfo> entry : columnsMap.entrySet()) {
            String columnName = entry.getKey().replace("`", "");
            if (mappingInfo.isEsUsePrefix()) {
                columnName = getTableName(mappingInfo.getSourceMedia().getName()) + "|" + columnName;
            }
            ColumnOperationInfo columnOperationInfo = entry.getValue();
            ColumnMeta columnMeta = new ColumnMeta();
            columnMeta.setType(columnOperationInfo.getType());
            ColumnMeta esColumnMeta = rdbmsMapping.toES(columnMeta);
            Map<String, String> columnMap = Maps.newHashMap();
            String esColumnType = esColumnMeta.getType();
            columnMap.put("type", esColumnType);
            //es5.0版本以后不再使用string类型，而是用keyword和text代替
            if (esColumnType.equals("string")) {
                if(compareVersion(version,baseVal)>0){
                    columnMap.put("type", "keyword");
                }else{
                    columnMap.put("index", "not_analyzed");
                }
            }
            if (esColumnType.equals("date")) {
                columnMap.put("format", "yyyy-MM-dd HH:mm:ss");
            }
            //新增字段已添加
            if (propMap.containsKey(columnName) && columnMap.get("type").equals(propMap.get(columnName))) {
                continue;
            }else if(propMap.containsKey(columnName) && columnOperationInfo.isModify()){
                throw new ErrorException(CodeContext.MODIFY_ES_TYPE_ERROR_CODE);
            }else if(!propMap.containsKey(columnName) && columnOperationInfo.isModify()){
                throw new ErrorException(CodeContext.MODIFY_ES_TYPE_ERROR_CODE);
            }else if(propMap.containsKey(columnName)){
                throw new ErrorException(CodeContext.COLUMN_EXISTS_ERROR_CODE);
            }
            columnObject.put(columnName, columnMap);
        }
        columnPro.put("properties", columnObject);
        return columnPro;
    }

    private static String getTableName(String name) {
        if(StringUtils.isEmpty(name)) {
            return "";
        }
        int index = name.lastIndexOf("_");
        if(name.matches(WILD_PATTERN)) {
            return name.substring(0,index);
        }
        if(name.matches(NUMBER_PATTERN)) {
            return name.substring(0,index);
        }
        return name;
    }

    private static int compareVersion(String version, String otherVersion) {
        String[] vArray1 = version.split("\\.");
        String[] vArray2 = otherVersion.split("\\.");
        int v1Size = vArray1.length;
        int v2Size = vArray2.length;
        int size = v1Size;
        if(size > v2Size){
            size = v2Size;
        }
        for (int i = 0; i< size; i++){
            if(Integer.valueOf(vArray1[i])==Integer.valueOf(vArray2[i])){
                continue;
            }
            if(Integer.valueOf(vArray1[i])>Integer.valueOf(vArray2[i])){
                return 1;
            }
            return -1;
        }
        if(v1Size>size){
            return 1;
        }
        if(v2Size>size){
            return -1;
        }
        return 0;
    }

    /**
     * 获取索引所对应的参数
     * @param json
     * @return
     */
    private static void toIndexPropMap(JSONObject json,Map<String,String> propMap) {
        Iterator it = json.keySet().iterator();
        while (it.hasNext()){
            String key = (String) it.next();
            Object object = json.getObject(key,Object.class);
            if (key.equals("properties")){
                JSONObject propJson = json.getJSONObject(key);
                Iterator propIt = propJson.keySet().iterator();
                while (propIt.hasNext()){
                    String prop = (String) propIt.next();
                    JSONObject valJson = propJson.getJSONObject(prop);
                    String type = valJson.getString("type");
                    propMap.put(prop,type);
                }
            }else if(object instanceof JSONObject){
                  toIndexPropMap((JSONObject)object,propMap);
            }
        }
    }

    public static MediaSourceType getMediaSourceType(MediaMappingInfo mappingInfo) {
        //返回操作类型以及列和类型映射关系
        MediaSourceType mediaSourceType;
        if (mappingInfo.getSourceMedia().getMediaSource().getType() == MediaSourceType.VIRTUAL) {
            mediaSourceType = mappingInfo.getSourceMedia().getMediaSource().getSimulateMsType();
        } else {
            mediaSourceType = mappingInfo.getSourceMedia().getMediaSource().getType();
        }
        return mediaSourceType;
    }

    static class ColumnOperationInfo {
        private String name;
        private String type;
        private boolean modify = false;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isModify() {
            return modify;
        }

        public void setModify(boolean modify) {
            this.modify = modify;
        }
    }

    public static void main(String[] args) {
        System.out.println(getTableName("t_oss_device_abnormal_[0000-0031]"));
    }

}
