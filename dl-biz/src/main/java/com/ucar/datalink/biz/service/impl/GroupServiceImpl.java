package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.TaskDAO;
import com.ucar.datalink.biz.dal.WorkerDAO;
import com.ucar.datalink.biz.service.GroupService;
import com.ucar.datalink.biz.dal.GroupDAO;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.worker.WorkerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lubiao on 2017/1/20.
 */
@Service
public class GroupServiceImpl implements GroupService {

    public static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Autowired
    private GroupDAO groupDAO;

    @Autowired
    private TaskDAO taskDAO;

    @Autowired
    private WorkerDAO workerDAO;

    @Override
    public List<GroupInfo> getAllGroups() {
        return groupDAO.listAllGroups();
    }

    @Override
    public Boolean insert(GroupInfo groupInfo) {
        Integer num = groupDAO.insert(groupInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean update(GroupInfo groupInfo) {
        Integer num = groupDAO.update(groupInfo);
        if (num >= 0) {
            return true;
        }

        return false;
    }

    @Override
    public Boolean delete(Long id) {
        List<WorkerInfo> workers = workerDAO.listByGroupId(id);
        if (workers != null && !workers.isEmpty()) {
            throw new ValidationException("当前有Worker归属于该分组，不能进行删除操作.");
        }

        List<TaskInfo> tasks = taskDAO.listByGroupId(id);
        if (tasks != null && !tasks.isEmpty()) {
            throw new ValidationException("当前有Task归属于该分组，不能进行删除操作.");
        }

        Integer num = groupDAO.delete(id);
        if (num >= 0) {
            return true;
        }

        return false;
    }

    @Override
    public GroupInfo getById(Long id) {
        return groupDAO.getById(id);
    }

    @Override
    public Integer groupCount() {
        return groupDAO.groupCount();
    }

    @Override
    public List<StatisDetail> getCountByName() {
        return groupDAO.getCountByName();
    }
}
