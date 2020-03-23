package com.ucar.datalink.manager.core.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * md5工具类
 *
 * @author wenbin.song
 * @date 2019/03/06
 */
public class Md5Util {
    private static final Logger logger = LoggerFactory.getLogger(Md5Util.class);

    /**
     * 用于获取一个String的md5值
     *
     * @param str
     * @return
     */
    public static String getMd5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(str.getBytes());
            StringBuilder sb = new StringBuilder(40);
            for (byte x : bs) {
                if ((x & 0xff) >> 4 == 0) {
                    sb.append("0").append(Integer.toHexString(x & 0xff));
                } else {
                    sb.append(Integer.toHexString(x & 0xff));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }

}
