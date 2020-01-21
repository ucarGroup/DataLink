package com.ucar.datalink.manager.core.web.util;

import com.google.common.base.Joiner;
import com.ucar.datalink.biz.dal.SyncApplyDAO;
import com.ucar.datalink.biz.meta.ElasticSearchUtil;
import com.ucar.datalink.biz.module.PropertyConstant;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.es.EsMediaSrcParameter;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.writer.hdfs.FileSplitMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.FileSplitStrategy;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsFileParameter;
import com.ucar.datalink.domain.sync.*;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.util.Env;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 增量同步工具类
 *
 * @author wenbin.song
 * @date 2019/04/16
 */
public class IncrementSyncUtil {
    private static Logger logger = LoggerFactory.getLogger(IncrementSyncUtil.class);

    private static TaskConfigService taskService;

    private static MediaService mediaService;

    private static MediaSourceService mediaSourceService;

    private static SyncApplyDAO syncApplyDAO;

    private static GroupService groupService;

    private static ElasticSearchService elasticSearchService;

    private static final String NAME_SUFFIX_A = "_A";

    private static final String NAME_SUFFIX_B = "_B";

    private static final String KEY_WORD_LUCKY = "lucky";

    private static final Map<String, FileSplitMode> fileSplitModeMap = new HashMap<>(4);

    public static final Map<MediaSourceType, List<MediaSourceType>> mediaSourceMappingMatchMap = new HashMap<MediaSourceType, List<MediaSourceType>>(8);

    static {
        taskService = DataLinkFactory.getObject(TaskConfigService.class);
        mediaService = DataLinkFactory.getObject(MediaService.class);
        mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        syncApplyDAO = DataLinkFactory.getObject(SyncApplyDAO.class);
        groupService = DataLinkFactory.getObject(GroupService.class);
        elasticSearchService = DataLinkFactory.getObject(ElasticSearchService.class);

        List<MediaSourceType> mysqlList = new ArrayList<>();
        mysqlList.add(MediaSourceType.MYSQL);
        mysqlList.add(MediaSourceType.SQLSERVER);
        mysqlList.add(MediaSourceType.ORACLE);
        mysqlList.add(MediaSourceType.POSTGRESQL);
        mysqlList.add(MediaSourceType.ELASTICSEARCH);
        mysqlList.add(MediaSourceType.HDFS);
        mysqlList.add(MediaSourceType.FLEXIBLEQ);
        mysqlList.add(MediaSourceType.DOVE);
        mysqlList.add(MediaSourceType.HBASE);
        mysqlList.add(MediaSourceType.SDDL);
        mysqlList.add(MediaSourceType.KUDU);
        mysqlList.add(MediaSourceType.KAFKA);

        List<MediaSourceType> hbaseList = new ArrayList<>();
        hbaseList.add(MediaSourceType.MYSQL);
        hbaseList.add(MediaSourceType.SQLSERVER);
        hbaseList.add(MediaSourceType.ORACLE);
        hbaseList.add(MediaSourceType.POSTGRESQL);
        hbaseList.add(MediaSourceType.ELASTICSEARCH);
        hbaseList.add(MediaSourceType.HDFS);
        hbaseList.add(MediaSourceType.FLEXIBLEQ);
        hbaseList.add(MediaSourceType.DOVE);
        hbaseList.add(MediaSourceType.HBASE);
        hbaseList.add(MediaSourceType.SDDL);
        hbaseList.add(MediaSourceType.KAFKA);

        List<MediaSourceType> fqList = new ArrayList<>();
        fqList.add(MediaSourceType.MYSQL);
        fqList.add(MediaSourceType.SQLSERVER);
        fqList.add(MediaSourceType.ORACLE);
        fqList.add(MediaSourceType.POSTGRESQL);
        fqList.add(MediaSourceType.ELASTICSEARCH);
        fqList.add(MediaSourceType.HDFS);
        fqList.add(MediaSourceType.FLEXIBLEQ);
        fqList.add(MediaSourceType.HBASE);
        fqList.add(MediaSourceType.SDDL);

        mediaSourceMappingMatchMap.put(MediaSourceType.MYSQL, mysqlList);
        mediaSourceMappingMatchMap.put(MediaSourceType.HBASE, hbaseList);

        fileSplitModeMap.put("HALFHOUR", FileSplitMode.HALFHOUR);
        fileSplitModeMap.put("HOUR", FileSplitMode.HOUR);
        fileSplitModeMap.put("DAY", FileSplitMode.DAY);
    }


    public static boolean processIncrement(SyncApplyInfo syncApplyInfo, Long taskId, boolean isReuseTask, String zkServer, String currentEnv) {
        boolean isSuccess;
        try {
            SyncApplyContent syncApplyContent = syncApplyInfo.getApplyContentObj();
            SyncApplyParameter syncApplyParameter = syncApplyContent.getApplyParameterList().get(0);
            Long srcMediaSourceId = syncApplyParameter.getSrcMediaSourceId();
            Long tarMediaSourceId = syncApplyParameter.getTargetMediaSourceId();
            MediaSourceInfo srcInfo = mediaSourceService.getById(srcMediaSourceId);
            MediaSourceInfo destInfo = mediaSourceService.getById(tarMediaSourceId);

            TaskInfo incrementTask = null;
            if (taskId == null) {
                //autoChooseTask
                incrementTask = findExistsTask(srcInfo, destInfo, isReuseTask);
                //测试和预生产自动创建task
                if (!currentEnv.equals(Env.PROD.getName()) && incrementTask == null) {
                    incrementTask = taskService.createTask(srcInfo, destInfo, getGroupId(srcInfo, destInfo), zkServer, currentEnv);
                }
                if (incrementTask == null) {
                    syncApplyInfo.setApplyStatus(SyncApplyStatus.INCREMENT_FAILED);
                    syncApplyInfo.setReplyRemark("同步申请增量映射配置失败！applyId = " + syncApplyInfo.getId() + "<br>" + "<br>" + "失败原因：没有找到Task，请先创建对应的Task！");
                    syncApplyDAO.updateApplyStatus(syncApplyInfo);
                    return false;
                }
            } else {
                incrementTask = taskService.getTask(taskId);
            }

            MappingConfigView mappingConfigView = new MappingConfigView();
            prepareMappingInfo(incrementTask, srcInfo, destInfo, syncApplyParameter.getSyncApplyMappings(), mappingConfigView, syncApplyInfo.getId());
            mappingConfigView.setParameter(getParameter(destInfo, ""));

            //createMediaMapping
            if (mappingConfigView.getSrcNames().length > 0) {
                mediaService.insert(
                        buildMediaInfo(mappingConfigView.getSrcNames(), srcInfo, srcMediaSourceId),
                        buildMediaMappingInfo(mappingConfigView, destInfo, incrementTask.getId()),
                        buildMediaColumnMappingInfo(mappingConfigView)
                );
                syncApplyInfo.setApplyStatus(SyncApplyStatus.INCREMENT_FINISH);
                syncApplyDAO.updateApplyStatus(syncApplyInfo);
                logger.info("Add media-mapping succeed. applyId = " + syncApplyInfo.getId() + " and taskId = " + incrementTask.getId());
                isSuccess = true;
                //清空Task的映射缓存
                mediaService.cleanTableMapping(incrementTask.getId());
            } else {
                syncApplyInfo.setApplyStatus(SyncApplyStatus.INCREMENT_FINISH);
                syncApplyDAO.updateApplyStatus(syncApplyInfo);
                isSuccess = true;
            }
        } catch (Exception e) {
            syncApplyInfo.setApplyStatus(SyncApplyStatus.INCREMENT_FAILED);
            syncApplyInfo.setReplyRemark("同步申请增量映射配置失败！applyId = " + syncApplyInfo.getId() + "<br>" + "<br>" + "Exception：" + e);
            syncApplyDAO.updateApplyStatus(syncApplyInfo);
            isSuccess = false;
            logger.error("Add media-mapping failed. applyId =" + syncApplyInfo.getId(), e);
        }
        return isSuccess;
    }


    /**
     * 根据源和目标数据源查找对应的task
     *
     * @param srcMediaSourceInfo
     * @param targetMediaSourceInfo
     * @return
     */
    public static TaskInfo findExistsTask(MediaSourceInfo srcMediaSourceInfo, MediaSourceInfo targetMediaSourceInfo, boolean isReuseTask) throws ErrorException {
        List<TaskInfo> readerTaskList = taskService.getTasksByReaderMediaSourceId(srcMediaSourceInfo.getId());
        TaskInfo findTask = null;
        for (TaskInfo taskInfo : readerTaskList) {
            List<PluginWriterParameter> writerParameters = taskInfo.getTaskWriterParameterObjs();
            for (PluginWriterParameter writerParameter : writerParameters) {
                if (srcMediaSourceInfo.getType() == MediaSourceType.MYSQL || srcMediaSourceInfo.getSimulateMsType() == MediaSourceType.MYSQL) {
                    if (writerParameter.getSupportedSourceTypes().contains(targetMediaSourceInfo.getType()) || writerParameter.getSupportedSourceTypes().contains(targetMediaSourceInfo.getSimulateMsType())) {
                        findTask = taskInfo;
                        break;
                    }
                } else if (srcMediaSourceInfo.getType() == MediaSourceType.HBASE || srcMediaSourceInfo.getSimulateMsType() == MediaSourceType.HBASE) {
                    if (taskInfo.isLeaderTask() && (writerParameter.getSupportedSourceTypes().contains(targetMediaSourceInfo.getType()) || writerParameter.getSupportedSourceTypes().contains(targetMediaSourceInfo.getSimulateMsType()))) {
                        findTask = taskInfo;
                        break;
                    }
                }
            }
        }
        //如果是测试环境或者是预生产环境复用task
        if (findTask == null && isReuseTask && readerTaskList != null && readerTaskList.size() > 0) {
            findTask = readerTaskList.get(0);
            MediaSourceType srcMediaSourceType = mediaService.getRealDataSource(srcMediaSourceInfo).getType();
            MediaSourceType targetMediaSourceType = mediaService.getRealDataSource(targetMediaSourceInfo).getType();
            //需要做一下判断，源和目标的类型对应关系
            List<MediaSourceType> mediaSourceTypeList = mediaSourceMappingMatchMap.get(srcMediaSourceType);
            if (mediaSourceTypeList != null && mediaSourceTypeList.contains(targetMediaSourceType)) {
                //配置目标类型参数
                taskService.configTaskWriter(findTask, targetMediaSourceType);
            }else {
                throw new ErrorException(CodeContext.TARGET_TYPE_ERROR_CODE);
            }
            taskService.updateTask(findTask);
        }
        return findTask;
    }

    public static List<Long> prepareMappingInfo(TaskInfo taskInfo, MediaSourceInfo srcInfo, MediaSourceInfo targetMediaInfo, List<SyncApplyMapping> syncApplyMappings, MappingConfigView mappingConfigView) {
        return prepareMappingInfo(taskInfo, srcInfo, targetMediaInfo, syncApplyMappings, mappingConfigView, 0L);
    }

    public static List<Long> prepareMappingInfo(TaskInfo taskInfo, MediaSourceInfo srcInfo, MediaSourceInfo targetMediaInfo, List<SyncApplyMapping> syncApplyMappings, MappingConfigView mappingConfigView, Long syncApplyInfoId) {
        //find mappingTableList
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("taskId", taskInfo.getId());
        mapParam.put("targetMediaSourceId", targetMediaInfo.getId());
        List<MediaMappingInfo> mappingTableList = mediaSourceService.getMappingByTaskIdAndTargetMediaSourceId(mapParam);
        //get SyncApplyMappingInfo
        List<String> srcTableName = new ArrayList<>();
        List<String> tarTableName = new ArrayList<>();
        List<String> sourceColumns = new ArrayList<>();
        List<String> targetColumns = new ArrayList<>();
        List<String> joinColumns = new ArrayList<>();
        List<String> isAddTablePrefixs = new ArrayList<>();
        List<ColumnMappingMode> columnMappingModes = new ArrayList<>();

        List<Long> mappingIdList = new ArrayList<Long>();
        for (SyncApplyMapping applyMapping : syncApplyMappings) {
            boolean mappingTableExist = false;
            for (MediaMappingInfo mappingInfo : mappingTableList) {
                if (mappingInfo.getSourceMedia().getName().equals(applyMapping.getSourceTableName())
                        &&applyMapping.getTargetTableName().equals(mappingInfo.getTargetMediaName())) {
                    mappingTableExist = true;
                    mappingIdList.add(mappingInfo.getId());
                    break;
                }
            }
            if (mappingTableExist) {
                logger.error("mapping table exists. tableName = " + applyMapping.getSourceTableName() + " and applyId =" + syncApplyInfoId + " and taskId = " + taskInfo.getId());
                continue;
            }

            //hbase开启表复制
            try {
                HbaseSyncUtil.openReplication(srcInfo, taskInfo, applyMapping.getSourceTableName());
            } catch (IOException e) {
                logger.info("开启表 [ " + applyMapping.getSourceTableName() + " ]复制失败", e);
                throw new RuntimeException("开启表 [ " + applyMapping.getSourceTableName() + " ]复制失败");
            }

            srcTableName.add(applyMapping.getSourceTableName());
            tarTableName.add(applyMapping.getTargetTableName());

            ColumnMappingMode mappingMode = applyMapping.getColumnMappingMode();
            columnMappingModes.add(mappingMode);

            String sourceStr = "";
            String targetStr = "";
            //"sourceColumn": ["violation_name", "status"]
            List<String> srcColumn = applyMapping.getSourceColumn();
            List<String> tarColumn = applyMapping.getTargetColumn();
            if (mappingMode != ColumnMappingMode.NONE) {
                sourceStr = Joiner.on(",").join(srcColumn);
                targetStr = Joiner.on(",").join(tarColumn);
            }
            sourceColumns.add(sourceStr);
            targetColumns.add(targetStr);

            Map<String, String> map = applyMapping.getOtherMappingRelation();
            if (map != null && map.size() > 0) {
                joinColumns.add(map.get(PropertyConstant.ES_JOIN_COLUMN));
                isAddTablePrefixs.add(map.get(PropertyConstant.ES_IS_TABLE_PREFIX));
            }
        }

        setMappingConfigView(mappingConfigView, targetMediaInfo, srcTableName, tarTableName, sourceColumns, targetColumns,
                joinColumns, isAddTablePrefixs, columnMappingModes);

        return mappingIdList;
    }

    public static List<MediaInfo> buildMediaInfo(String[] src_names, MediaSourceInfo srcInfo, Long srcMediaSourceId) {
        if (src_names == null && src_names.length == 0) {
            throw new RuntimeException("sourceTableName is empty");
        }
        MediaSourceInfo realMediaSourceInfo;
        if (srcInfo.getType().equals(MediaSourceType.VIRTUAL)) {
            realMediaSourceInfo = mediaService.getRealDataSource(srcInfo);
        } else {
            realMediaSourceInfo = srcInfo;
        }
        List<MediaInfo> tablelist = new ArrayList<>();
        for (String tableName : src_names) {
            MediaInfo mediaInfo = new MediaInfo();
            mediaInfo.setName(tableName);
            mediaInfo.setMediaSourceId(srcMediaSourceId);
            //TODO,如果namespace为空，则设置为default，此处只是临时支持hbase，暂时这么用，
            //TODO 后续整个MediaMappingController都需要进行深度重构
            mediaInfo.setNamespace(StringUtils.isBlank(realMediaSourceInfo.getParameterObj().getNamespace()) ? "default" : realMediaSourceInfo.getParameterObj().getNamespace());
            tablelist.add(mediaInfo);
        }
        return tablelist;
    }

    public static List<MediaMappingInfo> buildMediaMappingInfo(MappingConfigView mappingConfigView, MediaSourceInfo destInfo, Long taskId) {
        if (mappingConfigView.getDestNames() == null && mappingConfigView.getDestNames().length == 0) {
            throw new RuntimeException("targetTableName is empty");
        }
        List<MediaMappingInfo> mediaMappingList = new ArrayList<>();
        for (int i = 0; i < mappingConfigView.getDestNames().length; i++) {
            MediaMappingInfo mediaMapping = new MediaMappingInfo();
            mediaMapping.setTaskId(taskId);
            mediaMapping.setTargetMediaSourceId(destInfo.getId());
            mediaMapping.setTargetMediaNamespace(destInfo.getParameterObj().getNamespace());
            mediaMapping.setTargetMediaName(mappingConfigView.getDestNames()[i]);
            mediaMapping.setColumnMappingMode(mappingConfigView.getColumnMappingMode()[i]);
            mediaMapping.setJoinColumn(mappingConfigView.getJoinColumn()[i]);
            mediaMapping.setValid(true);
            mediaMapping.setWritePriority(5L);
            mediaMapping.setEsUsePrefix(Boolean.valueOf(mappingConfigView.getIsAddTablePrefix()[i]));
            mediaMapping.setParameter(mappingConfigView.getParameter());

            //设置es routing信息
            Map<String,String> map = IncrementSyncUtil.getEsRoutingInfo(mediaMapping.getTargetMediaSourceId(),mediaMapping.getTargetMediaName());
            mediaMapping.setEsRouting(StringUtils.isNotBlank(map.get("esRouting")) ? map.get("esRouting") : "");
            mediaMapping.setEsRoutingIgnore(StringUtils.isNotBlank(map.get("esRoutingIgnore")) ? map.get("esRoutingIgnore") : "");

            mediaMappingList.add(mediaMapping);
        }
        return mediaMappingList;
    }

    public static Map getEsRoutingInfo(Long mediaSourceId, String targetTableName){
        Map<String,String> resultMap = new HashMap<String,String>();

        if(mediaSourceId == null){
            throw new DatalinkException("缺少参数mediaSourceId");
        }

        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(mediaSourceId);
        if (!(mediaSourceInfo.getType() == MediaSourceType.ELASTICSEARCH || (mediaSourceInfo.getType() == MediaSourceType.VIRTUAL && mediaSourceInfo.getSimulateMsType() == MediaSourceType.ELASTICSEARCH) )) {
            return resultMap;
        }
        if(mediaSourceInfo.getType() == MediaSourceType.VIRTUAL && mediaSourceInfo.getSimulateMsType() == MediaSourceType.ELASTICSEARCH){
            mediaSourceInfo = mediaService.getRealDataSource(mediaSourceInfo);
        }

        if(StringUtils.isBlank(targetTableName)){
            throw new DatalinkException("缺少参数索引名");
        }

        //取es ip
        EsMediaSrcParameter parameter = mediaSourceInfo.getParameterObj();
        String ipList = parameter.getClusterHosts();
        String ip = ipList.split(",")[0];
        //es index
        String[] indexNameArr = targetTableName.split("\\.");
        String index = indexNameArr[0];

        //获取es routing信息
        resultMap = elasticSearchService.getEsRoutingInfo(ip,index);
        resultMap.put("success","true");

        return resultMap;
    }

    public static List<MediaColumnMappingInfo> buildMediaColumnMappingInfo(MappingConfigView mappingConfigView) {
        if (mappingConfigView.getDestColumns() == null && mappingConfigView.getDestColumns().length == 0) {
            throw new RuntimeException("targetColumn is empty");
        }
        List<MediaColumnMappingInfo> mappingList = new ArrayList<>();
        for (int i = 0; i < mappingConfigView.getDestColumns().length; i++) {
            MediaColumnMappingInfo mediaColumnMappingInfo = new MediaColumnMappingInfo();
            mediaColumnMappingInfo.setSourceColumn(mappingConfigView.getSrcColumns()[i]);
            mediaColumnMappingInfo.setTargetColumn(mappingConfigView.getDestColumns()[i]);
            mappingList.add(mediaColumnMappingInfo);
        }
        return mappingList;
    }

    private static void setMappingConfigView(MappingConfigView mappingConfigView, MediaSourceInfo targetMediaInfo, List<String> srcTableName, List<String> tarTableName, List<String> sourceColumns,
                                             List<String> targetColumns, List<String> joinColumns, List<String> isAddTablePrefixs, List<ColumnMappingMode> columnMappingModes) {
        String[] src_names = new String[srcTableName.size()];
        String[] dest_names = new String[tarTableName.size()];
        ColumnMappingMode[] columnMappingMode = new ColumnMappingMode[srcTableName.size()];
        String[] srcColumn = new String[srcTableName.size()];
        String[] tarColumn = new String[tarTableName.size()];
        String[] joinColumn = new String[srcTableName.size()];
        String[] isAddTablePrefix = new String[srcTableName.size()];

        src_names = srcTableName.toArray(src_names);
        dest_names = tarTableName.toArray(dest_names);

        columnMappingMode = columnMappingModes.toArray(columnMappingMode);
        srcColumn = sourceColumns.toArray(srcColumn);
        tarColumn = targetColumns.toArray(tarColumn);
        if (joinColumns.size() > 0) {
            joinColumn = joinColumns.toArray(joinColumn);
        }
        if (isAddTablePrefixs.size() > 0) {
            isAddTablePrefix = isAddTablePrefixs.toArray(isAddTablePrefix);
        }
        if (mappingConfigView != null) {
            mappingConfigView.setSrcNames(src_names);
            mappingConfigView.setDestNames(dest_names);
            mappingConfigView.setSrcColumns(srcColumn);
            mappingConfigView.setDestColumns(tarColumn);
            mappingConfigView.setJoinColumn(joinColumn);
            mappingConfigView.setIsAddTablePrefix(isAddTablePrefix);
            mappingConfigView.setColumnMappingMode(columnMappingMode);
        }
    }

    public static String getParameter(MediaSourceInfo targetMediaSourceInfo, String fileSplitMode) {
        if (StringUtils.isEmpty(fileSplitMode)) {
            return "{}";
        }
        if (targetMediaSourceInfo.getType() != MediaSourceType.HDFS && targetMediaSourceInfo.getType() != MediaSourceType.HDFS) {
            return "{}";
        }
        fileSplitMode = fileSplitMode.toUpperCase();
        FileSplitMode splitMode = fileSplitModeMap.get(fileSplitMode);
        if(splitMode == null) {
            splitMode = FileSplitMode.DAY;
        }
        HdfsFileParameter parameter = new HdfsFileParameter();
        List<FileSplitStrategy> strategyList = new ArrayList<>();
        FileSplitStrategy fileSplitStrategy = new FileSplitStrategy();
        fileSplitStrategy.setEffectiveDate(new Date());
        fileSplitStrategy.setFileSplitMode(splitMode);
        strategyList.add(fileSplitStrategy);
        parameter.setFileSplitStrategieList(strategyList);
        return parameter.toJsonString();
    }

    private static Long getGroupId(MediaSourceInfo srcInfo, MediaSourceInfo destInfo) {
        List<GroupInfo> groupInfos = groupService.getAllGroups();
        String srcInfoName = srcInfo.getName();
        String destInfoName = destInfo.getName();
        String suffix = srcInfoName.substring(srcInfoName.length() - 2);
        String prefix = srcInfoName.substring(0, srcInfoName.length() - 2);
        boolean isDoubleCenter = false;
        if (suffix.equals(NAME_SUFFIX_A)) {
            if ((prefix + NAME_SUFFIX_B).equals(destInfoName)) {
                isDoubleCenter = true;
            }
        } else if (suffix.equals(NAME_SUFFIX_B)) {
            if ((prefix + NAME_SUFFIX_A).equals(destInfoName)) {
                isDoubleCenter = true;
            }
        }
        if (isDoubleCenter) {
            List<String> wordList = new ArrayList<>();
            wordList.add("group_doubleCenter");
            wordList.add("group_acrossLab");
            return matchGroupId(groupInfos, wordList);
        } else if (srcInfoName.contains(KEY_WORD_LUCKY)) {
            List<String> wordList = new ArrayList<>();
            wordList.add("lucky");
            return matchGroupId(groupInfos, wordList);
        } else {
            return groupInfos.get(0).getId();
        }
    }


    private static Long matchGroupId(List<GroupInfo> groupInfos, List<String> wordList) {
        for (GroupInfo groupInfo : groupInfos) {
            for (String word : wordList) {
                if (groupInfo.getGroupName().contains(word)) {
                    return groupInfo.getId();
                }
            }
        }
        return groupInfos.get(0).getId();
    }

    /**
     * 配置映射的相关参数
     *
     * @author songwenbin
     * @date 2019/03/01
     */
    public static class MappingConfigView {
        private String[] srcNames;
        private String[] destNames;
        private String[] srcColumns;
        private String[] destColumns;
        private ColumnMappingMode[] columnMappingMode;
        private String[] joinColumn;
        private String[] isAddTablePrefix;
        private String parameter;

        public String[] getSrcNames() {
            return srcNames;
        }

        public void setSrcNames(String[] srcNames) {
            this.srcNames = srcNames;
        }

        public String[] getDestNames() {
            return destNames;
        }

        public void setDestNames(String[] destNames) {
            this.destNames = destNames;
        }

        public String[] getSrcColumns() {
            return srcColumns;
        }

        public void setSrcColumns(String[] srcColumns) {
            this.srcColumns = srcColumns;
        }

        public String[] getDestColumns() {
            return destColumns;
        }

        public void setDestColumns(String[] destColumns) {
            this.destColumns = destColumns;
        }

        public ColumnMappingMode[] getColumnMappingMode() {
            return columnMappingMode;
        }

        public void setColumnMappingMode(ColumnMappingMode[] columnMappingMode) {
            this.columnMappingMode = columnMappingMode;
        }

        public String[] getJoinColumn() {
            return joinColumn;
        }

        public void setJoinColumn(String[] joinColumn) {
            this.joinColumn = joinColumn;
        }

        public String[] getIsAddTablePrefix() {
            return isAddTablePrefix;
        }

        public void setIsAddTablePrefix(String[] isAddTablePrefix) {
            this.isAddTablePrefix = isAddTablePrefix;
        }

        public String getParameter() {
            return parameter;
        }

        public void setParameter(String parameter) {
            this.parameter = parameter;
        }
    }

}
