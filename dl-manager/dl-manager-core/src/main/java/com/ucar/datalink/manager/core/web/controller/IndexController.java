package com.ucar.datalink.manager.core.web.controller;

import com.google.common.base.Joiner;
import com.ucar.datalink.biz.service.GroupService;
import com.ucar.datalink.common.jvm.JvmSnapshot;
import com.ucar.datalink.common.jvm.JvmUtils;
import com.ucar.datalink.common.zookeeper.ManagerMetaData;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.server.ServerStatusMonitor;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "/")
@AuthIgnore
public class IndexController{

    @Autowired
    GroupService groupService;

    @RequestMapping(value = "/")
    public ModelAndView index() throws Exception{
        return new ModelAndView("index");
    }

    @RequestMapping(value = "/main")
    public ModelAndView main() throws Exception{
        ModelAndView mav = new ModelAndView("main");
        List<GroupInfo> groupList = groupService.getAllGroups();
        ServerStatusMonitor monitor = ServerContainer.getInstance().getServerStatusMonitor();
        List<ManagerMetaData> all = monitor.getAllAliveManagers();
        ManagerMetaData active = monitor.getActiveManagerMetaData();
        List<String> allManagerAddress = all.stream().map(ManagerMetaData::getAddress).collect(Collectors.toList());
        String allManagers = Joiner.on(",").skipNulls().join(allManagerAddress);
        String activeManager = active == null ? null : active.getAddress();
        String startTime = null;
        JvmSnapshot jvmSnapshot = JvmUtils.buildJvmSnapshot();
        if (jvmSnapshot != null) {
            startTime = DateFormatUtils.format(jvmSnapshot.getStartTime(), "yyyy-MM-dd HH:mm:ss");
        }
        mav.addObject("groupList", groupList);
        mav.addObject("allManagers", allManagers);
        mav.addObject("activeManager", activeManager);
        mav.addObject("startTime", startTime);
        return mav;
    }
}