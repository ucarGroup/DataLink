package com.ucar.datalink.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.ucar.datalink.biz.cron.entity.EntityCronUtil;
import com.ucar.datalink.biz.cron.entity.EntityQuartzJob;
import com.ucar.datalink.biz.dal.SyncApplyDAO;
import com.ucar.datalink.biz.job.JobConfigBuilder;
import com.ucar.datalink.biz.job.JobContentParseUtil;
import com.ucar.datalink.biz.meta.*;
import com.ucar.datalink.biz.module.JobExtendProperty;
import com.ucar.datalink.biz.module.PropertyConstant;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.service.impl.JobServiceDynamicArgs;
import com.ucar.datalink.biz.spark.ColumnInfo;
import com.ucar.datalink.biz.spark.HBaseColumnInfo;
import com.ucar.datalink.biz.spark.SyncTableInfo;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.job.*;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.es.EsMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.writer.hdfs.FileSplitMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.FileSplitStrategy;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsFileParameter;
import com.ucar.datalink.domain.sync.*;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.domain.user.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created by yang.wang09 on 2018-04-27 14:39.
 * <p>
 * 包括以下重要的函数
 * 1.创建job(一个或多个)
 * 2.启动job(一个或多个)
 * 3.将新增/修改的列信息发送给CDSE(如果有新增/修改的)
 * 4.发送创建表的信息给CDSE
 */
public class SyncUtil {

    private static Logger logger = LoggerFactory.getLogger(SyncUtil.class);

    private static JobService jobService;

    private static MediaSourceService mediaSourceService;

    private static JobControlService jobControlService;

    private static SyncApplyDAO syncApplyDAO;

    private static MediaService mediaService;

    private static TaskConfigService taskService;

    private static UserService userService;

    private static JobScheduleService jobScheduleService;

    private static GroupService groupService;

    private static final String Full = "Full";

    private static final String Increment = "Increment";

    /**
     * 特殊的转义字符，创建全量任务时，有些特殊字符无法保存，先将这些字符用特殊的字符代替
     * 等传到后端后再将其还原为原始的字符
     */
    public static final String ESPECIALLY_CHAR_AND = "@AND@";

    public static final String ESPECIALLY_CHAR_AND_ORGINAL = "&";

    private static final String ES_CLUSTER_PREFIX = "es_";

    private static final String ES_CLUSTER_SUFFIX = "_vir";

    private static final String ES_DELETE_URL = ConfigReadUtil.getString("datax.es.delete.url");

    static {
        //init("biz/spring/datalink-biz.xml");
        //init("ext/spring/manager-ext.xml");
        jobService = DataLinkFactory.getObject(JobService.class);
        mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        jobControlService = DataLinkFactory.getObject(JobServiceDynamicArgs.class);
        syncApplyDAO = DataLinkFactory.getObject(SyncApplyDAO.class);
        mediaService = DataLinkFactory.getObject(MediaService.class);
        taskService = DataLinkFactory.getObject(TaskConfigService.class);
        userService = DataLinkFactory.getObject(UserService.class);
        jobScheduleService = DataLinkFactory.getObject(JobScheduleService.class);
        groupService = DataLinkFactory.getObject(GroupService.class);
    }


    public static void updatSyncApplyInfo(SyncApplyInfo syncApplyInfo) {
        syncApplyDAO.updateApplyStatus(syncApplyInfo);
    }


    public static List<JobConfigInfo> getJobConfigInfoList(long id) {
        List<JobConfigInfo> list = jobService.getJobConfigListByApplyId(id);
        return list;
    }


    /**
     * 创建若干个job
     *
     * @param syncApplyInfo
     */
    public static void createFullJob(SyncApplyInfo syncApplyInfo) {
        SyncApplyContent syncApplyContent = syncApplyInfo.getApplyContentObj();
        SyncApplyParameter syncApplyParameter = syncApplyContent.getApplyParameterList().get(0);
        List<SyncApplyMapping> syncApplyMappings = syncApplyParameter.getSyncApplyMappings();
        List<String> srcTableName = new ArrayList<>();
        List<String> tarTableName = new ArrayList<>();
        Map<String, List<String>> srcColumns = new HashMap<String, List<String>>();
        Map<String, List<String>> targetColumns = new HashMap<String, List<String>>();
        Map<String, ColumnMappingMode> mappingModes = new HashMap<>();
        Map<String, Map<String, String>> otherAttMapping = new HashMap<>();

        int index = 0;
        for (SyncApplyMapping applyMapping : syncApplyMappings) {
            srcTableName.add(applyMapping.getSourceTableName());
            tarTableName.add(applyMapping.getTargetTableName());
            if (applyMapping.getColumnMappingMode().isInclude() || applyMapping.getColumnMappingMode().isExclude()) {
                srcColumns.put(applyMapping.getSourceTableName()+index, applyMapping.getSourceColumn());
                targetColumns.put(applyMapping.getTargetTableName()+index, applyMapping.getTargetColumn());
                mappingModes.put(applyMapping.getSourceTableName(), applyMapping.getColumnMappingMode());
            }
            otherAttMapping.put(applyMapping.getSourceTableName()+index, applyMapping.getOtherMappingRelation());
            logger.info("createFullJob->" + applyMapping.toString());
            ++index;
        }
        String sourceTableName = Joiner.on(",").join(srcTableName);
        String targetTableName = Joiner.on(",").join(tarTableName);

        //createJobConfig
        Long srcMediaSourceId = syncApplyParameter.getSrcMediaSourceId();
        Long tarMediaSourceId = syncApplyParameter.getTargetMediaSourceId();
        MediaSourceInfo srcInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(srcMediaSourceId);
        MediaSourceInfo destInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(tarMediaSourceId);

        String[] src_names = sourceTableName.split(",");
        String[] dest_names;
        List<String> paths = new ArrayList<>();

        if (destInfo.getType() == MediaSourceType.HDFS) {
            dest_names = sourceTableName.split(",");
        } else {
            dest_names = targetTableName.split(",");
        }
        for (int i = 0; i < src_names.length; i++) {
            try {
                //JobConfigBuilder要改造了，支持源端输入列名，目标端输入列名
                List<String> tmp_src_columns = srcColumns.get(src_names[i]+i);
                List<String> tmp_target_columns = targetColumns.get(dest_names[i]+i);
                ColumnMappingMode mode = mappingModes.get(src_names[i]);
                Map<String, String> map = otherAttMapping.get(src_names[i]+i);
                JobExtendProperty jobExtendProperty = assembleJobExtentProperty(map);

                String jobContent = JobConfigBuilder.buildJson(srcInfo, destInfo, jobExtendProperty, src_names[i],
                        tmp_src_columns, dest_names[i], tmp_target_columns, mode);
                //String jobContent = JobConfigBuilder.buildJson(srcInfo, destInfo, new JobExtendProperty(), src_names[i], dest_names[i]);
                jobContent = JobConfigBuilder.modifySyncApplyJobContentSpeed(srcInfo,destInfo,jobContent);
                if (destInfo.getType() == MediaSourceType.ELASTICSEARCH) {
                    jobContent = JobConfigBuilder.buildJson(srcInfo, destInfo, jobExtendProperty, src_names[i],
                            tmp_src_columns, src_names[i], tmp_target_columns, mode);
                }

                jobContent = DataxUtil.formatJson(jobContent);
                JobConfigInfo info = new JobConfigInfo();
                info.setJob_content(jobContent);
                info.setJob_media_name(dest_names[i]);
                info.setJob_src_media_source_id(Integer.parseInt(srcMediaSourceId.toString()));
                info.setJob_target_media_source_id(Integer.parseInt(tarMediaSourceId.toString()));

                String job_name = dest_names[i] + "_" + DataxUtil.randomString(10);
                info.setJob_name(job_name);
                info.setTiming_yn(false);
                info.setApply_id(syncApplyInfo.getId());

                if (destInfo.getType() == MediaSourceType.ELASTICSEARCH) {
                    List<MediaSourceInfo> mediaSourceInfos = VirtualDataSourceUtil.fromRealMediaSourceToAllMediaSource(tarMediaSourceId);
                    mediaSourceInfos.forEach(mediaSourceInfo -> {
                        EsMediaSrcParameter parameter = mediaSourceInfo.getParameterObj();
                        String value = parameter.getClusterHosts();
                        String newJson = JobConfigBuilder.modifyWriterPath(info.getJob_content(), value);
                        info.setJob_content(newJson);
                        String tmpName = info.getJob_name();
                        String random_name = tmpName + "_" + randomString(10);
                        info.setJob_name(random_name);
                        if (info.isTiming_yn()) {
                            random_name = "CRON_" + random_name;
                            info.setJob_name(random_name);
                            createSchedule(info);
                        } else {
                            createAndSend(info);
                        }
                        info.setJob_name(tmpName);
                        random_name = "";
                    });
                } else {
                    createAndSend(info);
                }


                paths.add(DataxUtil.parseHDFSWritePath(info));
                logger.info("Create job config succeed. applyId =" + syncApplyInfo.getId() + " and jobId = " + info.getId());
            } catch (Exception e) {
                logger.error("Create job config failed. applyId =" + syncApplyInfo.getId(), e);
            }
        }
        MailUtil.sendMailByAsynchronous(srcInfo, destInfo, sourceTableName, paths, syncApplyInfo);
    }


    /**
     * 批量执行若干job
     *
     * @param applyInfo
     */
    public static void executeJob(SyncApplyInfo applyInfo) {
        List<JobConfigInfo> jobList = jobService.getJobConfigListByApplyId(applyInfo.getId());
        for (JobConfigInfo jobConfig : jobList) {
            startJob(jobConfig);
        }
    }

    /**
     * 启动一个job
     *
     * @param jobConfigInfo
     */
    private static void startJob(JobConfigInfo jobConfigInfo) {
        try {
            long id = jobConfigInfo.getId();
            String jobName = jobConfigInfo.getJob_name();
            String worker = "";

            if (StringUtils.isBlank(String.valueOf(id))) {
                logger.warn("job id is null");
                throw new RuntimeException("Start job failed : job id is null.");
            }
            if (StringUtils.isBlank(jobName)) {
                logger.warn("job name is null");
                throw new RuntimeException("Start job failed : job name is null.");
            }

            DataxCommand command = new DataxCommand();
            command.setJobId(id);
            command.setJobName(jobName);
            command.setType(DataxCommand.Type.Start);
            String result = jobControlService.start(command, worker);
        } catch (Exception e) {
            logger.error("Auto start job failed. jobId = " + jobConfigInfo.getId(), e);

        }
    }


    public static boolean checkFullFinish(SyncApplyInfo applyInfo) {
        boolean isFinish;
        FullFinishCheck check = checkFullResult(applyInfo);
        if (check.totalJob > 0 && check.totalJob == check.succeedJobIds.size()) {
            applyInfo.setApplyStatus(SyncApplyStatus.FULL_FINISH);
            applyInfo.setReplyRemark("同步申请全量job执行成功！applyId = " + applyInfo.getId() + "请确认是否需要增量，并通知管理员执行!");
            if (!MailUtil.getMailedIfPresent(applyInfo.getId(), SyncApplyStatus.FULL_FINISH)) {
                applyInfo.setNeedNotify(true);
                MailUtil.getMailed(applyInfo.getId(), SyncApplyStatus.FULL_FINISH);
            }
            syncApplyDAO.updateApplyStatus(applyInfo);
            logger.info("All jobs execute succeed. applyId = " + applyInfo.getId() + ".jobIds:" + check.succeedJobIds.toString());
            isFinish = true;
        } else if (check.failedJobIds.size() > 0 && check.totalJob == (check.succeedJobIds.size() + check.failedJobIds.size())) {
            applyInfo.setApplyStatus(SyncApplyStatus.FULL_FAILED);
            applyInfo.setReplyRemark("同步申请全量job执行失败！applyId = " + applyInfo.getId() +
                    "<br>" + "<br>" + "失败的jobIds：" + check.failedJobIds.toString() + "<br>" + "<br>" + "成功的jobIds：" + check.succeedJobIds.toString());
            if (!MailUtil.getMailedIfPresent(applyInfo.getId(), SyncApplyStatus.FULL_FAILED)) {
                applyInfo.setNeedNotify(true);
                MailUtil.getMailed(applyInfo.getId(), SyncApplyStatus.FULL_FAILED);
            }
            syncApplyDAO.updateApplyStatus(applyInfo);
            isFinish = false;
        } else {
            isFinish = false;
        }
        return isFinish;
    }

    public static FullFinishCheck checkFullResult(SyncApplyInfo info) {
        List<JobConfigInfo> jobList = jobService.getJobConfigListByApplyId(info.getId());
        List<Long> succeedJobIds = new ArrayList<>();
        List<Long> failedJobIds = new ArrayList<>();
        for (JobConfigInfo jobConfig : jobList) {
            JobExecutionInfo jobExecutionInfo = jobService.lastExecuteJobExecutionInfo(jobConfig.getId());
            if (jobExecutionInfo != null) {
                if (JobExecutionState.SUCCEEDED.equals(jobExecutionInfo.getState())) {
                    succeedJobIds.add(jobConfig.getId());
                    logger.info("job executes succeed. jobId = " + jobConfig.getId());
                } else if (JobExecutionState.FAILED.equals(jobExecutionInfo.getState())) {
                    failedJobIds.add(jobConfig.getId());
                    logger.info("job executes failed. jobId = " + jobConfig.getId());
                }
            }
        }
        FullFinishCheck check = new FullFinishCheck();
        check.failedJobIds.addAll(failedJobIds);
        check.succeedJobIds.addAll(succeedJobIds);
        check.totalJob = jobList.size();
        return check;
    }

    public static boolean fullFirst(SyncApplyInfo applyInfo) {
        boolean fullFirst;
        SyncApplyContent syncApplyContent = applyInfo.getApplyContentObj();
        SyncApplyParameter syncApplyParameter = syncApplyContent.getApplyParameterList().get(0);
        Long tarMediaSourceId = syncApplyParameter.getTargetMediaSourceId();
        MediaSourceInfo destInfo = mediaSourceService.getById(tarMediaSourceId);
        fullFirst = destInfo.getType() != MediaSourceType.HDFS;
        return fullFirst;
    }

    public static boolean isAutokeeper(SyncApplyInfo applyInfo) {
        boolean isAutokeeper = false;
        List<SyncApproveInfo> approveInfo = syncApplyDAO.getSyncApproveInfoByApplyId(applyInfo.getId());
        if (approveInfo.size() == 1) {
            UserInfo approveUser = userService.getById(approveInfo.get(0).getApproveUserId());
            List<RoleInfo> roles = approveUser.getRoleInfoList();
            for (RoleInfo role : roles) {
                if (role.getCode().equals("AUTOKEEPER")) {
                    isAutokeeper = true;
                }
            }
        }
        return isAutokeeper;
    }

    /**
     * 在执行job之前，获取最新的列信息，然后跟表中保存的job列信息比较，具体步骤如下：
     * 1.首先根据JobConfig信息，获取job的json内容
     * 2.解析json内容，获取column列信息
     * 3.根据Joconfig的源库，表等内容获取最新的列信息
     * 4.比较新列，旧列，找出新增的/修改的列
     * 5.将新增/修改(如果有的话)的列发送给CDSE
     *
     * @param jobConfig
     * @throws Exception
     */
    public static void sendModifyJobToCDSE(JobConfigInfo jobConfig) throws Exception {

    }


    /**
     * 当job执行成功后，发送创建表的信息给CDSE
     * 参考链接
     * http://wiki.10101111.com/pages/viewpage.action?pageId=173033468
     */
    public static void sendCreateJobInfoToCDSE(SyncApplyInfo applyInfo, JobConfigInfo jobConfig) throws Exception {
        //根据同步申请信息，job config信息来封装JSON，发送给CDSE
        if (jobConfig.isTiming_yn()) {
            return;
        }
        String email = assembleEmail(applyInfo);
        boolean isFull = isFull(applyInfo);
        boolean isIncrement = isIncrement(applyInfo);
        sendToCDSE(jobConfig, email, isFull, isIncrement);
    }


    public static void sendCreateJobInfoToCDSE(JobConfigInfo jobConfig) throws Exception {
//        if (jobConfig.isTiming_yn()) {
//            return;
//        }
        long targetMediaSourceId = jobConfig.getJob_target_media_source_id();
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(targetMediaSourceId);
        if (mediaSourceInfo.getType() != MediaSourceType.HDFS) {
            return;
        }
        sendToCDSE(jobConfig, "", true, false);
    }


    private static void sendToCDSE(JobConfigInfo jobConfig, String email, boolean isFull, boolean isIncrement) throws Exception {
        //根据同步申请信息，job config信息来封装JSON，发送给CDSE
        long mediaSourceId = jobConfig.getJob_src_media_source_id();
        MediaSourceInfo mediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(mediaSourceId);

        List<ColumnMeta> columns = null;
        String tableName = parseJobMediaName(jobConfig.getJob_media_name());
        if (mediaSourceInfo.getType() == MediaSourceType.MYSQL || mediaSourceInfo.getType() == MediaSourceType.SQLSERVER || mediaSourceInfo.getType() == MediaSourceType.POSTGRESQL) {
            columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
        } else if(mediaSourceInfo.getType() == MediaSourceType.ORACLE || mediaSourceInfo.getType() == MediaSourceType.HANA) {
            columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
        } else if (mediaSourceInfo.getType() == MediaSourceType.HDFS) {
            columns = HDFSUtil.getColumns(mediaSourceInfo, tableName);
        } else if (mediaSourceInfo.getType() == MediaSourceType.ELASTICSEARCH) {
            columns = ElasticSearchUtil.getColumns(mediaSourceInfo, tableName);
        } else if (mediaSourceInfo.getType() == MediaSourceType.HBASE) {
            columns = HBaseUtil.getColumns(mediaSourceInfo, tableName);
        } else {
            throw new UnsupportedOperationException("unsupport db type " + mediaSourceInfo.getType() + "  job_config->" + jobConfig);
        }

        MediaMeta mm = new MediaMeta();
        mm.setColumn(columns);
        mm.setDbType(mediaSourceInfo.getType());
        mm.setName(mediaSourceInfo.getName());
        mm.setNameSpace(mediaSourceInfo.getParameterObj().getNamespace());
        MediaMeta transformMeta = MetaMapping.transformToHDFS(mm);

        ColumnInfo[] columnInfos = null;
        if (mediaSourceInfo.getType() == MediaSourceType.HBASE) {
            columnInfos = new HBaseColumnInfo[columns.size()];
            for (int i = 0; i < columnInfos.length; i++) {
                HBaseColumnInfo hci = new HBaseColumnInfo();
                ColumnMeta cm = columns.get(i);
                hci.setColName(cm.getName());
                hci.setFamilyName(cm.getColumnFamily());
                hci.setComment(cm.getColumnDesc());
                hci.setHiveType(transformMeta.getColumn().get(i).getType());
                columnInfos[i] = hci;
            }
        } else {
            columnInfos = new ColumnInfo[columns.size()];
            for (int i = 0; i < columnInfos.length; i++) {
                ColumnInfo ci = new ColumnInfo();
                ColumnMeta cm = columns.get(i);
                ci.setComment(cm.getColumnDesc());
                ci.setName(cm.getName());
                ci.setType(cm.getType());
                ci.setTypeLength("" + cm.getLength());
                ci.setTypePrecision("" + cm.getDecimalDigits());
                ci.setHiveType(transformMeta.getColumn().get(i).getType());
                columnInfos[i] = ci;
            }
        }
        String hdfsPath = parseHDFSPath(jobConfig.getJob_content());
        SyncTableInfo table = new SyncTableInfo();
        table.setApplicant(email);
        table.setColumns(columnInfos);
        table.setDatabase(mediaSourceInfo.getName());
        table.setDbType(mediaSourceInfo.getType().name());
        table.setHdfsLocation(hdfsPath);
        table.setIsFull(isFull);
        table.setIsIncrement(isIncrement);
        table.setTable(tableName);

        SyncTableInfo[] tt = new SyncTableInfo[1];
        tt[0] = table;
        String msg = JSONObject.toJSONString(tt);
        logger.info("sendCreateJobInfoToCDSE [send info] ->" + msg);
        Map<String, String> kv = new HashMap<String, String>();
        kv.put("data", msg);
        String token = ConfigReadUtil.getString("datax.auto.cdse.token");
        kv.put("token", token);

        String url = ConfigReadUtil.getString("datax.auto.cdse.create.url");
        String result = HttpUtils.post(url, kv);
        //根据 result内容，判断是否执行成功
        String createInfo = table.getTable() + " " + table.getDatabase() + " " + table.getTable();
        logger.info("sendCreateJobInfoToCDSE [" + createInfo + "] result -> " + result);

    }


    public static void checkModifyColumn(JobConfigInfo info) {
        try {
            long srcId = info.getJob_src_media_source_id();
            long destId = info.getJob_target_media_source_id();
            MediaSourceInfo srcInfo = mediaSourceService.getById(srcId);
            MediaSourceInfo destInfo = mediaSourceService.getById(destId);
            if (srcInfo.getType() != MediaSourceType.MYSQL && srcInfo.getType() != MediaSourceType.SQLSERVER) {
                return;
            }
            if (destInfo.getType() != MediaSourceType.HDFS) {
                return;
            }

            SyncUtil.sendModifyJobToCDSE(info);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static class FullFinishCheck {
        public int totalJob = 0;
        public List<Long> succeedJobIds = new ArrayList<>();
        public List<Long> failedJobIds = new ArrayList<>();
    }


    private static String assembleEmail(SyncApplyInfo info) {
        return info.getApplyUserInfo().getUcarEmail() + "@ucarinc.com";
    }


    private static boolean isFull(SyncApplyInfo applyInfo) {
        if (Full.equals(applyInfo.getApplyType()) || applyInfo.getIsInitialData()) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isIncrement(SyncApplyInfo applyInfo) {
        if (Increment.equals(applyInfo.getApplyType())) {
            return true;
        } else {
            return false;
        }
    }


    @Transactional
    private static void createAndSend(JobConfigInfo info) {
        try {
            parseAndSetJobSrcTargetTable(info);
            jobService.createJobConfig(info);
            SyncUtil.sendCreateJobInfoToCDSE(info);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    @Transactional
    private static void createSchedule(JobConfigInfo info) {
        try {
            parseAndSetJobSrcTargetTable(info);
            jobService.createJobConfig(info);
            SyncUtil.sendCreateJobInfoToCDSE(info);
            long jobId = jobService.latestJobConfigRecord().getId();
            JobScheduleInfo scheduleInfo = EntityCronUtil.assembleDefaultScheduleInfo(jobId);
            scheduleInfo.setJobId(jobId);
            jobScheduleService.create(scheduleInfo);
            long scheduleId = jobScheduleService.latestJobScheduleRecord().getId();
            scheduleInfo.setId(scheduleId);
            EntityQuartzJob job = EntityCronUtil.assembleCronTaskWithMD5(info, scheduleInfo);
            EntityCronUtil.schdule(job);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public static void parseAndSetJobSrcTargetTable(JobConfigInfo info) {
        String json = info.getJob_content();
        String readType = JobContentParseUtil.parseJobReaderType(json);
        String writeType = JobContentParseUtil.parseJobWriterType(json);

        String readerTable = JobContentParseUtil.getReaderTable(json);
        String writerTable = JobContentParseUtil.getWriterTable(json);
        if(StringUtils.isNotBlank(readerTable)) {
            info.setJob_media_name(readerTable);
        }
        if(StringUtils.isNotBlank(writerTable)) {
            info.setJob_media_target_name(writerTable);
        }
    }


    public static String parseJobMediaName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        } else {
            if (name.contains("\\.")) {
                return name.split("\\.")[0];
            }
            return name;
        }
    }

    public static String parseHDFSPath(String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        String path = (String) connConf.get("job.content[0].writer.parameter.path");
        if (StringUtils.isBlank(path)) {
            return "";
        } else {
            return path;
        }
    }


    public static boolean delESWriterIndex(JobConfigInfo info) {
        if(info == null) {
            return false;
        }
        try {
            long targetMediaSourceId = info.getJob_target_media_source_id();
            MediaSourceInfo mediaSourceInfo = mediaService.getMediaSourceById(targetMediaSourceId);
            if(mediaSourceInfo.getType() != MediaSourceType.ELASTICSEARCH) {
                return false;
            }
            if( JobConfigBuilder.isESWriterPreDel(info.getJob_content()) ) {
                //String esClusterName = mediaSourceInfo.getName();
                String esClusterName = "";
                //目前只有 es6.3.0_wjl 这个集群可以支持删除功能
                //这个名字是ES那边的集群名字，不是在data-link上配置的
                //通过mediaSourceInfo.getDesc()获取的集群描述，就是ES那边的集群名字
                if(StringUtils.isNotBlank(mediaSourceInfo.getDesc())) {
                    esClusterName = mediaSourceInfo.getDesc();
                }
//                if(esClusterName.startsWith(ES_CLUSTER_PREFIX)) {
//                    esClusterName = esClusterName.substring(ES_CLUSTER_PREFIX.length());
//                    if( esClusterName.endsWith(ES_CLUSTER_SUFFIX) ) {
//                        int length = esClusterName.length() - ES_CLUSTER_SUFFIX.length();
//                        esClusterName = esClusterName.substring(0,length);
//                    }
//                }
                String esIndex = JobConfigBuilder.getESWriterIndex(info.getJob_content());
                String url = ES_DELETE_URL+"?clusterName="+esClusterName+"&indexName="+esIndex;
                logger.info("pre del url->"+url);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
                String body = responseEntity.getBody();
                logger.info("response body->"+body);
                return true;
            }
            return false;
        }catch(Exception e) {
            logger.error(e.getMessage(),e);
            return false;
        }
    }




    public static boolean isAdaptModify(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            String adapt = (String) connConf.get("job.setting.adaptiveFieldModify");
            boolean isAdapt = Boolean.parseBoolean(adapt);
            if (isAdapt) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }


    public static void main(String[] args) {

    }

    public static void init(String filePath) {
        try {
            org.springframework.context.support.ClassPathXmlApplicationContext initializedContext =
                    new org.springframework.context.support.ClassPathXmlApplicationContext(filePath) {

                        @Override
                        protected void customizeBeanFactory(org.springframework.beans.factory.support.DefaultListableBeanFactory beanFactory) {
                            super.customizeBeanFactory(beanFactory);
                            beanFactory.setAllowBeanDefinitionOverriding(false);
                        }
                    };

            // ContextHolder.applicationContext = initializedContext;
        } catch (Throwable e) {
            throw new RuntimeException("ERROR ## Datalink Factory initial failed.", e);
        }
    }





    public static String randomString(int len) {
        len = 10;
        String chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678";    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
        int maxPos = chars.length();
        String pwd = "";
        for (int i = 0; i < len; i++) {
            pwd += chars.charAt((int) Math.floor(Math.random() * maxPos));
        }
        return pwd;
    }


    //private static void assembleJobContent(MediaSourceInfo srcInfo,MediaSourceInfo destInfo,JobExtendProperty jobExtendProperty,
    //String[] src_names,List<String> tmp_src_columns,String[] dest_names,List<String> tmp_target_columns,ColumnMappingMode mode) {
    //    JobConfigBuilder.buildJson(srcInfo,destInfo,jobExtendProperty,src_names[i],
    //            tmp_src_columns,dest_names[i],tmp_target_columns,mode);
    //}

    private static JobExtendProperty assembleJobExtentProperty(Map<String, String> map) {
        JobExtendProperty property = new JobExtendProperty();
        if (map == null || map.size() == 0) {
            return property;
        }
        logger.info("assembleJobExtentProperty->" + map.toString());
        Map<String, String> reader = new HashMap<>();
        Map<String, String> writer = new HashMap<>();
        for (Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            if (PropertyConstant.ES_JOIN_COLUMN.equals(key)) {
                writer.put(PropertyConstant.ES_JOIN_COLUMN, entry.getValue());
            }
            if (PropertyConstant.ES_READER_INDEX_TYPE.equals(key)) {
                reader.put(PropertyConstant.ES_READER_INDEX_TYPE, entry.getValue());
            }
            if (PropertyConstant.ES_WRITER_INDEX_TYPE.equals(key)) {
                writer.put(PropertyConstant.ES_WRITER_INDEX_TYPE, entry.getValue());
            }
            if (PropertyConstant.ES_IS_TABLE_PREFIX.equals(key)) {
                writer.put(PropertyConstant.ES_IS_TABLE_PREFIX, entry.getValue());
            }
            if(PropertyConstant.WHERE_CONDITION.equals(key)) {
                reader.put(PropertyConstant.WHERE, entry.getValue());
            }
        }
        property.setReader(reader);
        property.setWriter(writer);
        return property;
    }


}
