package com.ucar.datalink.manager.core.web.util;

import java.util.regex.Pattern;
/**
 * 数字工具类
 *
 * @author wenbin.song
 * @date 2019/03/12
 */
public class NumberUtil {
    private static final String NUMBER_REG = "^[-+]?[\\d]*$";

    /**
     * 判断字符串是否为整数
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile(NUMBER_REG);
        return pattern.matcher(str).matches();
    }

}
