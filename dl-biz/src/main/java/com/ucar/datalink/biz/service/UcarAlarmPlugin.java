package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.alarm.AlarmMessage;

public interface UcarAlarmPlugin {

    String getPluginName();

    void doSend(AlarmMessage message);
}
