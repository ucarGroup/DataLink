package com.ucar.datalink.biz.utils.flinker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by yang.wang09 on 2019-02-28 19:12.
 */
public class GetBetweenDate {

    private static final Logger logger = LoggerFactory.getLogger(GetBetweenDate.class);

    /**
     * 返回begin到end之间的日期
     * @param begin
     * @param end
     * @return
     */
    public static List<String> getBetweenDate(String begin,String end){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> betweenList = new ArrayList<String>();

        try{
            Calendar startDay = Calendar.getInstance();
            startDay.setTime(format.parse(begin));
            startDay.add(Calendar.DATE, -1);

            while(true){
                startDay.add(Calendar.DATE, 1);
                Date newDate = startDay.getTime();
                String newend=format.format(newDate);
                betweenList.add(newend);
                if(end.equals(newend)){
                    break;
                }
            }
        }catch (Exception e) {
            logger.error("get between date error", e);
        }

        return betweenList;
    }

}
