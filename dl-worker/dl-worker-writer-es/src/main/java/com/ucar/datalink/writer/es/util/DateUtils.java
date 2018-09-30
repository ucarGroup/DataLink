package com.ucar.datalink.writer.es.util;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * @author 和超逸 (cy.he@zuche.com)
 * @since 2016年8月10日 下午2:53:50
 */
public abstract class DateUtils {

    public static Date parse(String time) {
        if (time == null || time.equals("0000-00-00 00:00:00.000"))
            return null;
        return DateTime.parse(time.trim().replace(' ', 'T')).toDate();
    }

    public static String format(Date time) {
        return format(time, "yyyy-MM-dd HH:mm:ss");
    }

    public static String format(Date time, String pattern) {
        if (time == null)
            return null;
        return new DateTime(time).toString(pattern);
    }

}
