package com.ucar.datalink.manager.core.web.controller.meta;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.meta.MetaManager;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.domain.media.MediaInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.ModeUtils;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.domain.vo.ResponseVo;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.utils.TableModeUtil;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.util.AES;
import com.ucar.datalink.util.VirtualDataSourceUtil;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/metaData")
@LoginIgnore
public class MetaDataController {
    private static final Logger logger = LoggerFactory.getLogger(MetaDataController.class);

    private static final SimpleDateFormat year_sdf = new SimpleDateFormat("yyyy");

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

    private static final String MYSQL = MediaSourceType.MYSQL.name();

    private static final String SQLSERVER = MediaSourceType.SQLSERVER.name();

    private static final String POSTGRESQL = MediaSourceType.POSTGRESQL.name();

    private static final String HBASE = MediaSourceType.HBASE.name();

    private static final String ORACLE = MediaSourceType.ORACLE.name();

    private static final String HDFS = MediaSourceType.HDFS.name();

    private static final String SUCCESS = "SUCCESS";

    private static final String DSPIDER_PREFIX = "DSPIDER_";


    @Autowired
    private MediaService mediaService;
    @Autowired
    private MediaSourceService mediaSourceService;

    @RequestMapping(value = "/dbs")
    @ResponseBody
    public Object allDataBase(@RequestParam("dbType") String dbType) {
        ResponseVo responseVo = new ResponseVo();
        try {
            if(StringUtils.isBlank(dbType) ) {
                throw new ErrorException(CodeContext.DBTYPE_NOTNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.DBTYPE_NOTNULL_ERROR_CODE));
            }

            boolean flag = false;
            List<MediaSourceType> searchTypes = new ArrayList<>();
            List<MediaSourceType> types = MediaSourceType.getAllMediaSourceTypesForBidData();
            for(MediaSourceType mediaSourcType : types) {
                if(mediaSourcType.name().equalsIgnoreCase(dbType)) {
                    flag = true;
                    searchTypes.add(mediaSourcType);
                    break;
                }
            }
            if(!flag){
                //不支持，抛错
                throw new ErrorException(CodeContext.NOTSUPPORT_DATABASE_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.NOTSUPPORT_DATABASE_ERROR_CODE));
            }
            List<MediaSourceInfo> list = VirtualDataSourceUtil.findMediaSourcesForSingleLab(searchTypes);

            MetaDataController.DBInfo[] dbs = new MetaDataController.DBInfo[list.size()];
            for(int i=0;i<list.size();i++) {
                MetaDataController.DBInfo info = new MetaDataController.DBInfo();
                info.setId(list.get(i).getId());
                info.setName(list.get(i).getName());
                dbs[i] = info;
            }
            responseVo.getData().put("dbs",dbs);
        } catch (ErrorException e) {
            logger.error("查询数据库出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }


    @RequestMapping(value = "/tables")
    @ResponseBody
    public Object getAllTables(@RequestParam("dbType") String dbType, @RequestParam("dbName") String dbName,String isOnlyWildcard,String wildcardTable) {
        logger.info(String.format("Receive a data request: \r\n dbType is %s ,\r\n dbName is %s,\r\n isOnlyWildcard is %s,\r\n wildcardTable is %s",
                dbType,dbName,isOnlyWildcard,wildcardTable));
        ResponseVo responseVo = new ResponseVo();
        try {
            if(StringUtils.isBlank(dbType) ) {
                throw new ErrorException(CodeContext.DBTYPE_NOTNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.DBTYPE_NOTNULL_ERROR_CODE));
            }
            if(StringUtils.isBlank(dbName)) {
                throw new ErrorException(CodeContext.DBNAME_NOTNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.DBNAME_NOTNULL_ERROR_CODE));
            }
            MediaSourceInfo mediaSourceInfo = getMediaSourceInfo(dbName);
            List<MediaMeta> metas = MetaManager.getTables(mediaSourceInfo);
            if(mediaSourceInfo.getType() == MediaSourceType.HDFS || mediaSourceInfo.getType() == MediaSourceType.ELASTICSEARCH
                    || mediaSourceInfo.getType() == MediaSourceType.KUDU) {
                for (MediaMeta mediaMeta : metas) {
                    mediaMeta.setName(mediaMeta.getNameSpace()+"."+mediaMeta.getName());
                }
            }
            logger.debug(metas.toString());
            String[] tableNames = null;
            if(metas!=null && metas.size()>0) {
                tableNames = new String[metas.size()];
                for(int i=0;i< metas.size();i++) {
                    tableNames[i] = metas.get(i).getName();
                }
            }
            List<String> tableNameList = Arrays.asList(tableNames);
            Set<String> set = Sets.newLinkedHashSet();
            tableNameList.stream().forEach(t -> set.add(t));
            tableNameList.stream().forEach(t -> {
                //必须先判断monthly，再判断yearly
                String result = ModeUtils.tryBuildMonthlyPattern(t);
                if (ModeUtils.isMonthlyPattern(result)) {
                    set.add(result);
                } else {
                    result = ModeUtils.tryBuildYearlyPattern(t);
                    if (ModeUtils.isYearlyPattern(result)) {
                        set.add(result);
                    }
                }
            });

            tableNameList = set.stream().map(i -> i).collect(Collectors.toList());
            //处理分表
            TableModeUtil.doTableModel(tableNameList);

            //过滤处理，只返回带通配符的表
            if(!StringUtils.isEmpty(isOnlyWildcard) && "1".equals(isOnlyWildcard)) {
                Iterator<String> it = tableNameList.iterator();
                PatternMatcher matcher = new Perl5Matcher();
                while (it.hasNext()) {
                    String filterTable = it.next();
                    String result = ModeUtils.tryBuildMonthlyPattern(filterTable);
                    if (ModeUtils.isMonthlyPattern(result)&&!ModeUtils.isMonthlyPattern(filterTable)) {
                        it.remove();
                        continue;
                    }
                    result = ModeUtils.tryBuildYearlyPattern(filterTable);
                    if (ModeUtils.isYearlyPattern(result)&&!ModeUtils.isYearlyPattern(filterTable)) {
                        it.remove();
                        continue;
                    }
                    if(TableModeUtil.isWildTable(matcher,filterTable)){
                        it.remove();
                        continue;
                    }
                }
            }else{
                //返回通配符下的表
                if(!StringUtils.isEmpty(wildcardTable) && mediaSourceInfo.getType() == MediaSourceType.HBASE) {
                    String tablePrefix = "";
                    if(ModeUtils.isYearlyPattern(wildcardTable)){
                        tablePrefix = wildcardTable.substring(0,wildcardTable.length()-7);
                    }else if(ModeUtils.isMonthlyPattern(wildcardTable)){
                        tablePrefix = wildcardTable.substring(0,wildcardTable.length()-9);
                    }
                    if(!StringUtils.isEmpty(tablePrefix)) {
                        Iterator<String> it = tableNameList.iterator();
                        while (it.hasNext()) {
                            String filterTable = it.next();
                            if(ModeUtils.isNumber(filterTable.replace(tablePrefix,""))){
                                continue;
                            }
                            it.remove();
                        }
                    }
                }
            }

            responseVo.getData().put("tables",tableNameList);
        } catch (ErrorException e) {
            logger.error("查询表出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @RequestMapping(value = "/tableInfo")
    @ResponseBody
    public Object getTableMetaInfo(@RequestParam("dbType") String dbType, @RequestParam("dbName") String dbName,
                                   @RequestParam("tableName") String tableName) {
        ResponseVo responseVo = new ResponseVo();
        try {
            if(StringUtils.isEmpty(dbName)) {
                throw new ErrorException(CodeContext.DBNAME_ISNULL_ERROR_CODE);
            }
            if(StringUtils.isEmpty(tableName)) {
                throw new ErrorException(CodeContext.TABLE_NOTNULL_ERROR_CODE);
            }
            Date date = new Date();
            String dateMonthStr = sdf.format(date);
            String dateYearStr = year_sdf.format(date);
            MediaInfo.ModeValue modeValue = ModeUtils.parseMode(tableName);
            if(modeValue.getMode().isMulti()) {
                tableName = modeValue.getMultiValue().get(0);
            }else if(modeValue.getMode().isMonthly()) {
                tableName = tableName.substring(0,tableName.length()-9)+dateMonthStr;
            }else if(modeValue.getMode().isYearly()) {
                tableName = tableName.substring(0,tableName.length()-7)+dateYearStr;
            }
            MediaSourceInfo mediaSourceInfo = getMediaSourceInfo(dbName);
            MediaMeta mediaMeta = new MediaMeta();
            List<ColumnMeta> list = MetaManager.getColumns(mediaSourceInfo, tableName);
            if(mediaSourceInfo.getType() == MediaSourceType.HBASE) {
                if(list == null) {
                    list = new ArrayList<>();
                }
                boolean isHaveRowkey = false;
                for (ColumnMeta columnMeta:list) {
                    if("rowkey".equals(columnMeta.getName())) {
                        isHaveRowkey = true;
                        break;
                    }
                }
                if(!isHaveRowkey) {
                    ColumnMeta columnMeta = new ColumnMeta();
                    columnMeta.setName("rowkey");
                    columnMeta.setType("Bytes");
                    columnMeta.setIsPrimaryKey(true);
                    list.add(0,columnMeta);
                }
            }
            mediaMeta.setColumn(list);
            mediaMeta.setDbType(mediaSourceInfo.getType());
            mediaMeta.setName(tableName);
            List<MetaManager.ColumnMetaInfo> columnMetaInfos = MetaManager.getTableInfos(mediaMeta, mediaSourceInfo);
            responseVo.getData().put("columnMetaInfos",columnMetaInfos);
        } catch (ErrorException e) {
            logger.error("查询表信息出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @RequestMapping(value = "/destTableInfo")
    @ResponseBody
    public Object getDestTableInfo(@RequestParam("dbType") String dbType, @RequestParam("dbName") String dbName,
                                   @RequestParam("tableName") String tableName, @RequestParam("destDbType") String destDatabaseType) {
        ResponseVo responseVo = new ResponseVo();
        try {
            if(StringUtils.isBlank(destDatabaseType) ) {
                throw new ErrorException(CodeContext.DBTYPE_NOTNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.DBTYPE_NOTNULL_ERROR_CODE));
            }
            destDatabaseType = destDatabaseType.toUpperCase();
            if(!MediaSourceType.HDFS.name().equals(destDatabaseType) && !MediaSourceType.KUDU.name().equals(destDatabaseType)){
                throw new ErrorException(CodeContext.NOTSUPPORT_DATABASE_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.NOTSUPPORT_DATABASE_ERROR_CODE));
            }

            Date date = new Date();
            String dateMonthStr = sdf.format(date);
            String dateYearStr = year_sdf.format(date);
            MediaInfo.ModeValue modeValue = ModeUtils.parseMode(tableName);
            if(modeValue.getMode().isMulti()) {
                tableName = modeValue.getMultiValue().get(0);
            }else if(modeValue.getMode().isMonthly()) {
                tableName = tableName.substring(0,tableName.length()-9)+dateMonthStr;
            }else if(modeValue.getMode().isYearly()) {
                tableName = tableName.substring(0,tableName.length()-7)+dateYearStr;
            }

            MediaSourceInfo mediaSourceInfo = getMediaSourceInfo(dbName);
            MediaMeta mediaMeta = MetaManager.getMediaMeta(mediaSourceInfo, tableName);
            if(mediaSourceInfo.getType() == MediaSourceType.HBASE) {
                List<ColumnMeta> list = mediaMeta.getColumn();
                if(list == null) {
                    list = new ArrayList<>();
                }
                boolean isHaveRowkey = false;
                for (ColumnMeta columnMeta:list) {
                    if("rowkey".equals(columnMeta.getName())) {
                        isHaveRowkey = true;
                        break;
                    }
                }
                if(!isHaveRowkey) {
                    ColumnMeta columnMeta = new ColumnMeta();
                    columnMeta.setName("rowkey");
                    columnMeta.setType("Bytes");
                    columnMeta.setIsPrimaryKey(true);
                    list.add(0,columnMeta);
                }
            }
            List<MetaManager.ColumnMetaInfo> columnMetaInfos = MetaManager.getTableInfos(mediaMeta, mediaSourceInfo);

            List<ColumnMeta> columnMeta = null;
            if(MediaSourceType.HDFS.name().equalsIgnoreCase(destDatabaseType)){
                MediaMeta toHDFSMediaMeta = MetaMapping.transformToHDFS(mediaMeta);
                columnMeta = toHDFSMediaMeta.getColumn();
            }else if(MediaSourceType.KUDU.name().equalsIgnoreCase(destDatabaseType)){
                MediaMeta kuduMiaMeta = MetaMapping.transformToKUDU(mediaMeta);
                columnMeta = kuduMiaMeta.getColumn();
            }

            if(columnMetaInfos==  null  || columnMeta == null || columnMeta.size() != columnMetaInfos.size()){
                throw new ErrorException(CodeContext.EXCEPTION_TRYAGAIN_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.EXCEPTION_TRYAGAIN_ERROR_CODE));
            }
            for(int i = 0; i < columnMetaInfos.size(); i ++){
                columnMetaInfos.get(i).setName(columnMeta.get(i).getName());
                columnMetaInfos.get(i).setType(columnMeta.get(i).getType());
            }
            responseVo.getData().put("columnMetaInfos",columnMetaInfos);
        } catch (ErrorException e) {
            logger.error("查询表信息出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @RequestMapping(value = "/dbInfo")
    @ResponseBody
    public Object getAllTables(@RequestParam("dbId") String dbId) {
        ResponseVo responseVo = new ResponseVo();
        try {
            if(StringUtils.isBlank(dbId) ) {
                throw new ErrorException(CodeContext.DBID_NOTNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.DBID_NOTNULL_ERROR_CODE));
            }
            MediaSourceInfo mediaSourceInfo = mediaService.getMediaSourceById(Long.parseLong(dbId));
            mediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(mediaSourceInfo.getId());
            String dbName = mediaSourceInfo.getName();
            String dbType = mediaSourceInfo.getType().name().toString();

            List<MediaMeta> metas = MetaManager.getTables(mediaSourceInfo);

            logger.debug(metas.toString());
            String[] tableNames = null;
            MetaDataController.DBInfoTables dbInfo = new MetaDataController.DBInfoTables();
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
            responseVo.getData().put("dbInfos",dbInfo);
        }  catch (ErrorException e) {
            logger.error("查询表信息出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @ResponseBody
    @RequestMapping(value = "/dbDetail")
   public Object dbDetail(String mediaSourceId,String dbType,String dbName) {
        ResponseVo responseVo = new ResponseVo();
        try {
            logger.info("收到的请求参数: mediaSourceId="+mediaSourceId+",dbType="+dbType+",dbName="+dbName);
            if(StringUtils.isEmpty(mediaSourceId)&&
                    StringUtils.isEmpty(dbType)&& StringUtils.isEmpty(dbName)) {
                throw new ErrorException(CodeContext.PARAMETER_NONULL_ERROR_CODE);
            }

            MediaSourceInfo mediaSourceInfo = null;
            if(!StringUtils.isEmpty(mediaSourceId)) {
                mediaSourceInfo = mediaService.getMediaSourceById(Long.valueOf(mediaSourceId));
            }else {
                List<MediaSourceInfo>  mediaSourceInfoList = mediaSourceService.getMediaSourceLikeSchema(dbName);
                //没有查询出，或者查询出多个抛异常
                if(mediaSourceInfoList == null || mediaSourceInfoList.size()<1) {
                    throw new ErrorException(CodeContext.DATABASE_NONFIND_ERROR_CODE);
                }
                if(mediaSourceInfoList.size()>1) {
                    throw new ErrorException(CodeContext.DATABASE_MOREONE_ERROR_CODE);
                }
                mediaSourceInfo = mediaSourceInfoList.get(0);
            }

            //判断是否是虚拟爱数据源，如果是虚拟数据源则查找当前主机房的数据源
            MediaSourceInfo realMediaSourceInfo = mediaService.getRealDataSource(mediaSourceInfo);
            RdbMediaSrcParameter rdbMediaSrcParameter = realMediaSourceInfo.getParameterObj();

            Integer port = rdbMediaSrcParameter.getPort();

            RdbMediaSrcParameter.WriteConfig writeConfig = rdbMediaSrcParameter.getWriteConfig();
            writeConfig.setWriteHost(writeConfig.getWriteHost()+":"+port);
            writeConfig.setPassword(AES.encrypt(writeConfig.getDecryptPassword(), ManagerConfig.current().getPasswordAesKey()));

            RdbMediaSrcParameter.ReadConfig readConfig = rdbMediaSrcParameter.getReadConfig();
            readConfig.setPassword(AES.encrypt(readConfig.getDecryptPassword(),ManagerConfig.current().getPasswordAesKey()));
            List<String> readIpList =readConfig.getHosts();
            List<String> newReadIpList = new ArrayList<>();
            for (String ip : readIpList) {
                newReadIpList.add(ip+":"+port);
            }
            readConfig.setHosts(newReadIpList);
            readConfig.setEtlHost(readConfig.getEtlHost()+":"+port);

            responseVo.getData().put("encoding",rdbMediaSrcParameter.getEncoding());
            responseVo.getData().put("isTIDB",rdbMediaSrcParameter.getIsTIDB());
            responseVo.getData().put("mediaSourceType",rdbMediaSrcParameter.getMediaSourceType());
            responseVo.getData().put("mediaSourceName",rdbMediaSrcParameter.getName());
            responseVo.getData().put("namespace",rdbMediaSrcParameter.getNamespace());
            responseVo.getData().put("writeConfig",writeConfig);
            responseVo.getData().put("readConfig",readConfig);
        }catch (ErrorException e) {
            logger.error("查询表信息出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
   }

    private MediaSourceInfo getMediaSourceInfo( String databaseName) throws Exception {

        MediaSourceInfo mediaSourceInfo = mediaSourceService.getOneByName(databaseName);
        if( mediaSourceInfo == null ) {
            //传入的db name在数据库中没找到，抛错
            throw new ErrorException(CodeContext.DATABASE_NOTFOUND_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.DATABASE_NOTFOUND_ERROR_CODE));
        }

        long virtualMediaSourceId = mediaSourceInfo.getId();
        mediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById( virtualMediaSourceId );

        return mediaSourceInfo;
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
