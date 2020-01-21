package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.doublecenter.LabSwitchInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DoubleCenterService {

     String getCenterLab(Object virtualMsId);

     String getLabByIp(String ip);

     void stopOrStartIncrementTask(List<Long> mediaSourceIdList, TargetState targetState);

     void changeDataSource(List<Long> needSwitchIdList, String targetLab);

     void oneKeyStopSync(List<Long> needSwitchMysqlRealIdList);

     Boolean insertLabSwitchInfo(LabSwitchInfo labSwitchInfo);

     Boolean updateLabSwitchInfo(LabSwitchInfo labSwitchInfo);

     LabSwitchInfo getLabSwitchByVersion(String version);

     List<Long> taskTransform(Boolean isUpdate);

     void checkTaskTransform();

     void acrossTaskTransform(List<TaskInfo> taskInfoList);

     List<LabSwitchInfo> findAll();

     MediaSourceInfo checkVirtualChangeReal(Long virtualMediaSourceId);

     List<TaskInfo> findAssociatedTaskList(List<Long> mediaSourceIdList);

     void virtualChangeReal(List<TaskInfo> taskList,Long virtualMediaSourceId,MediaSourceInfo mediaSourceInfoA);

     void oneKeyReverseSync(List<Long> needSwitchMysqlRealList, Date switchStartTimeDate);

     void updateTaskPosition(List<TaskInfo> taskList, Long newTimeStamps);

     void reStartWorker(List<TaskInfo> taskList);

     void updateDb(String version,Integer status,Integer process);

     void updateVersionToZk(String version, Integer status);

}
