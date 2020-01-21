package com.ucar.datalink.flinker.core.statistics.container.collector;

import com.ucar.datalink.flinker.api.base.TaskInfo;
import com.ucar.datalink.flinker.api.statistics.PerfTrace;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import com.ucar.datalink.flinker.core.job.meta.State;
import com.ucar.datalink.flinker.core.statistics.communication.Communication;
import com.ucar.datalink.flinker.core.statistics.communication.LocalTGCommunicationManager;
import com.ucar.datalink.flinker.core.util.container.CoreConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCollector {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCollector.class);
    private Map<Integer, Communication> taskCommunicationMap = new ConcurrentHashMap<Integer, Communication>();
    private Long jobId;

    public Map<Integer, Communication> getTaskCommunicationMap() {
        return taskCommunicationMap;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public void registerTGCommunication(List<Configuration> taskGroupConfigurationList) {
        for (Configuration config : taskGroupConfigurationList) {
            int taskGroupId = config.getInt(
                    CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID);
            LocalTGCommunicationManager.registerTaskGroupCommunication(taskGroupId, new Communication());
        }
    }

    public void registerTaskCommunication(List<Configuration> taskConfigurationList) {
        for (Configuration taskConfig : taskConfigurationList) {
            int taskId = taskConfig.getInt(CoreConstant.TASK_ID);
            this.taskCommunicationMap.put(taskId, new Communication());
        }
    }

    public Communication collectFromTask() {
        Communication communication = new Communication();
        communication.setState(State.SUCCEEDED);
        //收集task用
        collectTaskInfo();
        for (Communication taskCommunication : this.taskCommunicationMap.values()) {
            communication.mergeFrom(taskCommunication);
        }

        return communication;
    }

    private void collectTaskInfo(){
        try{
            PerfTrace perfTrace = PerfTrace.getInstance();
            if(perfTrace == null){
                LOG.error("collectTaskInfo perfTrace is null");
                ErrorRecord.addError("collectTaskInfo perfTrace is null");
                return;
            }
            Map<Integer, TaskInfo> taskMap = perfTrace.getTaskCommunicationMap();
            for (Map.Entry<Integer, Communication> entry : taskCommunicationMap.entrySet()) {
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setTaskId(entry.getKey());
                taskInfo.setTaskInfo(entry.getValue().getCounter());
                taskMap.put(entry.getKey(), taskInfo);
            }
        }catch (Exception e){
            LOG.error("collectTaskInfo is error",e);
            ErrorRecord.addError("collectTaskInfo is error"+e.getMessage());
        }
    }

    public abstract Communication collectFromTaskGroup();

    public Map<Integer, Communication> getTGCommunicationMap() {
        return LocalTGCommunicationManager.getTaskGroupCommunicationMap();
    }

    public Communication getTGCommunication(Integer taskGroupId) {
        return LocalTGCommunicationManager.getTaskGroupCommunication(taskGroupId);
    }

    public Communication getTaskCommunication(Integer taskId) {
        return this.taskCommunicationMap.get(taskId);
    }
}
