package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.ucar.datalink.common.errors.TaskConflictException;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.biz.service.TaskStatusService;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import org.I0Itec.zkclient.DataUpdater;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 任务状态管理服务类，基于Zookeeper.
 * <p>
 * Created by lubiao on 2016/12/6.
 */
@Service
public class TaskStatusServiceImpl implements TaskStatusService {

    @Override
    public void addStatus(TaskStatus status) throws TaskConflictException {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        String statusPath = DLinkZkPathDef.getTaskStatusNode(status.getId());

        byte[] bytes = JSON.toJSONBytes(status);
        try {
            zkUtils.zkClient().createPersistent(DLinkZkPathDef.getTaskNode(status.getId()), true);
            zkUtils.zkClient().create(statusPath, bytes, CreateMode.EPHEMERAL);
        } catch (ZkNodeExistsException e) {
            byte[] data = zkUtils.zkClient().readData(statusPath, true);
            if (data != null) {
                TaskStatus otherTaskStatus = JSON.parseObject(data, TaskStatus.class);
                throw new TaskConflictException(status.getId(), status.getWorkerId(), otherTaskStatus.getWorkerId(),
                        status.getExecutionId(), otherTaskStatus.getExecutionId());
            } else {
                addStatus(status);
            }
        }
    }

    @Override
    public void updateStatus(TaskStatus status) {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        String statusPath = DLinkZkPathDef.getTaskStatusNode(status.getId());
        byte[] bytes = JSON.toJSONBytes(status);

        zkUtils.zkClient().updateDataSerialized(statusPath, new DataUpdater<byte[]>() {

            @Override
            public byte[] update(byte[] currentData) {
                return bytes;
            }
        });
    }

    @Override
    public void removeStatus(String taskId) {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        String statusPath = DLinkZkPathDef.getTaskStatusNode(taskId);
        zkUtils.zkClient().delete(statusPath);
    }

    @Override
    public Collection<TaskStatus> getAll() {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        List<TaskStatus> result = new ArrayList<>();
        for (String taskId : tasks()) {
            byte[] bytes = zkUtils.zkClient().readData(DLinkZkPathDef.getTaskStatusNode(taskId), true);
            if (bytes != null) {
                result.add(JSON.parseObject(bytes, TaskStatus.class));
            }
        }
        return result;
    }

    @Override
    public TaskStatus getStatus(String taskId) {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        byte[] bytes = zkUtils.zkClient().readData(DLinkZkPathDef.getTaskStatusNode(taskId), true);
        if (bytes != null) {
            return JSON.parseObject(bytes, TaskStatus.class);
        } else {
            return null;
        }
    }

    @Override
    public Set<String> tasks() {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        try {
            List<String> list = zkUtils.zkClient().getChildren(DLinkZkPathDef.TaskRoot);
            return list == null ? Sets.newHashSet() : list.stream().collect(Collectors.toSet());
        } catch (ZkNoNodeException e) {
            return Sets.newHashSet();
        }

    }
}
