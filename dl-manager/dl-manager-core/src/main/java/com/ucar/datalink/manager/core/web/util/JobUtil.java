package com.ucar.datalink.manager.core.web.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.MetaManager;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.service.JobControlService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.JobExecutionState;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.web.controller.job.JobConfigController;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * job工具类
 *
 * @author wenbin.song
 * @date 2019/03/06
 */
public class JobUtil {
    private static final Logger logger = LoggerFactory.getLogger(JobUtil.class);

    /**
     * 调用者来源对应关系
     */
    public static final Map<String,String> sourceMap = new HashMap<>();

    static {
        sourceMap.put("lucky","LUCKY");
        sourceMap.put("ucar","DSPIDER");
    }

    public static boolean checkJobStateWhenZNodeNotExist(long id) {
        boolean isSuccess = false;
        for(int i=0;i<10;i++) {
            JobExecutionInfo info = ((JobControlService)DataLinkFactory.getObject("dynamic")).state(id);
            if( info!=null && JobExecutionState.SUCCEEDED.equals(info.getState())) {
                isSuccess = true;
                break;
            }
            try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(),e);
            }
        }
        return isSuccess;
    }

    /**
     * 解析jobId,请求的jobId中可能带有环境信息，需要去掉
     * @param jobId
     * @return
     */
    public static String parseJobId(String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return jobId;
        }
        String env = getEnvSuffix();
        if (jobId.endsWith(env)) {
            jobId = jobId.substring(0, jobId.length() - env.length());
        }
        return jobId;
    }

    /**
     * 获取环境后缀
     * @return
     */
    public static String getEnvSuffix() {
        String currentEnv = ManagerConfig.current().getCurrentEnv();
        if (StringUtils.isBlank(currentEnv)) {
            return "_";
        } else {
            return "_" + currentEnv;
        }
    }

    /**
     * 添加环境后缀
     * @return
     */
    public static String appendEnvSuffix(String id) {
        return id + JobUtil.getEnvSuffix();
    }

    /**
     * 校验jobId的合法性
     * @param jobId
     * @return
     */
    public static boolean jobConfigIdCheck(String jobId) {
        return checkNumber(jobId);
    }

    /**
     * 校验executeId的合法性
     * @param executeId
     * @return
     */
    public static boolean jobExecutionIdCheck(String executeId) {
        return checkNumber(executeId);
    }

    public static boolean checkNumber(String number) {
        if (StringUtils.isBlank(number)) {
            return false;
        }
        try {
            long id = Long.parseLong(number);
            if (id > 0) {
                return true;
            }
        } catch (Exception e) {
            //ignore
        }
        return false;
    }

    public static String genJobName(String prefix,String tableName) {
        String jobName = "";
        if(!StringUtils.isEmpty(prefix)){
            jobName += prefix+"_";
        }
        jobName += tableName + "_"+ JobConfigController.randomString(10);
        return jobName;
    }

    public static String updateColumns(MediaSourceInfo srcMediaSourceInfo,String configJson, List<Column> columns,String destDbType) {
        List<Object> readerList = new ArrayList<>();
        List<Map<String,String>> writerList = new ArrayList<>();

        DLConfig config = DLConfig.parseFrom(configJson);
        List<Object> writerListOldList = config.getList("job.content[0].writer.parameter.column");
        Map<String,String> destMap = new HashMap<>(16);
        for(Object object : writerListOldList) {
            JSONObject jsonObject = (JSONObject) object;
            destMap.put(jsonObject.getString("name"),jsonObject.getString("type"));
        }

        for (Column srcColumn : columns){
            Map<String,String> readerMap = new HashMap<>(8);
            Map<String,String> writerMap = new HashMap<>(8);

            if(srcMediaSourceInfo.getType().isRdbms()){
                readerList.add(srcColumn.getName());
            }else {
                readerMap.put("name",srcColumn.getName());
                readerMap.put("type",srcColumn.getType());
                readerList.add(readerMap);
            }

            String name = srcColumn.getName();
            String destColumnType = destMap.get(name);
            if("hdfs".equalsIgnoreCase(destDbType)) {
                name = name.replaceAll("\\.","_");
                if(StringUtils.isEmpty(destColumnType)) {
                    destColumnType = "string";
                }
            }

            writerMap.put("name",name);
            writerMap.put("type",destColumnType);
            writerList.add(writerMap);
        }

        config.set("job.content[0].reader.parameter.column",readerList);
        config.set("job.content[0].writer.parameter.column",writerList);
        return config.toJSON();
    }

    public static List<Column> getHbaseColumns(List<ColumnMeta> columnList, String hbaseColumns) {
        List<Column> list = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(hbaseColumns);
        List<String> columnFamilyList = new ArrayList<>();
        Map<String,ColumnMeta> columnMetaMap = new HashMap<>(16);
        for (ColumnMeta columnMeta:columnList) {
            columnFamilyList.add(columnMeta.getColumnFamily());
            columnMetaMap.put(columnMeta.getColumnFamily().toLowerCase()+"_"+columnMeta.getName().toLowerCase().replaceAll("\\.","_"),columnMeta);
        }
        for (int i = 0;i < jsonArray.size();i++){
            String name = jsonArray.getJSONObject(i).getString("name");
            String fullName = name;
            ColumnMeta columnMeta = columnMetaMap.get(name.toLowerCase().replaceAll("\\.","_"));
            if("rowkey".equalsIgnoreCase(fullName)){
                //不做处理
            }else if(columnMeta !=null) {
                fullName = columnMeta.getColumnFamily()+":"+columnMeta.getName();
            }else {
                boolean flag = false;
                for(String columnFamilyName:columnFamilyList) {
                    if(fullName.startsWith(columnFamilyName)) {
                        int index = fullName.indexOf(columnFamilyName);
                        fullName = columnFamilyName+":"+ fullName.substring(columnFamilyName.length()+index+1);
                        flag = true;
                        break;
                    }
                }
                if(!flag) {//默认取下划线第一个
                    String[] names = fullName.split("_");
                    if(names.length>1){
                        int index = fullName.indexOf("_");
                        fullName = names[0]+":"+fullName.substring(index+1).replaceAll("_",".");
                    }
                }
            }
            Column column = new Column();
            column.setName(fullName);
            column.setType("Bytes");
            list.add(column);
        }
        return list;
    }

    public static List<Column> getColumns(List<ColumnMeta> columnMetaList, String columns) {
        List<Column> list = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(columns);
        Map<String,ColumnMeta> columnMetaMap = new HashMap<>(16);
        for (ColumnMeta columnMeta:columnMetaList) {
            columnMetaMap.put(columnMeta.getName().toLowerCase(),columnMeta);
        }
        for (int i = 0;i < jsonArray.size();i++) {
            String name = jsonArray.getJSONObject(i).getString("name");
            ColumnMeta columnMeta = columnMetaMap.get(name.toLowerCase());
            if(columnMeta != null) {
                Column column = new Column();
                column.setName(columnMeta.getName());
                column.setType(columnMeta.getType());
                list.add(column);
            }
        }
        return list;
    }

    public static List<Column> getDestColumns(MediaSourceInfo srcMediaSourceInfo, MediaMeta mediaMeta, String destDbType) {
        List<ColumnMeta> columnMetaList = null;
        if(MediaSourceType.HDFS.name().equalsIgnoreCase(destDbType)){
            MediaMeta toHDFSMediaMeta = MetaMapping.transformToHDFS(mediaMeta);
            columnMetaList = toHDFSMediaMeta.getColumn();
        }else if(MediaSourceType.KUDU.name().equalsIgnoreCase(destDbType)){
            MediaMeta kuduMiaMeta = MetaMapping.transformToKUDU(mediaMeta);
            columnMetaList = kuduMiaMeta.getColumn();
        }
        List<Column> columnList = new ArrayList<>();
        if(columnMetaList!=null){
            for (ColumnMeta columnMeta :columnMetaList) {
                Column column = new Column();
                String columnName = columnMeta.getName();
                if(!StringUtils.isEmpty(columnMeta.getColumnFamily())) {
                    columnName = columnMeta.getColumnFamily()+":"+columnName;
                }
                column.setName(columnName);
                column.setType(columnMeta.getType());
                columnList.add(column);
            }
        }
       return columnList;
    }

    public static class Column {
       private String name;
       private String type;

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
    }
}
