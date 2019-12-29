package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.TaskDecorateDAO;
import com.ucar.datalink.biz.service.TaskDecorateService;
import com.ucar.datalink.domain.decorate.TaskDecorate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xy.li
 * @date 2019/05/31
 */
@Service
public class TaskDecorateServiceImpl implements TaskDecorateService {


    @Autowired
    private TaskDecorateDAO taskDecorateDAO;

    @Override
    public List<TaskDecorate> getList(long taskId, String taskName) {
        return this.taskDecorateDAO.listByCondition(taskId,taskName);
    }

    @Override
    public Boolean insert(TaskDecorate mediaSourceInfo) {
        return this.taskDecorateDAO.insert(mediaSourceInfo) == 1;
    }

    @Override
    public Boolean update(TaskDecorate mediaSourceInfo) {
        return this.taskDecorateDAO.update(mediaSourceInfo) == 1;
    }

    @Override
    public Boolean delete(Long id) {
        TaskDecorate taskDecorate = new TaskDecorate();
        taskDecorate.setId(id);
        taskDecorate.setDeleted(true);
        return this.taskDecorateDAO.update(taskDecorate) == 1;
    }

    @Override
    public TaskDecorate getById(Long id) {
        return this.taskDecorateDAO.findById(id);
    }

    @Override
    public Integer recordCount() {
        return this.taskDecorateDAO.recordCount();
    }
}
