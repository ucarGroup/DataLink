package com.ucar.datalink.manager.core.web.controller.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.job.DataxJobConfigConstant;
import com.ucar.datalink.biz.job.JobConfigBuilder;
import com.ucar.datalink.biz.mapping.AbstractMapping;
import com.ucar.datalink.biz.mapping.MappingFactory;
import com.ucar.datalink.biz.meta.MetaManager;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.biz.module.JobExtendProperty;
import com.ucar.datalink.biz.module.TimingJobExtendPorperty;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.ddl.DdlSqlUtils;
import com.ucar.datalink.biz.utils.ddl.SQLStatementHolder;
import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.*;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.relationship.SqlType;
import com.ucar.datalink.domain.sync.SyncApplyMapping;
import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.vo.ResponseVo;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.controller.task.BaseTaskController;
import com.ucar.datalink.manager.core.web.util.*;
import com.ucar.datalink.util.DataxUtil;
import com.ucar.datalink.util.SyncModifyUtil;
import com.ucar.datalink.util.VirtualDataSourceUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * job任务的新接口，之前jobService接口还保留，以后新的接入使用这个新接口
 *
 * @author songwenbin
 * @date 2019/03/01
 */
@Controller
@LoginIgnore
@RequestMapping(value = "/jobServer")
public class JobServerController extends BaseTaskController {
    private static final Logger logger = LoggerFactory.getLogger(JobServerController.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

    //开启定时任务
    private static final String TIMING_YES = "1";

    @Autowired
    private MediaSourceService mediaSourceService;
    @Autowired
    private TaskConfigService taskService;
    @Autowired
    private SysPropertiesService sysPropertiesService;
    @Autowired
    private JobService jobService;
    @Autowired
    @Qualifier("dynamic")
    JobControlService jobControlService;
    @Autowired
    private MediaService mediaService;
    @Autowired
    private SyncRelationService syncRelationService;
    @Autowired
    @Qualifier("taskPositionServiceZkImpl")
    private TaskPositionService taskPositionService;
    @Autowired
    DoubleCenterDataxService doubleCenterDataxService;

    @RequestMapping("/configMapping")
    @ResponseBody
    public Object configMapping(String srcDbId, String destDbType, String destDbName, String tableMappings,
                                String schema, String fileSplitMode, String source, String resetTime) {
        logger.info("receive data : srcDbId=" + srcDbId + " , destDbName=" + destDbName + " , tableMappings=" + tableMappings + " ,schema=" + schema
        +", fileSplitMode="+fileSplitMode+", resetTime="+resetTime);

        ResponseVo responseVo = new ResponseVo();
        try {
            //1,校验参数的合法性
            if (StringUtils.isEmpty(srcDbId)) {
                throw new ErrorException(CodeContext.SRCDBID_NOTNULL_ERROR_CODE);
            }
            if (StringUtils.isEmpty(destDbName)) {
                throw new ErrorException(CodeContext.DESTDBNAME_NOTNULL_ERROR_CODE);
            }
            if (StringUtils.isEmpty(source)) {
                throw new ErrorException(CodeContext.SOURCE_NOTNULL_ERROR_CODE);
            }
            if (StringUtils.isEmpty(tableMappings)) {
                throw new ErrorException(CodeContext.TABLEMAPPINGS_NOTNULL_ERROR_CODE);
            }
            if (!NumberUtil.isInteger(srcDbId)) {
                throw new ErrorException(CodeContext.STRING_NOTNUMBER_ERROR_CODE);
            }

            MediaSourceInfo srcMediaSourceInfo = mediaSourceService.getById(Long.valueOf(srcDbId));
            MediaSourceInfo targetMediaSourceInfo = mediaSourceService.getOneByName(destDbName);
            if (srcMediaSourceInfo == null) {
                throw new ErrorException(CodeContext.SRCSOURCE_NOTFUND_ERROR_CODE);
            }
            if (targetMediaSourceInfo == null) {
                throw new ErrorException(CodeContext.TARGET_NOTFUND_ERROR_CODE);
            }
            List<MediaSourceInfo> realMediaSourceInfoList = mediaService.listRealMediaSourceInfos(srcMediaSourceInfo);
            List<MediaSourceInfo> realTargetMediaSourceInfoList = mediaService.listRealMediaSourceInfos(targetMediaSourceInfo);

            JSONArray array = JSONArray.parseArray(tableMappings);
            String[] sourceTableName = new String[array.size()];
            String[] targetTableName = new String[array.size()];
            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                Iterator<Map.Entry<String, Object>> it = jsonObject.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> entry = it.next();
                    sourceTableName[i] = entry.getKey();
                    targetTableName[i] = (String) entry.getValue();
                }
            }
            Set<String> tableNameSet = new HashSet<>();
            //校验mysql数据库表是否有主键
            MediaMappingConfigUtil.validateMysqlTablePk(realMediaSourceInfoList, sourceTableName, tableNameSet);
            //校验目标数据源是否存在
            MediaMappingConfigUtil.validateExistsTargetMedia(realTargetMediaSourceInfoList,sourceTableName, tableNameSet, targetTableName);

            //2,创建任务（先判断是否有相应的任务，如果有则复用之前的，如果没有则创建一个新的）
            TaskInfo taskInfo = IncrementSyncUtil.findExistsTask(srcMediaSourceInfo, targetMediaSourceInfo, ManagerConfig.current().getIsReuseTask());
            boolean isFirst = false;
            if (null == taskInfo) {
                SysPropertiesInfo sysPropertiesInfo = sysPropertiesService.getSysPropertiesByKey(source + "_group");
                if (sysPropertiesInfo == null) {
                    throw new ErrorException(CodeContext.GROUP_NOTFOUND_ERROR_CODE);
                }
                //创建task
                if (srcMediaSourceInfo.getType() == MediaSourceType.MYSQL || srcMediaSourceInfo.getSimulateMsType() == MediaSourceType.MYSQL) {
                    taskInfo = taskService.createTask(srcMediaSourceInfo, targetMediaSourceInfo, Long.valueOf(sysPropertiesInfo.getPropertiesValue()), ManagerConfig.current().getZkServer(),
                            ManagerConfig.current().getCurrentEnv());
                }
                isFirst = true;
            }
            if (taskInfo == null) {
                throw new ErrorException(CodeContext.TASK_CREATED_ERROR_CODE);
            }
            //3,创建映射
            IncrementSyncUtil.MappingConfigView mappingConfigView = new IncrementSyncUtil.MappingConfigView();
            List<SyncApplyMapping> syncApplyMappings = getSyncMappingList(tableMappings);
            List<Long> list = IncrementSyncUtil.prepareMappingInfo(taskInfo, srcMediaSourceInfo, targetMediaSourceInfo, syncApplyMappings, mappingConfigView);
            mappingConfigView.setParameter(IncrementSyncUtil.getParameter(targetMediaSourceInfo, fileSplitMode));

            List<Long> mappingIdList = null;
            if (mappingConfigView.getSrcNames().length > 0) {
                if (!StringUtils.isEmpty(schema)) {
                    targetMediaSourceInfo.getParameterObj().setNamespace(schema);
                }
                mappingIdList = mediaService.insert(
                        IncrementSyncUtil.buildMediaInfo(mappingConfigView.getSrcNames(), srcMediaSourceInfo, srcMediaSourceInfo.getId()),
                        IncrementSyncUtil.buildMediaMappingInfo(mappingConfigView, targetMediaSourceInfo, taskInfo.getId()),
                        IncrementSyncUtil.buildMediaColumnMappingInfo(mappingConfigView)
                );
            }

            if (srcMediaSourceInfo.getType() == MediaSourceType.MYSQL) {
                syncRelationService.clearSyncRelationCache();//清空同步检测关系中的缓存
                if (!StringUtils.isEmpty(resetTime)) {
                    MysqlReaderPosition position = (MysqlReaderPosition) taskPositionService.getPosition(String.valueOf(taskInfo.getId()));
                    if (position == null) {
                        //重试3次，防止刚创建的Task还开始消费，位点重置失败
                        int i = 0;
                        while (i < 3) {
                            i++;
                            MysqlReaderPosition newposition = (MysqlReaderPosition) taskPositionService.getPosition(String.valueOf(taskInfo.getId()));
                            if (newposition != null) {
                                newposition.setTimestamp(Long.valueOf(resetTime));
                                newposition.setSourceAddress(new InetSocketAddress("0.0.0.0", newposition.getSourceAddress().getPort()));
                                sendRestartCommand(String.valueOf(taskInfo.getId()), newposition);
                                break;
                            } else {
                                Thread.sleep(1000);
                            }
                            if (i == 3) {
                                logger.info("Task重启失败！还未开始消费，无法重置位点");
                                throw new ErrorException(CodeContext.TASK_RESTART_ERROR_CODE);
                            }
                        }

                    } else {
                        position.setTimestamp(Long.valueOf(resetTime));
                        position.setSourceAddress(new InetSocketAddress("0.0.0.0", position.getSourceAddress().getPort()));
                        sendRestartCommand(String.valueOf(taskInfo.getId()), position);

                    }
                } else {
                    //重试3次，防止刚创建的Task还未来得及分配Worker，重启失败
                    int i = 0;
                    while (i < 3) {
                        try {
                            i++;
                            sendRestartCommand(String.valueOf(taskInfo.getId()), null);
                            break;
                        } catch (Exception e) {
                            logger.info("Task重启失败",e);
                            Thread.sleep(1000);
                        }
                        if (i == 3) {
                            logger.info("Task重启失败！");
                            throw new ErrorException(CodeContext.TASK_RESTART_ERROR_CODE);
                        }
                    }
                }
            } else {
                //清空Task的映射缓存
                mediaService.cleanTableMapping(taskInfo.getId());
            }

            if (mappingIdList == null) {
                mappingIdList = new ArrayList<>();
            }
            //4,返回任务id和映射id列表
            if (list != null && list.size() > 0) {
                for (Long mappingId : list) {
                    mappingIdList.add(mappingId);
                }
            }
            responseVo.getData().put("taskId", taskInfo.getId());
            responseVo.getData().put("mappingIds", mappingIdList);
        } catch (ErrorException e) {
            logger.error("创建配置映射出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()) + ":" + e.getMessage());
        }
        return responseVo;
    }


    @RequestMapping(value = "/createJob")
    @ResponseBody
    public Object create(String srcDbId, String destDbType, String destDbName, String table, String hbaseColumns,
                         String timingYn, String timingTransferType, String where, String source, String tarTableName,
                         String columns, String extendedParam) {
        logger.info("receive data : srcDbId=" + srcDbId + " , destDbName=" + destDbName + " , table=" + table + " ,hbaseColumns=" + hbaseColumns+"," +
                "tarTableName = "+tarTableName);
        ResponseVo responseVo = new ResponseVo();
        try {
            if (StringUtils.isEmpty(srcDbId)) {
                throw new ErrorException(CodeContext.SRCDBID_NOTNULL_ERROR_CODE);
            }
            if (StringUtils.isEmpty(destDbName)) {
                throw new ErrorException(CodeContext.DESTDBNAME_NOTNULL_ERROR_CODE);
            }
            if (StringUtils.isEmpty(table)) {
                throw new ErrorException(CodeContext.DESTDBNAME_NOTNULL_ERROR_CODE);
            }
            if (StringUtils.isEmpty(source)) {
                throw new ErrorException(CodeContext.SOURCE_NOTNULL_ERROR_CODE);
            }
            if (!NumberUtil.isInteger(srcDbId)) {
                throw new ErrorException(CodeContext.STRING_NOTNUMBER_ERROR_CODE);
            }
            if (StringUtils.isEmpty(timingYn)) {
                timingYn = "0";
            }

            Map<String,String> extendedMap = new HashMap<String,String>();
            if( StringUtils.isNotBlank(extendedParam) ) {
                extendedMap = JSONObject.parseObject(extendedParam,Map.class);
            }
            long srcId = Long.parseLong(srcDbId);
            MediaSourceInfo targetMediaSourceInfo = mediaSourceService.getOneByName(destDbName);
            if (targetMediaSourceInfo == null) {
                throw new ErrorException(CodeContext.TARGETMEDIA_NOTEXISTS_ERROR_CODE);
            }

            MediaSourceInfo srcMediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(srcId);
            MediaSourceInfo realTargetMediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoByInfo(targetMediaSourceInfo);

            JobExtendProperty jobExtendProperty = new JobExtendProperty();
            TimingJobExtendPorperty timing = new TimingJobExtendPorperty();
            if (timingYn.equals(TIMING_YES)) {
                timing.setIsOpen("true");
                timing.setType(timingTransferType);
            }
            jobExtendProperty.setTiming(timing);
            if (!StringUtils.isEmpty(where) && !"null".equalsIgnoreCase(where)) {
                jobExtendProperty.getReader().put("where", where);
            }
            if(StringUtils.isEmpty(tarTableName)) {
                tarTableName = table;
            }

            String json = JobConfigBuilder.buildJson(srcMediaSourceInfo, table, realTargetMediaSourceInfo, tarTableName, jobExtendProperty);
            if (StringUtils.isBlank(json)) {
                throw new ErrorException(CodeContext.GEN_CONFIG_ERROR_CODE);
            }

            //设置hbaseColumns
            json = setHbaseColumns(srcMediaSourceInfo, table, hbaseColumns, json, destDbType);
            json = setColumns(srcMediaSourceInfo, table, columns, json, destDbType);
            json = JobConfigBuilder.modifySyncApplyJobContentSpeed(srcMediaSourceInfo, targetMediaSourceInfo, json);
            if (targetMediaSourceInfo.getType() == MediaSourceType.ELASTICSEARCH) { //默认不加表前缀
                json = isAddTablePrefix(json, false);
            }
            JobConfigInfo jobConfigInfo = createJobConfigInfo(targetMediaSourceInfo, srcId, table, json, source, timingTransferType);
            jobConfigInfo.setJob_content(JobConfigBuilder.removeEscapeCharacter(jobConfigInfo.getJob_content()));
            //处理扩展情况
            jobConfigInfo.setJob_content( JobConfigBuilder.processExtend(jobConfigInfo.getJob_content(),extendedMap) );
            jobService.createJobConfig(jobConfigInfo);

            //生成jobSignal
            String content = jobConfigInfo.getId() + jobConfigInfo.getJob_media_name();
            String md5 = Md5Util.getMd5(content);
            List<Map<String, Object>> jobList = new ArrayList<>();
            Map<String, Object> jobMap = new HashMap<>(4);
            jobMap.put("jobId", jobConfigInfo.getId());
            jobMap.put("jobSignal", md5);
            jobList.add(jobMap);
            responseVo.getData().put("jobs", jobList);
        } catch (ErrorException e) {
            logger.error("创建job出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    private String isAddTablePrefix(String json, boolean isAdd) {
        DLConfig connConf = DLConfig.parseFrom(json);
        connConf.set("job.content[0].writer.parameter.isAddTablePrefix", "false");
        return connConf.toJSON();
    }

    @RequestMapping("/jobInfo")
    @ResponseBody
    public Object queryJobInfo(String jobId) {
        ResponseVo responseVo = new ResponseVo();
        try {
            if (StringUtils.isEmpty(jobId)) {
                throw new ErrorException(CodeContext.JOBID_NOTNULL_ERROR_CODE);
            }
            long configId = Long.parseLong(jobId);
            JobConfigInfo configInfo = jobService.getJobConfigById(configId);
            if (configInfo == null) {
                throw new ErrorException(CodeContext.CONFIG_NOTFOUND_ERROR_CODE);
            }

            String json = configInfo.getJob_content();
            DLConfig conConf = DLConfig.parseFrom(json);
            Object columObject = conConf.get("job.content[0].reader.parameter.column");

            MediaSourceInfo srcMediaSourceInfo = mediaSourceService.getById(configInfo.getJob_src_media_source_id());
            MediaSourceInfo realMediaSourceInfo = mediaService.getRealDataSource(srcMediaSourceInfo);
            String tableName = "";
            if (realMediaSourceInfo.getType().isRdbms() ) {
                tableName = conConf.getString("job.content[0].reader.parameter.connection[0].table[0]");
            } else {
                tableName = conConf.getString("job.content[0].reader.parameter.table");
            }
            if(!StringUtils.isEmpty(tableName) && tableName.indexOf(",")>0) {
                tableName = tableName.split(",")[0];
            }
            //其他类型获取方式
            MediaMeta mediaMeta = MetaManager.getMediaMeta(realMediaSourceInfo, tableName);
            if (mediaMeta != null) {
                List<MetaManager.ColumnMetaInfo> columnMetaInfos = MetaManager.getTableInfos(mediaMeta, realMediaSourceInfo);
                List<MetaManager.ColumnMetaInfo> list = new ArrayList<>();
                JSONArray jsonArray = (JSONArray) columObject;
                for (int i = 0; i < jsonArray.size(); i++) {
                    String columnName = "";
                    if (jsonArray.get(i) instanceof String) {
                        columnName = (String) jsonArray.get(i);
                    } else {
                        columnName = ((JSONObject) jsonArray.get(i)).getString("name");
                    }
                    String finalColumnName = columnName;
                    boolean isFind = false;
                    for (MetaManager.ColumnMetaInfo column : columnMetaInfos) {
                        if (finalColumnName.equalsIgnoreCase(StringUtils.isEmpty(column.getHbaseFamily()) ? column.getName() : column.getHbaseFamily() + ":" + column.getName())) {
                            list.add(column);
                            isFind = true;
                            break;
                        }
                    }
                    if (!isFind) {
                        if (realMediaSourceInfo.getType() == MediaSourceType.HBASE) {
                            if ("rowkey".equalsIgnoreCase(finalColumnName)) {
                                MetaManager.ColumnMetaInfo info = new MetaManager.ColumnMetaInfo();
                                info.setName("rowkey");
                                info.setIsPrimaryKey("true");
                                info.setType("Bytes");
                                list.add(info);
                            } else {
                                MetaManager.ColumnMetaInfo info = new MetaManager.ColumnMetaInfo();
                                String[] nameSplit = finalColumnName.split(":");
                                info.setHbaseFamily(nameSplit[0]);
                                info.setName(nameSplit[1]);
                                info.setType("Bytes");
                                list.add(info);
                            }
                        }
                    }
                }
                conConf.set("job.content[0].reader.parameter.column", list);
            }
            responseVo.getData().put("jobInfo", conConf.getInternal());
        } catch (ErrorException e) {
            logger.error("查询数据库出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @RequestMapping(value = "/start")
    @ResponseBody
    public Object doStart(@RequestParam("jobId") String jobId, @RequestParam("jobSignal") String jobSignal,
                          @RequestParam(value = "parameter", required = false) String parameter,
                          @RequestParam(value = "jvmMax", required = false) String jvmMax,
                          @RequestParam(value = "jvmMin", required = false) String jvmMin,
                          HttpServletRequest request, HttpServletResponse response) {
        ResponseVo responseVo = new ResponseVo();
        try {
            jobId = JobUtil.parseJobId(jobId);

            if (!JobUtil.jobConfigIdCheck(jobId)) {
                throw new ErrorException(CodeContext.JOBID_NOTNULL_ERROR_CODE);
            }
            long job_id = Long.parseLong(jobId);
            JobConfigInfo info = jobService.getJobConfigById(job_id);
            if (info == null) {
                throw new ErrorException(CodeContext.JOB_NOTFUND_ERROR_CODE);
            }
            //   info.setJob_content( DataxUtil.preDateInitial(info) );
            String job_name = info.getJob_name();
            if (DataxUtil.isJobRunning(job_name)) {
                //如果当前这个job还没执行完，就给调用方返回一个 Long.MIN_VALUE，做一个特殊标志
                //待调用方下次再调用 doStart()
                logger.info("[JobServiceController]current job not end " + job_name);
                throw new ErrorException(CodeContext.JOB_ISRUNNING_ERROR_CODE);
            }

            //如果job关联的数据源正在切机房中，禁止job启动
            if (doubleCenterDataxService.isSwitchLabIng(info.getJob_src_media_source_id()) || doubleCenterDataxService.isSwitchLabIng(info.getJob_target_media_source_id())) {
                String msg = "当前job(" + job_name + ")关联的数据源正在切机房中，job暂时不能启动，请稍后再试！";
                logger.info(msg);
                throw new ErrorException(CodeContext.DOUBLECENTER_SWITCH_ERROR_CODE);
            }

            String job_content = info.getJob_content();
            Map<String, String> jsonToMap = JsonUtil.jsonStringToMap(parameter);
            jsonToMap.put(DataxJobConfigConstant.HTTP_PARAMETER_LAST_EXECUTE_TIME, "1970-05-12 12:30:00");
            Map<String, String> map = DataxUtil.replaceDynamicParameter(info, jsonToMap);
            logger.info("[JobServiceController]dynamic parameter -> " + map.toString());
            DataxCommand command = new DataxCommand();
            command.setJobId(new Long(jobId));
            command.setJobName(job_name);
            command.setType(DataxCommand.Type.Start);
            if (map != null && map.size() > 0) {
                command.setDynamicParam(true);
                command.setMapParam(map);
            }
            if (StringUtils.isNotBlank(info.getTiming_parameter())) {
                TimingParameter p = JSONObject.parseObject(info.getTiming_parameter(), TimingParameter.class);
                command.setJvmArgs(p.getJvmMemory());
            }

            //如果本地没启动datax服务，并且没有填写worker机器ip的话，将Command对象转换成json之前
            //就会报错了，这里方便后续调试加一个datax机器地址
            //info.setTiming_target_worker("1.2.3.4");

            logger.info("[JobServiceController]pre start -> " + info.toString());
            if (StringUtils.isNotBlank(jvmMax) && StringUtils.isNotBlank(jvmMin)) {
                StringBuilder jvmArgs = new StringBuilder();
                jvmArgs.append("-Xms").append(jvmMin).append(" ").append("-Xmx").append(jvmMax);
                command.setJvmArgs(jvmArgs.toString());
            }

            String msg = jobControlService.start(command, info.getTiming_target_worker());
            responseVo.getData().put("executeId", JobUtil.appendEnvSuffix(msg));
        } catch (ErrorException e) {
            logger.error("执行job出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    /**
     * 根据 job执行的id查询单个job的执行状态，同时获取ZK上的job状态，如果两者都是 running 状态，则认为这个job状态是正常的并返回</br>
     * 否则如果zk上的状态是 running 而db中获取的不是，或者db中的状态是 running 而zk上获取的不是，则认为这个job当前的状态是异常，</br>
     * 将数据库中的 job execution表中的装改改为失败，并返回错误消息给调用方
     *
     * @param executeId
     * @param md5
     * @return
     */
    @RequestMapping(value = "/state")
    @ResponseBody
    public Object state(@RequestParam("executeId") String executeId, @RequestParam("jobSignal") String md5) {
        ResponseVo responseVo = new ResponseVo();
        try {
            executeId = JobUtil.parseJobId(executeId);
            if (!JobUtil.jobExecutionIdCheck(executeId)) {
                throw new ErrorException(CodeContext.EXECUTEID_NOTNULL_ERROR_CODE);
            }

            JobExecutionInfo info = jobControlService.state(Long.parseLong(executeId));
            JobConfigInfo configInfo = jobService.getJobConfigById(info.getJob_id());
            logger.info("[JobServiceController]pre state " + info.toString());

            //一切返回的状态以DB为准，如果DB中的状态是RUNNING，再检查zookeeper，若zookeeper是stop则表示这个进程挂了，返回失败
            //并更新数据库状态将此运行记录设为failure
            if (JobExecutionState.RUNNING.equals(info.getState())) {
                //如果zk的的状态为非运行
                if (!DataxUtil.isJobRunning(configInfo.getJob_name())) {
                    //数据库的状态是RUNNING，zk上的节点不存在了，不一定是任务不正常，可能是正常的退出
                    //轮询10次做检查数据库状态，如果还是RUNNING，则认为这个任务结束的状态不正常，设置为failure
                    if (!checkJobStateWhenZNodeNotExist(Long.parseLong(executeId))) {
                        String msg = "this job =" + info.toString() + "  process has been hung up";
                        logger.warn("[JobServiceController]" + msg);
                        jobService.modifyJobExecutionState(JobExecutionState.FAILED, msg, info.getId());
                        //再查一下
                        info = jobControlService.state(Long.parseLong(executeId));
                    }
                }
            }
            //如果job运行失败，也返回失败状态
            if (JobExecutionState.FAILED.equals(info.getState())) {
                String msg = "this job execute failure " + info.toString();
                logger.warn("[JobServiceController] " + msg);
            }

            info.setOriginal_configuration("");
            info.setTask_communication_info("");
            if(StringUtils.isNotBlank(info.getException())) {
                if(info.getException().length()>500) {
                    //重新设置exception的长度，设置为500
                    info.getException().substring(0,500);
                }
            }
            responseVo.getData().put("state", info);
           // logger.info("[JobServiceController]result -> " + JSONObject.toJSONString(responseVo));
        } catch (ErrorException e) {
            logger.error("查询任务状态出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @RequestMapping(value = "/mapping")
    @ResponseBody
    public Object mappingInfo(@RequestParam("mappingId") String mappingId) {
        logger.info("receive data : mappingId=" + mappingId );

        ResponseVo responseVo = new ResponseVo();
        try {

            if (StringUtils.isEmpty(mappingId)) {
                throw new ErrorException(CodeContext.MAPPINGID_NOTNULL_ERROR_CODE);
            }

            MediaMappingInfo mediaMappingInfo = mediaService.findMediaMappingsById(Long.valueOf(mappingId));
            MediaInfo mediaInfo = mediaService.findMediaById(mediaMappingInfo.getSourceMediaId());
            TaskInfo taskInfo = taskService.getTask(mediaMappingInfo.getTaskId());
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(taskInfo.getReaderMediaSourceId());

            Map<String, Object> mappingConfigInfo = new HashMap<>(16);
            mappingConfigInfo.put("srcType", mediaSourceInfo.getType());
            mappingConfigInfo.put("realType", mediaSourceInfo.getSimulateMsType() == null ? mediaSourceInfo.getType() : mediaSourceInfo.getSimulateMsType());
            mappingConfigInfo.put("srcDatabase", mediaSourceInfo.getName());
            mappingConfigInfo.put("srcTable", mediaInfo.getName());
            mappingConfigInfo.put("destDatabase", mediaMappingInfo.getTargetMediaNamespace());
            mappingConfigInfo.put("destTable", mediaMappingInfo.getTargetMediaName());
            mappingConfigInfo.put("taskId", mediaMappingInfo.getTaskId());
            mappingConfigInfo.put("srcMediaSourceId", mediaSourceInfo.getId());
            mappingConfigInfo.put("targetMediaSourceId", mediaMappingInfo.getTargetMediaSourceId());
            mappingConfigInfo.put("parameter", mediaMappingInfo.getParameterObj());
            mappingConfigInfo.put("columnMappingMode", mediaMappingInfo.getColumnMappingMode());
            mappingConfigInfo.put("writePriority", mediaMappingInfo.getWritePriority());
            mappingConfigInfo.put("valid", mediaMappingInfo.isValid());
            mappingConfigInfo.put("interceptorId", mediaMappingInfo.getInterceptorId());
            mappingConfigInfo.put("joinColumn", mediaMappingInfo.getJoinColumn());
            mappingConfigInfo.put("esUsePrefix", mediaMappingInfo.isEsUsePrefix());
            mappingConfigInfo.put("prefixName", mediaMappingInfo.getPrefixName());
            mappingConfigInfo.put("createTime", sdf.format(mediaMappingInfo.getCreateTime()));

            responseVo.getData().put("mappingConfigInfo", mappingConfigInfo);

        } catch (ErrorException e) {
            logger.error("查询任务状态出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @RequestMapping(value = "/jobConfig")
    @ResponseBody
    public Object jobConfig(@RequestParam("jobId") String jobId) {
        ResponseVo responseVo = new ResponseVo();
        try {

            if (StringUtils.isEmpty(jobId)) {
                throw new ErrorException(CodeContext.JOBID_NOTNULL_ERROR_CODE);
            }
            jobId = JobUtil.parseJobId(jobId);
            if (!JobUtil.checkNumber(jobId)) {
                throw new ErrorException(CodeContext.JOBID_NOTNULL_ERROR_CODE);
            }
            JobConfigInfo jobConfig = jobService.getJobConfigById(Long.valueOf(jobId));
            if (jobConfig == null) {
                throw new ErrorException(CodeContext.CONFIG_NOTFOUND_ERROR_CODE);
            }
            DLConfig conConf = DLConfig.parseFrom(jobConfig.getJob_content());
            responseVo.getData().put("jobInfo", conConf.getInternal());
            responseVo.getData().put("jobId", jobConfig.getId());
            responseVo.getData().put("jobName", jobConfig.getJob_name());
            responseVo.getData().put("applyId", jobConfig.getApply_id());
            responseVo.getData().put("tableName", jobConfig.getJob_media_name());
            responseVo.getData().put("srcMediaSourceId", jobConfig.getJob_src_media_source_id());
            responseVo.getData().put("targetMediaSourceId", jobConfig.getJob_target_media_source_id());
            responseVo.getData().put("timingYn", jobConfig.isTiming_yn());
            responseVo.getData().put("timingTransferType", jobConfig.getTiming_transfer_type());
            responseVo.getData().put("timingOnYn", jobConfig.isTiming_on_yn());
            responseVo.getData().put("createTime", sdf.format(jobConfig.getCreate_time()));
        } catch (ErrorException e) {
            logger.error("查询任务状态出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @RequestMapping(value = "/doSql")
    @ResponseBody
    public Object doSql(@RequestParam("dbId") String dbId, @RequestParam("sql") String sql) {
        logger.info("receive data : dbId=" + dbId + " , sql=" + sql );

        ResponseVo responseVo = new ResponseVo();
        try {

            if (StringUtils.isEmpty(dbId)) {
                throw new ErrorException(CodeContext.DBID_NOTNULL_ERROR_CODE);
            }
            if (StringUtils.isEmpty(sql)) {
                throw new ErrorException(CodeContext.SQL_NOTNULL_ERROR_CODE);
            }
            if (!JobUtil.checkNumber(dbId)) {
                throw new ErrorException(CodeContext.DBID_NOTNUMBER_ERROR_CODE);
            }
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(Long.valueOf(dbId));

            List<SQLStatementHolder> holders = DdlSqlUtils.buildSQLStatement(mediaSourceInfo.getType(), sql);
            for (SQLStatementHolder holder : holders) {
                holder.check();
                if (holder.getSqlType() != SqlType.SelectTable) {
                    throw new ErrorException(CodeContext.SELECT_MUST_ERROR_CODE);
                }
            }

            List<Map<String, Object>> list = RDBMSUtil.executeSql(mediaSourceInfo, sql);

            responseVo.getData().put("result", list);
        } catch (ErrorException e) {
            logger.error("查询任务状态出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @RequestMapping(value = "/getMappingId")
    @ResponseBody
    public Object getMappingId(String targetMediaSourceName,String targetNamespace,String targetTableName) {
        logger.info("receive data : targetMediaSourceName=" + targetMediaSourceName + " , targetNamespace=" + targetNamespace + " , targetTableName=" + targetTableName );
        ResponseVo responseVo = new ResponseVo();
        try {

            if (StringUtils.isEmpty(targetMediaSourceName )) {
                throw new ErrorException(CodeContext.TARGETMEDIASOURCENAME_NOTNULL_ERROR_CODE);
            }
            if(StringUtils.isEmpty(targetNamespace)) {
                throw new ErrorException(CodeContext.TARGETNAMESPACE_NOTNULL_ERROR_CODE);
            }
            if (StringUtils.isEmpty(targetTableName)) {
                throw new ErrorException(CodeContext.TARGETTABLENAME_NOTNULL_ERROR_CODE);
            }
            MediaSourceInfo targetMediaSourceInfo = mediaSourceService.getOneByName(targetMediaSourceName);
            if(targetMediaSourceInfo == null) {
                throw new ErrorException(CodeContext.TARGETMEDIA_NOTEXISTS_ERROR_CODE);
            }

            List<MediaMappingInfo> mediaMappingInfoList = mediaService.getMappingsByTargetMediaNameAndNamespace(targetMediaSourceInfo.getId(),targetNamespace,targetTableName);

            if(mediaMappingInfoList == null || mediaMappingInfoList.size()<1) {
                MediaSourceInfo srcMediaSourceInfo = mediaSourceService.getOneByName(targetNamespace);
                if(srcMediaSourceInfo == null) {
                    throw new ErrorException(CodeContext.SRCSOURCE_NOTFUND_ERROR_CODE);
                }
                mediaMappingInfoList = mediaService.getMappingsByMediaSourceIdAndTargetTable(srcMediaSourceInfo.getId(),targetMediaSourceInfo.getId(),targetTableName);
            }

            List<Long> returnMediaMappingIdList = new ArrayList<>(8);
            for (MediaMappingInfo mediaMappingInfo : mediaMappingInfoList) {
                returnMediaMappingIdList.add(mediaMappingInfo.getId());
            }

            responseVo.getData().put("mappingIdList", returnMediaMappingIdList);
        } catch (ErrorException e) {
            logger.error("查询任务状态出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    @RequestMapping(value = "/getJobId")
    @ResponseBody
    public Object getJobId(String sourceType,String targetMediaSourceName,String targetNamespace,String targetTableName) {
        logger.info("receive data : sourceType="+sourceType+", targetMediaSourceName=" + targetMediaSourceName + " , targetNamespace=" + targetNamespace + " , targetTableName=" + targetTableName );
        ResponseVo responseVo = new ResponseVo();
        try {
            if (StringUtils.isEmpty(targetMediaSourceName )) {
                throw new ErrorException(CodeContext.TARGETMEDIASOURCENAME_NOTNULL_ERROR_CODE);
            }
            if(StringUtils.isEmpty(targetNamespace)) {
                throw new ErrorException(CodeContext.TARGETNAMESPACE_NOTNULL_ERROR_CODE);
            }
            if (StringUtils.isEmpty(targetTableName)) {
                throw new ErrorException(CodeContext.TARGETTABLENAME_NOTNULL_ERROR_CODE);
            }
            MediaSourceInfo targetMediaSourceInfo = mediaSourceService.getOneByName(targetMediaSourceName);
            if(targetMediaSourceInfo == null) {
                throw new ErrorException(CodeContext.TARGETMEDIA_NOTEXISTS_ERROR_CODE);
            }

            List<MediaSourceInfo> srcMediaSourceInfoList = null;
            if("hbase".equalsIgnoreCase(sourceType)) {
                MediaSourceInfo mediaSourceInfo = mediaSourceService.getOneByName(targetNamespace);
                srcMediaSourceInfoList.add(mediaSourceInfo);
            }else {
                //如果是mysql,sqlserver等rdbms数据库则需要根据schema去查询数据源
                srcMediaSourceInfoList = mediaSourceService.getMediaSourceLikeSchema(targetNamespace);
            }

            if(srcMediaSourceInfoList == null || srcMediaSourceInfoList.size() < 1) {
                throw new ErrorException(CodeContext.SRCSOURCE_NOTFUND_ERROR_CODE);
            }

            List<Long> srcMediaSourceIdList = new ArrayList<>(8);
            for (MediaSourceInfo mediaSourceInfo : srcMediaSourceInfoList){
                srcMediaSourceIdList.add(mediaSourceInfo.getId());
            }

            List<JobConfigInfo> jobConfigInfoList = jobService.getJobsBySrcIdAndTargetIdAndTable(srcMediaSourceIdList,targetMediaSourceInfo.getId(),targetTableName);

            List<Long> returnJobIdList = new ArrayList<>(16);
            for (JobConfigInfo jobConfigInfo : jobConfigInfoList) {
                returnJobIdList.add(jobConfigInfo.getId());
            }

            responseVo.getData().put("jobIdList", returnJobIdList);
        } catch (ErrorException e) {
            logger.error("查询任务状态出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }


    @RequestMapping(value = "/transformToHiveType")
    @ResponseBody
    public Object toHiveType(String sourceType,String columnType) {
        logger.info("receive data : sourceType=" + sourceType + ", columnType=" + columnType );
        ResponseVo responseVo = new ResponseVo();
        try {
            if (StringUtils.isEmpty(sourceType)) {
                throw new ErrorException(CodeContext.SOURCE_TYPE_ERROR_CODE);
            }
            if (StringUtils.isEmpty(columnType)) {
                throw new ErrorException(CodeContext.DATABASE_COLUMN_TYPE_ERROR_CODE);
            }
            AbstractMapping mapping = MappingFactory.creteRDBMSMapping();
            ColumnMeta columnMeta = new ColumnMeta();
            columnMeta.setName("x");
            columnMeta.setType(columnType.toLowerCase());
            ColumnMeta cm = mapping.toHDFS(columnMeta);
            responseVo.getData().put("type",cm.getType());
        } catch (ErrorException e) {
            logger.error("查询任务状态出现异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        return responseVo;
    }

    private  boolean checkJobStateWhenZNodeNotExist(long id) {
        boolean isSuccess = false;
        for(int i=0;i<10;i++) {
            JobExecutionInfo info = jobControlService.state(id);
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

    private List<SyncApplyMapping> getSyncMappingList(String tableMappings) {
        List<SyncApplyMapping> syncApplyMappings = new ArrayList<>();
        JSONArray array = JSONArray.parseArray(tableMappings);
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            SyncApplyMapping applyMapping = new SyncApplyMapping();
            Iterator<Map.Entry<String, Object>> it = jsonObject.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                applyMapping.setSourceTableName(entry.getKey());
                applyMapping.setTargetTableName((String) entry.getValue());
                applyMapping.setColumnMappingMode(ColumnMappingMode.NONE);
                syncApplyMappings.add(applyMapping);
                break;
            }
        }
        return syncApplyMappings;
    }

    private String setHbaseColumns(MediaSourceInfo srcMediaSourceInfo, String table, String hbaseColumns, String json, String destDbType) throws Exception {
        if (srcMediaSourceInfo.getType() == MediaSourceType.HBASE && !StringUtils.isEmpty(hbaseColumns)) {
            MediaMeta mediaMeta = MetaManager.getMediaMeta(srcMediaSourceInfo, table);
            List<JobUtil.Column> hbaseColumnList = JobUtil.getHbaseColumns(mediaMeta.getColumn(), hbaseColumns);
            //List<JobUtil.Column> destColumnList = JobUtil.getDestColumns(srcMediaSourceInfo,mediaMeta,destDbType);
            json = JobUtil.updateColumns(srcMediaSourceInfo, json, hbaseColumnList, destDbType);
        }
        return json;
    }

    private String setColumns(MediaSourceInfo srcMediaSourceInfo, String table, String columns, String json, String destDbType) throws Exception {
        if (StringUtils.isEmpty(columns)) {
            return json;
        }
        if (srcMediaSourceInfo.getType().isRdbms()) {
            MediaMeta mediaMeta = MetaManager.getMediaMeta(srcMediaSourceInfo, table);
            List<JobUtil.Column> columnList = JobUtil.getColumns(mediaMeta.getColumn(), columns);
            //List<JobUtil.Column> destColumnList = JobUtil.getDestColumns(srcMediaSourceInfo,mediaMeta,destDbType);
            json = JobUtil.updateColumns(srcMediaSourceInfo, json, columnList, destDbType);
        }
        return json;
    }

    private JobConfigInfo createJobConfigInfo(MediaSourceInfo targetMediaSourceInfo, Long srcId, String table, String json, String source,
                                              String timingTransferType) {
        JobConfigInfo jobConfigInfo = new JobConfigInfo();
        String nameWithRand = JobUtil.genJobName(JobUtil.sourceMap.get(source), table);
        jobConfigInfo.setJob_name(nameWithRand);
        jobConfigInfo.setIs_delete(false);
        jobConfigInfo.setJob_content(json);
        jobConfigInfo.setJob_media_name(table);
        jobConfigInfo.setJob_src_media_source_id(srcId);
        jobConfigInfo.setJob_target_media_source_id(targetMediaSourceInfo.getId());
        jobConfigInfo.setTiming_yn(true);
        jobConfigInfo.setTiming_transfer_type(timingTransferType);
        return jobConfigInfo;
    }

}
