package com.ucar.datalink.manager.core.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.ModeUtils;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.plugin.reader.hbase.HBaseReaderParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskType;
import com.ucar.datalink.domain.vo.TaskMediaNameVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * hbase表同步工具类
 *
 * @author wenbin.song
 * @date 2019/04/10
 */
public class HbaseSyncUtil {

    private static final Logger logger = LoggerFactory.getLogger(HbaseSyncUtil.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

    private static final SimpleDateFormat year_sdf = new SimpleDateFormat("yyyy");

    private static final String MONTH_SUFFIX_1 = "_${yyyyMM}";

    private static final String YEAR_SUFFIX = "_${yyyy}";


    private static SysPropertiesService sysPropertiesService;

    private static MediaSourceService mediaSourceService;

    private static MediaService meidaService;

    private static TaskConfigService taskConfigService;

    private static AlarmService alarmService;

    private static HbaseService hbaseService;

    static {
        sysPropertiesService = DataLinkFactory.getObject(SysPropertiesService.class);
        mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        meidaService = DataLinkFactory.getObject(MediaService.class);
        taskConfigService = DataLinkFactory.getObject(TaskConfigService.class);
        alarmService = DataLinkFactory.getObject(AlarmService.class);
        hbaseService = DataLinkFactory.getObject(HbaseService.class);
    }

    /**
     * 开启表复制
     *
     * @param srcMediaSourceInfo
     * @param tableName
     */
    public static void openReplication(MediaSourceInfo srcMediaSourceInfo, TaskInfo taskInfo, String tableName) throws IOException {
        if (srcMediaSourceInfo.getType() == MediaSourceType.HBASE || srcMediaSourceInfo.getSimulateMsType() == MediaSourceType.HBASE) {
            ClusterZkInfo sourceClusterInfo = getSourceClusterInfo(taskInfo, srcMediaSourceInfo);
            ClusterZkInfo targetCluseterInfo = getTargetClusterInfo(taskInfo);

            if (tableName.endsWith(MONTH_SUFFIX_1)) {
                tableName = tableName.replace(MONTH_SUFFIX_1, "");
            } else if (ModeUtils.isMonthlySuffix(tableName)) {
                tableName = tableName.substring(0, tableName.length() - 7);
            } else if (tableName.endsWith(YEAR_SUFFIX)) {
                tableName = tableName.replace(YEAR_SUFFIX, "");
            }else if(tableName.endsWith(YEAR_SUFFIX)){
                tableName = tableName.replace(YEAR_SUFFIX,"");
            }

            String peerId = sourceClusterInfo.getPeerId();/*sysHbasePeerCache.getUnchecked(srcMediaSourceInfo.getName());*/
            if (StringUtils.isEmpty(peerId)) {
                throw new RuntimeException("没有找到peerId");
            }

            String result = hbaseService.doAddTable(sourceClusterInfo.getClusterZk(), sourceClusterInfo.getClusterZkBasePath(),
                    targetCluseterInfo.getClusterZk(), targetCluseterInfo.getClusterZkBasePath(), peerId, tableName);

            if (StringUtils.isEmpty(result)) {
                logger.info("addTableToPeer接口返回结果为空!");
                throw new RuntimeException("开启hbase表[" + tableName + "]复制异常!");
            }
            JSONObject jsonObject = JSON.parseObject(result);
            if (!(jsonObject.getInteger("status") == 1 || jsonObject.getInteger("status") == 7)) {
                logger.info("addTableToPeer接口返回结果为 [" + result + " ]");
                throw new RuntimeException("开启hbase表[" + tableName + "]复制异常,原因:" + jsonObject.getString("description"));
            }
        }
    }

    /**
     * 巡检映射下的所有表是否开启复制
     */
    public static void checkReplicateTables() {
        logger.info("check hbase tables...");

        List<TaskInfo> taskInfoList = getHbaseTasks();

        //1,遍历task，获取该task下配置的所有表
        Map<Long, TaskInfo> taskInfoMap = new HashMap<>(16);
        List<Long> taskIdList = new ArrayList<>();
        taskInfoList.forEach(taskInfo -> {
            taskIdList.add(taskInfo.getId());
            taskInfoMap.put(taskInfo.getId(), taskInfo);
        });

        Map<Long, List<String>> taskTablesMap = getTaskTablesMap(taskIdList);

        //2,从hbase sever上获取该peer下的所有开启复制的表
        for (Map.Entry<Long, List<String>> entry : taskTablesMap.entrySet()) {
            Long taskId = entry.getKey();
            List<String> tables = entry.getValue();
            TaskInfo taskInfo = taskInfoMap.get(taskId);

            ClusterZkInfo sourceClusterInfo = getSourceClusterInfo(taskInfo);
            ClusterZkInfo targetCluseterInfo = getTargetClusterInfo(taskInfo);

            String peerId = sourceClusterInfo.getPeerId();/*sysHbasePeerCache.getUnchecked(sourceClusterInfo.getSourceMediaName());*/
            if (StringUtils.isEmpty(peerId)) {
                throw new RuntimeException("没有找到peerId");
            }

            String result = hbaseService.doGetTables(sourceClusterInfo.getClusterZk(), sourceClusterInfo.getClusterZkBasePath(),
                    targetCluseterInfo.getClusterZk(), targetCluseterInfo.getClusterZkBasePath(), peerId);

            if (StringUtils.isEmpty(result)) {
                logger.info("getPeerTables接口返回结果为空!");
            }

            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("status") != 1) {
                logger.info("getPeerTables接口返回结果为 [" + result + " ]");
            }
            JSONArray queryTablesArray = jsonObject.getJSONArray("tables");
            //3,前两者进行比较，1中有表不在2中则发起警报
            for (String table : tables) {
                if (!queryTablesArray.contains(table)) {
                    //不暂停task任务，只报警，后续再针对该表补数据
//                    DataLinkFactory.getObject(TaskConfigService.class).pauseTask(taskId);
                    //发警报
                    alarmService.alarm("taskId为[ " + taskId + " ]下的表[ " + table + " ]不在peer服务器集群中，未开启复制。", "hbase表复制异常",false);
                    logger.info("表[ " + table + " ]不在peer服务器集群中，请人工处理");
                }
            }
        }
    }

    private static ClusterZkInfo getTargetClusterInfo(TaskInfo taskInfo) {
        ClusterZkInfo targetClusterZkInfo = new ClusterZkInfo();
        HBaseReaderParameter baseReaderParameter = (HBaseReaderParameter) taskInfo.getTaskReaderParameterObj();
        MediaSourceInfo targetZkMediaSource = meidaService.getMediaSourceById(baseReaderParameter.getReplZkMediaSourceId());
        if (targetZkMediaSource == null) {
            throw new RuntimeException("没有找到datalink对应的zk集群信息");
        }
        ZkMediaSrcParameter targetZkParameter = targetZkMediaSource.getParameterObj();
        String targetClusterZkBasePath = baseReaderParameter.getReplZnodeParent();
        targetClusterZkInfo.setClusterZk(targetZkParameter.getServers());
        targetClusterZkInfo.setClusterZkBasePath(targetClusterZkBasePath);
        return targetClusterZkInfo;
    }

    private static ClusterZkInfo getSourceClusterInfo(TaskInfo taskInfo) {
        Long readerMediaSourceId = taskInfo.getReaderMediaSourceId();
        MediaSourceInfo srcMediaSourceInfo = meidaService.getMediaSourceById(readerMediaSourceId);

        return getSourceClusterInfo(taskInfo, srcMediaSourceInfo);
    }

    private static ClusterZkInfo getSourceClusterInfo(TaskInfo taskInfo, MediaSourceInfo srcMediaSourceInfo) {
        ClusterZkInfo clusterZkInfo = new ClusterZkInfo();
        MediaSourceInfo centerMediaSourceInfo = meidaService.getRealDataSource(srcMediaSourceInfo);
        HBaseMediaSrcParameter hbaseMediaSrcParameter = centerMediaSourceInfo.getParameterObj();
        Long zkMediaSourceId = hbaseMediaSrcParameter.getZkMediaSourceId();
        MediaSourceInfo zkMediaSourceInfo = mediaSourceService.getById(zkMediaSourceId);
        ZkMediaSrcParameter zkMediaSrcParameter = zkMediaSourceInfo.getParameterObj();
        String sourceClusterZkBasePath = hbaseMediaSrcParameter.getZnodeParent();

        clusterZkInfo.setClusterZk(zkMediaSrcParameter.getServers());
        clusterZkInfo.setClusterZkBasePath(sourceClusterZkBasePath);
        clusterZkInfo.setSourceMediaName(srcMediaSourceInfo.getName());
        clusterZkInfo.setPeerId(hbaseMediaSrcParameter.getPeerId());
        return clusterZkInfo;
    }

    private static Map<Long, List<String>> getTaskTablesMap(List<Long> taskIdList) {
        List<TaskMediaNameVo> mediaNameList = meidaService.getMediaNamesByTaskId(taskIdList);
        //处理表，通配符转成当前月，如果有具体月份的不是当前月的去掉
        Map<Long, List<String>> taskTablesMap = new HashMap<>(16);
        Date date = new Date();
        String dateMonthStr = sdf.format(date);
        String dateYearStr = year_sdf.format(date);
        for (TaskMediaNameVo taskMediaNameVo : mediaNameList) {
            Long taskId = taskMediaNameVo.getTaskId();
            String tableName = taskMediaNameVo.getMediaName();
            List<String> tableNameList = taskTablesMap.get(taskId);
            if (tableNameList == null) {
                tableNameList = new ArrayList<>();
            }
            if (tableName.endsWith(MONTH_SUFFIX_1)) {
                tableName = tableName.substring(0, tableName.length() - 9) + dateMonthStr;
                taskMediaNameVo.setMediaName(tableName);
            } else if (tableName.endsWith(YEAR_SUFFIX)) {
                tableName = tableName.substring(0, tableName.length() - 7) + dateYearStr;
                taskMediaNameVo.setMediaName(tableName);
            } else if (ModeUtils.isMonthlySuffix(tableName)) {
                String suffix = tableName.substring(tableName.length() - 6);
                if (!suffix.equals(dateMonthStr)) {
                    continue;
                }
            }
            if (!tableNameList.contains(tableName)) {
                tableNameList.add(tableName);
            }
            taskTablesMap.put(taskId, tableNameList);
        }
        return taskTablesMap;
    }

    private static Map<String, String> covertMap(String hbase_peerId) {
        Map<String, String> map = new HashMap<>(8);
        if (StringUtils.isEmpty(hbase_peerId)) {
            return map;
        }
        JSONArray jsonArray = JSONArray.parseArray(hbase_peerId);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("name");
            String peerId = jsonObject.getString("peerId");
            map.put(name, peerId);
        }
        return map;
    }

    /**
     * 获取hbase leaderTask列表
     *
     * @return
     */
    public static List<TaskInfo> getHbaseTasks() {
        List<TaskInfo> taskInfoList = taskConfigService.getTasksByType(TaskType.HBASE);
        List<TaskInfo> leaderTaskList = new ArrayList<>(8);
        taskInfoList.forEach(taskInfo -> {
            if (taskInfo.isLeaderTask()) {
                leaderTaskList.add(taskInfo);
            }
        });
        return leaderTaskList;
    }


    static class ClusterZkInfo {
        private String clusterZkBasePath;
        private String clusterZk;
        private String sourceMediaName;
        private String peerId;

        public String getClusterZkBasePath() {
            return clusterZkBasePath;
        }

        public void setClusterZkBasePath(String clusterZkBasePath) {
            this.clusterZkBasePath = clusterZkBasePath;
        }

        public String getSourceMediaName() {
            return sourceMediaName;
        }

        public void setSourceMediaName(String sourceMediaName) {
            this.sourceMediaName = sourceMediaName;
        }

        public String getClusterZk() {
            return clusterZk;
        }

        public void setClusterZk(String clusterZk) {
            this.clusterZk = clusterZk;
        }

        public String getPeerId() {
            return peerId;
        }

        public void setPeerId(String peerId) {
            this.peerId = peerId;
        }
    }

}
