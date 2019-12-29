package com.ucar.datalink.manager.core.web.controller.meadiaMapping;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.interceptor.InterceptorInfo;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import com.ucar.datalink.manager.core.web.dto.mediaMapping.MediaMappingView;
import com.ucar.datalink.manager.core.web.dto.mediaSource.MediaSourceView;
import com.ucar.datalink.manager.core.web.util.MediaMappingConfigUtil;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by csf on 2017/4/12.
 */
@Controller
@RequestMapping(value = "/mediaMapping/")
public class MediaMappingController {

    private static final Logger logger = LoggerFactory.getLogger(MediaMappingController.class);

    @Autowired
    private MediaService mediaService;

    @Autowired
    private InterceptorService interceptorService;

    @Autowired
    private MediaSourceService mediaSourceService;

    @Autowired
    private TaskConfigService taskConfigService;

    @Autowired
    private SyncRelationService syncRelationService;

    @RequestMapping(value = "/mediaSourceList")
    public ModelAndView mediaSourceList() {
        ModelAndView mav = new ModelAndView("mediaMapping/list");
        List<TaskInfo> taskList = taskConfigService.getList();
        mav.addObject("taskList", CollectionUtils.isEmpty(taskList) ? taskList : taskList.stream().filter(t -> t.getLeaderTaskId() == null).collect(Collectors.toList()));
        mav.addObject("sourceMediaSourceList", mediaSourceService.getList());
        mav.addObject("targetMediaSourceList", mediaSourceService.getList());
        return mav;
    }

    @RequestMapping(value = "/initMediaMapping")
    @ResponseBody
    public Page<MediaMappingView> initMediaMapping(@RequestBody Map<String, String> map) {
        Long mediaSourceId = Long.valueOf(map.get("mediaSourceId"));
        Long targetMediaSourceId = Long.valueOf(map.get("targetMediaSourceId"));
        String mediaName = map.get("srcMediaName");
        Long taskId = Long.valueOf(map.get("taskId"));
        if (StringUtils.isBlank(mediaName)) {
            mediaName = null;
        }
        //目标端表名
        String targetMediaName = map.get("targetMediaName");
        if (StringUtils.isBlank(targetMediaName)) {
            targetMediaName = null;
        }

        Page<MediaMappingView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<MediaMappingInfo> mappingListsForQueryPage = mediaService.mappingListsForQueryPage(
                mediaSourceId == -1L ? null : mediaSourceId,
                targetMediaSourceId == -1L ? null : targetMediaSourceId,
                taskId == -1 ? null : taskId,
                mediaName,
                targetMediaName);

        //构造view
        List<MediaMappingView> mediaMappingViews = mappingListsForQueryPage.stream().map(i -> {
            MediaMappingView view = new MediaMappingView();
            view.setId(i.getId());

            view.setSrcMediaNamespace(i.getSourceMedia().getNamespace());
            view.setSrcMediaName(i.getSourceMedia().getName());
            view.setSrcMediaId(i.getSourceMediaId());
            view.setSrcMediaSourceId(i.getSourceMedia().getMediaSourceId());
            view.setSrcMediaSourceName(i.getSourceMedia().getMediaSource().getName());
            view.setTargetMediaNamespace(i.getTargetMediaNamespace());
            view.setTargetMediaName(i.getTargetMediaName());
            view.setTargetMediaSourceId(i.getTargetMediaSourceId());
            view.setTargetMediaSourceName(i.getTargetMediaSource().getName());
            view.setValid(i.isValid());
            view.setTaskName(i.getTaskInfo().getTaskName());
            view.setWritePriority(i.getWritePriority());
            view.setJoinColumn(i.getJoinColumn());
            view.setEsUsePrefix(i.isEsUsePrefix());
            view.setGeoPositionConf(i.getGeoPositionConf());
            view.setSkipIds(i.getSkipIds());
            view.setParameter(i.getParameter());
            view.setCreateTime(i.getCreateTime());
            return view;
        }).collect(Collectors.toList());
        PageInfo<MediaMappingInfo> pageInfo = new PageInfo<>(mappingListsForQueryPage);
        page.setDraw(page.getDraw());
        page.setAaData(mediaMappingViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("mediaMapping/add");
        List<TaskInfo> taskList = taskConfigService.getList();
        List<InterceptorInfo> interceptorList = interceptorService.getList();
        mav.addObject("taskList", CollectionUtils.isEmpty(taskList) ? taskList : taskList.stream().filter(t -> t.getLeaderTaskId() == null).collect(Collectors.toList()));
        mav.addObject("interceptorList", interceptorList);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/getTableName")
    @AuthIgnore
    public Map<String, Object> getTableNameAndTargetNamespace(Long taskId, Long targetMediaNamespaceId) {
        Map<String, Object> map = new HashMap<>();
        TaskInfo taskInfo = taskConfigService.getTask(taskId);
        //源端
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(taskInfo.getTaskReaderParameterObj().getMediaSourceId());
        if (mediaSourceInfo.getType() == MediaSourceType.SDDL) {
            SddlMediaSrcParameter sddlParameter = mediaSourceInfo.getParameterObj();
            mediaSourceInfo = mediaSourceService.getById(sddlParameter.getProxyDbId());
        }

        List<String> tableNameList;
        if (mediaSourceInfo.getType() == MediaSourceType.HBASE) {
            tableNameList = mediaSourceService.getHbaseTableNames(mediaSourceInfo);
        } else {
            tableNameList = mediaSourceService.getRdbTableNames(mediaSourceInfo);
        }

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

        //目标端
        MediaSourceInfo targetMediaSourceInfo = mediaSourceService.getById(targetMediaNamespaceId);
        map.put("tableNameList", tableNameList);
        map.put("targetNamespace", targetMediaSourceInfo.getParameterObj().getNamespace());
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/getSourceDataBase")
    @AuthIgnore
    public Map<String, Object> getSourceDataBase(Long taskId, Long targetMediaSourceId) {
        TaskInfo taskInfo = taskConfigService.getTask(taskId);
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(taskInfo.getTaskReaderParameterObj().getMediaSourceId());
        List<PluginWriterParameter> pluginWriterList = taskInfo.getTaskWriterParameterObjs();
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        if (pluginWriterList != null && pluginWriterList.size() > 0) {
            for (PluginWriterParameter per : pluginWriterList) {
                setMediaSource.addAll(per.getSupportedSourceTypes());
            }
        }
        List<MediaSourceInfo> mediaSourceList = mediaSourceService.getListByType(setMediaSource);
        List<MediaSourceView> targetList = new ArrayList<MediaSourceView>();
        Map<String, Object> map = new HashMap<String, Object>();
        MediaSourceView sourceMedia = new MediaSourceView();
        sourceMedia.setId(mediaSourceInfo.getId());
        sourceMedia.setName(mediaSourceInfo.getName());
        if (mediaSourceList != null && mediaSourceList.size() > 0) {
            for (MediaSourceInfo targetMedias : mediaSourceList) {
                if (targetMedias.getId().equals(mediaSourceInfo.getId())) {
                    continue;
                }
                MediaSourceView targetMedia = new MediaSourceView();
                targetMedia.setId(targetMedias.getId());
                targetMedia.setName(targetMedias.getName());
                targetList.add(targetMedia);
            }
        }
        map.put("source", sourceMedia);
        map.put("target", targetList);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/getColumnName")
    @AuthIgnore
    public List<String> getColumnName(Long id, String tableName) {
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(id);

        if (mediaSourceInfo.getType() == MediaSourceType.HBASE) {
            return mediaSourceService.getHbaseColumnNames(mediaSourceInfo, tableName);
        } else {
            if (mediaSourceInfo.getType() == MediaSourceType.SDDL) {
                SddlMediaSrcParameter sddlParameter = mediaSourceInfo.getParameterObj();
                mediaSourceInfo = mediaSourceService.getById(sddlParameter.getProxyDbId());
            }
            return mediaSourceService.getRdbColumnNames(mediaSourceInfo, tableName);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(HttpServletRequest request) {

        try {
            String id = request.getParameter("srcMediaSourceId");
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
            List<MediaMappingInfo> mediaMappingList = buildMediaMappingInfo(request, id);
            mediaService.insert(
                    buildMediaInfo(request, mediaSourceInfo, id),
                    mediaMappingList,
                    buildMediaColumnMappingInfo(request, id)
            );
            mediaService.cleanTableMapping(Long.valueOf(request.getParameter("taskId")));
            for (MediaMappingInfo mediaMappingInfo : mediaMappingList) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(mediaMappingInfo, "004020300", AuditLogOperType.insert.getValue()));
            }
            return "success";
        } catch (Exception e) {
            logger.error("Add media-mapping failed.", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("mediaMapping/edit");
        MediaMappingInfo mediaMappingInfo = mediaService.findMediaMappingsById(Long.valueOf(id));
        List<MediaColumnMappingInfo> mediaColumnMapping = mediaService.findMediaColumnByMappingId(mediaMappingInfo.getId());

        String sourceColumn = "";
        String targetColumn = "";
        for (MediaColumnMappingInfo mappingColumn : mediaColumnMapping) {
            sourceColumn += mappingColumn.getSourceColumn() + ",";
            targetColumn += mappingColumn.getTargetColumn() + ",";
        }
        if (StringUtils.isNotBlank(sourceColumn)) {
            sourceColumn = sourceColumn.substring(0, sourceColumn.length() - 1);
        }
        if (StringUtils.isNotBlank(targetColumn)) {
            targetColumn = targetColumn.substring(0, targetColumn.length() - 1);
        }

        List<InterceptorInfo> interceptorList = interceptorService.getList();
        mav.addObject("interceptorList", interceptorList);
        mav.addObject("mediaMappingInfo", buildMediaMappingView(mediaMappingInfo));
        mav.addObject("sourceColumn", sourceColumn);
        mav.addObject("targetColumn", targetColumn);
        return mav;
    }

    private MediaMappingView buildMediaMappingView(MediaMappingInfo mediaMappingInfo) {
        MediaMappingView mediaMappingView = new MediaMappingView();
        mediaMappingView.setSrcMediaName(mediaMappingInfo.getSourceMedia().getName());
        mediaMappingView.setSrcMediaSourceId(mediaMappingInfo.getSourceMedia().getMediaSourceId());
        mediaMappingView.setSrcMediaNamespace(mediaMappingInfo.getSourceMedia().getNamespace());
        mediaMappingView.setSrcMediaId(mediaMappingInfo.getSourceMediaId());
        mediaMappingView.setSrcMediaSourceName(mediaMappingInfo.getSourceMedia().getMediaSource().getName());

        mediaMappingView.setTargetMediaName(mediaMappingInfo.getTargetMediaName());
        mediaMappingView.setTargetMediaNamespace(mediaMappingInfo.getTargetMediaNamespace());
        mediaMappingView.setTargetMediaSourceName(mediaMappingInfo.getTargetMediaSource().getName());
        mediaMappingView.setTargetMediaSourceId(mediaMappingInfo.getTargetMediaSourceId());

        mediaMappingView.setTaskName(mediaMappingInfo.getTaskInfo().getTaskName());
        mediaMappingView.setId(mediaMappingInfo.getId());
        mediaMappingView.setParameter(mediaMappingInfo.getParameter());
        mediaMappingView.setColumnMappingMode(mediaMappingInfo.getColumnMappingMode());
        mediaMappingView.setInterceptorId(mediaMappingInfo.getInterceptorId());
        mediaMappingView.setValid(mediaMappingInfo.isValid());
        mediaMappingView.setTaskId(mediaMappingInfo.getTaskId());
        mediaMappingView.setWritePriority(mediaMappingInfo.getWritePriority());
        mediaMappingView.setJoinColumn(mediaMappingInfo.getJoinColumn());
        mediaMappingView.setEsUsePrefix(mediaMappingInfo.isEsUsePrefix());
        mediaMappingView.setEsRouting(mediaMappingInfo.getEsRouting());
        mediaMappingView.setEsRoutingIgnore(mediaMappingInfo.getEsRoutingIgnore());
        mediaMappingView.setGeoPositionConf(mediaMappingInfo.getGeoPositionConf());
        mediaMappingView.setSkipIds(mediaMappingInfo.getSkipIds());
        mediaMappingView.setTargetMediaSourceId(mediaMappingInfo.getTargetMediaSourceId());
        return mediaMappingView;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(HttpServletRequest request, @ModelAttribute("mediaMappingInfo") MediaMappingInfo mediaMappingInfo) {
        try {
            MediaColumnMappingInfo columnMappingInfo = new MediaColumnMappingInfo();
            columnMappingInfo.setId(mediaMappingInfo.getId());
            columnMappingInfo.setTargetColumn(request.getParameter("targetColumnHidden"));
            columnMappingInfo.setSourceColumn(request.getParameter("sourceColumnHidden"));
            if (StringUtils.isBlank(mediaMappingInfo.getParameter())) {
                mediaMappingInfo.setParameter("{}");
            }
            mediaService.update(columnMappingInfo, mediaMappingInfo);
            MediaMappingInfo mediaMapping = mediaService.findMediaMappingsById(mediaMappingInfo.getId());
            AuditLogUtils.saveAuditLog(getAuditLogInfo(mediaMappingInfo, "004020500", AuditLogOperType.update.getValue()));
            mediaService.cleanTableMapping(mediaMapping.getTaskId());
        } catch (Exception e) {
            logger.error("Edit media-mapping failed.", e);
            return e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/toView")
    public ModelAndView toView(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("mediaMapping/view");
        MediaMappingInfo mediaMappingInfo = mediaService.findMediaMappingsById(Long.valueOf(id));
        List<MediaColumnMappingInfo> mediaColumnMapping = mediaService.findMediaColumnByMappingId(mediaMappingInfo.getId());

        String sourceColumn = "";
        String targetColumn = "";
        for (MediaColumnMappingInfo mappingColumn : mediaColumnMapping) {
            sourceColumn += mappingColumn.getSourceColumn() + ",";
            targetColumn += mappingColumn.getTargetColumn() + ",";
        }
        if (StringUtils.isNotBlank(sourceColumn)) {
            sourceColumn = sourceColumn.substring(0, sourceColumn.length() - 1);
        }
        if (StringUtils.isNotBlank(targetColumn)) {
            targetColumn = targetColumn.substring(0, targetColumn.length() - 1);
        }

        List<InterceptorInfo> interceptorList = interceptorService.getList();
        mav.addObject("interceptorList", interceptorList);
        mav.addObject("mediaMappingInfo", buildMediaMappingView(mediaMappingInfo));
        mav.addObject("sourceColumn", sourceColumn);
        mav.addObject("targetColumn", targetColumn);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            MediaMappingInfo mediaMappingInfo = mediaService.findMediaMappingsById(Long.valueOf(id));
            mediaService.delete(Long.valueOf(id));
            mediaService.cleanTableMapping(mediaMappingInfo.getTaskId());

            AuditLogUtils.saveAuditLog(getAuditLogInfo(mediaMappingInfo, "004020600", AuditLogOperType.delete.getValue()));
        } catch (Exception e) {
            logger.error("Delete media-mapping failed.", e);
            return e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/dataCheck")
    @ResponseBody
    public String dataCheck(Long mappingId, Long startId, Long endId) {
        DataCheckResult result;
        try {
            result = RDBMSUtil.checkData(mappingId, startId, endId);
        } catch (Exception e) {
            logger.error("校验失败：", e);
            return "fail";
        }
        return JSON.toJSONString(result);
    }

    @RequestMapping(value = "/getAllMediaMappings")
    @ResponseBody
    public Map<String, Object> getAllMediaMappings(String taskId) {
        Map<String, Object> map = new HashMap<>();
        try {
            List<MediaMappingInfo> mappingLists = mediaService.getAllMediaMappingsByTaskId(Long.valueOf(taskId));

            //构造view
            List<MediaMappingView> mediaMappingViews = mappingLists.stream().map(i -> {
                MediaMappingView view = new MediaMappingView();
                view.setId(i.getId());
                view.setSrcMediaNamespace(i.getSourceMedia().getNamespace());
                view.setSrcMediaName(i.getSourceMedia().getName());
                view.setSrcMediaSourceName(i.getSourceMedia().getMediaSource().getName());
                view.setTargetMediaNamespace(i.getTargetMediaNamespace());
                view.setTargetMediaName(i.getTargetMediaName());
                view.setTargetMediaSourceName(i.getTargetMediaSource().getName());
                view.setTaskName(i.getTaskInfo().getTaskName());
                return view;
            }).collect(Collectors.toList());
            map.put("mediaMappingList", mediaMappingViews);
        } catch (Exception e) {
            logger.error("校验失败：", e);
        }
        return map;
    }

    private List<MediaInfo> buildMediaInfo(HttpServletRequest request, MediaSourceInfo mediaSourceInfo, String id) throws Exception {
        String[] sourceTableName = request.getParameterValues("sourceTableName");
        String[] targetTableName = request.getParameterValues("targetTableName");

        Long targetMediaSourceId = null;
        if (StringUtils.isNotBlank(request.getParameter("targetMediaNamespaceId"))) {
            targetMediaSourceId = Long.valueOf(request.getParameter("targetMediaNamespaceId"));
        }
        if (sourceTableName == null && sourceTableName.length == 0) {
            throw new RuntimeException("sourceTableName is empty");
        }
        if (mediaSourceInfo.getId().longValue() == targetMediaSourceId) {
            throw new RuntimeException("源端和目标端不能是同一个库");
        }

        List<MediaInfo> tablelist = new ArrayList<MediaInfo>();


        MediaSourceInfo targetMediaSourceInfo = mediaSourceService.getById(targetMediaSourceId);
        MediaSrcParameter mediaSrcParameter = mediaSourceInfo.getParameterObj();

        Set<String> tableNameSet = new HashSet<>();
        //校验mysql数据库表是否有主键
        MediaMappingConfigUtil.validateMysqlTablePk(mediaSourceInfo, sourceTableName, tableNameSet);
        //校验目标数据源是否存在
        MediaMappingConfigUtil.validateExistsTargetMedia(targetMediaSourceInfo, sourceTableName, tableNameSet, targetTableName);

        //TODO,如果namespace为空，则设置为default，此处只是临时支持hbase，暂时这么用，
        //TODO 后续整个MediaMappingController都需要进行深度重构
        String namespace = StringUtils.isBlank(mediaSrcParameter.getNamespace()) ? "default" : mediaSrcParameter.getNamespace();
        for (String tableName : sourceTableName) {
            MediaInfo mediaInfo = new MediaInfo();
            mediaInfo.setName(tableName);
            mediaInfo.setMediaSourceId(Long.valueOf(id));
            mediaInfo.setNamespace(namespace);
            tablelist.add(mediaInfo);
        }
        return tablelist;
    }

    private List<MediaMappingInfo> buildMediaMappingInfo(HttpServletRequest request, String id) {
        String[] targetTableName = request.getParameterValues("targetTableName");
        String[] columnMappingMode = request.getParameterValues("columnMappingModeHidden");
        String[] writePriority = request.getParameterValues("writePriorityHidden");
        String[] joinColumn = request.getParameterValues("joinColumnHidden");
        String[] esUsePrefix = request.getParameterValues("esUsePrefixHidden");
        String[] esRouting = request.getParameterValues("esRoutingHidden");
        String[] esRoutingIgnore = request.getParameterValues("esRoutingIgnoreHidden");
        String[] geoPositionConf = request.getParameterValues("geoPositionConfHidden");
        String[] skipIds = request.getParameterValues("skipIdsHidden");
        String[] parameter = request.getParameterValues("parameterHidden");
        String[] valid = request.getParameterValues("validHidden");
        String[] interceptorId = request.getParameterValues("interceptorIdHidden");
        if (targetTableName == null && targetTableName.length == 0) {
            throw new RuntimeException("targetTableName is empty");
        }

        List<MediaMappingInfo> mediaMappingList = new ArrayList<MediaMappingInfo>();
        for (int i = 0; i < targetTableName.length; i++) {
            MediaMappingInfo mediaMapping = new MediaMappingInfo();
            if (StringUtils.isNotBlank(columnMappingMode[i])) {
                mediaMapping.setColumnMappingMode(ColumnMappingMode.valueOf(columnMappingMode[i]));
            }
            if (StringUtils.isNotBlank(interceptorId[i])) {
                mediaMapping.setInterceptorId(Long.valueOf(interceptorId[i]));
            }

            mediaMapping.setValid(Boolean.valueOf(valid[i]));
            mediaMapping.setEsUsePrefix(Boolean.valueOf(esUsePrefix[i]));
            mediaMapping.setGeoPositionConf(geoPositionConf[i]);
            mediaMapping.setSkipIds(skipIds[i]);
            if (StringUtils.isBlank(parameter[i])) {
                parameter[i] = "{}";
            }
            mediaMapping.setParameter(parameter[i]);
            mediaMapping.setTaskId(Long.valueOf(request.getParameter("taskId")));
            if (StringUtils.isNotBlank(writePriority[i])) {
                mediaMapping.setWritePriority(Long.valueOf(writePriority[i]));
            }
            if (StringUtils.isNotBlank(joinColumn[i])) {
                mediaMapping.setJoinColumn(joinColumn[i]);
            }
            if (StringUtils.isNotBlank(request.getParameter("targetMediaNamespaceId"))) {
                mediaMapping.setTargetMediaSourceId(Long.valueOf(request.getParameter("targetMediaNamespaceId")));
            }
            mediaMapping.setTargetMediaNamespace(request.getParameter("targetMediaNamespace"));
            mediaMapping.setTargetMediaName(targetTableName[i]);
            mediaMapping.setEsRouting(esRouting[i]);
            mediaMapping.setEsRoutingIgnore(esRoutingIgnore[i]);

            mediaMappingList.add(mediaMapping);
        }
        return mediaMappingList;
    }

    private List<MediaColumnMappingInfo> buildMediaColumnMappingInfo(HttpServletRequest request, String id) {
        String[] sourceColumn = request.getParameterValues("sourceColumnHidden");
        String[] targetColumn = request.getParameterValues("targetColumnHidden");
        if (targetColumn == null && targetColumn.length == 0) {
            throw new RuntimeException("targetColumn is empty");
        }
        List<MediaColumnMappingInfo> mappingList = new ArrayList<>();
        for (int i = 0; i < targetColumn.length; i++) {
            MediaColumnMappingInfo mediaColumnMappingInfo = new MediaColumnMappingInfo();
            mediaColumnMappingInfo.setSourceColumn(sourceColumn[i]);
            mediaColumnMappingInfo.setTargetColumn(targetColumn[i]);
            mappingList.add(mediaColumnMappingInfo);
        }
        return mappingList;
    }

    private AuditLogInfo getAuditLogInfo(MediaMappingInfo mediaMappingInfo, String menuCode, String operType) {
        AuditLogInfo logInfo = new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName("MediaMapping");
        logInfo.setOperType(operType);
        logInfo.setOperKey(mediaMappingInfo.getId());
        logInfo.setOperRecord(mediaMappingInfo.toString());
        return logInfo;
    }

}
