package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ucar.datalink.biz.dal.TaskPositionDAO;
import com.ucar.datalink.biz.service.TaskPositionService;
import com.ucar.datalink.domain.Position;
import com.ucar.datalink.domain.task.TaskPositionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lubiao on 2018/3/12.
 */
@Service("taskPositionServiceDbImpl")
public class TaskPositionServiceDbImpl implements TaskPositionService {

    @Autowired
    TaskPositionDAO taskPositionDAO;

    @Override
    public void updatePosition(String taskId, Position position) {
        String taskPosition = JSONObject.toJSONString(position, SerializerFeature.WriteClassName);
        TaskPositionInfo taskPositionInfo = new TaskPositionInfo();
        taskPositionInfo.setTaskId(Long.valueOf(taskId));
        taskPositionInfo.setTaskPosition(taskPosition);
        taskPositionDAO.updateTaskPosition(taskPositionInfo);
    }

    @Override
    public Position getPosition(String taskId) {
        TaskPositionInfo taskPositionInfo = taskPositionDAO.getByTaskId(Long.valueOf(taskId));
        return taskPositionInfo == null ? null : JSONObject.parseObject(taskPositionInfo.getTaskPosition(), Position.class);
    }
}
