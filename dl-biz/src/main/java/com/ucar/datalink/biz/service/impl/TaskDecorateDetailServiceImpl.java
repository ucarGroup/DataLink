package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.TaskDecorateDAO;
import com.ucar.datalink.biz.dal.TaskDecorateDetailDAO;
import com.ucar.datalink.biz.service.TaskDecorateDetailService;
import com.ucar.datalink.domain.decorate.TaskDecorateDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xy.li
 * @date 2019/06/04
 */
@Service
public class TaskDecorateDetailServiceImpl implements TaskDecorateDetailService {

    @Autowired
    private TaskDecorateDetailDAO taskDecorateDetailDAO;

    @Override
    public List<TaskDecorateDetail> listByCondition(long decorateId) {
        return this.taskDecorateDetailDAO.listByCondition(decorateId);
    }

    @Override
    public List<TaskDecorateDetail> queryBytaskIdAndStatus(long taskId,int status) {
        return this.taskDecorateDetailDAO.queryBytaskIdAndStatus(taskId,status);
    }

    @Override
    public TaskDecorateDetail findById(Long id) {
        return this.taskDecorateDetailDAO.findById(id);
    }

    @Override
    public Integer insert(TaskDecorateDetail taskInfo) {
        return this.taskDecorateDetailDAO.insert(taskInfo);
    }

    @Override
    public Integer update(TaskDecorateDetail taskInfo) {
        return this.taskDecorateDetailDAO.update(taskInfo);
    }

    @Override
    public Integer recordCount(long decorateId) {
        return this.taskDecorateDetailDAO.recordCount(decorateId);
    }
}
