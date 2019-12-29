package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.decorate.TaskDecorateDetail;

import java.util.List;

/**
 * @author xy.li
 * @date 2019/06/04
 */
public interface TaskDecorateDetailService {

    List<TaskDecorateDetail> listByCondition( long decorateId);

    List<TaskDecorateDetail> queryBytaskIdAndStatus(long taskId,int status);

    TaskDecorateDetail findById(Long id);

    Integer insert(TaskDecorateDetail taskInfo);

    Integer update(TaskDecorateDetail taskInfo);

    Integer recordCount(long recordCount);


}
