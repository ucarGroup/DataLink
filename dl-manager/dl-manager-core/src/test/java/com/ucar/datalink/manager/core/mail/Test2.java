package com.ucar.datalink.manager.core.mail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yang.wang09 on 2018-05-29 18:10.
 */
public class Test2 {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final long ONE_DAY_TIME_BY_MILLISECOND = 24 * 3600 * 1000;


    public static void main(String[] args) throws ParseException {
        Test2 t = new Test2();
        t.go();
    }


    public void go() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        Date d = format.parse("2018-05-29");
        long time = d.getTime() - ONE_DAY_TIME_BY_MILLISECOND;
        Date d2 = new Date(time);
        String str = format.format(d2);
        System.out.println(str);

    }

}
