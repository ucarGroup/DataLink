
package com.ucar.datalink.manager.core.web.controller.decorate;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.domain.decorate.TaskDecorate;
import com.ucar.datalink.domain.decorate.TaskDecorateDetail;
import com.ucar.datalink.domain.decorate.TaskDecorateStatus;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskType;
import com.ucar.datalink.manager.core.web.controller.task.BaseTaskController;
import com.ucar.datalink.manager.core.web.dto.taskDecorate.TaskDecorateDetailView;
import com.ucar.datalink.manager.core.web.dto.taskDecorate.TaskDecorateView;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.util.HashedWheelTimer;
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
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author xy.li
 * @date 2019/05/30
 */
@Controller
@RequestMapping(value = "/decorate/")
public class DecorateController extends BaseTaskController {

    private static Logger logger = LoggerFactory.getLogger(DecorateController.class);

    @Autowired
    private TaskConfigService taskService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private TaskDecorateService taskDecorateService;

    @Autowired
    private MediaSourceService mediaSourceService;

    @Autowired
    private TaskConfigService taskConfigService;

    @Autowired
    private TaskDecorateDetailService taskDecorateDetailService;



    @RequestMapping(value = "/toList")
    public ModelAndView toList() {
        ModelAndView mav = new ModelAndView("decorate/list");
        mav.addObject("taskList",getTaskInfo());
        return mav;
    }


    private List<TaskInfo> getTaskInfo(){
        List<TaskInfo> taskList = taskService.getList();
        HashSet<MediaSourceType> mysqlSourceType = new HashSet<>();
        mysqlSourceType.add(MediaSourceType.MYSQL);
        List<MediaSourceInfo> listByType = mediaSourceService.getListByType(mysqlSourceType);
        Set<Long> sourceIds = listByType.stream().map(i -> i.getId()).collect(Collectors.toSet());
        return CollectionUtils.isEmpty(taskList) ? taskList : taskList.stream().filter(t -> sourceIds.contains(t.getReaderMediaSourceId())).collect(Collectors.toList());
    }


    @RequestMapping(value = "/queryDecorate")
    @ResponseBody
    public Page<TaskDecorateView> queryDecorate(@RequestBody Map<String, String> map) {
        String tableName = map.get("tableName");
        long taskId = -1L;
        if(!StringUtils.isBlank(map.get("taskId"))){
            taskId = Long.valueOf(map.get("taskId"));
        }
        Page<TaskDecorateView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        List<TaskDecorate> list = this.taskDecorateService.getList(taskId, tableName);

        List<TaskDecorateView> taskDecorateViews = list.stream().map(i ->{
            TaskDecorateView view = new TaskDecorateView();
            view.setId(i.getId());
            view.setRemark(i.getRemark());
            view.setStatement(i.getStatement());
            view.setTableName(i.getTableName());
            view.setTaskName(i.getTaskName());
            view.setTaskId(i.getTaskId());
            view.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(i.getCreateTime().getTime())));
            view.setModifyTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(i.getModifyTime().getTime())));
            return view;
        }).collect(Collectors.toList());

        PageInfo pageInfo = new PageInfo<>(list);
        page.setDraw(page.getDraw());
        page.setAaData(taskDecorateViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }



    @RequestMapping(value = "/toAddDecorate")
    public ModelAndView toAddDecorate() {
        ModelAndView mav = new ModelAndView("decorate/add");
        mav.addObject("decorateList",getTaskInfo());
        return mav;
    }


    @RequestMapping(value = "/start")
    @ResponseBody
    public String start(Long id) {
        try {
            TaskDecorate taskDecorate = this.taskDecorateService.getById(id);
            TaskDecorateDetail taskDecorateDetail = new TaskDecorateDetail();
            taskDecorateDetail.setDecorateId(taskDecorate.getId());
            taskDecorateDetail.setStatus(TaskDecorateStatus.NEW_CREATED.getCode());
            taskDecorateDetail.setExecutedLog("");
            taskDecorateDetailService.insert(taskDecorateDetail);
            sendRestartCommand(String.valueOf(taskDecorate.getTaskId()), null);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }




    @RequestMapping(value = "/findTables")
    @ResponseBody
    public Set<String> findTables(Long taskId) {
        Set<String> tables = new HashSet<>();
        try {
            List<MediaMappingInfo> list = mediaService.mappingListsForQueryPage( null,null,   taskId,
                    null,null);

            if(list != null){
                for(MediaMappingInfo mmi : list){
                    if(!mmi.isValid()){
                        continue;
                    }
                    String name = mmi.getSourceMedia().getName();
                    tables.add(name);
                }
            }
            return tables;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return tables;
    }

    @RequestMapping(value = "/doAddDecorate")
    @ResponseBody
    public String doAddDecorate(HttpServletRequest request) {

        try {
            TaskDecorate taskDecorate = requestToTaskDecorate(request);
            Boolean inserted = taskDecorateService.insert(taskDecorate);
        } catch (Exception e) {
            logger.error("doAddDecorate Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }


    private TaskDecorate requestToTaskDecorate(HttpServletRequest request){
        TaskDecorate taskDecorate = new TaskDecorate();
        String taskId = request.getParameter("taskId");
        String tableName = request.getParameter("tableName");
        String remark = request.getParameter("remark");
        String statement = request.getParameter("statement");
        String id = request.getParameter("id");

        if(!StringUtils.isBlank(id)){
            taskDecorate.setId(Integer.parseInt(id));
        }
        taskDecorate.setTaskId(Long.parseLong(taskId));
        taskDecorate.setTableName(tableName);
        taskDecorate.setRemark(remark);
        taskDecorate.setStatement(statement);
        return taskDecorate;
    }







    @RequestMapping(value = "/toUpdateDecorate")
    public ModelAndView toUpdateMysqlTask(Long id) {
        ModelAndView mav = new ModelAndView("decorate/edit");
        TaskDecorate taskDecorate = taskDecorateService.getById(id);
        mav.addObject("taskDecorate", taskDecorate);
        return mav;
    }

    @RequestMapping(value = "/doUpdateDecorate")
    @ResponseBody
    public String doUpdateMysqlTask(HttpServletRequest request) {
        try {
            TaskDecorate taskDecorate = requestToTaskDecorate(request);
            TaskDecorate obj = new TaskDecorate();
            obj.setId(taskDecorate.getId());
            obj.setStatement(taskDecorate.getStatement());
            obj.setRemark(taskDecorate.getRemark());
            Boolean updated = taskDecorateService.update(obj);
        } catch (Exception e) {
            logger.error("doUpdateMysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/deleteDecorate")
    @ResponseBody
    public String deleteDecorate(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            TaskDecorate taskDecorate = taskDecorateService.getById(id);
            taskDecorate.setDeleted(true);
            taskDecorateService.update(taskDecorate);
        } catch (Exception e) {
            logger.error("deleteDecorate Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }




    @RequestMapping(value = "/doHistory")
    @ResponseBody
    public Page<TaskDecorateDetailView> doHistory(@RequestBody Map<String, String> map) {
        long id = -1L;
        if (!StringUtils.isBlank(map.get("decorateId"))) {
            id = Long.valueOf(map.get("decorateId"));
        }
        Page<TaskDecorateDetailView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<TaskDecorateDetail> taskDecorateDetails = taskDecorateDetailService.listByCondition(id);

        List<TaskDecorateDetailView> view = new ArrayList();
        for(TaskDecorateDetail t : taskDecorateDetails){
            TaskDecorateDetailView taskDecorateDetailView = new TaskDecorateDetailView();
            taskDecorateDetailView.fillProperty(t);
            view.add(taskDecorateDetailView);
        }

        PageInfo pageInfo = new PageInfo<>(taskDecorateDetails);
        page.setDraw(page.getDraw());
        page.setAaData(view);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }


    @ResponseBody
    @RequestMapping(value = "/toHistory")
    public ModelAndView toHistory(HttpServletRequest request) {
        String decorateId = request.getParameter("id");
        ModelAndView mav = new ModelAndView("decorate/history");
        mav.addObject("decorateId", decorateId);
        return mav;
    }



}
