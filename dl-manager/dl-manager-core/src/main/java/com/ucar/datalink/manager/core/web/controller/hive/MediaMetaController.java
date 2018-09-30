package com.ucar.datalink.manager.core.web.controller.hive;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yang.wang09 on 2018-07-16 11:33.
 */
@Controller
@RequestMapping(value = "/hive/")
@LoginIgnore
public class MediaMetaController {

    private static final Logger logger = LoggerFactory.getLogger(MediaMetaController.class);

    private static final String MYSQL = MediaSourceType.MYSQL.name();

    private static final String SQLSERVER = MediaSourceType.SQLSERVER.name();

    private static final String POSTGRESQL = MediaSourceType.POSTGRESQL.name();

    private static final String HBASE = MediaSourceType.HBASE.name();

    private static final String SUCCESS = "SUCCESS";


    @Autowired
    MediaService mediaService;

    @RequestMapping(value = "/dbs")
    @ResponseBody
    public Object allDataBase(@RequestParam("DB_TYPE") String type) {
        try {
            if(StringUtils.isBlank(type) ) {
                return assembleError("db type is empty");
            }

            List<MediaSourceInfo> list = null;
            List<String> dbNames = new ArrayList<>();
            if( MYSQL.equalsIgnoreCase(type) ) {
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.MYSQL);
            }
            else if( SQLSERVER.equalsIgnoreCase(type) ) {
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.SQLSERVER);
            }
            else if( POSTGRESQL.equalsIgnoreCase(type)) {
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.POSTGRESQL);
            }
            else if( HBASE.equalsIgnoreCase(type)) {
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.HBASE);
            }
            else {
                //不支持，抛错
                return assembleError("unknown db type "+type);
            }

            for(MediaSourceInfo mediaSourceInfo : list) {
                dbNames.add(mediaSourceInfo.getName() );
            }
            return assembleDBs(dbNames.toArray(new String[]{}),SUCCESS);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleError(e.getMessage());
        }
    }


    @RequestMapping(value = "/tables")
    @ResponseBody
    public Object getAllTables(@RequestParam("DB_TYPE") String type, @RequestParam("DB_NAME") String name) {
        try {
            if(StringUtils.isBlank(type) ) {
                return assembleError("db type is empty");
            }
            if(StringUtils.isBlank(name)) {
                return assembleError("db name is empty");
            }

            List<MediaSourceInfo> list = null;
            List<String> dbNames = new ArrayList<>();
            if( MYSQL.equalsIgnoreCase(type) ) {
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.MYSQL);
            }
            else if( SQLSERVER.equalsIgnoreCase(type) ) {
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.SQLSERVER);
            }
            else if( POSTGRESQL.equalsIgnoreCase(type)) {
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.POSTGRESQL);
            }
            else if( HBASE.equalsIgnoreCase(type)) {
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.HBASE);
            }
            else {
                //不支持，抛错
                return assembleError("unknown db type "+type);
            }

            boolean isFindDBName = false;
            MediaSourceInfo mediaSourceInfo = null;
            for(MediaSourceInfo info : list) {
                if( info.getName().equalsIgnoreCase(name) ) {
                    mediaSourceInfo = info;
                    isFindDBName = true;
                    break;
                }
            }
            if( !isFindDBName ) {
                //传入的db name在数据库中没找到，抛错
                return assembleError("not found db name");
            }

            List<MediaMeta> metas = null;
            if( mediaSourceInfo.getType()==MediaSourceType.MYSQL || mediaSourceInfo.getType()==MediaSourceType.SQLSERVER || mediaSourceInfo.getType()==MediaSourceType.POSTGRESQL) {
                metas = RDBMSUtil.getTables(mediaSourceInfo);
            }
            else if( mediaSourceInfo.getType()==MediaSourceType.HBASE ) {
                metas = HBaseUtil.getTables(mediaSourceInfo);
            }

            else {
                //不支持
                throw new UnsupportedOperationException("db type unsupport "+mediaSourceInfo.getType());
            }

            logger.debug(metas.toString());
            String[] tableNames = null;
            if(metas!=null && metas.size()>0) {
                tableNames = new String[metas.size()];
                for(int i=0;i< metas.size();i++) {
                    tableNames[i] = metas.get(i).getName();
                }
            }
            return assembleTables(tableNames,SUCCESS);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleError(e.getMessage());
        }
    }



    @RequestMapping(value = "/columns")
    @ResponseBody
    public Object allColumns(@RequestParam("DB_TYPE") String type, @RequestParam("DB_NAME") String name,
    @RequestParam("TABLE_NAME") String tname) {
        try {
            if(StringUtils.isBlank(type) ) {
                return assembleError("db type empty");
            }
            if(StringUtils.isBlank(name)) {
                return assembleError("db name is empty");
            }
            if(StringUtils.isBlank(tname)) {
                return assembleError("table name is empty");
            }

            List<MediaSourceInfo> list = null;
            if( MYSQL.equalsIgnoreCase(type) ) {
                //执行mysql语句
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.MYSQL);
            }
            else if( SQLSERVER.equalsIgnoreCase(type) ) {
                //执行sqlserver语句
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.SQLSERVER);
            }
            else if( HBASE.equalsIgnoreCase(type)) {
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.HBASE);
            }
            else {
                //不支持，抛错
                return assembleError("unsupport db type");
            }
            boolean isFindDBName = false;
            MediaSourceInfo mediaSourceInfo = null;
            for(MediaSourceInfo info : list) {
                if( info.getName().equalsIgnoreCase(name) ) {
                    mediaSourceInfo = info;
                    isFindDBName = true;
                    break;
                }
            }
            if( !isFindDBName ) {
                //传入的db name在数据库中没找到，抛错
                return assembleError("not found db name");
            }

            MediaMeta mediaMeta = new MediaMeta();
            List<ColumnMeta> columns = null;
            if(mediaSourceInfo.getType()==MediaSourceType.MYSQL) {
                columns = RDBMSUtil.getColumns(mediaSourceInfo, tname);
                mediaMeta.setDbType(MediaSourceType.MYSQL);
            }
            else if( mediaSourceInfo.getType()==MediaSourceType.SQLSERVER) {
                columns = RDBMSUtil.getColumns(mediaSourceInfo, tname);
                mediaMeta.setDbType(MediaSourceType.SQLSERVER);
            }
            else if(mediaSourceInfo.getType()==MediaSourceType.POSTGRESQL) {
                columns = RDBMSUtil.getColumns(mediaSourceInfo, tname);
                mediaMeta.setDbType(MediaSourceType.POSTGRESQL);
            }
            else if(mediaSourceInfo.getType()==MediaSourceType.HBASE) {
                columns = HBaseUtil.getColumns(mediaSourceInfo, tname);
                mediaMeta.setDbType(MediaSourceType.HBASE);
            }
            else {
                //不支持
                throw new UnsupportedOperationException("db type unsupport "+mediaSourceInfo.getType());
            }


            mediaMeta.setColumn(columns);
            mediaMeta.setName(name);
            MediaMeta toHDFSMediaMeta = MetaMapping.transformToHDFS(mediaMeta);
            List<ColumnMeta> hdfsColumnMeta = toHDFSMediaMeta.getColumn();

            List<TableInfo> tableInfoList = new ArrayList<>();
            for(int i=0;i<columns.size();i++) {
                TableInfo info = new TableInfo();
                if(mediaSourceInfo.getType() == MediaSourceType.HBASE) {
                    info.setHbaseFamily(columns.get(i).getColumnFamily());
                }
                if(columns.get(i).isPrimaryKey()) {
                    info.setIsPrimaryKey("true");
                }
                info.setName(columns.get(i).getName());
                info.setType(columns.get(i).getType());
                info.setLength(columns.get(i).getLength());
                info.setDecimalDigits(columns.get(i).getDecimalDigits());
                info.setColumnDesc(columns.get(i).getColumnDesc());

                info.setHiveColumnName(hdfsColumnMeta.get(i).getName());
                info.setHiveColumnType(hdfsColumnMeta.get(i).getType());
                tableInfoList.add(info);
            }
            return assembleColumns(tableInfoList.toArray(new TableInfo[]{}),SUCCESS);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleError(e.getMessage());
        }
    }


    private static Object assembleError(String msg) {
        return assembleJsonArray(null,null,null,msg);
    }

    private static Object assembleDBs(String[] dbs, String msg) {
        return assembleJsonArray(dbs,null,null,msg);
    }

    private static Object assembleTables(String[] tables, String msg) {
        return assembleJsonArray(null,tables,null,msg);
    }

    private static Object assembleColumns(TableInfo[] tableInfos, String msg) {
        return assembleJsonArray(null,null,tableInfos,msg);
    }


    private static Object assembleJsonArray(String[] dbs, String[] tables, TableInfo[] columns,String msg) {
        ResponseInfo info = new ResponseInfo();
        if(dbs != null) {
            info.setDbs(dbs);
            info.setCount(dbs.length+"");
        }
        else if(tables != null) {
            info.setTables(tables);
            info.setCount(tables.length+"");
        }
        else if(columns != null) {
            info.setColumns(columns);
            info.setCount(columns.length+"");
        }
        else {
            info.setCount("-1");
        }
        info.setMsg(msg);
        String json = JSONObject.toJSONString(info);
        Object obj = JSONObject.parse(json);
        return obj;
    }

    private static class ResponseInfo {
        private String count;
        private String msg;
        private String[] dbs;
        private String[] tables;
        private TableInfo[] columns;

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String[] getDbs() {
            return dbs;
        }

        public void setDbs(String[] dbs) {
            this.dbs = dbs;
        }

        public String[] getTables() {
            return tables;
        }

        public void setTables(String[] tables) {
            this.tables = tables;
        }

        public TableInfo[] getColumns() {
            return columns;
        }

        public void setColumns(TableInfo[] columns) {
            this.columns = columns;
        }
    }

    private static class TableInfo {
        //列名
        private String name;
        //hbase的family
        private String hbaseFamily;
        //列类型
        private String type;
        //类长度
        private Integer length;
        //列信息
        private String columnDesc;
        //列精度
        private Integer decimalDigits;

        private String isPrimaryKey;

        private String hiveColumnName;
        private String hiveColumnType;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHbaseFamily() {
            return hbaseFamily;
        }

        public void setHbaseFamily(String hbaseFamily) {
            this.hbaseFamily = hbaseFamily;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public String getColumnDesc() {
            return columnDesc;
        }

        public void setColumnDesc(String columnDesc) {
            this.columnDesc = columnDesc;
        }

        public Integer getDecimalDigits() {
            return decimalDigits;
        }

        public void setDecimalDigits(Integer decimalDigits) {
            this.decimalDigits = decimalDigits;
        }

        public String getHiveColumnName() {
            return hiveColumnName;
        }

        public void setHiveColumnName(String hiveColumnName) {
            this.hiveColumnName = hiveColumnName;
        }

        public String getHiveColumnType() {
            return hiveColumnType;
        }

        public void setHiveColumnType(String hiveColumnType) {
            this.hiveColumnType = hiveColumnType;
        }

        public String getIsPrimaryKey() {
            return isPrimaryKey;
        }

        public void setIsPrimaryKey(String isPrimaryKey) {
            this.isPrimaryKey = isPrimaryKey;
        }
    }


}
