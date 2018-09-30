package com.ucar.datalink.manager.core.web.controller.group;

import com.ucar.datalink.biz.service.GroupService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.group.GroupView;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by csf on 2017/3/29.
 */
@Controller
@RequestMapping(value = "/group/")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @RequestMapping(value = "/groupList")
    public ModelAndView groupList() {
        ModelAndView mav = new ModelAndView("group/list");
        return mav;
    }

    @RequestMapping(value = "/initGroup")
    @ResponseBody
    public Page<GroupView> initGroup() {
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
        return new Page<GroupView>(groupViews);
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
            Boolean isSuccess = groupService.delete(Long.valueOf(id));
            if (isSuccess) {
                return "success";
            }
        } catch (ValidationException e) {
            return e.getMessage();
        }

        return "fail";
    }

}
