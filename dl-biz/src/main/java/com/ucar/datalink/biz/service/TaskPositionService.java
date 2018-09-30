package com.ucar.datalink.biz.service;


import com.ucar.datalink.domain.Position;

/**
 * Created by lubiao on 2016/12/6.
 */
public interface TaskPositionService {
    /**
     * 更新指定task的位点信息
     * @param taskId
     * @param position
     */
    void updatePosition(String taskId, Position position);

    /**
     * 获取指定Task的位点信息，如果没有则返回null
     * @param taskId
     * @return
     */
    Position getPosition(String taskId);
}
