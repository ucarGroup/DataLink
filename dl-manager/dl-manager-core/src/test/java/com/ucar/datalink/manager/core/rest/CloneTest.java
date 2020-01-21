package com.ucar.datalink.manager.core.rest;

import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;

public class CloneTest {

    public static void main(String[] args) throws CloneNotSupportedException {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskName("test");
        taskInfo.setIsLeaderTask(false);
        taskInfo.setLabId(100L);
        taskInfo.setTargetState(TargetState.PAUSED);

        TaskInfo task2 = taskInfo.clone();
        task2.setTaskName("test2");

        System.out.println(task2.getTargetState());
        System.out.println(task2.getLabId());
        System.out.println(task2.isLeaderTask());
        System.out.println(task2.getTaskName());
        System.out.println(taskInfo.getTaskName());

    }
}
