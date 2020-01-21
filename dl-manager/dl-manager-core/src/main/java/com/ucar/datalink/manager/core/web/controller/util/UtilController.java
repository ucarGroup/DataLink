package com.ucar.datalink.manager.core.web.controller.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.common.event.CommonEvent;
import com.ucar.datalink.domain.Position;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2017/8/21.
 */
@Controller
@RequestMapping(value = "/util/")
public class UtilController {
    private static final Logger LOG = LoggerFactory.getLogger(UtilController.class);

    private static final ExecutorService pool = Executors.newCachedThreadPool();

    @Autowired
    private MediaSourceService mediaSourceService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private WorkerService workerService;
    @Autowired
    DoubleCenterService doubleCenterService;
    @Autowired
    private SyncRelationService syncRelationService;
    @Autowired
    private TaskConfigService taskService;
    @Autowired
    DoubleCenterDataxService doubleCenterDataxService;
    @Autowired
    @Qualifier("taskPositionServiceZkImpl")
    TaskPositionService taskPositionService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    MediaService mediaService;

    @RequestMapping(value = "/util")
    public ModelAndView utilList() {
        ModelAndView mav = new ModelAndView("util/util");
        return mav;
    }

    @RequestMapping(value = "/initOperations")
    @ResponseBody
    public Page<OperationInfo> initOperations() {
        List<OperationInfo> list = new ArrayList<>();
        list.add(new OperationInfo(1L, "autoAddEtlHost", "为所有的Rdbms数据源自动增加EtlUser配置."));
        list.add(new OperationInfo(2L, "ucar_CheckData", "开启/关闭，专车的sddl同步追踪"));
        list.add(new OperationInfo(3L, "tcar_CheckData", "开启/关闭，租车的sddl同步追踪"));
        list.add(new OperationInfo(4L, "lucky_CheckData", "开启/关闭，lucky的sddl同步追踪"));
        list.add(new OperationInfo(5L, "taskTransformFirstStep", "一键改造旧任务，共3步，这是第1步，去停止任务"));
        list.add(new OperationInfo(6L, "taskTransformSecondStep", "一键改造旧任务，共3步，这是第2步,去修改mediaSourceId、清缓存、启任务"));
        list.add(new OperationInfo(7L, "dataxVirtualTransform", "一键改造旧任务，共3步，这是第3步，一键改造datax的任务(任务使用虚拟数据源)"));

        list.add(new OperationInfo(10L, "switchDataSourceForAllTimingJob", "一键修改所有定时job(切换数据源)"));
        list.add(new OperationInfo(11L, "stopAllRunningJob", "一键修停止所有运行的job"));
        list.add(new OperationInfo(12L, "virtualChangeRealFirstStep", "去除双中心，共2步，这是第1步，去停止任务"));
        list.add(new OperationInfo(13L, "virtualChangeRealSecondStep", "去除双中心，共2步，这是第2步，去修改mediaSourceId、清缓存、启任务"));
        list.add(new OperationInfo(14L, "checkJobContent", "双中心切换完之后，检查job的内容"));
        list.add(new OperationInfo(15L, "reSwitchDbOrSddl", "单库切换，手动触发进行切换(切换失败时才使用)"));
        list.add(new OperationInfo(16L, "reNotifyDbms", "单库切换，手动通知dbms结果(通知失败时才使用)"));

        list.add(new OperationInfo(98L, "oneKeyReverseSyncFirstStep", "一键反向同步,共2步，这是第1步，修改数据源"));
        list.add(new OperationInfo(99L, "oneKeyReverseSyncSecondStep", "一键反向同步,共2步，这是第2步，重置位点、重启worker"));

        return new Page<>(list);
    }

    @RequestMapping(value = "/doOperation")
    @ResponseBody
    public Object doOperation(HttpServletRequest request) {
        LOG.info("doOperation is start!");
        try {
            Map<String,String> map = new HashMap<String,String>();
            Long id = Long.valueOf(request.getParameter("id"));
            if (id == 1L) {
                autoAddEtlHost();
            } else if (id == 2l || id == 3l || id == 4l) {
                switchSddlCheckData(id);
            }else if (id == 5L) {
                String msg = taskTransformFirstStep();
                map.put("result","success");
                map.put("msg",msg);
                return map;
            }else if (id == 6L) {
                taskTransformSecondStep();
            }else if(id == 7L) {
                dataxVirtualTransform();
            }else if(id == 10L) {
                switchDataSourceForAllTimingJob();
            }else if(id == 11L) {
                stopAllRunningJob();
            }

            else if(id == 98L) {
                oneKeyReverseSyncFirstStep();
            }
            else {
                return "无效的操作ID";
            }
            map.put("result","success");
            return map;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private void autoAddEtlHost() {
        List<MediaSourceInfo> list = mediaSourceService.getListByType(Sets.newHashSet(MediaSourceType.SQLSERVER, MediaSourceType.MYSQL, MediaSourceType.POSTGRESQL));
        list.forEach(i -> {
            RdbMediaSrcParameter rdbMediaSrcParameter = i.getParameterObj();
            RdbMediaSrcParameter.ReadConfig readConfig = rdbMediaSrcParameter.getReadConfig();
            if (StringUtils.isBlank(readConfig.getEtlHost())) {
                readConfig.setEtlHost(readConfig.getHosts().get(0));
            }
            i.setParameter(rdbMediaSrcParameter.toJsonString());
            mediaSourceService.update(i);
        });
    }

    public void switchSddlCheckData (Long id) {
        LOG.info("switchSddlCheckData: id is " + id);
        String currentEnv = ManagerConfig.current().getCurrentEnv();
        List<Long> sddlGroupIds = new ArrayList<>();
        if (id == 2l) { // UCAR / TCAR
            if ("prod".equals(currentEnv)) {
                sddlGroupIds.add(12l);
            }else if ("dev".equals(currentEnv)) {
                sddlGroupIds.add(11l);
            }else {

                sddlGroupIds.addAll(getTestGroupIds());
            }
        } else if (id == 3l) { // LUCKY
            if ("prod".equals(currentEnv)) {
                sddlGroupIds.add(13l);
            } else {

                sddlGroupIds.addAll(getTestGroupIds());
            }
        } else if (id == 4l) { // LUCKY
            if ("prod".equals(currentEnv)) {
                sddlGroupIds.add(14l);
            } else {

                sddlGroupIds.addAll(getTestGroupIds());
            }
        }

        for (Long groupId : sddlGroupIds) {
            List<WorkerInfo> listWorker = workerService.getListForQuery(groupId == -1L ? null : groupId);

            for (WorkerInfo workerInfo : listWorker) {
                if (workerInfo != null) {
                    LOG.info("switchSddlCheckData: ip is " + workerInfo.getWorkerAddress());

                    Map<String, String> map = new HashMap<>();
                    map.put(CommonEvent.EVENT_NAME_KEY, "event_sddl_checkdata_on_off");

                    String url = "http://" + workerInfo.getWorkerAddress() + ":" + workerInfo.getRestPort() +  "/worker/eventProcess";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity request = new HttpEntity(JSONObject.toJSONString(map, SerializerFeature.WriteClassName), headers);
                    new RestTemplate().postForObject(url, request, Map.class);
                }
            }

        }

    }


    /**
     * 把支持双中心任务的数据源改造成虚拟数据源(第一步: 停任务)
     */
    private String taskTransformFirstStep() {

        //校验
        LOG.info("改造任务校验");
        doubleCenterService.checkTaskTransform();

        LOG.info("开始查询需要停止的任务");
        List<Long> list = doubleCenterService.taskTransform(false);
        LOG.info("查询到需要停止的任务有: " + JSON.toJSONString(list));

        List<String> nameList = new ArrayList<String>();
        for(Long taskId : list){
            nameList.add(taskService.getTask(taskId).getTaskName());
            taskService.pauseTask(taskId);
        }
        LOG.info("本次已经停止的任务有：" + JSON.toJSONString(nameList));
        return "本次已经停止的任务有: " + JSON.toJSONString(nameList);
    }

    /**
     * 把支持双中心任务的数据源改造成虚拟数据源(第二步: 改mediaSourceId、清缓存、启任务)
     */
    private void taskTransformSecondStep() throws Exception {

        //校验
        LOG.info("改造任务校验");
        doubleCenterService.checkTaskTransform();

        LOG.info("开始改造任务");
        List<Long> list = doubleCenterService.taskTransform(true);
        LOG.info("查询到需要改造的任务id有: " + JSON.toJSONString(list));
        List<String> nameList = new ArrayList<String>();
        for(Long taskId : list){
            nameList.add(taskService.getTask(taskId).getTaskName());
        }
        LOG.info("查询到需要改造的任务name有: " + JSON.toJSONString(nameList));

        //清缓存
        cleanMediaMapping(list);

        //启任务
        for(Long taskId : list){
            taskService.resumeTask(taskId);
        }
        LOG.info("改造任务完成");
    }

    /**
     * 一键改造datax的任务(任务使用虚拟数据源
     */
    private void dataxVirtualTransform() {
        doubleCenterDataxService.oldDataSourceChangeToVirtual();
        LOG.info("已经将datax的旧数据源切换置新的虚拟数据源！");
    }

    private void switchDataSourceForAllTimingJob() {
        doubleCenterDataxService.switchDataSourceForAllTimingJob();
        LOG.info("已经切换了所有定时任务的数据源！");
    }

    private void stopAllRunningJob() {
        doubleCenterDataxService.stopAllRunningJob();
        LOG.info("已经停止了所有job！");
    }

    /**
     * 清理mapping缓存
     *
     * @param taskIdlist
     */
    private void cleanMediaMapping(List<Long> taskIdlist) throws Exception {

        //清理映射缓存
        for(Long taskId : taskIdlist){
            mediaService.cleanTableMapping(taskId);
        }

    }



    /**
     * 一键反向同步
     */
    private void oneKeyReverseSyncFirstStep() {

        LOG.info("一键反向同步，修改数据开始");

        List<TaskInfo> taskInfoList = taskService.findAcrossLabList();

        //修改taskName、 task MediaSourceId、MediaId
        doubleCenterService.acrossTaskTransform(taskInfoList);

        //清空监控缓存
        monitorService.clearCache();

        LOG.info("一键反向同步，修改数据结束");
    }

    @RequestMapping(value = "/toRestartMysqlTask")
    public ModelAndView oneKeyReverseSyncSecondStep() {
        ModelAndView mav = new ModelAndView("util/mysqlTaskRestart");
        return mav;
    }

    @RequestMapping(value = "/doRestartMysqlTask")
    @ResponseBody
    public String doRestartMysqlTask(@RequestBody Map<String, String> restartParams) {
        try {

            if(StringUtils.isBlank(restartParams.get("newTimeStamps"))){
                return "重置时间不能为空";
            }
            Long newTimeStamps = Long.valueOf(restartParams.get("newTimeStamps"));

            //查询所有跨机房的task
            List<TaskInfo> taskList = taskService.findAcrossLabList();
            List<Long> taskIdlist = taskList.stream().map(taskInfo -> taskInfo.getId()).collect(Collectors.toList());

            //修改task位点
            doubleCenterService.updateTaskPosition(taskList,newTimeStamps);

            //重启worker
            doubleCenterService.reStartWorker(taskList);

            //启任务
            for(Long taskId : taskIdlist){
                taskService.resumeTask(taskId);
            }

        } catch (Exception e) {
            LOG.error("Restart MysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }


    /**
     * 重启task
     *
     * @param taskInfoList
     */
    private void restarTask(List<TaskInfo> taskInfoList, Long newTimeStamps) {

        List<Future<?>> futures = new ArrayList<>();
        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();

        for(TaskInfo taskInfo : taskInfoList){
            MysqlReaderPosition position = (MysqlReaderPosition) taskPositionService.getPosition(String.valueOf(taskInfo.getId()));
            position.setTimestamp(newTimeStamps);
            position.setSourceAddress(new InetSocketAddress("0.0.0.0", position.getSourceAddress().getPort()));
            ClusterState.MemberData memberData = clusterState.getMemberData(taskInfo.getId());
            String url = "http://" + memberData.getWorkerState().url() + "/tasks/" + taskInfo.getId() + "/restart";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity request = new HttpEntity(
                    position != null ? JSONObject.toJSONString(position, SerializerFeature.WriteClassName) : null,
                    headers);
            futures.add(pool.submit(new RestFeture(url, request)));

        }

        for(Future f: futures) {
            try {
                f.get();
            } catch (Exception e) {
                LOG.error(e.getMessage(),e);
            }
        }

    }


    @RequestMapping(value = "/checkJobContent")
    public ModelAndView checkJobContent(Long id,Model model) {
        List<JobConfigInfo> list = doubleCenterDataxService.checkAllDataSource();
        StringBuilder sb = new StringBuilder();
        list.forEach(j->{
            sb.append(j.getJob_name());
        });
        ModelAndView mav = new ModelAndView("util/checkJobContent");
        mav.addObject("str",sb.toString());
        return mav;
    }


    @RequestMapping(value = "/toVirtualChangeReal")
    public String toVirtualChangeReal(Long id,Model model) {
        model.addAttribute("id",id);
        return "util/virtualChangeReal";
    }

    /**
     * 把虚拟数据源变成真实数据源(第一步: 停任务)
     */
    @RequestMapping(value = "/virtualChangeRealFirstStep")
    @ResponseBody
    private Object virtualChangeRealFirstStep (@RequestBody Map<String, String> restartParams) {

        try{
            LOG.info("虚拟变实际，开始查询需要停止的任务");
            if(StringUtils.isBlank(restartParams.get("virtualMediaSourceId"))){
                return "虚拟数据源id不能为空";
            }
            Long virtualMediaSourceId = Long.valueOf(restartParams.get("virtualMediaSourceId"));

            //校验
            doubleCenterService.checkVirtualChangeReal(virtualMediaSourceId);

            //查询要改造的任务
            List<Long> mediaSourceIdList = new ArrayList<Long>();
            mediaSourceIdList.add(virtualMediaSourceId);
            List<TaskInfo> taskList = doubleCenterService.findAssociatedTaskList(mediaSourceIdList);

            //停止任务
            List<String> nameList = new ArrayList<String>();
            for(TaskInfo taskInfo : taskList){
                nameList.add(taskInfo.getTaskName());
                taskService.pauseTask(taskInfo.getId());
            }

            LOG.info("虚拟变实际，本次已经停止的任务有：" + JSON.toJSONString(nameList));
            Map<String,String> resultMap = new HashMap<String,String>();
            resultMap.put("result","success");
            resultMap.put("msg","虚拟变实际，本次已经停止的任务有: " + JSON.toJSONString(nameList));
            return resultMap;
        } catch (Exception e) {
            LOG.error("Stop MysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
    }

    /**
     * 把虚拟数据源变成真实数据源(第二步: 改mediaSourceId、清缓存、启任务)
     */
    @RequestMapping(value = "/virtualChangeRealSecondStep")
    @ResponseBody
    private Object virtualChangeRealSecondStep(@RequestBody Map<String, String> restartParams) {

        try{
            LOG.info("虚拟变实际，开始改造任务");

            if(StringUtils.isBlank(restartParams.get("virtualMediaSourceId"))){
                return "虚拟数据源id不能为空";
            }
            Long virtualMediaSourceId = Long.valueOf(restartParams.get("virtualMediaSourceId"));

            //校验
            MediaSourceInfo mediaSourceInfoA = doubleCenterService.checkVirtualChangeReal(virtualMediaSourceId);

            //查询要改造的任务
            List<Long> mediaSourceIdList = new ArrayList<Long>();
            mediaSourceIdList.add(virtualMediaSourceId);
            List<TaskInfo> taskList = doubleCenterService.findAssociatedTaskList(mediaSourceIdList);
            List<String> nameList = taskList.stream().map(TaskInfo :: getTaskName).collect(Collectors.toList());
            LOG.info("虚拟变实际，查询到需要改造的任务name有: " + JSON.toJSONString(nameList));

            //改造
            doubleCenterService.virtualChangeReal(taskList,virtualMediaSourceId,mediaSourceInfoA);
            LOG.info("虚拟变实际，入库完成。");

            //清缓存
            List<Long> idList = taskList.stream().map(TaskInfo :: getId).collect(Collectors.toList());
            cleanMediaMapping(idList);

            //启任务
            for(Long taskId : idList){
                taskService.resumeTask(taskId);
            }
            LOG.info("虚拟变实际，改造任务完成");
            Map<String,String> resultMap = new HashMap<String,String>();
            resultMap.put("result","success");
            return resultMap;
        } catch (Exception e) {
            LOG.error("Restart MysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }

    }

    /**
     * 单库切机房失败后重试入口
     * @return
     */
    @RequestMapping(value = "/reSwitchDbOrSddl")
    public ModelAndView reSwitchDbOrSddl() {
        ModelAndView mav = new ModelAndView("util/reSwitchDbOrSddl");
        return mav;
    }

    /**
     * 单库切换，手动通知dbms结果(通知失败时才使用)
     * @return
     */
    @RequestMapping(value = "/reNotifyDbms")
    public ModelAndView reNotifyDbms() {
        ModelAndView mav = new ModelAndView("util/reNotifyDbms");
        return mav;
    }

    private class RestFeture implements Callable {
        final String url;
        final HttpEntity request;

        RestFeture(String url,HttpEntity request) {
            this.url = url;
            this.request = request;
        }

        @Override
        public Object call() throws Exception {
            return new RestTemplate().postForObject(url, request, Map.class);
        }
    }

    private List<Long> getTestGroupIds () {
        List<GroupInfo> listGroup = groupService.getAllGroups();

        List<Long> groupId = listGroup.stream().map(i -> {
            return i.getId();
        }).collect(Collectors.toList());

        return groupId;
    }

    class OperationInfo {
        private Long id;
        private String name;
        private String desc;

        public OperationInfo() {

        }

        public OperationInfo(Long id, String name, String desc) {
            this.id = id;
            this.name = name;
            this.desc = desc;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
