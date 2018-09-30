package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.task.TaskPositionInfo;

/**
 * Created by lubiao on 2018/3/12.
 */
public interface TaskPositionDAO {

    int updateTaskPosition(TaskPositionInfo taskPositionInfo);

    TaskPositionInfo getByTaskId(Long taskId);
}
