package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.service.TaskStatusMismatchLogService;
import com.ucar.datalink.domain.task.TaskStatusMismatchLogInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by lubiao on 2018/4/25.
 */
public class TaskStatusMismatchLogServiceTest {

    private ApplicationContext context;

    @Before
    public void before() {
        context = new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
    }

    @Test
    public void testInsert(){
        TaskStatusMismatchLogInfo info = new TaskStatusMismatchLogInfo();
        info.setTaskId(1L);
        info.setWorkerId(2L);
        info.setActionType(TaskStatusMismatchLogInfo.ActionType.UPDATE);
        info.setLocalStatus("dddddd");
        info.setRemoteStatus("dddddd");
        context.getBean(TaskStatusMismatchLogService.class).insert(info);
    }
}
