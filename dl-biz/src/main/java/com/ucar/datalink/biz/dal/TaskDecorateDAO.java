package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.decorate.TaskDecorate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xy.li
 * @date 2019/05/29
 */
public interface TaskDecorateDAO {

    List<TaskDecorate> listByCondition(@Param(value = "taskId") long taskId, @Param(value = "tableName") String taskName);

    TaskDecorate findById(Long id);

    Integer insert(TaskDecorate taskInfo);

    Integer update(TaskDecorate taskInfo);

    Integer recordCount();

}
