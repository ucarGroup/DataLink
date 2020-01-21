package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.AlarmStrategyDAO;
import com.ucar.datalink.biz.service.AlarmStrategyService;
import com.ucar.datalink.common.utils.DateUtil;
import com.ucar.datalink.domain.alarm.AlarmStrategyInfo;
import com.ucar.datalink.domain.alarm.StrategyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class AlarmStrategyServiceImpl implements AlarmStrategyService {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final Logger logger = LoggerFactory.getLogger(AlarmStrategyServiceImpl.class);

    @Autowired
    private AlarmStrategyDAO alarmStrategyDAO;

    @Override
    public List<AlarmStrategyInfo> getAlarmStrategyList(Long priorityId,String name) {
        return alarmStrategyDAO.getAlarmStrategyList(priorityId,name);
    }

    @Override
    public Boolean insert(AlarmStrategyInfo alarmStrategyInfo) {
        Integer num = alarmStrategyDAO.insert(alarmStrategyInfo);
        if(num > 0){
            return true;
        }
        return false;
    }

    @Override
    public AlarmStrategyInfo getById(Long id) {
        return alarmStrategyDAO.getById(id);
    }

    @Override
    public Boolean update(AlarmStrategyInfo alarmStrategyInfo) {
        Integer num = alarmStrategyDAO.update(alarmStrategyInfo);
        if(num > 0){
            return true;
        }
        return false;
    }

    @Override
    public Boolean delete(Long id) {
        Integer num = alarmStrategyDAO.delete(id);
        if (num > 0) {
            return true;
        }

        return false;
    }

    @Override
    public AlarmStrategyInfo getByTaskIdAndType(Long taskId, Integer monitorType) {
        return alarmStrategyDAO.getByTaskIdAndType(taskId,monitorType);
    }

    @Override
    public StrategyConfig getStrategyConfig(List<StrategyConfig> strategys) {
        if (strategys != null && strategys.size() >0) {
            for (StrategyConfig strategyConfig : strategys) {
                Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR);
                int month = now.get(Calendar.MONTH) + 1;
                int day = now.get(Calendar.DAY_OF_MONTH);
                Date date = now.getTime();
                //rangeTime : 00:00-02:30
                String[] range = strategyConfig.getTimeRange().split("-");
                try {
                    Date start = sdf.parse(year + "-" + month + "-" + day + " " + range[0] + ":00");
                    Date end = sdf.parse(year + "-" + month + "-" + day + " " + range[1] + ":00");
                    if(Integer.valueOf(range[0].replaceAll(":",""))>
                            Integer.valueOf(range[1].replaceAll(":",""))){
                        now.setTime(end);
                        now.add(Calendar.DAY_OF_MONTH,1);
                        end = now.getTime();
                    }
                    if(date.after(start) && date.before(end)){
                        return strategyConfig;
                    }
                } catch (ParseException e) {
                    logger.info("时间比较",e);
                }
            }
        }
        return new StrategyConfig();
    }
}
