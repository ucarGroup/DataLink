package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.task.TaskShadowInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by lubiao on 2019/8/5.
 */
public interface TaskShadowDAO {
    TaskShadowInfo getTaskShadowById(Long id);

    TaskShadowInfo getMinTaskShadowInInitState(Long taskId);

    TaskShadowInfo getTaskShadowInExecutingState(Long taskId);

    void insertTaskShadow(TaskShadowInfo taskShadowInfo);

    void updateTaskShadowState(TaskShadowInfo taskShadow);

    long discardTaskShadow(long id);

    List<TaskShadowInfo> taskShadowListsForQueryPage(@Param(value="taskId") Long taskId,@Param("state") String state);
}
