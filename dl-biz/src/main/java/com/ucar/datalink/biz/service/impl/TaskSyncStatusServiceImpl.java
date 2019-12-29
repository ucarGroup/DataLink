package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ucar.datalink.biz.service.TaskStatusService;
import com.ucar.datalink.biz.service.TaskSyncStatusService;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.task.TaskSyncStatus;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by lubiao on 2019/2/18.
 */
@Service
public class TaskSyncStatusServiceImpl implements TaskSyncStatusService {

    @Autowired
    TaskStatusService taskStatusService;

    @Override
    public void updateSyncStatus(String taskId, TaskSyncStatus syncStatus) {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        String path = DLinkZkPathDef.getTaskSyncStatusNode(taskId);
        byte[] data = JSON.toJSONBytes(syncStatus, SerializerFeature.WriteClassName);
        try {
            zkUtils.zkClient().writeData(path, data);
        } catch (ZkNoNodeException e) {
            zkUtils.zkClient().createPersistent(path, data, true);// 第一次节点不存在，则尝试创建
        }
    }

    @Override
    public TaskSyncStatus getSyncStatus(String taskId) {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        String path = DLinkZkPathDef.getTaskSyncStatusNode(taskId);

        byte[] data = zkUtils.zkClient().readData(path, true);
        if (data == null || data.length == 0) {
            return null;
        }

        return JSON.parseObject(data, TaskSyncStatus.class);
    }

    @Override
    public Collection<TaskSyncStatus> getAll() {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        List<TaskSyncStatus> result = new ArrayList<>();
        Set<String> tasks = taskStatusService.tasks();
        for (String taskId : tasks) {
            byte[] bytes = zkUtils.zkClient().readData(DLinkZkPathDef.getTaskSyncStatusNode(taskId), true);
            if (bytes != null) {
                result.add(JSON.parseObject(bytes, TaskSyncStatus.class));
            }
        }
        return result;
    }
}
