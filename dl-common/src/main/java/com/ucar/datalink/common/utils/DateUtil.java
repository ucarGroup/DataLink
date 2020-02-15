package com.ucar.datalink.common.utils;

import java.util.Calendar;

/**
 * created by swb on 2019/01/06
 */
public class DateUtil {

    /**
     * 判断是否是周六日
     * @return
     */
    public static boolean isWeekend(){
        Calendar c= Calendar.getInstance();
        int weekDay=c.get(Calendar.DAY_OF_WEEK);
        if(weekDay==Calendar.SUNDAY || weekDay==Calendar.SATURDAY ){
           return true;
        }
        return false;
    }


}
