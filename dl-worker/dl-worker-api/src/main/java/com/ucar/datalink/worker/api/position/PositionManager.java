package com.ucar.datalink.worker.api.position;

import com.ucar.datalink.domain.Position;

/**
 * 数据同步位点管理类，负责查询、更新消费位点.
 * PositionManager是Datalink提供的一个公用位点管理组件，如果TaskReader有自己的位点管理机制，用自己的机制即可。
 * <p>
 * Created by lubiao on 2016/11/23.
 */
public interface PositionManager {

    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void stop();

    /**
     * 是否已经启动
     *
     * @return
     */
    boolean isStart();

    /**
     * 更新消费位点到存储介质(定时更新)
     *
     * @param taskId
     * @param position
     */
    void updatePosition(String taskId, Position position);

    /**
     * 更新消费位点到存储介质(立即更新),调用该方法时需确保Task已经处于关闭状态，否则updatePositionNow和updatePosition会有并发问题
     *
     * @param taskId
     * @param position
     */
    void updatePositionNow(String taskId, Position position);

    /**
     * 获取当前消费到的位点,如果没有则返回null
     *
     * @param taskId
     * @return
     */
    <T extends Position> T getPosition(String taskId);

    /**
     * 如果存在的话，将某个任务的Position信息废弃，即：停止进行位点更新操作，避免因并发update导致的数据一致性问题
     * @param taskId
     */
    void discardPosition(String taskId);
}
