package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.decorate.TaskDecorate;

import java.util.List;

/**
 * @author xy.li
 * @date 2019/05/31
 */
public interface TaskDecorateService {

    List<TaskDecorate> getList(long taskId, String taskName);

    Boolean insert(TaskDecorate mediaSourceInfo);

    Boolean update(TaskDecorate mediaSourceInfo);

    Boolean delete(Long id);

    TaskDecorate getById(Long id);

    Integer recordCount();


}
