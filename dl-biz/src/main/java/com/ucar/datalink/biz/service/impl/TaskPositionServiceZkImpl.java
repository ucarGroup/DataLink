package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ucar.datalink.biz.service.TaskPositionService;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.Position;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.springframework.stereotype.Service;

/**
 * Created by lubiao on 2016/12/6.
 */
@Service("taskPositionServiceZkImpl")
public class TaskPositionServiceZkImpl implements TaskPositionService {

    @Override
    public void updatePosition(String taskId, Position position) {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        String path = DLinkZkPathDef.getTaskPositionNode(taskId);
        byte[] data = JSON.toJSONBytes(position, SerializerFeature.WriteClassName);
        try {
            zkUtils.zkClient().writeData(path, data);
        } catch (ZkNoNodeException e) {
            zkUtils.zkClient().createPersistent(path, data, true);// 第一次节点不存在，则尝试创建
        }
    }

    @Override
    public Position getPosition(String taskId) {
        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        String path = DLinkZkPathDef.getTaskPositionNode(taskId);

        byte[] data = zkUtils.zkClient().readData(path, true);
        if (data == null || data.length == 0) {
            return null;
        }

        return JSON.parseObject(data, Position.class);
    }
}
