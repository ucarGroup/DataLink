package com.ucar.datalink.flinker.plugin.writer.eswriter;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.flinker.api.element.Column;
import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.client.EsClient;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.constant.ESEnum;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.vo.BatchContentVo;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.vo.BatchDocVo;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.vo.BatchUpsertContentVo;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.vo.BulkResultVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yw.zhang02 on 2016/7/26.
 */
public class EsHelper {

    private static final Logger logger = LoggerFactory.getLogger(EsHelper.class);

    private static final EsMergeColumn esMergeColumn = new EsMergeColumn();

    /**
     * 批量创建
     * <p>
     * 使用INDEX，重复创建时会替换，注意主子表合并时的替换操作，需要在重新update子表
     *
     * @param index
     * @param type
     * @param table
     * @param columnConfig
     * @param recordList
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String batchCreateDoc(Boolean isAddTablePrefix, String index, String type, String table, List<Object> columnConfig, List<Record> recordList, String esRouting, String esRoutingIgnore) throws UnsupportedEncodingException {

        BatchDocVo vo = createBatchDocVo(index);

        List<BatchContentVo> list = createBatchContentVoList(ESEnum.BatchActionEnum.INDEX,
                type, table, columnConfig, recordList, isAddTablePrefix,esRouting,esRoutingIgnore);
        //没有要同步的数据，直接返回成功
        if(CollectionUtils.isEmpty(list)){
            Map<String,Boolean> map = new HashMap<String,Boolean>();
            map.put("errors",false);
            return JSON.toJSONString(map);
        }
        BulkResultVo bulkResultVo = EsClient.batchDocWithResultParse(vo, list);
        bulkResultVo.checkFailed();
        return bulkResultVo.getJsonString();

    }

    /**
     * 批量update
     * <p>
     * 使用UPDATE 当更新的记录不存在时会报404错误
     *
     * @param index
     * @param type
     * @param table
     * @param columnConfig
     * @param recordList
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String batchUpdateDoc(Boolean isAddTablePrefix, String index, String type, String table, List<Object> columnConfig, List<Record> recordList, String esRouting, String esRoutingIgnore) throws UnsupportedEncodingException {
        BatchDocVo vo = createBatchDocVo(index);

        List<BatchContentVo> list = createBatchContentVoList(ESEnum.BatchActionEnum.UPDATE,
                type, table, columnConfig, recordList, isAddTablePrefix, esRouting, esRoutingIgnore);
        //没有要同步的数据，直接返回成功
        if(CollectionUtils.isEmpty(list)){
            Map<String,Boolean> map = new HashMap<String,Boolean>();
            map.put("errors",false);
            return JSON.toJSONString(map);
        }
        BulkResultVo bulkResultVo = EsClient.batchDocWithResultParse(vo, list);
        bulkResultVo.checkFailed();
        return bulkResultVo.getJsonString();

    }

    /**
     *  批量upsert操作
     * @param isAddTablePrefix
     * @param index
     * @param type
     * @param table
     * @param columnConfig
     * @param recordList
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String batchUpsertDoc(Boolean isAddTablePrefix, String index, String type, String table, List<Object> columnConfig, List<Record> recordList, String esRouting, String esRoutingIgnore) throws UnsupportedEncodingException {
        BatchDocVo vo = createBatchDocVo(index);
        List<BatchUpsertContentVo> list = createBatchUpsertContentVoList(type, table, columnConfig, recordList, isAddTablePrefix, esRouting, esRoutingIgnore);
        //logger.info("batchUpsertDoc list size1 : ");
        //没有要同步的数据，直接返回成功
        if(CollectionUtils.isEmpty(list)){
            Map<String,Boolean> map = new HashMap<String,Boolean>();
            map.put("errors",false);
            return JSON.toJSONString(map);
        }
        //logger.info("batchUpsertDoc list size2 : " + list.size());
        BulkResultVo bulkResultVo = EsClient.batchUpsertDocWithResultParse(vo,list);
        bulkResultVo.checkFailed();
        return bulkResultVo.getJsonString();

    }


    public static BatchDocVo createBatchDocVo(String index) {
        BatchDocVo vo = new BatchDocVo();
        vo.setIndex(index);
        vo.setBatchType("_bulk");
        return vo;
    }

    public static List<BatchUpsertContentVo> createBatchUpsertContentVoList(String type, String table, List<Object> columnConfig, List<Record> recordList, Boolean isAddTablePrefix, String esRouting, String esRoutingIgnore){
        List<BatchUpsertContentVo> list = new ArrayList<BatchUpsertContentVo>();
        Map<String, Object> map = null;
        for (Record record : recordList) {
            BatchUpsertContentVo batchUpsertContentVo = new BatchUpsertContentVo();

            
            //走routing逻辑
            if(StringUtils.isNotBlank(esRouting)){

                //logger.info("datax write to es，走了routing逻辑");

                //组装routing值且校验本条数据是否能忽略
                Map resultMap = getEsRoutingValue(esRouting,esRoutingIgnore,record,columnConfig);
                String esRoutingValue = String.valueOf(resultMap.get("esRoutingValue"));
                boolean canIgnoreThisRecord = (Boolean)resultMap.get("canIgnoreThisRecord");

                if(canIgnoreThisRecord){
                    continue;
                }

                //去掉最后的逗号
                esRoutingValue = esRoutingValue.substring(0,esRoutingValue.length() - 1);
                batchUpsertContentVo.setRoutingValue(esRoutingValue);
            }

            batchUpsertContentVo.setType(type);

            if (record.getId() != null) {
                batchUpsertContentVo.setId(record.getId().toString());
            } else {
                Column IdColumn = record.getColumn(0);
                String idValue = String.valueOf(IdColumn.getRawData());
                batchUpsertContentVo.setId(idValue);
            }

            if (esMergeColumn.isMergeColumn()) {
                map = convertRecord2MapAndMergeColumn(record, table, columnConfig, isAddTablePrefix);
            } else {
                map = convertRecord2Map(record, table, columnConfig, isAddTablePrefix);
            }
            for(Map.Entry<String, Object> entry : map.entrySet()){
                batchUpsertContentVo.putUpdateDate(entry.getKey(),entry.getValue());
            }

            list.add(batchUpsertContentVo);
        }
        return list;


    }


    public static List<BatchContentVo> createBatchContentVoList(ESEnum.BatchActionEnum batchActionEnum, String type, String table, List<Object> columnConfig, List<Record> recordList, Boolean isAddTablePrefix, String esRouting, String esRoutingIgnore) {
        List<BatchContentVo> list = new ArrayList<BatchContentVo>();
        Map<String, Object> map = null;
        for (Record record : recordList) {

            BatchContentVo contentVo = new BatchContentVo();

            //走routing逻辑
            if(StringUtils.isNotBlank(esRouting)){

                //logger.info("datax write to es，走了routing逻辑");

                //组装routing值且校验本条数据是否能忽略
                Map resultMap = getEsRoutingValue(esRouting,esRoutingIgnore,record,columnConfig);
                String esRoutingValue = String.valueOf(resultMap.get("esRoutingValue"));
                boolean canIgnoreThisRecord = (Boolean)resultMap.get("canIgnoreThisRecord");

                if(canIgnoreThisRecord){
                    continue;
                }

                //去掉最后的逗号
                esRoutingValue = esRoutingValue.substring(0,esRoutingValue.length() - 1);
                contentVo.setRoutingValue(esRoutingValue);
            }

            contentVo.setBatchActionEnum(batchActionEnum);
            contentVo.setType(type);

            if (record.getId() != null) {
                contentVo.setId(record.getId().toString());
            } else {
                Column IdColumn = record.getColumn(0);
                String idValue = String.valueOf(IdColumn.getRawData());
                contentVo.setId(idValue);
            }

            if (esMergeColumn.isMergeColumn()) {
                map = convertRecord2MapAndMergeColumn(record, table, columnConfig, isAddTablePrefix);
            } else {
                map = convertRecord2Map(record, table, columnConfig, isAddTablePrefix);
            }
            contentVo.setContent(map);
            list.add(contentVo);
        }
        return list;
    }
    
    /**
     * 组装routing值且校验本条数据是否能忽略
     * @param esRouting
     * @param esRoutingIgnore
     * @param record
     * @param columnConfig
     * @return
     */
    private static Map getEsRoutingValue(String esRouting, String esRoutingIgnore, Record record, List<Object> columnConfig){
        Map resultMap = new HashMap();
        String esRoutingValue = "";
        boolean canIgnoreThisRecord = false;
        Boolean esRoutingIgnoreTemp = Boolean.parseBoolean(esRoutingIgnore);

        String[] esRoutingArr = esRouting.split(",");
        for (String columnName : esRoutingArr){
            Object columnValue = getColumnValue(record,columnConfig,columnName);
            if(!isNullOrEmpty(columnValue)){
                String columnValueStr = String.valueOf(columnValue);
                esRoutingValue += columnValueStr + ",";
            }else{
                if(esRoutingIgnoreTemp){
                    canIgnoreThisRecord = true;
                    break;
                }else{
                    throw DataXException.asDataXException(EsWriterErrorCode.ES_ROUTING_EXCEPTION, "有问题的这条数据是：" + record.toString());
                }
            }
        }
        resultMap.put("esRoutingValue",esRoutingValue);
        resultMap.put("canIgnoreThisRecord",canIgnoreThisRecord);
        return resultMap;
    }

    /**
     * 判断null或者空串
     *
     * @param object
     * @return
     */
    private static boolean isNullOrEmpty(Object object){
        if(object == null){
            return true;
        }
        if(object instanceof String){
            return StringUtils.isBlank((String)object);
        }
        return false;
    }

    /**
     * 获取列值
     *
     * @param columnName
     * @return
     */
    private static Object getColumnValue(Record record, List<Object> columnConfig, String columnName) {
        int columnNumber = record.getColumnNumber();
        for (int i = 0; i < columnNumber; i++) {
            Column column = record.getColumn(i);
            String columnNameTemp = String.valueOf(columnConfig.get(i));
            if(StringUtils.equals(columnNameTemp,columnName)){
                //返回列值
                return column.getRawData();
            }
        }
        return null;
    }    
    

    /**
     * ˙
     * record记录替换为EsMap
     *
     * @param record
     * @param table
     * @param columnConfig
     * @return
     */
    private static Map<String, Object> convertRecord2Map(Record record, String table, List<Object> columnConfig, Boolean isAddTablePrefix) {
        Map<String, Object> map = new HashMap<String, Object>();
        int columnNumber = record.getColumnNumber();
        for (int i = 0; i < columnNumber; i++) {
            Column column = record.getColumn(i);
            String columnName = String.valueOf(columnConfig.get(i));
            Object columnValue = convert2EsColumnValue(column);
            String esColumn = convert2EsColumnName(table, columnName, isAddTablePrefix);
            map.put(esColumn, columnValue);
        }
        return map;
    }

    /**
     * record记录替换为EsMap 合并字段
     *
     * @param record
     * @param table
     * @param columnConfig
     * @return
     */
    private static Map<String, Object> convertRecord2MapAndMergeColumn(Record record, String table, List<Object> columnConfig, Boolean isAddTablePrefix) {
        Map<String, Object> map = new HashMap<String, Object>();
        int columnNumber = record.getColumnNumber();
        for (int i = 0; i < columnNumber; i++) {
            Column column = record.getColumn(i);
            String columnName = String.valueOf(columnConfig.get(i));
            Object columnValue = convert2EsColumnValue(column);
            String esColumn = convert2EsColumnName(table, columnName, isAddTablePrefix);
            List<String> esMergeColumnList = esMergeColumn.getAllMergeColumnName();
            if (!esMergeColumnList.contains(columnName)) {
                map.put(esColumn, columnValue);
            } else {
                for (EsMergeColumn.Column mergeColumn : esMergeColumn.getAllMergeColumn()) {
                    mergeColumn(mergeColumn, columnName, columnValue, map, table, isAddTablePrefix);
                }
            }
        }
        return map;
    }

    private static void mergeColumn(EsMergeColumn.Column esMergeColumn, String columnName, Object columnValue, Map<String, Object> map, String table, Boolean isAddTablePrefix) {
        String esMergeColumnName = convert2EsColumnName(table, esMergeColumn.getMergeColumnName(), isAddTablePrefix);
        if (esMergeColumn.getColumnNameList().contains(columnName) && Key.ES_LON_LAT_MERGE_TYPE.equals(esMergeColumn.getMergeType())) {
            Map<String, Object> mergeMap = (Map<String, Object>) map.get(esMergeColumnName);
            if (mergeMap != null) {
                setLonAndlatValue(esMergeColumn, mergeMap, columnName, columnValue);
            } else {
                mergeMap = new HashMap<String, Object>();
                setLonAndlatValue(esMergeColumn, mergeMap, columnName, columnValue);
                map.put(esMergeColumnName, mergeMap);
            }
        } else if (esMergeColumn.getColumnNameList().contains(columnName) && Key.ES_OTHER_MERGE_TYPE.equals(esMergeColumn.getMergeType())) {
            List<Object> mergeList = (List<Object>) map.get(esMergeColumnName);
            if (mergeList != null) {
                mergeList.add(columnValue);
                if (mergeList.size() == esMergeColumn.getColumnNameList().size()) {
                    map.put(esMergeColumnName, StringUtils.join(mergeList, ","));
                }
            } else {
                mergeList = new ArrayList<Object>();
                mergeList.add(columnValue);
                map.put(esMergeColumnName, mergeList);
            }
        }
    }

    private static void setLonAndlatValue(EsMergeColumn.Column esMergeColumn, Map<String, Object> mergeMap, String columnName, Object columnValue) {
        columnValue = columnValue == null ? "0.0" : columnValue;
        if (org.apache.commons.lang.StringUtils.isNotBlank(String.valueOf(columnValue))) {
            Double value = 0.0;
            try {
                value = Double.valueOf(String.valueOf(columnValue));
            } catch (Exception e) {
                logger.error("setLonAndlatValue :" + columnValue, e);
            }
            if (esMergeColumn.getLon().equals(columnName)) {
                if (value >= -180 && value <= 180) {
                    if (value == 0.0) {
                        mergeMap.put("lon", "0.0");
                    } else {
                        mergeMap.put("lon", value);
                    }
                } else {
                    mergeMap.put("lon", "0.0");
                }
            } else if (esMergeColumn.getLat().equals(columnName)) {
                if (value >= -90 && value <= 90) {
                    if (value == 0.0) {
                        mergeMap.put("lat", "0.0");
                    } else {
                        mergeMap.put("lat", value);
                    }
                } else {
                    mergeMap.put("lat", "0.0");
                }
            }
        } else {
            if (esMergeColumn.getLon().equals(columnName)) {
                mergeMap.put("lon", "0.0");
            } else if (esMergeColumn.getLat().equals(columnName)) {
                mergeMap.put("lat", "0.0");
            }
        }
    }


    /**
     * 对读出的结果进行转化
     *
     * @param column
     * @return
     */
    private static Object convert2EsColumnValue(Column column) {
        Object columnValue = column.getRawData();

        if (columnValue != null && column.getType() == Column.Type.DATE) {
            Date date = new Date((Long) columnValue);
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        }

        return columnValue;

    }

    /**
     * es的column名称 配置结果为tableName|columnName
     *
     * @param table
     * @param columnName
     * @param isAddTablePrefix false:不加表前缀  true:加表前缀
     * @return
     */
    private static String convert2EsColumnName(String table, String columnName, Boolean isAddTablePrefix) {
        if (isAddTablePrefix) {
            return MessageFormat.format("{0}|{1}", table, columnName);
        } else {
            return columnName;
        }
    }

    /**
     * 初始化合并字段
     *
     * @param originalConfig
     */
    public static void initMergeColumn(Configuration originalConfig) {
        String column = originalConfig.getNecessaryValue(Key.COLUMN, EsWriterErrorCode.REQUIRED_VALUE);
        List<Map> mergeColumnList = originalConfig.getList(Key.ES_MERGE_COLUMN, Map.class);
        esMergeColumn.init(mergeColumnList, column);
    }


    public static void main(String[] arg) {
        Configuration configuration = Configuration.from(EsHelper.class.getClassLoader().getResourceAsStream("all.json"));
        List<Object> list = (List<Object>) configuration.get("job.content[0].reader.parameter.column");
        System.out.println(list.size());
    }
}
