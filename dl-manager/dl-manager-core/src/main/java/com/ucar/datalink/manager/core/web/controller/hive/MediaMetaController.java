package com.ucar.datalink.manager.core.web.controller.hive;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.job.JobConfigBuilder;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.biz.module.JobExtendProperty;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.controller.job.JobConfigController;
import com.ucar.datalink.util.VirtualDataSourceUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.MessageDigest;
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

    private static final String KUDU = MediaSourceType.KUDU.name();

    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL = "FAIL";

    private static final String DSPIDER_PREFIX = "DSPIDER_";

    @Autowired
    MediaService mediaService;

    @Autowired
    JobService jobService;



    @RequestMapping(value = "/dbs")
    @ResponseBody
    public Object allDataBase(@RequestParam("DB_TYPE") String type) {
        try {
            if(StringUtils.isBlank(type) ) {
                return assembleError("db type is empty");
            }
            List<MediaSourceType> types = new ArrayList<>();
            if( MYSQL.equalsIgnoreCase(type) ) {
                types.add(MediaSourceType.MYSQL);
            }
            else if( SQLSERVER.equalsIgnoreCase(type) ) {
                types.add(MediaSourceType.SQLSERVER);
            }
            else if( POSTGRESQL.equalsIgnoreCase(type)) {
                types.add(MediaSourceType.POSTGRESQL);
            }
            else if( HBASE.equalsIgnoreCase(type)) {
                types.add(MediaSourceType.HBASE);
            }
            else if( KUDU.equalsIgnoreCase(type)) {
                types.add(MediaSourceType.KUDU);
            }
            else {
                //不支持，抛错
                return assembleError("unknown db type "+type);
            }

            List<MediaSourceInfo> list = VirtualDataSourceUtil.findMediaSourcesForSingleLab(types);

            DBInfo[] dbs = new DBInfo[list.size()];
            for(int i=0;i<list.size();i++) {
                DBInfo info = new DBInfo();
                info.setId(list.get(i).getId());
                info.setName(list.get(i).getName());
                dbs[i] = info;
            }
            return assembleDBs(dbs,SUCCESS);
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

            List<MediaSourceType> types = new ArrayList<>();
            if( MYSQL.equalsIgnoreCase(type) ) {
                types.add(MediaSourceType.MYSQL);
            }
            else if( SQLSERVER.equalsIgnoreCase(type) ) {
                types.add(MediaSourceType.SQLSERVER);
            }
            else if( POSTGRESQL.equalsIgnoreCase(type)) {
                types.add(MediaSourceType.POSTGRESQL);
            }
            else if( HBASE.equalsIgnoreCase(type)) {
                types.add(MediaSourceType.HBASE);
            }
            else {
                //不支持，抛错
                return assembleError("unknown db type "+type);
            }
            List<MediaSourceInfo> list = VirtualDataSourceUtil.findMediaSourcesForSingleLab(types);

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

            long virtualMediaSourceId = mediaSourceInfo.getId();
            mediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById( virtualMediaSourceId );
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

            List<MediaSourceType> types = new ArrayList<>();
            if( MYSQL.equalsIgnoreCase(type) ) {
                types.add(MediaSourceType.MYSQL);
            }
            else if( SQLSERVER.equalsIgnoreCase(type) ) {
                types.add(MediaSourceType.SQLSERVER);
            }
            else if( HBASE.equalsIgnoreCase(type)) {
                types.add(MediaSourceType.HBASE);
            }else {
                //不支持，抛错
                return assembleError("unsupport db type");
            }
            List<MediaSourceInfo> list = VirtualDataSourceUtil.findMediaSourcesForSingleLab(types);

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
            long virtualMediaSourceId = mediaSourceInfo.getId();
            mediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById( virtualMediaSourceId );

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
            }else {
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



    @RequestMapping(value = "/tableInfo")
    @ResponseBody
    public Object getTableMetaInfo(@RequestParam("DB_TYPE") String databaseType, @RequestParam("DB_NAME") String databaseName,
                                   @RequestParam("TABLE_NAME") String tableName) {
        try {
            MediaSourceInfo mediaSourceInfo = getMediaSourceInfo(databaseType, databaseName);
            MediaMeta mediaMeta = getMediaMeta(mediaSourceInfo, tableName);
            List<ColumnMetaInfo> columnMetaInfos = getTableInfos(mediaMeta, mediaSourceInfo);
            return assembleTableMetaInfoResponse(columnMetaInfos,null,SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return assembleTableMetaInfoResponse(null,e.getMessage(),FAIL);
        }
    }



    @RequestMapping(value = "/destTableInfo")
    @ResponseBody
    public Object getDestTableInfo(@RequestParam("DB_TYPE") String databaseType, @RequestParam("DB_NAME") String databaseName,
                                   @RequestParam("TABLE_NAME") String tableName, @RequestParam("DEST_DB_TYPE") String destDatabaseType) {
        try {
            if(StringUtils.isBlank(destDatabaseType) ) {
                throw new Exception("destDatabaseType type empty");
            }
            destDatabaseType = destDatabaseType.toUpperCase();
            if(!MediaSourceType.HDFS.name().equals(destDatabaseType) && !MediaSourceType.KUDU.name().equals(destDatabaseType)){
                throw new Exception(String.format("destDatabaseType[%s] type not support!",destDatabaseType));
            }

            MediaSourceInfo mediaSourceInfo = getMediaSourceInfo(databaseType, databaseName);
            MediaMeta mediaMeta = getMediaMeta(mediaSourceInfo, tableName);
            List<ColumnMetaInfo> columnMetaInfos = getTableInfos(mediaMeta, mediaSourceInfo);

            List<ColumnMeta> columnMeta = null;
            if(MediaSourceType.HDFS.name().equals(destDatabaseType)){
                MediaMeta toHDFSMediaMeta = MetaMapping.transformToHDFS(mediaMeta);
                columnMeta = toHDFSMediaMeta.getColumn();
            }else if(MediaSourceType.KUDU.name().equals(destDatabaseType)){
                MediaMeta kuduMiaMeta = MetaMapping.transformToKUDU(mediaMeta);
                columnMeta = kuduMiaMeta.getColumn();
            }

            if(columnMetaInfos==  null  || columnMeta == null || columnMeta.size() != columnMetaInfos.size()){
                throw new Exception("异常,请重试！");
            }
            for(int i = 0; i < columnMetaInfos.size(); i ++){
                columnMetaInfos.get(i).setName(columnMeta.get(i).getName());
                columnMetaInfos.get(i).setType(columnMeta.get(i).getType());
            }
            return assembleTableMetaInfoResponse(columnMetaInfos,null,SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return assembleTableMetaInfoResponse(null,e.getMessage(),FAIL);
        }

    }




    private MediaSourceInfo getMediaSourceInfo(String databaseType, String databaseName) throws Exception {

        List<MediaSourceType> types = new ArrayList<>();
        if( MYSQL.equalsIgnoreCase(databaseType) ) {
            types.add(MediaSourceType.MYSQL);
        }
        else if( SQLSERVER.equalsIgnoreCase(databaseType) ) {
            types.add(MediaSourceType.SQLSERVER);
        }
        else if( HBASE.equalsIgnoreCase(databaseType)) {
            types.add(MediaSourceType.HBASE);
        }else {
            throw new Exception("unsupport db type");
        }
        List<MediaSourceInfo> list = VirtualDataSourceUtil.findMediaSourcesForSingleLab(types);

        boolean isFindDBName = false;
        MediaSourceInfo mediaSourceInfo = null;
        for(MediaSourceInfo info : list) {
            if( info.getName().equalsIgnoreCase(databaseName) ) {
                mediaSourceInfo = info;
                isFindDBName = true;
                break;
            }
        }
        if( !isFindDBName ) {
            throw new Exception("not found db name");
        }
        long virtualMediaSourceId = mediaSourceInfo.getId();
        return VirtualDataSourceUtil.getRealMediaSourceInfoById( virtualMediaSourceId );
    }



    private MediaMeta  getMediaMeta(MediaSourceInfo mediaSourceInfo,String tableName) throws Exception {
        MediaMeta mediaMeta = new MediaMeta();
        List<ColumnMeta> columns = null;
        if(mediaSourceInfo.getType()==MediaSourceType.MYSQL) {
            columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
            mediaMeta.setDbType(MediaSourceType.MYSQL);
        }
        else if( mediaSourceInfo.getType()==MediaSourceType.SQLSERVER) {
            columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
            mediaMeta.setDbType(MediaSourceType.SQLSERVER);
        }
        else if(mediaSourceInfo.getType()==MediaSourceType.POSTGRESQL) {
            columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
            mediaMeta.setDbType(MediaSourceType.POSTGRESQL);
        }
        else if(mediaSourceInfo.getType()==MediaSourceType.HBASE) {
            columns = HBaseUtil.getColumns(mediaSourceInfo, tableName);
            mediaMeta.setDbType(MediaSourceType.HBASE);
        }else {
            //不支持
            throw new UnsupportedOperationException("db type unsupport "+mediaSourceInfo.getType());
        }
        mediaMeta.setColumn(columns);
        mediaMeta.setName(tableName);
        return mediaMeta;
    }

    private List<ColumnMetaInfo> getTableInfos(MediaMeta mediaMeta,MediaSourceInfo mediaSourceInfo) {
        List<ColumnMeta> columns = mediaMeta.getColumn();
        List<ColumnMetaInfo> columnMetaInfos = new ArrayList<>();
        for(int i=0;i<columns.size();i++) {
            ColumnMetaInfo info = new ColumnMetaInfo();
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
            columnMetaInfos.add(info);
        }
        return columnMetaInfos;
    }

    @RequestMapping(value = "/dbInfo")
    @ResponseBody
    public Object getAllTables(@RequestParam("DB_ID") String id) {
        try {
            if(StringUtils.isBlank(id) ) {
                return assembleError("db id empty");
            }
            MediaSourceInfo mediaSourceInfo = mediaService.getMediaSourceById(Long.parseLong(id));
            mediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(mediaSourceInfo.getId());
            String dbName = mediaSourceInfo.getName();
            String dbType = mediaSourceInfo.getType().name().toString();

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
            DBInfoTables dbInfo = new DBInfoTables();
            if(metas!=null && metas.size()>0) {
                tableNames = new String[metas.size()];
                for(int i=0;i< metas.size();i++) {
                    tableNames[i] = metas.get(i).getName();
                }
            } else {
                tableNames = new String[0];
            }
            dbInfo.setName(dbName);
            dbInfo.setType(dbType);
            dbInfo.setTables(tableNames);
            return assembleJsonArray(dbInfo);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleError(e.getMessage());
        }

    }







    @RequestMapping(value = "/jsonInfo")
    @ResponseBody
    public Object getJsonInfo(@RequestParam("CONFIG_ID") String id) {
        try {
            long configId = Long.parseLong(id);
            JobConfigInfo configInfo = jobService.getJobConfigById(configId);
            if(configInfo == null) {
                return assembleJsonArray(null,null,null,SUCCESS);
            }
            if( !isTimingJob(configInfo) ) {
                return assembleError("set timing job first in data-link console");
            }
            String json = configInfo.getJob_content();
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].writer.parameter");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("json", obj);
            jsonObject.put("msg",SUCCESS);
            jsonObject.put("count","1");
            return jsonObject;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleError(e.getMessage());
        }

    }


    @RequestMapping(value = "/create")
    @ResponseBody
    public Object create(@RequestBody Item item) {
        try {
            long srcId = Long.parseLong(item.getSrcId());
            long destId = Long.parseLong(item.getDestId());
            MediaSourceInfo srcInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(srcId);
            MediaSourceInfo destInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(destId);
            String jobName = item.getJobName();
            String json = null;
            if(jobName.contains(",")) {
                String[] arrNames = jobName.split(",");
                List<String> nameArray = new ArrayList<>();
                for(String s : arrNames) {
                    nameArray.add(s);
                }
                json = JobConfigBuilder.buildJson(srcInfo,destInfo,new JobExtendProperty(),nameArray);
                jobName = jobName.split(",")[0];
            }
            else {
                json = JobConfigBuilder.buildJson(srcInfo,jobName,destInfo,jobName,new JobExtendProperty());
            }
            if(StringUtils.isBlank(json)) {
                return assembleError("cannot gen json");
            }

            JobConfigInfo jobConfigInfo = new JobConfigInfo();
            String nameWithRand = DSPIDER_PREFIX +jobName +"_"+ JobConfigController.randomString(10);
            jobConfigInfo.setJob_name(nameWithRand);
            jobConfigInfo.setIs_delete(false);
            jobConfigInfo.setJob_content(json);
            jobConfigInfo.setJob_media_name(item.getJobName());
            jobConfigInfo.setJob_src_media_source_id(srcId);
            jobConfigInfo.setJob_target_media_source_id(destId);
            jobConfigInfo.setTiming_yn(true);
            jobService.createJobConfig(jobConfigInfo);
            JobConfigInfo newConfigInfo = jobService.lastJobConfigByName(item.getJobName());
            long newId = newConfigInfo.getId();
            ConfigInfo cInfo = new ConfigInfo();
            cInfo.setId(newId+"");
            cInfo.setMsg(SUCCESS);
            String resultJson = JSONObject.toJSONString(cInfo);
            Object obj = JSONObject.parse(resultJson);
            return obj;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleError(e.getMessage());
        }
    }



    @RequestMapping(value = "/md5")
    @ResponseBody
    public Object md5(@RequestParam(value = "CONFIG_ID", required = false) String configId,
                      @RequestParam(value = "EXECUTE_ID", required = false) String executionId) {
        try {
            if(StringUtils.isBlank(configId) && StringUtils.isBlank(executionId)) {
                return assembleError("config id, execute id is empty");
            }
            configId = parseId(configId);
            executionId = parseId(executionId);
            JobService service = DataLinkFactory.getObject(JobService.class);
            JobConfigInfo info = null;
            if(StringUtils.isNotBlank(executionId)) {
                JobExecutionInfo executionInfo = service.getJobExecutionById(Long.parseLong(executionId));
                long id = executionInfo.getJob_id();
                info = service.getJobConfigById(id);
            }
            if(StringUtils.isNotBlank(configId)) {
                info = service.getJobConfigById(Long.parseLong(configId));
            }
            if (info == null) {
                return assembleError("cannot get job config");
            }
            if( !isTimingJob(info) ) {
                return assembleError("set timing job first in data-link console");
            }
            long job_id = info.getId();
            //long src_media_id = info.getJob_src_media_source_id();
            //long target_media_id = info.getJob_target_media_source_id();
            String job_media_name = info.getJob_media_name();
            //String content = job_id + src_media_id + target_media_id + job_media_name;
            String content = job_id + job_media_name;
            String md5 = getMd5(content);
            SignalInfo signalInfo = new SignalInfo();
            signalInfo.setMsg(SUCCESS);
            signalInfo.setSignal(md5);
            String json = JSONObject.toJSONString(signalInfo);
            Object obj = JSONObject.parse(json);
            return obj;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleError(e.getMessage());
        }
    }



    private boolean isTimingJob(JobConfigInfo info) {
        return info.isTiming_yn();
    }


    private String parseId(String id) {
        if (StringUtils.isBlank(id)) {
            return id;
        }
        String env = getEnv();
        if (id.endsWith(env)) {
            id = id.substring(0, id.length() - env.length());
        }
        return id;
    }

    private String getEnv() {
        String currentEnv = ManagerConfig.current().getCurrentEnv();
        if (StringUtils.isBlank(currentEnv)) {
            return "_";
        } else {
            return "_" + currentEnv;
        }
    }

    /**
     * 用于获取一个String的md5值
     *
     * @param str
     * @return
     */
    public String getMd5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(str.getBytes());
            StringBuilder sb = new StringBuilder(40);
            for (byte x : bs) {
                if ((x & 0xff) >> 4 == 0) {
                    sb.append("0").append(Integer.toHexString(x & 0xff));
                } else {
                    sb.append(Integer.toHexString(x & 0xff));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }



    private static Object assembleError(String msg) {
        return assembleJsonArray(null,null,null,msg);
    }

    private static Object assembleDBs(DBInfo[] dbs, String msg) {
        return assembleJsonArray(dbs,null,null,msg);
    }

    private static Object assembleTables(String[] tables, String msg) {
        return assembleJsonArray(null,tables,null,msg);
    }

    private static Object assembleColumns(TableInfo[] tableInfos, String msg) {
        return assembleJsonArray(null,null,tableInfos,msg);
    }


    private static Object assembleTableMetaInfoResponse(List<ColumnMetaInfo> columnMetaInfos, String msg, String status) {
        TableMetaInfoResponse response = new TableMetaInfoResponse(columnMetaInfos,msg,status);
        String json = JSONObject.toJSONString(response);
        Object obj = JSONObject.parse(json);
        return obj;
    }



    private static class TableMetaInfoResponse{
        private String msg;
        private String status;
        private List<ColumnMetaInfo> columnMetaInfos;
        private int count;


        public TableMetaInfoResponse(List<ColumnMetaInfo> columnMetaInfos, String msg, String status) {
            if(columnMetaInfos != null){
                count = columnMetaInfos.size();
            }
            this.columnMetaInfos = columnMetaInfos;
            this.msg = msg;
            this.status = status;
        }


        public List<ColumnMetaInfo> getColumnMetaInfos() {
            return columnMetaInfos;
        }

        public void setColumnMetaInfos(List<ColumnMetaInfo> columnMetaInfos) {
            this.columnMetaInfos = columnMetaInfos;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }


    private static Object assembleJsonArray(DBInfo[] dbs, String[] tables, TableInfo[] columns,String msg) {
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


    private static Object assembleJsonArray(DBInfoTables infos) {
        ResponseInfo info = new ResponseInfo();
        info.setDbInfo(infos);
        if(infos.getTables().length > 0) {
            info.setCount(infos.getTables().length + "");
        } else {
            info.setCount("0");
        }
        info.setMsg(SUCCESS);
        String json = JSONObject.toJSONString(info);
        Object obj = JSONObject.parse(json);
        return obj;
    }



    private static class ResponseInfo {
        private String count;
        private String msg;
        private DBInfo[] dbs;
        private String[] tables;
        private TableInfo[] columns;
        private DBInfoTables dbInfo;

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

        public DBInfo[] getDbs() {
            return dbs;
        }

        public void setDbs(DBInfo[] dbs) {
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

        public DBInfoTables getDbInfo() {
            return dbInfo;
        }

        public void setDbInfo(DBInfoTables dbInfo) {
            this.dbInfo = dbInfo;
        }


    }


    private static class DBInfo {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    private static class ColumnMetaInfo{
        //列名
        private String name;
        //列类型
        private String type;
        //类长度
        private Integer length;
        //列信息
        private String columnDesc;
        //列精度
        private Integer decimalDigits;
        private String isPrimaryKey;
        //hbase的family
        private String hbaseFamily;

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

        public String getIsPrimaryKey() {
            return isPrimaryKey;
        }

        public void setIsPrimaryKey(String isPrimaryKey) {
            this.isPrimaryKey = isPrimaryKey;
        }

        public String getHbaseFamily() {
            return hbaseFamily;
        }

        public void setHbaseFamily(String hbaseFamily) {
            this.hbaseFamily = hbaseFamily;
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


    private static class DBInfoTables {
        private String name;
        private String type;
        private String[] tables;

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

        public String[] getTables() {
            return tables;
        }

        public void setTables(String[] tables) {
            this.tables = tables;
        }
    }


    private static class SignalInfo {
        private String msg;
        private String signal;

        public String getSignal() {
            return signal;
        }

        public void setSignal(String signal) {
            this.signal = signal;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }


    private static class Item {
        private String srcId;
        private String destId;
        private String jobName;

        public String getSrcId() {
            return srcId;
        }

        public void setSrcId(String srcId) {
            this.srcId = srcId;
        }

        public String getDestId() {
            return destId;
        }

        public void setDestId(String destId) {
            this.destId = destId;
        }

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }
    }

    private static class ConfigInfo {
        private String msg;
        private String id;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private static class KeyPair {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


}
