package com.ucar.datalink.manager.core.monitor.impl;

import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.biz.service.TaskTraceService;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskTraceInfo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.monitor.Monitor;
import com.ucar.datalink.manager.core.server.ServerContainer;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by djj on 19/4/24.
 *
 * 监控任务轨迹
 *      目前只监控任务在某个时间段运行在哪个机器、所属哪个分组
 */
@Service
public class TaskTraceMonitor extends Monitor {

    private static final Logger logger = LoggerFactory.getLogger(TaskTraceMonitor.class);

    @Autowired
    TaskConfigService taskConfigService;
    @Autowired
    TaskTraceService taskTraceService;

    @Override
    public void doMonitor() {

        try{

            //集群信息
            GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
            ClusterState clusterState = groupManager.getClusterState();
            Map<Long, Long> map = clusterState.getTaskWorkerMapping();

            List<TaskInfo> list = taskConfigService.getList();
            for(TaskInfo taskInfo : list){

                //workerId
                Long workId = map.get(taskInfo.getId());
                if(workId == null){
                    continue;
                }

                List<TaskTraceInfo> taskTraceInfoList = taskTraceService.findListByTaskId(taskInfo.getId(),null,null);
                //第一次录入当前信息
                if(CollectionUtils.isEmpty(taskTraceInfoList)){

                    //录入
                    TaskTraceInfo traceInfoTemp = new TaskTraceInfo();
                    traceInfoTemp.setTaskId(taskInfo.getId());
                    traceInfoTemp.setWorkerId(workId);
                    traceInfoTemp.setGroupId(taskInfo.getGroupId());
                    traceInfoTemp.setStartTime(new Date());
                    traceInfoTemp.setEndTime(new Date());
                    taskTraceService.insert(traceInfoTemp);
                }else{
                    TaskTraceInfo traceInfo = taskTraceInfoList.get(0);
                    if((!traceInfo.getGroupId().equals(taskInfo.getGroupId())) || (!traceInfo.getWorkerId().equals(workId))){

                        //更新旧记录结束时间
                        traceInfo.setEndTime(new Date());
                        //录入新的记录
                        TaskTraceInfo traceInfoTemp = new TaskTraceInfo();
                        traceInfoTemp.setTaskId(taskInfo.getId());
                        traceInfoTemp.setWorkerId(workId);
                        traceInfoTemp.setGroupId(taskInfo.getGroupId());
                        traceInfoTemp.setStartTime(new Date());
                        traceInfo.setEndTime(new Date());
                        taskTraceService.updateAndInsert(traceInfo,traceInfoTemp);
                    }else{
                        //更新当前记录结束时间
                        traceInfo.setEndTime(new Date());
                        taskTraceService.update(traceInfo);
                    }
                }

            }

        }catch (Exception e){
            logger.error("监控任务运行轨迹出现异常,该异常不影响业务，只日志记录即可,异常是: {}", e);
        }

    }

}
