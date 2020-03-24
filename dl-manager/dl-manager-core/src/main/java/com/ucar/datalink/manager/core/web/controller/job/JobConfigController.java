package com.ucar.datalink.manager.core.web.controller.job;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.meta.MetaManager;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobUtil;
import com.ucar.datalink.biz.utils.flinker.check.SyncCheckUtil;
import com.ucar.datalink.biz.utils.flinker.job.JobConfigBuilder;
import com.ucar.datalink.biz.utils.flinker.job.JobContentParseUtil;
import com.ucar.datalink.biz.utils.flinker.module.AdvanceJobProperty;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
import com.ucar.datalink.biz.utils.flinker.module.TimingJobExtendPorperty;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.event.HBaseRange;
import com.ucar.datalink.domain.job.*;
import com.ucar.datalink.domain.media.ColumnMappingMode;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.ModeUtils;
import com.ucar.datalink.domain.media.parameter.es.EsMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.domain.monitor.MonitorCat;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.manager.core.flinker.cron.QuartzManager;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import com.ucar.datalink.manager.core.web.dto.job.JobConfigView;
import com.ucar.datalink.manager.core.web.dto.job.JobExecutionView;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by user on 2017/7/13.
 */
@Controller
@RequestMapping(value = "/jobConfig/")
public class JobConfigController {

    private static final Logger logger = LoggerFactory.getLogger(JobConfigController.class);

    public static final String ESPECIALLY_CHAR_AND = "@AND@";

    public static final String ESPECIALLY_CHAR_AND_ORGINAL = "&";

    /**
     * 格式化时间
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";

    @Autowired
    JobService jobService;

    @Autowired
    JobRunQueueService queueService;

    @Autowired
    MediaSourceService mediaSourceService;

    @Autowired
    UserService userService;

    @Autowired
    WorkerService service;

    @Autowired
    @Qualifier("webconsole")
    JobControlService jobControlService;

    @Autowired
    private MailService mailService;

    @Autowired
    private JobScheduleService jobScheduleService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private MonitorService monitorService;

    @ResponseBody
    @RequestMapping(value = "/doGo")
    public String doGo(HttpServletRequest request) {
        return "success";
    }

    @RequestMapping(value = "/jobList")
    public ModelAndView jobList() {
        ModelAndView mav = new ModelAndView("jobConfig/list");
        return mav;
    }

    @RequestMapping(value = "/initJob")
    @ResponseBody
    public Page<JobConfigView> initJob(@RequestBody Map<String, String> map, HttpServletRequest request) {
        String isTiming = map.get("isTiming");
        String srcType = map.get("srcType");
        String destType = map.get("destType");
        String srcName = getStringByDefault(map.get("srcName"), null);
        String destName = getStringByDefault(map.get("destName"), null);
        String mediaName = getStringByDefault(map.get("mediaName"), null);
        String name = getStringByDefault(map.get("jobName"), null);
        long applyId = stringToLong(map.get("applyId"));
        long jobconfigId = stringToLong(map.get("jobconfigId"));
        String srcTableName = map.get("srcTableName");
        String targetTableName = map.get("targetTableName");

        HttpSession session = request.getSession();
        UserInfo user = (UserInfo) session.getAttribute("user");
        long applyUserId = 0;
        if (!userService.isSuper(user)) {
            applyUserId = user.getId();
        }
        if (StringUtils.isBlank(srcType)) {
            srcType = "-1";
        }
        if (StringUtils.isBlank(destType)) {
            destType = "-1";
        }
        if (StringUtils.isNotBlank(srcName)) {
            MediaSourceInfo info = mediaSourceService.getById(Long.parseLong(srcName));
            srcName = info.getName();
        }
        if (StringUtils.isNotBlank(destName)) {
            MediaSourceInfo info = mediaSourceService.getById(Long.parseLong(destName));
            destName = info.getName();
        }
        if(StringUtils.isBlank(srcTableName)) {
            srcTableName = null;
        }
        if(StringUtils.isBlank(targetTableName)) {
            targetTableName = null;
        }

        switch (srcType) {
            case "-1":
                srcType = null;
                break;
            case "ElasticSearch":
                srcType = MediaSourceType.ELASTICSEARCH.name().toUpperCase();
                break;
            case "HBase":
                srcType = MediaSourceType.HBASE.name().toUpperCase();
                break;
            case "HDFS":
                srcType = MediaSourceType.HDFS.name().toUpperCase();
                break;
            case "MySql":
                srcType = MediaSourceType.MYSQL.name().toUpperCase();
                break;
            case "SqlServer":
                srcType = MediaSourceType.SQLSERVER.name().toUpperCase();
                break;
            case "PostgreSql":
                srcType = MediaSourceType.POSTGRESQL.name().toUpperCase();
                break;
            case "SDDL":
                srcType = MediaSourceType.SDDL.name().toUpperCase();
                break;
        }
        switch (destType) {
            case "-1":
                destType = null;
                break;
            case "ElasticSearch":
                destType = MediaSourceType.ELASTICSEARCH.name().toUpperCase();
                break;
            case "HBase":
                destType = MediaSourceType.HBASE.name().toUpperCase();
                break;
            case "HDFS":
                destType = MediaSourceType.HDFS.name().toUpperCase();
                break;
            case "MySql":
                destType = MediaSourceType.MYSQL.name().toUpperCase();
                break;
            case "SqlServer":
                destType = MediaSourceType.SQLSERVER.name().toUpperCase();
                break;
            case "PostgreSql":
                destType = MediaSourceType.POSTGRESQL.name().toUpperCase();
                break;
            case "SDDL":
                destType = MediaSourceType.SDDL.name().toUpperCase();
                break;
            case "KUDU":
                destType = MediaSourceType.KUDU.name().toUpperCase();
                break;
        }

        Page<JobConfigView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        List<JobConfigInfo> jobConfigInfoList = jobService.queryJobConfigDBTypeByPaging(srcType, destType,
                srcName, destName, mediaName, name, applyId, applyUserId, isTiming, jobconfigId, srcTableName, targetTableName);
        List<JobConfigView> jobView = jobConfigInfoList.stream().map(i -> {
            JobConfigView view = new JobConfigView();
            String create_time = sdf.format(new Date(i.getCreate_time().getTime()));
            view.setCreate_time(create_time);
            view.setId(i.getId());
            view.setIs_delete(i.is_delete());
            view.setJob_content(i.getJob_content());
            view.setJob_desc(i.getJob_desc());
            view.setJob_media_name(i.getJob_media_name());
            view.setJob_media_target_name(i.getJob_media_target_name());
            view.setJob_name(i.getJob_name());

            MediaSourceInfo srcInfo = mediaSourceService.getById(new Long(i.getJob_src_media_source_id()));
            MediaSourceInfo targetInfo = mediaSourceService.getById(new Long(i.getJob_target_media_source_id()));
            if (srcInfo == null) {
                view.setJob_src_media_source_name("none");
            } else {
                view.setJob_src_media_source_name(srcInfo.getName());
            }
            if (targetInfo == null) {
                view.setJob_target_media_source_name("none");
            } else {
                view.setJob_target_media_source_name(targetInfo.getName());
            }
            view.setModify_time(i.getModify_time());
            if (i.getTiming_expression() == null || "".equals(i.getTiming_expression())) {
                view.setTiming_expression("none");
            } else {
                view.setTiming_expression(i.getTiming_expression());
            }
            view.setTiming_on_yn(i.isTiming_on_yn());

            if (i.getTiming_parameter() == null || "".equals(i.getTiming_parameter())) {
                view.setTiming_parameter("none");
            } else {
                view.setTiming_parameter(i.getTiming_parameter());
            }
            if (i.getTiming_target_worker() == null || "".equals(i.getTiming_target_worker())) {
                view.setTiming_target_worker("none");
            } else {
                view.setTiming_parameter(i.getTiming_parameter());
            }

            if (JobConfigInfo.TIMING_TRANSFER_TYPE_FULL.equals(i.getTiming_transfer_type())) {
                view.setTiming_transfer_type("全量");
            } else {
                view.setTiming_transfer_type("增量");
            }
            view.setTiming_yn(Boolean.toString(i.isTiming_yn()));

            //JobExecutionInfo executionInfo = jobService.lastExecuteJobExecutionInfo(i.getId());
            JobExecutionInfo executionInfo = jobService.lastExecuteJobExecutionInfoByOptimized(i.getId());
            if(executionInfo==null) {
                view.setCurrentState(JobExecutionState.UNEXECUTE);
            } else {
                switch (executionInfo.getState()) {
                    case JobExecutionState.FAILED :
                        view.setCurrentState( JobExecutionState.FAILED );
                        break;
                    case JobExecutionState.SUCCEEDED :
                        view.setCurrentState( JobExecutionState.SUCCEEDED );
                        break;
                    case JobExecutionState.KILLED :
                        view.setCurrentState( JobExecutionState.FAILED );
                        break;
                    case JobExecutionState.UNEXECUTE :
                        view.setCurrentState( JobExecutionState.UNEXECUTE );
                        break;
                    case JobExecutionState.ABANDONED :
                        view.setCurrentState( JobExecutionState.ABANDONED );
                        break;
                    case JobExecutionState.RUNNING :
                        view.setCurrentState( JobExecutionState.RUNNING );
                        break;
                    default :
                        view.setCurrentState( JobExecutionState.UNEXECUTE );
                        break;
                }
            }

            return view;
        }).collect(Collectors.toList());
        PageInfo<JobConfigInfo> pageInfo = new PageInfo<>(jobConfigInfoList);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }


    @ResponseBody
    @RequestMapping(value = "/works")
    public List<String> getWorks() {
        List<String> works = new ArrayList<>();
        try {
            List<String> list = FlinkerJobUtil.getDataxMachineAddress();
            if (list == null || list.size() == 0) {
                throw new RuntimeException("list is emtpy");
            }

            for (String s : list) {
                works.add(s);
            }
        } catch (Exception e) {
            return works;
        }
        return works;
    }


    @ResponseBody
    @RequestMapping(value = "/dbTypeChange")
    @AuthIgnore
    public Map<String, Object> dbTypeChange(@ModelAttribute("name") String name, HttpServletRequest request) {
        Set<MediaSourceType> types = new HashSet<>();
        if (name.toUpperCase().equals(MediaSourceType.ELASTICSEARCH.name())) {
            types.add(MediaSourceType.ELASTICSEARCH);
        } else if (name.toUpperCase().equals(MediaSourceType.HBASE.name())) {
            types.add(MediaSourceType.HBASE);
        } else if (name.toUpperCase().equals(MediaSourceType.HDFS.name())) {
            types.add(MediaSourceType.HDFS);
        } else if (name.toUpperCase().equals(MediaSourceType.MYSQL.name())) {
            types.add(MediaSourceType.MYSQL);
        } else if (name.toUpperCase().equals(MediaSourceType.SQLSERVER.name())) {
            types.add(MediaSourceType.SQLSERVER);
        } else if (name.toUpperCase().equals(MediaSourceType.POSTGRESQL.name())) {
            types.add(MediaSourceType.POSTGRESQL);
        } else if (name.toUpperCase().equals(MediaSourceType.SDDL.name())) {
            types.add(MediaSourceType.SDDL);
        } else if (name.toUpperCase().equals(MediaSourceType.KUDU.name())) {
            types.add(MediaSourceType.KUDU);
        } else if(name.toUpperCase().equals(MediaSourceType.ORACLE.name())) {
            types.add(MediaSourceType.ORACLE);
        } else if(name.toUpperCase().equals(MediaSourceType.HANA.name())) {
            types.add(MediaSourceType.HANA);
        }
        else {
            //忽略
        }
        List<MediaSourceInfo> list = mediaSourceService.getListByType(types);

        Map<String, Object> map = new HashMap<>();
        List<String> num = new ArrayList<>();
        List<String> val = new ArrayList<>();
        for (MediaSourceInfo info : list) {
            num.add(info.getId() + "");
            val.add(info.getName());
        }
        map.put("num", num);
        map.put("val", val);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/dbTypeChangeInDataCenter")
    @AuthIgnore
    public Map<String, Object> dbTypeChangeInDataCenter(@ModelAttribute("name") String name, HttpServletRequest request) {
        Set<MediaSourceType> types = new HashSet<>();
        if (name.toUpperCase().equals(MediaSourceType.ELASTICSEARCH.name())) {
            types.add(MediaSourceType.ELASTICSEARCH);
        } else if (name.toUpperCase().equals(MediaSourceType.HBASE.name())) {
            types.add(MediaSourceType.HBASE);
        } else if (name.toUpperCase().equals(MediaSourceType.HDFS.name())) {
            types.add(MediaSourceType.HDFS);
        } else if (name.toUpperCase().equals(MediaSourceType.MYSQL.name())) {
            types.add(MediaSourceType.MYSQL);
        } else if (name.toUpperCase().equals(MediaSourceType.SQLSERVER.name())) {
            types.add(MediaSourceType.SQLSERVER);
        } else if (name.toUpperCase().equals(MediaSourceType.POSTGRESQL.name())) {
            types.add(MediaSourceType.POSTGRESQL);
        } else if (name.toUpperCase().equals(MediaSourceType.SDDL.name())) {
            types.add(MediaSourceType.SDDL);
        } else if (name.toUpperCase().equals(MediaSourceType.KUDU.name())) {
            types.add(MediaSourceType.KUDU);
        } else if (name.toUpperCase().equals(MediaSourceType.ORACLE.name())) {
            types.add(MediaSourceType.ORACLE);
        } else if (name.toUpperCase().equals(MediaSourceType.HANA.name())) {
            types.add(MediaSourceType.HANA);
        }
        else {
            //忽略
        }
        List<MediaSourceInfo> list = mediaSourceService.getListByType(types);

        Map<String, Object> map = new HashMap<>();
        List<String> num = new ArrayList<>();
        List<String> val = new ArrayList<>();
        for (MediaSourceInfo info : list) {
            num.add(info.getId() + "");
            val.add(info.getName());
        }
        map.put("num", num);
        map.put("val", val);
        return map;
    }




    @ResponseBody
    @RequestMapping(value = "/crossDataCenter")
    @AuthIgnore
    public Map<String, Object> crossDataCenter(@ModelAttribute("name") String name, HttpServletRequest request) {
        Set<MediaSourceType> types = new HashSet<>();
        if (name.toUpperCase().equals(MediaSourceType.ELASTICSEARCH.name())) {
            types.add(MediaSourceType.ELASTICSEARCH);
        } else if (name.toUpperCase().equals(MediaSourceType.HBASE.name())) {
            types.add(MediaSourceType.HBASE);
        } else if (name.toUpperCase().equals(MediaSourceType.HDFS.name())) {
            types.add(MediaSourceType.HDFS);
        } else if (name.toUpperCase().equals(MediaSourceType.MYSQL.name())) {
            types.add(MediaSourceType.MYSQL);
        } else if (name.toUpperCase().equals(MediaSourceType.SQLSERVER.name())) {
            types.add(MediaSourceType.SQLSERVER);
        } else if (name.toUpperCase().equals(MediaSourceType.POSTGRESQL.name())) {
            types.add(MediaSourceType.POSTGRESQL);
        } else if (name.toUpperCase().equals(MediaSourceType.SDDL.name())) {
            types.add(MediaSourceType.SDDL);
        } else if (name.toUpperCase().equals(MediaSourceType.ORACLE.name())){
            types.add(MediaSourceType.ORACLE);
        } else {
            //忽略
        }

        List<MediaSourceInfo> mediaSourceList = mediaSourceService.getListByType(types);
        Map<String, Object> map = new HashMap<>();
        List<String> num = new ArrayList<>();
        List<String> val = new ArrayList<>();
        for (MediaSourceInfo info : mediaSourceList) {
            num.add(info.getId() + "");
            val.add(info.getName());
        }
        map.put("num", num);
        map.put("val", val);
        return map;
    }



    @ResponseBody
    @RequestMapping(value = "/namespaceContent")
    @AuthIgnore
    public List<String> namespaceContent(@ModelAttribute("id") String id, HttpServletRequest request) {
        Long mid = Long.parseLong(id);
        if (mid == null || mid < 0) {
            return new ArrayList<>();
        }

        //MediaSourceInfo info = mediaSourceService.getById(mid);
        MediaSourceInfo info = mediaSourceService.getById(mid);
        try {
            List<MediaMeta> list = MetaManager.getTables(info);
            StringBuilder sb = new StringBuilder();
            List<String> result = new ArrayList<>();
            for (MediaMeta meta : list) {
                if (meta.getDbType() == MediaSourceType.ELASTICSEARCH || meta.getDbType() == MediaSourceType.HDFS) {
                    result.add(meta.getNameSpace() + "." + meta.getName());
                } else {
                    result.add(meta.getName());
                }
            }
            //同步申请支持HBase增量申请时，可选按月/年分表的通配符
            Set<String> set = Sets.newLinkedHashSet();
            result.stream().forEach(t -> set.add(t));
            result.stream().forEach(t -> {
                //必须先判断monthly，再判断yearly
                String resultPattern = ModeUtils.tryBuildMonthlyPattern(t);
                if (ModeUtils.isMonthlyPattern(resultPattern)) {
                    set.add(resultPattern);
                } else {
                    resultPattern = ModeUtils.tryBuildYearlyPattern(t);
                    if (ModeUtils.isYearlyPattern(resultPattern)) {
                        set.add(resultPattern);
                    }
                }
            });
            result = set.stream().map(i -> i).collect(Collectors.toList());

            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }


    @ResponseBody
    @RequestMapping(value = "/doParseJobContent")
    @AuthIgnore
    public Object doParseJobContent(@ModelAttribute("srcID") String srcID, @ModelAttribute("destID") String destID, @ModelAttribute("mediaName") String mediaName) {
        MediaSourceInfo srcInfo = mediaSourceService.getById(Long.parseLong(srcID));
        MediaSourceInfo destInfo = mediaSourceService.getById(Long.parseLong(destID));
        try {
            if (mediaName.split(",").length == 1) {
                Map<String, String> map = new HashMap<>();
                String result = JobConfigBuilder.buildJson(srcInfo, mediaName, destInfo, mediaName, new JobExtendProperty());
                result = JobConfigBuilder.encryptPassword(result);
                Object json = JSONObject.parse(result);
                return json;
            } else {
                String[] arr = mediaName.split(",");
                List<String> list = new ArrayList<>();
                for (String s : arr) {
                    list.add(s);
                }
                Map<String, String> map = new HashMap<>();
                String result = JobConfigBuilder.buildJson(srcInfo, destInfo, new JobExtendProperty(), list);
                result = JobConfigBuilder.encryptPassword(result);
                Object json = JSONObject.parse(result);
                return json;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "{}";
    }


    @ResponseBody
    @RequestMapping(value = "/realoadJson")
    public Object realoadJson(@RequestBody JobExtendProperty property) {
        try {
            long srcID = property.getSrcID();
            long destID = property.getDestID();
            String mediaName = filterCharacter(property.getMediaName());
            Map<String, String> reader = property.getReader();
            Map<String, String> writer = property.getWriter();
            AdvanceJobProperty advenceProperty = property.getAdvance();
            TimingJobExtendPorperty timing = property.getTiming();

            MediaSourceInfo srcInfo = mediaSourceService.getById(srcID);
            MediaSourceInfo destInfo = mediaSourceService.getById(destID);
            if (mediaName.split(",").length == 1) {
                Map<String, String> map = new HashMap<>();
                String result = JobConfigBuilder.buildJson(srcInfo, mediaName, destInfo, mediaName, property);
                Object json = JSONObject.parse(JobConfigBuilder.encryptPassword(result));
                return json;
            } else {
                String[] arr = mediaName.split(",");
                List<String> list = new ArrayList<>();
                for (String s : arr) {
                    list.add(s);
                }
                Map<String, String> map = new HashMap<>();
                String result = JobConfigBuilder.buildJson(srcInfo, destInfo, property, list);
                Object json = JSONObject.parse(JobConfigBuilder.encryptPassword(result));
                return json;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "{}";
    }


    @ResponseBody
    @RequestMapping(value = "/doTimingDupCheck")
    @AuthIgnore
    public Object doTimingDupCheck(@ModelAttribute("srcID") String srcID, @ModelAttribute("destID") String destID, @ModelAttribute("mediaName") String mediaName, @ModelAttribute("timing_yn") String timing_yn) {
        try {
            long sid = Long.parseLong(srcID);
            long did = Long.parseLong(destID);
            boolean yn = Boolean.parseBoolean(timing_yn);
            if (yn) {
                int count = jobService.timingDupCheck(sid, did, mediaName);
                if (count <= 0) {
                    return "SUCCESS";
                }
                return count;
            }
            return "SUCCESS";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }

    }

    /**
     * 启动job
     *      支持批量启动
     *
     * @param request
     * @return
     */
    @ResponseBody
        @RequestMapping(value = "/doStart")
        public String doStart(HttpServletRequest request) {

            String idStr = request.getParameter("jobId");
            String jobName = request.getParameter("jobNameDisp");
            String jvmArgsXms = request.getParameter("jvmArgsXms");
            String jvmArgsXmx = request.getParameter("jvmArgsXmx");
            String worker = request.getParameter("worker");
            String isDebug = request.getParameter("isDebug");
            if (StringUtils.isBlank(idStr)) {
                logger.warn("id is null");
                return "fail";
            }
            if (StringUtils.isBlank(jobName)) {
                logger.warn("job name is null");
                return "fail";
            }
            if (StringUtils.isBlank(worker)) {
                logger.warn("worker address is null");
                return "fail";
            }

            //校验
            String[] jobIdArr = idStr.split(",");

            //启动
            for (String jobId : jobIdArr) {
                JobConfigInfo info = jobService.getJobConfigById(Long.parseLong(jobId));
                Map<String, String> map = FlinkerJobUtil.replaceDynamicParameter(info, new HashMap<String, String>());

                JobCommand command = new JobCommand();
                command.setJobId(new Long(jobId));
                command.setJobName(info.getJob_name());
                command.setType(JobCommand.Type.Start);
                if ("yes".equalsIgnoreCase(isDebug)) {
                    command.setDebug(true);
                }
                if (!StringUtils.isBlank(jvmArgsXms) && !StringUtils.isBlank(jvmArgsXmx)) {
                    command.setJvmArgs(MessageFormat.format("-Xms{0} -Xmx{1}", jvmArgsXms, jvmArgsXmx));
                }
                if (map != null && map.size() > 0) {
                    command.setDynamicParam(true);
                    command.setMapParam(map);
                }

                String result = jobControlService.start(command, worker);
                if(jobIdArr.length == 1){
                    return result;
                }
                AuditLogUtils.saveAuditLog(getAuditLogInfo(info, "005001010", AuditLogOperType.other.getValue()));
            }
        return "success";
    }


    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("jobConfig/add");
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("view") JobConfigView view) {
        try {
            int srcID = Integer.parseInt(view.getJob_src_media_source_name());
            int destID = Integer.parseInt(view.getJob_target_media_source_name());
            List<String> paths = new ArrayList<>();
            if (!JsonVerfiy(view.getJob_content())) {
                return "Json Formatting Illegality ,forbid save!!!";
            }
            String jobContent = view.getJob_content();
            if(StringUtils.isNotBlank(jobContent) && jobContent.contains(ESPECIALLY_CHAR_AND)) {
                jobContent = jobContent.replace(ESPECIALLY_CHAR_AND,ESPECIALLY_CHAR_AND_ORGINAL);
                view.setJob_content(jobContent);
            }

            if (view.getSrcType().toUpperCase().equals(MediaSourceType.SDDL.name())) {
                MediaSourceInfo info = mediaSourceService.getById(new Long(srcID));
                SddlMediaSrcParameter sddl = info.getParameterObj();
                List<Long> ids = sddl.getPrimaryDbsId();
                List<String> names = new ArrayList<>();
                for (Long id : ids) {
                    MediaSourceInfo msi = mediaSourceService.getById(id);
                    names.add(msi.getParameterObj().getNamespace());
                }
                String json = view.getJob_content();
                for (String db_name : names) {
                    String content = JobConfigBuilder.replaceJsonResult(MediaSourceType.SDDL, info, json, db_name);
                    JobConfigInfo configInfo = new JobConfigInfo();

                    configInfo.setJob_content(JobConfigBuilder.decryptPassword(content));
                    configInfo.setJob_media_name(view.getJob_media_name());
                    String job_name = view.getJob_media_name() + randomString(10);
                    configInfo.setJob_name(job_name);
                    configInfo.setJob_src_media_source_id(srcID);
                    configInfo.setJob_target_media_source_id(destID);
                    if (Boolean.parseBoolean(view.getTiming_yn())) {
                        configInfo.setTiming_yn(Boolean.parseBoolean(view.getTiming_yn()));
                        configInfo.setTiming_transfer_type(view.getTiming_transfer_type());
                        configInfo.setTiming_target_worker(view.getTiming_target_worker());
                        String parameter = view.getTiming_parameter();
                        if (StringUtils.isNotBlank(parameter)) {
                            configInfo.setTiming_parameter(parseTimingParameter(parameter));
                        }
                        createSchedule(configInfo);
                    } else {
                        createAndSend(configInfo);
                    }
                }
            }
            if (view.getSrcType().toUpperCase().equals(MediaSourceType.HBASE.name())) {
                String str = view.getHbase_split_count();
                int count = -1;
                try {
                    count = Integer.parseInt(str);
                } catch (Exception e) {
                    //ignore
                }
                if (count >= 1) {
                    MediaSourceInfo info = mediaSourceService.getById(new Long(srcID));
                    String tableName = view.getJob_media_name();
                    List<HBaseRange> ranges = HBaseUtil.generateHBaseSplitInfo(info, tableName, count);
                    String job_content = view.getJob_content();
                    JobConfigInfo configInfo = null;
                    for (HBaseRange r : ranges) {
                        String json = JobConfigBuilder.replaceJsonResult(MediaSourceType.HBASE, info, job_content, r);
                        json = FlinkerJobUtil.formatJson(json);
                        configInfo = new JobConfigInfo();
                        configInfo.setJob_content(JobConfigBuilder.decryptPassword(json));
                        configInfo.setJob_media_name(view.getJob_media_name());
                        String job_name = view.getJob_media_name() + randomString(10);
                        configInfo.setJob_name(job_name);
                        configInfo.setJob_src_media_source_id(srcID);
                        configInfo.setJob_target_media_source_id(destID);
                        if (Boolean.parseBoolean(view.getTiming_yn())) {
                            configInfo.setTiming_yn(Boolean.parseBoolean(view.getTiming_yn()));
                            configInfo.setTiming_transfer_type(view.getTiming_transfer_type());
                            configInfo.setTiming_target_worker(view.getTiming_target_worker());
                            String parameter = view.getTiming_parameter();
                            if (StringUtils.isNotBlank(parameter)) {
                                configInfo.setTiming_parameter(parseTimingParameter(parameter));
                            }
                        }
                    }
                    createAndSend(configInfo);
                    paths.add(FlinkerJobUtil.parseHDFSWritePath(configInfo));
                }
                //如果没有设置hbase count
                else {
                    JobConfigInfo info = new JobConfigInfo();
                    info.setJob_content(JobConfigBuilder.decryptPassword(view.getJob_content()));
                    info.setJob_media_name(view.getJob_media_name());
                    info.setJob_name(view.getJob_name());
                    info.setJob_src_media_source_id(srcID);
                    info.setJob_target_media_source_id(destID);
                    if (Boolean.parseBoolean(view.getTiming_yn())) {
                        info.setTiming_yn(Boolean.parseBoolean(view.getTiming_yn()));
                        info.setTiming_transfer_type(view.getTiming_transfer_type());
                        info.setTiming_target_worker(view.getTiming_target_worker());
                        String parameter = view.getTiming_parameter();
                        if (StringUtils.isNotBlank(parameter)) {
                            info.setTiming_parameter(parseTimingParameter(parameter));
                        }
                        createSchedule(info);
                    } else {
                        createAndSend(info);
                    }
                    paths.add(FlinkerJobUtil.parseHDFSWritePath(info));
                }
            } else if(view.getDestType().toUpperCase().equals(MediaSourceType.ELASTICSEARCH.name()))  {
                List<MediaSourceInfo> list = Lists.newArrayList(mediaSourceService.getById(new Long(destID)));
                JobConfigInfo info = new JobConfigInfo();
                info.setJob_name(view.getJob_name());
                info.setJob_content(JobConfigBuilder.decryptPassword(view.getJob_content()));
                info.setJob_media_name(view.getJob_media_name());
                info.setJob_src_media_source_id(srcID);
                info.setJob_target_media_source_id(destID);
                if (Boolean.parseBoolean(view.getTiming_yn())) {
                    info.setTiming_yn(Boolean.parseBoolean(view.getTiming_yn()));
                    info.setTiming_transfer_type(view.getTiming_transfer_type());
                    info.setTiming_target_worker(view.getTiming_target_worker());
                    String parameter = view.getTiming_parameter();
                    if (StringUtils.isNotBlank(parameter)) {
                        info.setTiming_parameter(parseTimingParameter(parameter));
                    }
                    createSchedule(info);
                } else {
                    list.forEach(mediaSourceInfo -> {
                        EsMediaSrcParameter parameter = mediaSourceInfo.getParameterObj();
                        String value = parameter.getClusterHosts();
                        String newJson = JobConfigBuilder.modifyWriterPath(info.getJob_content(),value);
                        info.setJob_content(newJson);
                        String job_name = view.getJob_name() + "_" + randomString(10);
                        info.setJob_name(job_name);
                        createAndSend(info);
                    });
                }

            }
            else {
                JobConfigInfo info = new JobConfigInfo();
                info.setJob_content(JobConfigBuilder.decryptPassword(view.getJob_content()));
                info.setJob_media_name(view.getJob_media_name());
                info.setJob_name(view.getJob_name());
                info.setJob_src_media_source_id(srcID);
                info.setJob_target_media_source_id(destID);
                if (Boolean.parseBoolean(view.getTiming_yn())) {
                    info.setTiming_yn(Boolean.parseBoolean(view.getTiming_yn()));
                    info.setTiming_transfer_type(view.getTiming_transfer_type());
                    info.setTiming_target_worker(view.getTiming_target_worker());
                    String parameter = view.getTiming_parameter();
                    if (StringUtils.isNotBlank(parameter)) {
                        info.setTiming_parameter(parseTimingParameter(parameter));
                    }
                    createSchedule(info);
                } else {
                    createAndSend(info);
                }
                paths.add(FlinkerJobUtil.parseHDFSWritePath(info));
            }
            return "success";
        } catch (Exception e) {
            logger.error("Add job config Error.", e);
            return e.getMessage();
        }

    }


    @RequestMapping(value = "/toFastAdd")
    public ModelAndView toFastAdd() {
        ModelAndView mav = new ModelAndView("jobConfig/fastAdd");
        return mav;
    }


    @ResponseBody
    @RequestMapping(value = "/doFastAdd")
    public String doFastAdd(@ModelAttribute("job_src_media_source_name") String srcID, @ModelAttribute("job_target_media_source_name") String destID,
    @ModelAttribute("timing_yn") String timing_yn, @ModelAttribute("create_schedule") String schedule_yn,
    @ModelAttribute("es_column_join") String es_column_join, HttpServletRequest request) {
        try {
            String[] src_names = request.getParameterValues("sourceTableName");
            String[] dest_names = request.getParameterValues("targetTableName");
            String[] sourceColumn = request.getParameterValues("sourceColumnHidden");
            String[] targetColumn = request.getParameterValues("targetColumnHidden");
            String[] columnMappingMode = request.getParameterValues("columnMappingModeHidden");

            MediaSourceInfo srcInfo = mediaSourceService.getById(Long.parseLong(srcID));
            MediaSourceInfo destInfo = mediaSourceService.getById(Long.parseLong(destID));
            List<String> paths = new ArrayList<>();
            //String[] src_names = srcName.split(",");
            //String[] dest_names = destName.split(",");
            for (int i = 0; i < src_names.length; i++) {
                String srcName = src_names[i];
                String destName = dest_names[i];
                String[] sourceColumnArr = sourceColumn[i].split("\\+");
                String[] targetColumnArr = targetColumn[i].split("\\+");
                ColumnMappingMode mode = ColumnMappingMode.valueOf(columnMappingMode[i]);
                List<String> srcColumns = new ArrayList<>();
                List<String> targetColumns = new ArrayList<>();
                for(String s : sourceColumnArr) {
                    srcColumns.add(s);
                }
                for(String s : targetColumnArr) {
                   targetColumns.add(s);
                }

                Map<String, String> map = new HashMap<>();
                JobExtendProperty property = new JobExtendProperty();
                String jobContent = "{}";
                if(destInfo.getType()==MediaSourceType.ELASTICSEARCH) {
                    HashMap<String,String> m = new HashMap<>();
                    if( !StringUtils.equals(srcName,destName) ) {
                        m.put("esWriterIndexType",destName);
                    }
                    if( StringUtils.isNotBlank(es_column_join)) {
                        m.put("joinColumn",es_column_join);
                    }
                    property.setWriter(m);
                    jobContent = JobConfigBuilder.buildJson(srcInfo, destInfo, property, srcName, srcColumns, srcName, targetColumns, mode);
                }
                else {
                    jobContent = JobConfigBuilder.buildJson(srcInfo, destInfo, property, srcName, srcColumns, destName, targetColumns, mode);
                }
                jobContent = FlinkerJobUtil.formatJson(jobContent);
                JobConfigInfo info = new JobConfigInfo();
                info.setJob_content(jobContent);
                info.setJob_media_name(dest_names[i]);
                info.setJob_src_media_source_id(Integer.parseInt(srcID));
                info.setJob_target_media_source_id(Integer.parseInt(destID));
                if (Boolean.parseBoolean(timing_yn)) {
                    String job_name = "CRON_" + dest_names[i] + "_" + randomString(10);
                    info.setJob_name(job_name);
                    info.setTiming_yn(true);
                } else {
                    String job_name = dest_names[i] + "_" + randomString(10);
                    info.setJob_name(job_name);
                    info.setTiming_yn(false);
                }

                //如果开启了schedule选项，需要往schedule表里面插入一个记录
                if (Boolean.parseBoolean(timing_yn) && Boolean.parseBoolean(schedule_yn)) {
                    if(destInfo.getType()==MediaSourceType.ELASTICSEARCH) {
                        List<MediaSourceInfo> mediaSourceInfos = Lists.newArrayList(mediaSourceService.getById(Long.parseLong(destID)));
                        mediaSourceInfos.forEach(mediaSourceInfo -> {
                            EsMediaSrcParameter parameter = mediaSourceInfo.getParameterObj();
                            String value = parameter.getClusterHosts();
                            String newJson = JobConfigBuilder.modifyWriterPath(info.getJob_content(),value);
                            info.setJob_content(newJson);
                            String job_name = info.getJob_name() + "_" + randomString(10);
                            if ( info.isTiming_yn() ) {
                                job_name = "CRON_" + job_name;
                            }
                            info.setJob_name(job_name);
                            createSchedule(info);
                        });
                    }
                    else {
                        createSchedule(info);
                    }

                    paths.add(FlinkerJobUtil.parseHDFSWritePath(info));
                } else {
                    if(destInfo.getType()==MediaSourceType.ELASTICSEARCH) {
                        List<MediaSourceInfo> mediaSourceInfos = Lists.newArrayList(mediaSourceService.getById(Long.parseLong(destID)));
                        mediaSourceInfos.forEach(mediaSourceInfo -> {
                            EsMediaSrcParameter parameter = mediaSourceInfo.getParameterObj();
                            String value = parameter.getClusterHosts();
                            String newJson = JobConfigBuilder.modifyWriterPath(info.getJob_content(),value);
                            info.setJob_content(newJson);
                            String tmpName = info.getJob_name();
                            String job_name = tmpName + "_" + randomString(10);
                            info.setJob_name(job_name);
                            createAndSend(info);
                            info.setJob_name(tmpName);
                            job_name = "";
                        });
                    } else {
                        createAndSend(info);
                    }
                    paths.add(FlinkerJobUtil.parseHDFSWritePath(info));
                }
            }
            String[] arr = request.getParameterValues("sourceTableName");
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<arr.length;i++) {
                if(i==arr.length-1) {
                    sb.append(arr[i]);
                }else {
                    sb.append(arr[i]).append(",");
                }
            }
            return "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }


    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("jobConfig/edit");
        JobConfigInfo info = new JobConfigInfo();
        if (StringUtils.isNotBlank(id)) {
            info = jobService.getJobConfigById(Long.parseLong(id));
        }
        JobConfigView view = new JobConfigView();
        if (info != null) {
            view.setId(info.getId());
            view.setJob_name(info.getJob_name());
            view.setTiming_yn(Boolean.toString(info.isTiming_yn()));
            view.setTiming_transfer_type(info.getTiming_transfer_type());
            view.setTiming_target_worker(info.getTiming_target_worker());
            if (StringUtils.isNotBlank(info.getTiming_parameter())) {
                TimingParameter p = JSONObject.parseObject(info.getTiming_parameter(), TimingParameter.class);
                view.setTiming_parameter(p.getJvmMemory());
            }
            //view.setTiming_parameter(info.getTiming_parameter());
            //Object json = JSONObject.parse(info.getJob_content());
            //view.setJson(json);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            String create_time = sdf.format(new Date(info.getCreate_time().getTime()));
            view.setCreate_time(create_time);
            view.setJob_content(JobConfigBuilder.encryptPassword(info.getJob_content()));
        }
        mav.addObject("jobConfigView", view);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    @Transactional
    public String doEdit(@ModelAttribute("jobConfigView") JobConfigView jobConfigView) {
        try {
            if (jobConfigView.getId() <= 0) {
                throw new RuntimeException("job config id is empty");
            }
            JobConfigInfo info = new JobConfigInfo();
            info.setId(jobConfigView.getId());
            info.setJob_name(jobConfigView.getJob_name());
            if (!JsonVerfiy(jobConfigView.getJob_content())) {
                return "Json Formatting Illegality ,forbid save!!!";
            }
            String jobContent = jobConfigView.getJob_content();
            if(StringUtils.isNotBlank(jobContent) && jobContent.contains(ESPECIALLY_CHAR_AND)) {
                jobContent = jobContent.replace(ESPECIALLY_CHAR_AND,ESPECIALLY_CHAR_AND_ORGINAL);
                jobConfigView.setJob_content(jobContent);
            }

            String json = jobConfigView.getJob_content();
            String readerTable = JobContentParseUtil.getReaderTable(json);
            String writerTable = JobContentParseUtil.getWriterTable(json);
            jobConfigView.setJob_media_name(readerTable);
            jobConfigView.setJob_media_target_name(writerTable);
            jobConfigView.setJob_content(json);

            info.setJob_content(JobConfigBuilder.decryptPassword(jobConfigView.getJob_content()));
            info.setTiming_yn(Boolean.parseBoolean(jobConfigView.getTiming_yn()));
            info.setTiming_transfer_type(jobConfigView.getTiming_transfer_type());
            info.setTiming_target_worker(jobConfigView.getTiming_target_worker());
            info.setJob_media_name(jobConfigView.getJob_media_name());
            info.setJob_media_target_name(jobConfigView.getJob_media_target_name());
            String parameter = jobConfigView.getTiming_parameter();
            if (StringUtils.isNotBlank(parameter)) {
                info.setTiming_parameter(parseTimingParameter(parameter));
            }

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date d = sdf.parse( jobConfigView.getCreate_time() );
            Timestamp timestamp = new Timestamp(d.getTime());
            info.setCreate_time(timestamp);
            jobService.modifyJobConfigContent(info);
            AuditLogUtils.saveAuditLog(getAuditLogInfo(info, "005001007", AuditLogOperType.update.getValue()));
            return "success";
        } catch (Exception e) {
            logger.error("Update job config Error.", e);
            return e.getMessage();
        }

    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            JobConfigInfo jobConfigInfo = jobService.getJobConfigById(Long.parseLong(id));
            List<JobScheduleInfo> scheduleInfoList = jobScheduleService.getByConfigId(Long.parseLong(id));
            QuartzManager.getInstance().removeJobScheduleList(jobConfigInfo, scheduleInfoList);
            jobScheduleService.removeByJobConfigId(Long.parseLong(id));
            jobService.deleteJobConfigById(Long.parseLong(id));

            AuditLogUtils.saveAuditLog(getAuditLogInfo(jobConfigInfo, "005001008", AuditLogOperType.delete.getValue()));
            return "success";
        } catch (ValidationException e) {
            logger.error("Delete HBase Media Source Error.", e);
            return e.getMessage();
        }
    }



    @ResponseBody
    @RequestMapping(value = "/toReloadJobContent")
    public String toReloadJobContent(HttpServletRequest request) {
        try {
            String id = request.getParameter("id");
            if (StringUtils.isBlank(id)) {
                return "fail";
            }
            JobConfigInfo jobConfigInfo = jobService.getJobConfigById(Long.parseLong(id));
            SyncCheckUtil.checkModifyColumnWithoutOpen(jobConfigInfo);
            return "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }


    @ResponseBody
    @RequestMapping(value = "/toHistory")
    public ModelAndView toHistory(HttpServletRequest request) {
        String id = request.getParameter("id");
        String name = jobService.getJobConfigNameById(Long.parseLong(id));
        ModelAndView mav = new ModelAndView("jobConfig/history");
        mav.addObject("job_name", name);
        return mav;
    }


    @ResponseBody
    @RequestMapping(value = "/doHistory")
    public Page<JobExecutionView> doHistory(@RequestBody Map<String, String> map, HttpServletRequest request) {
        String name = map.get("job_name");
        String stateType = map.get("statType");
        long job_id = -1L;
        String srcType = null;
        String destType = null;
        String srcName = null;
        String destName = null;
        String mediaName = null;
        String startTime = null;
        String endTime = null;
        boolean isCheckAbandoned = false;
        boolean isCheckStart = false;
        if (StringUtils.isNotBlank(name)) {
            request.setAttribute("job_name", name);
            job_id = jobService.getJobConfigIDByName(name);
        }
        switch (stateType) {
            case "-1":
                isCheckAbandoned = true;
                isCheckStart = true;
                stateType = null;
                break;
            case "UNEXECUTE":
                stateType = JobExecutionState.UNEXECUTE;
                isCheckStart = true;
                break;
            case "RUNNING":
                stateType = JobExecutionState.RUNNING;
                isCheckAbandoned = true;
                break;
            case "KILLED":
                stateType = JobExecutionState.KILLED;
                isCheckStart = true;
                break;
            case "FAILED":
                stateType = JobExecutionState.FAILED;
                isCheckStart = true;
                break;
            case "SUCCEEDED":
                stateType = JobExecutionState.SUCCEEDED;
                isCheckStart = true;
                break;
            case "ABANDONED":
                stateType = JobExecutionState.ABANDONED;
                isCheckStart = true;
                break;
        }


        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Page<JobExecutionView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        List<JobExecutionInfo> jobExecutionInfos = jobService.queryJobExecutionStateByOptimized(stateType, job_id, srcType, srcName, destType, destName, mediaName,startTime,endTime);
        List<JobExecutionView> jobView = jobExecutionInfos.stream().map(i -> {
            String jobConfigName = jobService.getJobConfigNameById(i.getJob_id());
            JobExecutionView view = new JobExecutionView();
            if (StringUtils.isNotBlank(jobConfigName)) {
                view.setJob_name(jobConfigName);
            } else {
                view.setJob_name("none");
            }
            view.setByte_speed_per_second(JobExecutionController.formatNumberToString(i.getByte_speed_per_second()));
            String start_time = sdf.format(new Date(i.getStart_time().getTime()));
            String end_time = "";
            if (i.getEnd_time() != null) {
                end_time = sdf.format(new Date(i.getEnd_time().getTime()));
            }
            view.setStart_time(start_time);
            view.setEnd_time(end_time);
            view.setException(i.getException());
            view.setId(i.getId());
            view.setJob_id(i.getJob_id());
            view.setJob_queue_execution_id(i.getJob_queue_execution_id());
            view.setOriginal_configuration(i.getOriginal_configuration());
            view.setPercentage((i.getPercentage() * 100) + "%");
            view.setPid(i.getPid());
            view.setRecord_speed_per_second(i.getRecord_speed_per_second() + "records/s");
            if (StringUtils.isBlank(i.getState())) {
                view.setState(JobExecutionState.UNEXECUTE);
            } else {
                view.setState(i.getState());
            }
            view.setTask_communication_info(i.getTask_communication_info());
            view.setTotal_error_records(i.getTotal_error_records());
            view.setTotal_record(i.getTotal_record());
            view.setWait_reader_time(JobExecutionController.rounding(i.getWait_reader_time()));
            view.setWait_writer_time(JobExecutionController.rounding(i.getWait_writer_time()));
            if (StringUtils.isBlank(i.getWorker_address())) {
                view.setWorker_address("");
            } else {
                view.setWorker_address(i.getWorker_address());
            }
            view.setAbandonedValue(0);
            if (JobExecutionState.ABANDONED.equals(view.getState()) || JobExecutionState.FAILED.equals(view.getState()) ||
                    JobExecutionState.KILLED.equals(view.getState()) || JobExecutionState.SUCCEEDED.equals(view.getState()) || JobExecutionState.UNEXECUTE.equals(view.getState())) {
                view.setStartValue(1);
            }
            try {
                String json = FlinkerJobUtil.formatJson(i.getOriginal_configuration());
                view.setOriginal_configuration(json);
            }catch(Exception e) {
                view.setOriginal_configuration(i.getOriginal_configuration());
            }
            return view;
        }).collect(Collectors.toList());
        PageInfo<JobExecutionInfo> pageInfo = new PageInfo<>(jobExecutionInfos);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        //如果是获取所有类型，或者是获取RUNNING类型，则需要检查当前任务状态
        //如果任务状态是RUNNING，但是在zookeeper中已经不存在了，则废弃这个任务
        if (isCheckAbandoned) {
            Set<String> tasks = FlinkerJobUtil.getDataxRunningTask();
            for (JobExecutionView view : jobView) {
                if (JobExecutionState.RUNNING.equals(view.getState()) && !tasks.contains(view.getJob_name())) {
                    view.setAbandonedValue(1);
                }
            }
        }
        return page;
    }


    @RequestMapping(value = "/toReloadJob")
    public ModelAndView toReloadJob() {
        ModelAndView mav = new ModelAndView("jobConfig/reload");
        return mav;
    }

    @RequestMapping(value = "/reloadJobList")
    @ResponseBody
    @AuthIgnore
    public Page<JobConfigView> doReoloadJobList(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String name_id = parseNumber(map.get("db_name"));
        if (StringUtils.isBlank(name_id)) {
            Page<JobConfigView> page = new Page<>(map);
            return page;
        }

        Page<JobConfigView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        List<JobConfigInfo> jobConfigInfoList = jobService.getJobConfigByMediaSourceID(Long.parseLong(name_id));
        List<JobConfigView> jobView = jobConfigInfoList.stream().filter(j->{
            if(j.isTiming_yn()) {
                return true;
            } else {
                return false;
            }
        }).map(i -> {
            JobConfigView view = new JobConfigView();
            view.setId(i.getId());
            view.setJob_desc(i.getJob_desc());
            view.setJob_media_name(i.getJob_media_name());
            view.setJob_name(i.getJob_name());
            view.setTiming_yn(Boolean.toString(i.isTiming_yn()));
            String create_time = sdf.format(new Date(i.getCreate_time().getTime()));
            view.setCreate_time(create_time);
            MediaSourceInfo srcInfo = mediaSourceService.getById(new Long(i.getJob_src_media_source_id()));
            MediaSourceInfo targetInfo = mediaSourceService.getById(new Long(i.getJob_target_media_source_id()));
            if (srcInfo == null) {
                view.setJob_src_media_source_name("none");
            } else {
                view.setJob_src_media_source_name(srcInfo.getName());
            }
            if (targetInfo == null) {
                view.setJob_target_media_source_name("none");
            } else {
                view.setJob_target_media_source_name(targetInfo.getName());
            }
            return view;
        }).collect(Collectors.toList());
        PageInfo<JobConfigInfo> pageInfo = new PageInfo<>(jobConfigInfoList);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    @ResponseBody
    @RequestMapping(value = "/doReoloadJob")
    public String doReoloadJob(@ModelAttribute("id") String id) {
        try {
            if (StringUtils.isBlank(id)) {
                return "media source id can not be null";
            }
            jobService.roloadJobsByMediaSourceId(Long.valueOf(id));
        } catch (Exception e) {
            logger.error("reload job failure,", e);
            return e.getMessage();
        }
        return "success";
    }

    @ResponseBody
    @RequestMapping(value = "/caclRegion")
    @AuthIgnore
    public String caclHbaseRegionCount(@ModelAttribute("id") String id, @ModelAttribute("tableName") String tableName) {
        MediaSourceInfo info = mediaSourceService.getById(Long.parseLong(id));
        return HBaseUtil.getRegionCount(info, tableName) + "";
    }


    @ResponseBody
    @RequestMapping(value = "/doJobConfigMD5Info")
    public String doJobConfigMD5Info(@ModelAttribute("id") String id) {
        try {
            JobConfigInfo info = jobService.getJobConfigById(Long.parseLong(id));
            long src_media_id = info.getJob_src_media_source_id();
            long target_media_id = info.getJob_target_media_source_id();
            String job_media_name = info.getJob_media_name();
            long job_id = info.getId();
            String content = job_id + src_media_id + target_media_id + job_media_name;
            return getMd5(content);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }


    @ResponseBody
    @RequestMapping(value = "/doAddJobRunQueue")
    public String doAddJobRunQueue(@ModelAttribute("idList") String idList) {
        if (StringUtils.isBlank(idList)) {
            return "failure";
        }
        JobRunQueueInfo info = new JobRunQueueInfo();
        int jobCount = idList.split(",").length;
        info.setJobIdList(idList);
        info.setJobCount(jobCount);
        info.setQueueState(JobRunQueueState.INIT);
        queueService.createJobRunQueueInfo(info);
        return "success";
    }

    @ResponseBody
    @RequestMapping(value = "/JVMSize")
    public String getJVMSize() {
        String maxSize = ManagerConfig.current().getFlinkerJobMaxJVMMemory();
        String minSize = ManagerConfig.current().getFlinkerJobMinJVMMemory();
        String result = maxSize + "," + minSize;
        return result;
    }

    private static AuditLogInfo getAuditLogInfo(JobConfigInfo info, String menuCode, String operType){
        AuditLogInfo logInfo=new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName(info.getJob_name());
        logInfo.setOperType(operType);
        logInfo.setOperKey(info.getId());
        logInfo.setOperRecord(info.toString());
        return logInfo;
    }

    private void createMonitorInfo(JobConfigInfo jobConfig) {
        MonitorInfo info = new MonitorInfo();
        info.setResourceId(jobConfig.getId());
        info.setResourceName(jobConfig.getJob_name());
        info.setMonitorCat(MonitorCat.FLINKER_MONITOR.getKey());
        info.setMonitorType(MonitorType.FLINKER_EXCEPTION_MONITOR.getKey());
        info.setIsEffective(1);
        info.setThreshold(60000);
        info.setIntervalTime(600L);
        info.setMonitorRange("00:00-23:59");
        monitorService.insert(info);
    }

    private JobScheduleInfo assembleScheduleInfo() {
        JobScheduleInfo scheduleInfo = new JobScheduleInfo();
        scheduleInfo.setCron("* 1 * * *");
        scheduleInfo.setIsDelete(false);
        scheduleInfo.setIsRetry(false);
        scheduleInfo.setRetryNumber(0);
        scheduleInfo.setRetryInterval(0);
        scheduleInfo.setMaxRunningTime(600L);
        scheduleInfo.setOnlineState(1);
        scheduleInfo.setIsSuspend(false);
        return scheduleInfo;
    }


    private JobScheduleInfo assembleScheduleInfo(JobConfigView view) {
        JobScheduleInfo scheduleInfo = new JobScheduleInfo();
        scheduleInfo.setCron(view.getSchedule_cron());
        scheduleInfo.setIsDelete(false);
        scheduleInfo.setIsRetry(Boolean.parseBoolean(view.getSchedule_is_retry()));
        scheduleInfo.setRetryNumber(Integer.parseInt(view.getSchedule_retry_interval()));
        scheduleInfo.setRetryInterval(Integer.parseInt(view.getSchedule_max_retry()));
        scheduleInfo.setMaxRunningTime(Long.parseLong(view.getSchedule_max_runtime()));
        if ("true".equalsIgnoreCase(view.getSchedule_online_state())) {
            scheduleInfo.setOnlineState(1);
        } else {
            scheduleInfo.setOnlineState(0);
        }
        scheduleInfo.setIsSuspend(Boolean.parseBoolean(view.getSchedule_is_suppend()));
        return scheduleInfo;
    }


    /**
     * 验证json是否有效
     *
     * @param json
     * @return
     */
    private static boolean JsonVerfiy(String json) {
        if (StringUtils.isBlank(json)) {
            return false;
        }
        try {
            JSONObject.parse(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private static String parseTimingParameter(String paremeter) {
        TimingParameter p = new TimingParameter();
        p.setJvmMemory(paremeter);
        return JSONObject.toJSONString(p);
    }


    private static String parseNumber(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        char[] ch = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : ch) {
            Character character = new Character(c);
            if (character.isDigit(c)) {
                sb.append(c);
            }
        }
        try {
            Long.parseLong(sb.toString());
        } catch (Exception e) {
            return null;
        }
        return sb.toString();
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


    public static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }


    private static String filterCharacter(String name) {
        if (name == null || "".equals(name)) {
            return name;
        }
        if (name.contains("'")) {
            name = name.replaceAll("'", "");
        }
        if (name.contains("\"")) {
            name = name.replaceAll("\"", "");
        }
        if (name.contains("[")) {
            name = name.replaceAll("\\[", "");
        }
        if (name.contains("]")) {
            name = name.replaceAll("\\]", "");
        }
        return name;
    }

    private static int parseStringToInt(Object obj, int defaultNum) {
        if (obj == null) {
            return defaultNum;
        }
        String str = obj.toString();
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return defaultNum;
        }
    }

    private static String getStringByDefault(Object obj, String value) {
        if (obj == null || StringUtils.isBlank(obj.toString())) {
            return value;
        }
        if ("-1".equals(obj.toString())) {
            return null;
        }
        return obj.toString().trim();
    }

    private static long stringToLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return -1;
        }
    }

    private class SendMailThread implements Runnable {
        private MediaSourceInfo srcInfo;
        private MediaSourceInfo destInfo;
        private String names;
        private List<String> paths;

        public SendMailThread(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, String names, List<String> paths) {
            this.srcInfo = srcInfo;
            this.destInfo = destInfo;
            this.names = names;
            if (paths != null && paths.size() > 0) {
                this.paths = new ArrayList<String>(paths);
            } else {
                this.paths = new ArrayList<String>();
            }
        }

        @Override
        public void run() {
            try {
                sendMail(srcInfo, destInfo, names, paths);
            } catch (Exception e) {
                logger.error("send mail failure", e);
            }
        }
    }

    private void sendMail(MediaSourceInfo src, MediaSourceInfo dest, String names, List<String> paths) throws Exception {
        String title = src.getName() + "库同步" + dest.getType().name();
        String content = assembleMailInfo(src, dest, names, paths);
    }

    public static String assembleMailInfo(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, String names, List<String> paths) throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append("hi:").append("<br>").append("<br>");
        String env = ManagerConfig.current().getCurrentEnv();
        buf.append("&nbsp &nbsp &nbsp").append("当前环境 : ").append(env).append("<br/>");
        buf.append("&nbsp &nbsp &nbsp").append("您有个数据同步任务").append("从").append(srcInfo.getName()).append("(").append(srcInfo.getParameterObj().getMediaSourceType().name()).append(")  ")
                .append("同步到").append(destInfo.getName()).append("(").append(destInfo.getParameterObj().getMediaSourceType().name()).append(")  ").append(",请查看").append("<br>");

        //增加hdfs写入路径信息
        String hdfsInfo = assembleHDFSPathInfo(srcInfo, destInfo, names, paths);
        if (StringUtils.isNotBlank(hdfsInfo)) {
            buf.append(hdfsInfo);
        }
        if (StringUtils.isNotBlank(names)) {
            String[] tables = names.split(",");
            for (String t : tables) {
                buf.append("").append("<br>");
                buf.append("表名称:").append(t).append("    ").append("<br>");
                List<ColumnMeta> columns = MetaManager.getColumns(srcInfo, t);
                buf.append(assembleTableInfo(columns, srcInfo, destInfo));
                buf.append("</table>");
                buf.append("<br/><br/>");
                buf.append("<hr/>");
            }//end for
        }
        return buf.toString();
    }


    private static String assembleHDFSPathInfo(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, String names, List<String> paths) {
        if (destInfo.getType() != MediaSourceType.HDFS) {
            return "<br/>";
        }
        if (StringUtils.isNotBlank(names)) {
            StringBuilder sb = new StringBuilder();
            String[] tables = names.split(",");
            if (tables.length > 0) {
                sb.append("HDFS写入的路径如下：<br/>");
            }
/*
            for (String t : tables) {
                String path = "";
                HDFSMediaSrcParameter parameter = (HDFSMediaSrcParameter)destInfo.getParameterObj();
                //path = parameter.getWritePath();
                if(StringUtils.isNotBlank(path)) {
                    sb.append(path).append("<br/>");
                }
            }
*/
            for (String p : paths) {
                if (StringUtils.isNotBlank(p)) {
                    sb.append(p).append("<br/>");
                }
            }
            return sb.toString();
        }
        return null;
    }

    private static String assembleTableInfo(List<ColumnMeta> columns, MediaSourceInfo info, MediaSourceInfo destInfo) {
        boolean needTransform = false;
        StringBuilder buf = new StringBuilder();
        if (MediaSourceType.HBASE == info.getType()) {
            if (MediaSourceType.ELASTICSEARCH == destInfo.getType() || MediaSourceType.HDFS == destInfo.getType() || MediaSourceType.POSTGRESQL == destInfo.getType()) {
                buf.append("<table border='1'>").append("<tr><td>列族名称</td><td>字段名称</td><td>转换后的类型</td></tr>");
                needTransform = true;
            } else {
                buf.append("<table border='1'>").append("<tr><td>列族名称</td><td>字段名称</td></tr>");
            }

        } else if (MediaSourceType.MYSQL == info.getType() || MediaSourceType.SQLSERVER == info.getType() || MediaSourceType.POSTGRESQL == info.getType()) {
            if (MediaSourceType.ELASTICSEARCH == destInfo.getType() || MediaSourceType.HDFS == destInfo.getType()) {
                buf.append("<table border='1'>").append("<tr><td>字段名称</td><td>字段类型</td><td>字段长度</td><td>字段精度</td><td>字段描述</td><td>转换后的类型</td></tr>");
                needTransform = true;
            } else {
                buf.append("<table border='1'>").append("<tr><td>字段名称</td><td>字段类型</td><td>字段长度</td><td>字段精度</td><td>字段描述</td></tr>");
            }
        } else {
            if (info.getType() != destInfo.getType() && (MediaSourceType.ELASTICSEARCH == info.getType() || MediaSourceType.HDFS == info.getType())) {
                buf.append("<table border='1'>").append("<tr><td>字段名称</td><td>字段类型</td><td>字段描述</td><td>转换后的类型</td></tr>");
                needTransform = true;
            } else {
                buf.append("<table border='1'>").append("<tr><td>字段名称</td><td>字段类型</td><td>字段描述</td></tr>");
            }
        }

        MediaMeta target = new MediaMeta();
        if (needTransform) {
            MediaMeta mm = new MediaMeta();
            mm.setColumn(columns);
            mm.setName(info.getName());
            mm.setNameSpace(info.getParameterObj().getNamespace());
            mm.setDbType(info.getType());
            if (destInfo.getType() == MediaSourceType.ELASTICSEARCH) {
                target = MetaMapping.transformToES(mm);
            } else if (destInfo.getType() == MediaSourceType.HBASE) {
                target = MetaMapping.transformToHBase(mm);
            } else if (destInfo.getType() == MediaSourceType.HDFS) {
                target = MetaMapping.transformToHDFS(mm);
            } else if (destInfo.getType() == MediaSourceType.MYSQL || destInfo.getType() == MediaSourceType.SQLSERVER || destInfo.getType() == MediaSourceType.POSTGRESQL) {
                target = MetaMapping.transformToRDBMS(mm);
            }
        }

        Map<String, ColumnMeta> map = new HashMap<>();
        if (target.getColumn() != null) {
            List<ColumnMeta> targetColumns = target.getColumn();
            for (ColumnMeta cm : targetColumns) {
                map.put(cm.getName(), cm);
            }
        }

        if (columns != null && columns.size() > 0) {
            for (ColumnMeta cm : columns) {
                ColumnMeta targetColumn = map.get(cm.getName());
                if (targetColumn == null) {
                    targetColumn = new ColumnMeta();
                    targetColumn.setType("");
                }
                if (MediaSourceType.HBASE == info.getType()) {
                    if (MediaSourceType.ELASTICSEARCH == destInfo.getType() || MediaSourceType.HDFS == destInfo.getType() || MediaSourceType.POSTGRESQL == destInfo.getType()) {
                        buf.append("<tr><td>").append(cm.getColumnFamily()).append("</td>").append("<td>").append(cm.getName()).append("</td>").append("<td>").append(targetColumn.getType()).append("</td></tr>");
                    } else {
                        buf.append("<tr><td>").append(cm.getColumnFamily()).append("</td>").append("<td>").append(cm.getName()).append("</td></tr>");
                    }
                } else if (MediaSourceType.MYSQL == info.getType() || MediaSourceType.SQLSERVER == info.getType() || MediaSourceType.POSTGRESQL == info.getType()) {
                    //如果目标端是HDFS或者ES，则把转换后的类型也显示出来
                    if (MediaSourceType.ELASTICSEARCH == destInfo.getType() || MediaSourceType.HDFS == destInfo.getType()) {
                        String columnDesc = "";
                        if (StringUtils.isNotBlank(cm.getColumnDesc())) {
                            columnDesc = cm.getColumnDesc();
                        }
                        buf.append("<tr><td>").append(cm.getName()).append("</td>").append("<td>").append(cm.getType()).append("</td>");
                        buf.append("<td>").append(cm.getLength()).append("</td>").append("<td>").append(cm.getDecimalDigits()).append("</td>").append("<td>").append(columnDesc).append("</td>").
                                append("<td>").append(targetColumn.getType()).append("</td></tr>");
                    } else {
                        String columnDesc = "";
                        if (StringUtils.isNotBlank(cm.getColumnDesc())) {
                            columnDesc = cm.getColumnDesc();
                        }
                        buf.append("<tr><td>").append(cm.getName()).append("</td>").append("<td>").append(cm.getType()).append("</td>");
                        buf.append("<td>").append(cm.getLength()).append("</td>").append("<td>").append(cm.getDecimalDigits()).append("</td>").append("<td>").append(columnDesc).append("</td></tr>");
                    }
                } else {
                    String columnDesc = "";
                    if (StringUtils.isNotBlank(cm.getColumnDesc())) {
                        columnDesc = cm.getColumnDesc();
                    }
                    if (info.getType() != destInfo.getType() && (MediaSourceType.ELASTICSEARCH == info.getType() || MediaSourceType.HDFS == info.getType())) {
                        buf.append("<tr><td>").append(cm.getName()).append("</td>").append("<td>").append(cm.getType()).append("</td>").append("<td>").append(columnDesc).append("</td>").
                                append("<td>").append(targetColumn.getType()).append("</td></tr>");
                    } else {
                        buf.append("<tr><td>").append(cm.getName()).append("</td>").append("<td>").append(cm.getType()).append("</td>").append("<td>").append(columnDesc).append("</td></tr>");
                    }
                }
            }
        }
        return buf.toString();
    }


    /**
     * 用于获取一个String的md5值
     *
     * @param str
     * @return
     */
    public static String getMd5(String str) {
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

    private void createSchedule(JobConfigInfo info) {
        try {
            parseAndSetJobSrcTargetTable(info);
            jobService.createJobConfig(info);

            long jobConfigId = jobService.getJobConfigIDByName(info.getJob_name());
            JobConfigInfo configInfo = jobService.getJobConfigById(jobConfigId);
            createMonitorInfo(configInfo);
            AuditLogUtils.saveAuditLog(getAuditLogInfo(info, "005001003", AuditLogOperType.insert.getValue()));
        }catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    @Transactional
    private  void createAndSend(JobConfigInfo info) {
        try {
            parseAndSetJobSrcTargetTable(info);
            jobService.createJobConfig(info);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void parseAndSetJobSrcTargetTable(JobConfigInfo info) {
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

}