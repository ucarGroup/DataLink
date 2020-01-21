package com.ucar.datalink.writer.sddl.util;

import java.lang.reflect.Field;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 03/11/2017.
 */
public class PropertyCheckUtil {

    /**
     * @Description: 对象是否不含有空属性
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 4:59 PM 03/11/2017
     */
    public static boolean hasNullProperty(Object obj) {
        if (obj == null) {
            return true;
        }

        boolean hasNull = false;

        try {
            for (Field f : obj.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                    if (f.get(obj) == null) {
                        hasNull = true;
                        break;
                    }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            hasNull = true;
        }

        return hasNull;
    }
}
