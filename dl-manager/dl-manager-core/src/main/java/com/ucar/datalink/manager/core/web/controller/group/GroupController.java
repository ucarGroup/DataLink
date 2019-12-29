package com.ucar.datalink.manager.core.web.controller.group;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.GroupService;
import com.ucar.datalink.biz.service.WorkerService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.group.GroupView;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by csf on 2017/3/29.
 */
@Controller
@RequestMapping(value = "/group/")
public class GroupController {

    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private GroupService groupService;
    @Autowired
    private WorkerService workerService;

    private static AuditLogInfo getAuditLogInfo(GroupInfo groupInfo, String menuCode, String operType) {
        AuditLogInfo logInfo = new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName(groupInfo.getGroupName());
        logInfo.setOperType(operType);
        logInfo.setOperKey(groupInfo.getId());
        logInfo.setOperRecord(groupInfo.toString());
        return logInfo;
    }

    @RequestMapping(value = "/groupList")
    public ModelAndView groupList() {
        ModelAndView mav = new ModelAndView("group/list");
        return mav;
    }

    @RequestMapping(value = "/initGroup")
    @ResponseBody
    public Page<GroupView> initGroup(@RequestBody Map<String, String> map) {

        Page<GroupView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        List<GroupInfo> listGroup = groupService.getAllGroups();
        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();
        //构造view
        List<GroupView> groupViews = listGroup.stream().map(i -> {
            GroupView view = new GroupView();
            view.setId(i.getId());
            ClusterState.GroupData groupData = clusterState.getGroupData(i.getId());
            if (groupData != null) {
                view.setGroupState(String.valueOf(groupData.getState()));
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(groupData.getLastReblanceTime());
                view.setLastReblanceTime(time);
            }
            view.setCreateTime(i.getCreateTime());
            view.setGroupDesc(i.getGroupDesc());
            view.setGroupName(i.getGroupName());
            view.setModifyTime(i.getModifyTime());
            view.setGenerationId(groupData != null ? groupData.getGenerationId() : 0);
            return view;
        }).collect(Collectors.toList());

        PageInfo<GroupInfo> pageInfo = new PageInfo<GroupInfo>(listGroup);
        page.setDraw(page.getDraw());
        page.setAaData(groupViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("group/add");
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("groupInfo") GroupInfo groupInfo) {
        Boolean isSuccess = groupService.insert(groupInfo);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(groupInfo, "001001003", AuditLogOperType.insert.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        GroupInfo groupInfo = new GroupInfo();
        ModelAndView mav = new ModelAndView("group/edit");
        if (StringUtils.isNotBlank(id)) {
            groupInfo = groupService.getById(Long.valueOf(id));
        }
        mav.addObject("groupInfo", groupInfo);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("groupInfo") GroupInfo groupInfo) {
        Boolean isSuccess = groupService.update(groupInfo);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(groupInfo, "001001005", AuditLogOperType.update.getValue()));
            return "success";
        } else {
            return "fail";
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
            Long idLong = Long.valueOf(id);
            GroupInfo groupInfo = groupService.getById(idLong);
            Boolean isSuccess = groupService.delete(idLong);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(groupInfo, "001001006", AuditLogOperType.delete.getValue()));
                return "success";
            }
        } catch (ValidationException e) {
            return e.getMessage();
        }

        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/doReBalance")
    private String doReBalance(HttpServletRequest requestPara) {
        String groupId = requestPara.getParameter("id");
        try {
            if (StringUtils.isEmpty(groupId)) {
                return "groupId不能为空";
            }
            ServerContainer.getInstance().getGroupCoordinator().forceRebalance(groupId);
            GroupInfo groupInfo = groupService.getById(Long.valueOf(groupId));
            AuditLogUtils.saveAuditLog(getAuditLogInfo(groupInfo, "001001007", AuditLogOperType.other.getValue()));

        } catch (Exception e) {
            logger.info("错误信息是：{}", e);
        }
        return "success";

    }

}
