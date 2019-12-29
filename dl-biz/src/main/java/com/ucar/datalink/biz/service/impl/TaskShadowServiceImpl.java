package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.TaskShadowDAO;
import com.ucar.datalink.biz.service.TaskShadowService;
import com.ucar.datalink.domain.task.TaskShadowInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lubiao on 2019/8/5.
 */
@Service
public class TaskShadowServiceImpl implements TaskShadowService {
    private static final Logger logger = LoggerFactory.getLogger(TaskShadowServiceImpl.class);

    @Autowired
    private TaskShadowDAO taskShadowDAO;

    @Override
    public void createTaskShadow(TaskShadowInfo taskShadowInfo) {
        taskShadowDAO.insertTaskShadow(taskShadowInfo);
    }

    @Override
    public TaskShadowInfo getTaskShadowById(Long id) {
        return taskShadowDAO.getTaskShadowById(id);
    }

    @Override
    public TaskShadowInfo getExecutingTaskShadow(Long taskId) {
        return taskShadowDAO.getTaskShadowInExecutingState(taskId);
    }

    @Override
    public TaskShadowInfo takeOneTaskShadow(Long taskId) {
        return taskShadowDAO.getMinTaskShadowInInitState(taskId);
    }

    @Override
    public void startTaskShadow(TaskShadowInfo taskShadowInfo) {
        if (TaskShadowInfo.State.INIT.equals(taskShadowInfo.getState())) {
            taskShadowInfo.setState(TaskShadowInfo.State.EXECUTING);
            taskShadowDAO.updateTaskShadowState(taskShadowInfo);
        } else {
            throw new IllegalStateException("start task shadow failed for the current state is not INIT.");
        }
        logger.info("Task Shadow {} started.", taskShadowInfo.getId());
    }

    @Override
    public void completeTaskShadow(TaskShadowInfo taskShadowInfo) {
        if (TaskShadowInfo.State.EXECUTING.equals(taskShadowInfo.getState())) {
            taskShadowInfo.setState(TaskShadowInfo.State.COMPLETE);
            taskShadowDAO.updateTaskShadowState(taskShadowInfo);
        } else {
            throw new IllegalStateException("complete task shadow failed for the current state is not EXECUTING.");
        }
        logger.info("Task Shadow {} completed.", taskShadowInfo.getId());
    }

    @Override
    public boolean discardTaskShadow(long id) {
        boolean result = taskShadowDAO.discardTaskShadow(id) > 0;
        logger.info("Task Shadow {} discarded.", id);
        return result;
    }

    @Override
    public List<TaskShadowInfo> taskShadowListsForQueryPage(Long taskId, String state) {
        return taskShadowDAO.taskShadowListsForQueryPage(taskId,state);
    }
}
