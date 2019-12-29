package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.decorate.TaskDecorateDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xy.li
 * @date 2019/05/30
 */
public interface TaskDecorateDetailDAO {

    List<TaskDecorateDetail> listByCondition(@Param(value = "decorateId") long decorateId);

    List<TaskDecorateDetail> queryBytaskIdAndStatus(@Param(value = "taskId") long taskId,@Param(value = "status") int status);

    TaskDecorateDetail findById(Long id);

    Integer insert(TaskDecorateDetail taskInfo);

    Integer update(TaskDecorateDetail taskInfo);

    Integer recordCount(long decorateId);

}
