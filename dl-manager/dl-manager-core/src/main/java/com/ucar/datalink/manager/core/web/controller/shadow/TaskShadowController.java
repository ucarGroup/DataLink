package com.ucar.datalink.manager.core.web.controller.shadow;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.TaskShadowService;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.task.TaskShadowInfo;
import com.ucar.datalink.domain.task.TaskShadowParameter;
import com.ucar.datalink.manager.core.web.dto.task.TaskShadowView;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/shadow/")
public class TaskShadowController {

    private static final Logger logger = LoggerFactory.getLogger(TaskShadowController.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private TaskShadowService taskShadowService;

    @RequestMapping(value = "/toShadowList")
    public ModelAndView toHistory(HttpServletRequest request, String taskId) {
        ModelAndView mav = new ModelAndView("task/shadowList");
        mav.addObject("taskId", taskId);
        return mav;
    }

    @RequestMapping(value = "/doShadowList")
    @ResponseBody
    public Page<TaskShadowView> initMediaMapping(@RequestBody Map<String, String> map) {
        logger.info("收到的参数为:" + map);
        Long taskId = Long.valueOf(map.get("taskId"));
        String state = map.get("state");
        if (StringUtils.isBlank(state)) {
            state = null;
        }
        Page<TaskShadowView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<TaskShadowInfo> taskShadowListsForQueryPage = taskShadowService.taskShadowListsForQueryPage(
                taskId, state);

        //构造view
        List<TaskShadowView> taskShadowViews = taskShadowListsForQueryPage.stream().map(i -> {
            TaskShadowView view = new TaskShadowView();
            view.setId(i.getId());
            view.setTaskName(i.getTaskInfo().getTaskName());
            view.setTaskId(i.getTaskId());
            view.setState(i.getState());
            String modifyTime = "";
            if (i.getModifyTime() != null) {
                modifyTime = sdf.format(i.getModifyTime());
            }
            view.setModifyTime(modifyTime);
            view.setParameter(i.getParameter());
            String createTime = sdf.format(i.getCreateTime());
            view.setCreateTime(createTime);
            view.setNote(i.getNote());
            view.setMappingIds(i.getParameterObj().getShadowMappingIds().toString());
            view.setResetTime(sdf.format(i.getParameterObj().getTimeStamp()));
            return view;
        }).collect(Collectors.toList());
        PageInfo<TaskShadowInfo> pageInfo = new PageInfo<>(taskShadowListsForQueryPage);
        page.setDraw(page.getDraw());
        page.setAaData(taskShadowViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    @RequestMapping(value = "/toAddShadow")
    public ModelAndView toAddShadow(HttpServletRequest request, String taskId) {
        ModelAndView mav = new ModelAndView("task/shadowAdd");
        mav.addObject("taskId", taskId);
        return mav;
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(@RequestBody Map<String, String> shadowParams) {
        try {
            logger.info("收到的请求参数为:" + shadowParams);
            String mappingIdsStr = shadowParams.get("mappingIds");
            String newTimeStampsStr = shadowParams.get("newTimeStamps");
            String taskId = shadowParams.get("taskId");
            String note = shadowParams.get("note");
            if (StringUtils.isEmpty(mappingIdsStr)) {
                throw new DatalinkException("请选择增量映射!");
            }
            if (StringUtils.isEmpty(newTimeStampsStr)) {
                throw new DatalinkException("请设置位点信息!");
            }
            Set<Long> mappingIdSet = new HashSet<>();
            String[] mappingIds = mappingIdsStr.split(",");
            for (String mappingId : mappingIds) {
                mappingIdSet.add(Long.valueOf(mappingId));
            }
            TaskShadowParameter taskShadowParameter = new TaskShadowParameter();
            taskShadowParameter.setTimeStamp(Long.valueOf(newTimeStampsStr));
            taskShadowParameter.setShadowMappingIds(mappingIdSet);
            TaskShadowInfo taskShadowInfo = new TaskShadowInfo();
            taskShadowInfo.setParameter(taskShadowParameter.toJsonString());
            taskShadowInfo.setTaskId(Long.valueOf(taskId));
            taskShadowInfo.setState(TaskShadowInfo.State.INIT);
            taskShadowInfo.setNote(note);

            taskShadowService.createTaskShadow(taskShadowInfo);
        } catch (Exception e) {
            logger.error("shadow add  Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

}
