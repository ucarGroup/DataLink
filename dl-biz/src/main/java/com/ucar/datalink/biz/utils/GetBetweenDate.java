package com.ucar.datalink.biz.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by yang.wang09 on 2019-02-28 19:12.
 */
public class GetBetweenDate {

    public static void main(String[] args) {
        List<String> list = getBetweenDate("2019-01-28","2019-02-28");
        for(String date:list){
            System.out.println(date);
        }
    }

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
            e.printStackTrace();
        }

        return betweenList;
    }

}
