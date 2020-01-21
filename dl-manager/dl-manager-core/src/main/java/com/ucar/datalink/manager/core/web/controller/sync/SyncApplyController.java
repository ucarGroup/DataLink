package com.ucar.datalink.manager.core.web.controller.sync;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.ucar.datalink.biz.dal.SyncApplyDAO;
import com.ucar.datalink.biz.module.PropertyConstant;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.media.ColumnMappingMode;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.sync.*;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import com.ucar.datalink.manager.core.web.dto.sync.SyncApplyView;
import com.ucar.datalink.manager.core.web.util.IncrementSyncUtil;
import com.ucar.datalink.manager.core.web.util.MediaMappingConfigUtil;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import com.ucar.datalink.util.SyncUtil;
import com.ucar.datalink.util.VirtualDataSourceUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by sqq on 2017/9/19.
 */
@Controller
@RequestMapping(value = "/sync/apply/")
public class SyncApplyController {

    private static final Logger logger = LoggerFactory.getLogger(SyncApplyController.class);

    @Autowired
    private SyncApplyService syncApplyService;

    @Autowired
    private SyncApplyDAO syncApplyDAO;

    @Autowired
    private MediaSourceService mediaSourceService;

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @Autowired
    private TaskConfigService taskService;

    @Autowired
    private  MediaService mediaService;

    @RequestMapping(value = "/syncApplyList")
    public ModelAndView syncApplyList(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("syncApply/list");
        mav.addObject("SyncApplyStatusList", SyncApplyStatus.getAllSyncApplyStatus());
        mav.addObject("applyUserIdList", userService.getList());
        UserInfo user = getLoginUser(request);
        String code = "";
        if (userService.isSuper(user)) {
            code = RoleType.SUPER.toString();
        } else if (userService.isApprover(user)) {
            code = RoleType.APPROVER.toString();
        }
        mav.addObject("roleType", code);
        return mav;
    }

    @RequestMapping(value = "/initSyncApply")
    @ResponseBody
    public Page<SyncApplyView> initSyncApply(@RequestBody Map<String, String> map, HttpServletRequest request) {
        String status = map.get("applyStatus");
        String applyUser = map.get("applyUserId");
        String type = map.get("applyType");
        SyncApplyStatus applyStatus = StringUtils.isBlank(status) ? null : SyncApplyStatus.valueOf(status);
        Long applyUserId = StringUtils.isBlank(applyUser) ? null : Long.valueOf(applyUser);
        String applyType = StringUtils.isBlank(type) ? null : type;

        UserInfo user = getLoginUser(request);
        Boolean isSuper = userService.isSuper(user);

        Page<SyncApplyView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<SyncApplyInfo> syncApplyListForQueryPage;
        if (isSuper) {
            syncApplyListForQueryPage = syncApplyService.syncApplyListForQueryPage(applyStatus, applyUserId, applyType, null);
        } else {
            syncApplyListForQueryPage = syncApplyService.syncApplyListForQueryPage(applyStatus, applyUserId, applyType, user.getId());
        }

        //构造view
        List<SyncApplyView> mediaMappingViews = syncApplyListForQueryPage.stream().map(i -> {
            SyncApplyView view = new SyncApplyView();
            view.setId(i.getId());
            view.setApplyStatus(i.getApplyStatus());
            view.setApplyType(i.getApplyType());
            view.setIsInitialData(i.getIsInitialData());
            view.setApplyContent(i.getApplyContent());
            view.setApplyRemark(i.getApplyRemark());
            view.setApplyUserId(i.getApplyUserId());
            view.setOperateUserId(i.getOperateUserId());
            view.setCreateTime(i.getCreateTime());
            Long srcMediaSourceId = i.getApplyContentObj().getApplyParameterList().get(0).getSrcMediaSourceId();
            Long targetMediaSourceId = i.getApplyContentObj().getApplyParameterList().get(0).getTargetMediaSourceId();
            MediaSourceInfo srcMediaSource = mediaSourceService.getById(srcMediaSourceId);
            MediaSourceInfo targetMediaSource = mediaSourceService.getById(targetMediaSourceId);
            if (srcMediaSource != null) {
                view.setSrcMediaSourceName(srcMediaSource.getName());
            }
            if (targetMediaSource != null) {
                view.setTargetMediaSourceName(targetMediaSource.getName());
            }
            if (i.getApplyUserInfo() != null) {
                view.setApplyUserName(i.getApplyUserInfo().getUserName());
            }
            Boolean canApprove = syncApplyService.canApprove(user.getId(), i.getId());
            view.setCanApprove(canApprove);
            if (isSuper) {
                view.setLoginRoleType(RoleType.SUPER);
            }
            view.setIsAutoKeeper(false);
            List<SyncApproveInfo> approveInfos = syncApplyService.getSyncApproveInfoByApplyId(i.getId());
            for (SyncApproveInfo approveInfo : approveInfos) {
                Long approveUserId = approveInfo.getApproveUserId();
                UserInfo approver = userService.getById(approveUserId);
                if (approver != null && userService.isAutoKeeper(approver)) {
                    view.setIsAutoKeeper(true);
                    break;
                }
            }
            return view;
        }).collect(Collectors.toList());
        PageInfo<SyncApplyInfo> pageInfo = new PageInfo<>(syncApplyListForQueryPage);
        page.setDraw(page.getDraw());
        page.setAaData(mediaMappingViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("syncApply/add");
        List<UserInfo> approveUserIdList = userService.getUserInfoByRoleType(RoleType.APPROVER);
        List<UserInfo> autoKeeperUserIdList = userService.getUserInfoByRoleType(RoleType.AUTOKEEPER);
        approveUserIdList.addAll(autoKeeperUserIdList);

        List<UserInfo> distinctList = approveUserIdList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(bean -> bean.getId()))), ArrayList::new));
        mav.addObject("approveUserIdList", distinctList);
        mav.addObject("srcMediaSourceTypeList", MediaSourceType.getAllSrcMediaSourceTypes());
        mav.addObject("srcMediaSourceTypeForIncrement", MediaSourceType.getAllSrcTypesForIncrement());
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(SyncApplyView syncApplyView, HttpServletRequest request) {
        try {
            String[] sourceTableName = request.getParameterValues("sourceTableName");
            String[] targetTableName = request.getParameterValues("targetTableName");
            String[] sourceColumn = request.getParameterValues("sourceColumnHidden");
            String[] targetColumn = request.getParameterValues("targetColumnHidden");
            String[] columnMappingMode = request.getParameterValues("columnMappingModeHidden");
            String srcMediaSourceId = request.getParameter("srcMediaSourceId");
            String targetMediaSourceId = request.getParameter("targetMediaSourceId");
            String[] esColumnJoin = request.getParameterValues(PropertyConstant.ES_JOIN_COLUMN);
            String[] whereConditions = request.getParameterValues(PropertyConstant.WHERE_CONDITION);
            String esIsPrefixColumn = request.getParameter("esIsPrefixColumn");

            if(srcMediaSourceId.equals(targetMediaSourceId)) {
                throw new RuntimeException("源端和目标端不能是同一个库");
            }

            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(Long.valueOf(srcMediaSourceId));
            MediaSourceInfo targetMediaSourceInfo = mediaSourceService.getById(Long.valueOf(targetMediaSourceId));
            List<MediaSourceInfo> realMediaSourceInfoList = mediaService.listRealMediaSourceInfos(mediaSourceInfo);
            List<MediaSourceInfo> realTargetMediaSourceInfoList = mediaService.listRealMediaSourceInfos(targetMediaSourceInfo);
            Set<String> tableNameSet = new HashSet<>();
            //校验mysql数据库表是否有主键
            MediaMappingConfigUtil.validateMysqlTablePk(realMediaSourceInfoList,sourceTableName,tableNameSet);
            //校验目标数据源是否存在
            MediaMappingConfigUtil.validateExistsTargetMedia(realTargetMediaSourceInfoList,sourceTableName,tableNameSet,targetTableName);

            SyncApplyInfo applyInfo = new SyncApplyInfo();
            SyncApplyContent applyContentObj = new SyncApplyContent();
            List<SyncApplyParameter> applyParameterList = new ArrayList<>();
            SyncApplyParameter applyParameter = new SyncApplyParameter();
            List<SyncApplyMapping> applyMappings = new ArrayList<>();
            for (int i = 0; i < sourceTableName.length; i++) {
                SyncApplyMapping applyMapping = new SyncApplyMapping();
                applyMapping.setSourceTableName(sourceTableName[i]);
                applyMapping.setTargetTableName(targetTableName[i]);
                applyMapping.setColumnMappingMode(ColumnMappingMode.valueOf(columnMappingMode[i]));
                String[] sourceColumnArr = sourceColumn[i].split("\\+");
                String[] targetColumnArr = targetColumn[i].split("\\+");
                List<String> sourceColumnList = new ArrayList<>();
                List<String> targetColumnList = new ArrayList<>();
                Collections.addAll(sourceColumnList, sourceColumnArr);
                Collections.addAll(targetColumnList, targetColumnArr);
                applyMapping.setSourceColumn(sourceColumnList);
                applyMapping.setTargetColumn(targetColumnList);
                if (parseMediaSourceTypeById(targetMediaSourceId) == MediaSourceType.ELASTICSEARCH) {
                    Map<String, String> map = applyMapping.getOtherMappingRelation();
                    if (map == null || map.size() == 0) {
                        map = new HashMap<>();
                    }
                    map.put(PropertyConstant.ES_JOIN_COLUMN, esColumnJoin[i]);
                    map.put(PropertyConstant.ES_WRITER_INDEX_TYPE, targetTableName[i]);
                    map.put(PropertyConstant.ES_IS_TABLE_PREFIX, esIsPrefixColumn);
                    applyMapping.setOtherMappingRelation(map);
                }
                if (parseMediaSourceTypeById(srcMediaSourceId) == MediaSourceType.ELASTICSEARCH) {
                    Map<String, String> map = applyMapping.getOtherMappingRelation();
                    if (map == null || map.size() == 0) {
                        map = new HashMap<>();
                    }
                    map.put(PropertyConstant.ES_WRITER_INDEX_TYPE, targetTableName[i]);
                    applyMapping.setOtherMappingRelation(map);
                }
                MediaSourceType mediaSourceType = parseMediaSourceTypeById(srcMediaSourceId);
                if(mediaSourceType==MediaSourceType.MYSQL || mediaSourceType==MediaSourceType.SQLSERVER || mediaSourceType==MediaSourceType.ORACLE || mediaSourceType==MediaSourceType.HANA) {
                    Map<String, String> map = applyMapping.getOtherMappingRelation();
                    if (map == null || map.size() == 0) {
                        map = new HashMap<>();
                    }
                    map.put(PropertyConstant.WHERE_CONDITION,whereConditions[i]);
                    applyMapping.setOtherMappingRelation(map);
                }
                applyMappings.add(applyMapping);
            }
            applyParameter.setSyncApplyMappings(applyMappings);
            applyParameter.setSrcMediaSourceId(Long.valueOf(srcMediaSourceId));
            applyParameter.setTargetMediaSourceId(Long.valueOf(targetMediaSourceId));
            applyParameterList.add(applyParameter);
            applyContentObj.setApplyParameterList(applyParameterList);
            applyInfo.setApplyContent(JSONObject.toJSONString(applyContentObj));
            applyInfo.setApplyType(syncApplyView.getApplyType());
            if (syncApplyView.getApplyType().equals("Full")) {
                applyInfo.setIsInitialData(false);
            } else {
                applyInfo.setIsInitialData(syncApplyView.getIsInitialData());
            }
            applyInfo.setApplyRemark(syncApplyView.getApplyRemark());
            applyInfo.setApplyStatus(SyncApplyStatus.SUBMITTED);
            applyInfo.setNeedNotify(true);

            UserInfo user = getLoginUser(request);
            applyInfo.setApplyUserId(user.getId());

            List<SyncApproveInfo> approveInfoList = new ArrayList<>();
            String approveUserIds = syncApplyView.getApproveUserId();
            String[] approveUserIdArr = approveUserIds.split(",");
            for (String approveId : approveUserIdArr) {
                SyncApproveInfo approveInfo = new SyncApproveInfo();
                approveInfo.setApproveUserId(Long.valueOf(approveId));
                approveInfo.setApproveStatus(SyncApproveStatus.NONE);
                approveInfoList.add(approveInfo);
            }
            syncApplyService.insert(applyInfo, approveInfoList);
            AuditLogUtils.saveAuditLog(getAuditLogInfo(applyInfo, "003001005", AuditLogOperType.insert.getValue()));
            return "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }
    private static AuditLogInfo getAuditLogInfo(SyncApplyInfo info, String menuCode, String operType){
        AuditLogInfo logInfo=new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName("同步申请");
        logInfo.setOperType(operType);
        logInfo.setOperKey(info.getId());
        logInfo.setOperRecord(info.toString());
        return logInfo;
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        ModelAndView mav = buildCommonApplyPage(request);
        mav.addObject("approve", false);
        mav.addObject("detail", false);
        return mav;
    }

    private ModelAndView buildCommonApplyPage(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("syncApply/edit");
        SyncApplyInfo applyInfo = syncApplyService.getSyncApplyInfoById(Long.valueOf(id));
        SyncApplyContent syncApplyContent = applyInfo.getApplyContentObj();
        List<SyncApplyParameter> syncApplyParameters = syncApplyContent.getApplyParameterList();
        List<SyncApplyMapping> syncApplyMappings = syncApplyParameters.get(0).getSyncApplyMappings();
        List<String> srcTableName = new ArrayList<>();
        List<String> tarTableName = new ArrayList<>();
        List<String> sourceColumns = new ArrayList<>();
        List<String> targetColumns = new ArrayList<>();
        List<ColumnMappingMode> columnMappingModes = new ArrayList<>();
        for (SyncApplyMapping applyMapping : syncApplyMappings) {
            srcTableName.add(applyMapping.getSourceTableName());//"t_alarm_classify"
            tarTableName.add(applyMapping.getTargetTableName());
            ColumnMappingMode mappingMode = applyMapping.getColumnMappingMode();
            columnMappingModes.add(mappingMode);
            List<String> srcColumn = applyMapping.getSourceColumn();//"sourceColumn": ["violation_name", "status"]
            List<String> tarColumn = applyMapping.getTargetColumn();
            String sourceStr = "";
            String targetStr = "";
            if (mappingMode != ColumnMappingMode.NONE) {
                sourceStr = Joiner.on("+").join(srcColumn);
                targetStr = Joiner.on("+").join(tarColumn);
            }
            sourceColumns.add(sourceStr);
            targetColumns.add(targetStr);
        }
        String sourceColumn = Joiner.on(",").join(sourceColumns);
        String targetColumn = Joiner.on(",").join(targetColumns);
        String sourceTableName = Joiner.on(",").join(srcTableName);
        String targetTableName = Joiner.on(",").join(tarTableName);
        String columnMappingMode = Joiner.on(",").join(columnMappingModes);

        List<UserInfo> approveUserIdList = userService.getUserInfoByRoleType(RoleType.APPROVER);
        List<UserInfo> autoKeeperUserIdList = userService.getUserInfoByRoleType(RoleType.AUTOKEEPER);
        approveUserIdList.addAll(autoKeeperUserIdList);

        List<UserInfo> distinctList = approveUserIdList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(bean -> bean.getId()))), ArrayList::new));
        mav.addObject("approveUserIdList", distinctList);
        mav.addObject("syncApplyView", buildSyncApplyView(applyInfo, getLoginUser(request)));
        mav.addObject("columnMappingMode", columnMappingMode);
        mav.addObject("sourceTableName", sourceTableName);
        mav.addObject("targetTableName", targetTableName);
        mav.addObject("sourceColumn", sourceColumn);
        mav.addObject("targetColumn", targetColumn);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(HttpServletRequest request, @ModelAttribute("syncApplyView") SyncApplyInfo syncApplyInfo) {
        try {
            String[] sourceTableName = request.getParameterValues("sourceTableName");
            String[] targetTableName = request.getParameterValues("targetTableName");
            String mappingMode = request.getParameter("columnMappingModeHidden");
            String[] columnMappingMode = mappingMode.split(",");
            String srcColumn = request.getParameter("sourceColumnHidden");
            String[] sourceColumn = srcColumn.split(",");
            String tarColumn = request.getParameter("targetColumnHidden");
            String[] targetColumn = tarColumn.split(",");
            String srcMediaSourceId = request.getParameter("srcMediaSourceId");
            String targetMediaSourceId = request.getParameter("targetMediaSourceId");
            SyncApplyContent applyContentObj = new SyncApplyContent();
            List<SyncApplyParameter> applyParameterList = new ArrayList<>();
            SyncApplyParameter applyParameter = new SyncApplyParameter();
            List<SyncApplyMapping> applyMappings = new ArrayList<>();
            for (int i = 0; i < sourceTableName.length; i++) {
                SyncApplyMapping applyMapping = new SyncApplyMapping();
                applyMapping.setSourceTableName(sourceTableName[i]);
                applyMapping.setTargetTableName(targetTableName[i]);
                applyMapping.setColumnMappingMode(ColumnMappingMode.valueOf(columnMappingMode[i]));
                if (applyMapping.getColumnMappingMode() != ColumnMappingMode.NONE) {
                    String[] sourceColumnArr = sourceColumn[i].split("\\+");
                    String[] targetColumnArr = targetColumn[i].split("\\+");
                    List<String> sourceColumnList = new ArrayList<>();
                    List<String> targetColumnList = new ArrayList<>();
                    Collections.addAll(sourceColumnList, sourceColumnArr);
                    Collections.addAll(targetColumnList, targetColumnArr);
                    applyMapping.setSourceColumn(sourceColumnList);
                    applyMapping.setTargetColumn(targetColumnList);
                }
                applyMappings.add(applyMapping);
            }
            applyParameter.setSrcMediaSourceId(Long.valueOf(srcMediaSourceId));
            applyParameter.setTargetMediaSourceId(Long.valueOf(targetMediaSourceId));
            applyParameter.setSyncApplyMappings(applyMappings);
            applyParameterList.add(applyParameter);
            applyContentObj.setApplyParameterList(applyParameterList);
            syncApplyInfo.setApplyContent(JSONObject.toJSONString(applyContentObj));

            syncApplyService.update(syncApplyInfo);
            AuditLogUtils.saveAuditLog(getAuditLogInfo(syncApplyInfo, "003001006", AuditLogOperType.update.getValue()));
        } catch (Exception e) {
            logger.error("Edit apply failed.", e);
            return e.getMessage();
        }
        return "success";
    }

    @ResponseBody
    @RequestMapping(value = "/toApprove")
    public ModelAndView toApprove(HttpServletRequest request) {
        ModelAndView mav = buildCommonApplyPage(request);
        mav.addObject("approve", true);
        mav.addObject("detail", false);
        return mav;
    }


    @RequestMapping(value = "/toDetail")
    public ModelAndView toDetail(HttpServletRequest request) {
        ModelAndView mav = buildCommonApplyPage(request);
        mav.addObject("approve", true);
        mav.addObject("detail", true);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doApproveOrReject")
    public String applyApproved(String applyId, String status, String approveRemark, HttpServletRequest request) {
        try {
            Long id = Long.valueOf(applyId);
            SyncApplyStatus applyStatus = SyncApplyStatus.valueOf(status);
            SyncApproveStatus approveStatus = SyncApproveStatus.valueOf(status);

            SyncApplyInfo applyInfo = new SyncApplyInfo();
            applyInfo.setId(id);
            applyInfo.setApplyStatus(applyStatus);

            SyncApproveInfo approveInfo = new SyncApproveInfo();
            approveInfo.setApplyId(id);
            approveInfo.setApproveStatus(approveStatus);
            approveInfo.setApproveRemark(approveRemark);
            UserInfo user = getLoginUser(request);
            approveInfo.setApproveUserId(user.getId());

            syncApplyService.doApproveOrReject(applyInfo, approveInfo, false);
        } catch (Exception e) {
            logger.error("Do approve failed.", e);
            return e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/doCancel")
    @ResponseBody
    public String doCancel(HttpServletRequest request) {
        try {
            String id = request.getParameter("id");
            SyncApplyInfo applyInfo = new SyncApplyInfo();
            applyInfo.setId(Long.valueOf(id));
            applyInfo.setApplyStatus(SyncApplyStatus.CANCELED);
            syncApplyDAO.updateApplyStatus(applyInfo);
            return "success";
        } catch (Exception e) {
            logger.error("Do cancel failed.", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/doCreateJobConfig")
    @ResponseBody
    public String doCreateJobConfig(HttpServletRequest request) {
        try {
            String id = request.getParameter("id");
            List<JobConfigInfo> jobList = jobService.getJobConfigListByApplyId(Long.valueOf(id));
            if (jobList != null && jobList.size() > 0) {
                return "该申请已经创建过job，请勿重复创建！";
            }
            SyncApplyInfo applyInfo = syncApplyService.getSyncApplyInfoById(Long.valueOf(id));
            SyncUtil.createFullJob(applyInfo);
            return "success";
        } catch (Exception e) {
            logger.error("create job failed.", e);
            return e.getMessage();
        }

    }

    @RequestMapping(value = "/toProcess")
    public ModelAndView toProcess(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("syncApply/process");
        Long id = Long.valueOf(request.getParameter("id"));
        SyncApplyInfo applyInfo = syncApplyService.getSyncApplyInfoById(id);
        SyncApplyView applyView = new SyncApplyView();
        applyView.setId(id);
        applyView.setApplyStatus(applyInfo.getApplyStatus());
        applyView.setReplyRemark(applyInfo.getReplyRemark());
        mav.addObject("syncApplyView", applyView);
        mav.addObject("SyncApplyStatusList", SyncApplyStatus.getProcessSyncApplyStatus());
        return mav;
    }

    @RequestMapping(value = "/doProcess")
    @ResponseBody
    public String doProcess(HttpServletRequest request, SyncApplyInfo applyInfo) {
        try {
            UserInfo user = getLoginUser(request);
            applyInfo.setOperateUserId(user.getId());
            SyncApplyStatus applyStatus = applyInfo.getApplyStatus();
            if (applyStatus == SyncApplyStatus.SUCCEEDED || applyStatus == SyncApplyStatus.FAILED) {
                applyInfo.setNeedNotify(true);
            }
            syncApplyDAO.updateApplyStatus(applyInfo);
            return "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/toAddMediaMapping")
    public ModelAndView toAddMediaMapping(Long id) {
        ModelAndView mav = new ModelAndView("syncApply/addMediaMapping");
        List<TaskInfo> taskList = taskService.getList();
        mav.addObject("applyId", id);
        mav.addObject("taskList", taskList);
        return mav;
    }

    @RequestMapping(value = "/doAddMediaMapping")
    @ResponseBody
    public String doAddMediaMapping(HttpServletRequest request) {
        try {
            Long applyId = Long.valueOf(request.getParameter("applyId"));
            Long taskId = Long.valueOf(request.getParameter("taskId"));
            SyncApplyInfo applyInfo = syncApplyService.getSyncApplyInfoById(applyId);
            SyncApplyContent syncApplyContent = applyInfo.getApplyContentObj();
            SyncApplyParameter syncApplyParameter = syncApplyContent.getApplyParameterList().get(0);
            Long srcMediaSourceId = syncApplyParameter.getSrcMediaSourceId();
            TaskInfo incrementTask = taskService.getTask(taskId);
            if (!incrementTask.getReaderMediaSourceId().equals(srcMediaSourceId)) {
                return "所选Task的Reader和同步申请的源端数据库不一致，请选择正确的Task！";
            }
            MediaSourceInfo srcMediaSourceInfo = mediaSourceService.getById(srcMediaSourceId);
            if (srcMediaSourceInfo.getType() == MediaSourceType.HBASE && !incrementTask.isLeaderTask()) {
                return "所选HbaseTask不是leaderTask，请选择正确的Task！";
            }
            if (IncrementSyncUtil.processIncrement(applyInfo, taskId, ManagerConfig.current().getIsReuseTask(),
                    ManagerConfig.current().getZkServer(), ManagerConfig.current().getCurrentEnv())) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("SyncApply Add Media Mapping Failed.", e);
            return "增加映射失败:" + e.getMessage();
        }
        return "增加映射失败!";
    }

    private SyncApplyView buildSyncApplyView(SyncApplyInfo applyInfo, UserInfo user) {
        SyncApplyView applyView = new SyncApplyView();
        applyView.setId(applyInfo.getId());
        applyView.setIsInitialData(applyInfo.getIsInitialData());
        applyView.setApplyStatus(applyInfo.getApplyStatus());
        applyView.setApplyType(applyInfo.getApplyType());
        applyView.setApplyUserId(applyInfo.getApplyUserId());
        applyView.setApplyUserName(applyInfo.getApplyUserInfo().getUserName());
        applyView.setApplyRemark(applyInfo.getApplyRemark());
        applyView.setNeedNotify(applyInfo.getNeedNotify());
        applyView.setApplyContent(applyInfo.getApplyContent());
        Long srcMediaSourceId = applyInfo.getApplyContentObj().getApplyParameterList().get(0).getSrcMediaSourceId();
        Long targetMediaSourceId = applyInfo.getApplyContentObj().getApplyParameterList().get(0).getTargetMediaSourceId();
        MediaSourceInfo srcMediaSource = mediaSourceService.getById(srcMediaSourceId);
        MediaSourceInfo targetMediaSource = mediaSourceService.getById(targetMediaSourceId);
        applyView.setSrcMediaSourceId(srcMediaSourceId);
        applyView.setTargetMediaSourceId(targetMediaSourceId);
        applyView.setSrcMediaSourceName(srcMediaSource.getName());
        applyView.setTargetMediaSourceName(targetMediaSource.getName());
        applyView.setSrcMediaSourceType(srcMediaSource.getType() == MediaSourceType.VIRTUAL ? srcMediaSource.getSimulateMsType() : srcMediaSource.getType());
        applyView.setTargetMediaSourceType(targetMediaSource.getType()== MediaSourceType.VIRTUAL ? targetMediaSource.getSimulateMsType() : targetMediaSource.getType());
        List<SyncApproveInfo> approveInfos = syncApplyService.getSyncApproveInfoByApplyId(applyInfo.getId());
        List<String> approveUserIdList = new ArrayList<>();
        for (SyncApproveInfo approveInfo : approveInfos) {
            Long approveUserId = approveInfo.getApproveUserId();
            approveUserIdList.add(approveUserId.toString());
            if (approveUserId.equals(user.getId())) {
                applyView.setApproveRemark(approveInfo.getApproveRemark());
            }
        }
        String approveUserIds = String.join(",", approveUserIdList);
        applyView.setApproveUserId(approveUserIds);
        return applyView;
    }

    @ResponseBody
    @RequestMapping(value = "/getMediaSourcesAndTargetTypes")
    @AuthIgnore
    public Map<String, Object> getMediaSourcesAndTargetTypes(String mediaSourceType) {
        Map<String, Object> map = new HashMap<>();
        MediaSourceType msType = MediaSourceType.valueOf(mediaSourceType);
        Set<MediaSourceType> typeSet = new HashSet<>();
        typeSet.add(msType);
        //List<MediaSourceInfo> mediaSourceList = mediaSourceService.getListByType(typeSet);
        List<MediaSourceInfo> mediaSourceList = VirtualDataSourceUtil.findMediaSourcesForSingleLab(new ArrayList<>(typeSet));
        List<MediaSourceType> targetTypeList = new ArrayList<>();
        if (msType == MediaSourceType.MYSQL || msType == MediaSourceType.SQLSERVER || msType==MediaSourceType.ORACLE || msType==MediaSourceType.HANA) {
            targetTypeList = MediaSourceType.getTargetTypesForRDBMS();
        } else if (msType == MediaSourceType.HDFS) {
            targetTypeList = MediaSourceType.getTargetTypesForHDFS();
        } else if (msType == MediaSourceType.HBASE) {
            targetTypeList = MediaSourceType.getTargetTypesForHBASE();
        } else if (msType == MediaSourceType.SDDL) {
            targetTypeList = MediaSourceType.getTargetTypesForSDDL();
        }
        map.put("mediaSourceList", mediaSourceList);
        map.put("targetTypeList", targetTypeList);
        return map;
    }


    private UserInfo getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (UserInfo) session.getAttribute("user");
    }

    private MediaSourceType parseMediaSourceTypeById(String mediaSourceId) {
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(Long.parseLong(mediaSourceId));
        return mediaSourceInfo.getType() == MediaSourceType.VIRTUAL ? mediaSourceInfo.getSimulateMsType() : mediaSourceInfo.getType();
    }
}
