package com.ucar.datalink.flinker.plugin.writer.eswriter;

import com.ucar.datalink.flinker.api.exception.DataXException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by csf on 16/12/27.
 */

public class EsMergeColumn {

    private static final Logger logger = LoggerFactory.getLogger(EsMergeColumn.class);

    private List<Column> allMergeColumn = new ArrayList<Column>();

    private List<String> allMergeColumnName = new ArrayList<String>();

    private  boolean isMergeColumn = false;

    public List<Column> getAllMergeColumn() {
        return allMergeColumn;
    }

    public void setAllMergeColumn(List<Column> allMergeColumn) {
        this.allMergeColumn = allMergeColumn;
    }

    public List<String> getAllMergeColumnName() {
        return allMergeColumnName;
    }

    public void setAllMergeColumnName(List<String> allMergeColumnName) {
        this.allMergeColumnName = allMergeColumnName;
    }

    public boolean isMergeColumn() {
        return isMergeColumn;
    }

    public void setIsMergeColumn(boolean isMergeColumn) {
        this.isMergeColumn = isMergeColumn;
    }

    public void init(List<Map> mergeColumnList,String column){
        if(mergeColumnList!=null && mergeColumnList.size()>0){
            for(Map mergeColumn : mergeColumnList){
                Column innerColumn = buildColumn(mergeColumn);
                innerColumn.check(column);
                allMergeColumn.add(innerColumn);
            }
            isMergeColumn = true;
        }
        printLog();
    }

    private Column buildColumn(Map mergeColumn){
        Column innerColumn = new Column();
        innerColumn.setLat((String)mergeColumn.get("lat"));
        innerColumn.setLon((String)mergeColumn.get("lon"));
        innerColumn.setMergeType((String)mergeColumn.get("mergeType"));
        innerColumn.setMergeColumnName((String)mergeColumn.get("mergeColumnName"));
        String columnNames = (String)mergeColumn.get("columnName");
        if(StringUtils.isNotBlank(columnNames)){
            innerColumn.setColumnNameList(Arrays.asList(columnNames.split(",")));
            allMergeColumnName.addAll(Arrays.asList(columnNames.split(",")));
        }
        if(StringUtils.isNotBlank((String)mergeColumn.get("lat"))){
            allMergeColumnName.add((String)mergeColumn.get("lat"));
            innerColumn.getColumnNameList().add((String)mergeColumn.get("lat"));
        }
        if(StringUtils.isNotBlank((String)mergeColumn.get("lon"))){
            allMergeColumnName.add((String)mergeColumn.get("lon"));
            innerColumn.getColumnNameList().add((String)mergeColumn.get("lon"));
        }
        return innerColumn;
    }

    private  void printLog(){
        logger.info("es writer init merge column :"+isMergeColumn);
        if(isMergeColumn){
            if(allMergeColumn.size()>0){
                logger.info("-------init merge column start--------");
                for(Column esMergeColumn : allMergeColumn){
                    logger.info("-------merge type-------"+esMergeColumn.getMergeType());
                    logger.info("-------merge colmun name-------"+esMergeColumn.getMergeColumnName());
                    if(esMergeColumn.getColumnNameList() != null && esMergeColumn.getColumnNameList().size() > 0){
                        for(String str : esMergeColumn.getColumnNameList()){
                            logger.info("-------colmun name-------"+str);
                        }
                    }
                }
                logger.info("-------init merge column end--------");
            }
        }
    }
     class Column {
        /**
         * 纬度
         */
        private String lat;
        /**
         * 经度
         */
        private String lon;
        /**
         * 合并类型
         * 1:经纬度合并
         * 2:其他
         */
        private String mergeType;
        /**
         * 合并后的字段名称
         */
        private String mergeColumnName;
        /**
         * 需要合并的字段名称
         */
        private List<String> columnNameList = new ArrayList<String>();

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLon() {
            return lon;
        }

        public void setLon(String lon) {
            this.lon = lon;
        }

        public String getMergeType() {
            return mergeType;
        }

        public void setMergeType(String mergeType) {
            this.mergeType = mergeType;
        }

         public List<String> getColumnNameList() {
             return columnNameList;
         }

         public void setColumnNameList(List<String> columnNameList) {
             this.columnNameList = columnNameList;
         }

         public String getMergeColumnName() {
            return mergeColumnName;
        }

        public void setMergeColumnName(String mergeColumnName) {
            this.mergeColumnName = mergeColumnName;
        }

        public void check(String columnNames) {

            if (StringUtils.isBlank(mergeType)) {
                throw DataXException.asDataXException(EsWriterErrorCode.ILLEGAL_VALUE, "es merge type is empty " + mergeType);
            }

            if (StringUtils.isBlank(mergeColumnName)) {
                throw DataXException.asDataXException(EsWriterErrorCode.ILLEGAL_VALUE, "es merge column name is empty " + mergeColumnName);
            }

            if (columnNameList == null || columnNameList.size()==0) {
                throw DataXException.asDataXException(EsWriterErrorCode.ILLEGAL_VALUE, "es column name empty " + columnNameList);
            }

            if(Key.ES_LON_LAT_MERGE_TYPE.equals(mergeType)){
                if (StringUtils.isBlank(lat) || StringUtils.isBlank(lon)) {
                    throw DataXException.asDataXException(EsWriterErrorCode.ILLEGAL_VALUE, "es lat or lon empty " + lat + "|" + lon);
                }
            }else if(Key.ES_OTHER_MERGE_TYPE.equals(mergeType)){
                columnNames = columnNames.substring(1, columnNames.length() - 1).replaceAll("\"", "");
                String[] columnArray = columnNames.split(",");
                int flag = 0;
                for (Object cName : columnArray) {
                    if (columnNameList.contains(cName)) {
                        flag++;
                    }
                }

                if (flag == columnNameList.size()) {
                    logger.info("es merge column init complete:[" + columnNameList + "]");
                } else {
                    throw DataXException.asDataXException(EsWriterErrorCode.CONFIG_INVALID_EXCEPTION, "es merge column init is error column :[" + columnNameList + "]" + ";flag:" + flag);
                }
            }else{
                throw DataXException.asDataXException(EsWriterErrorCode.ILLEGAL_VALUE, "es merge type is not exist " + mergeType);
            }

        }
    }
}