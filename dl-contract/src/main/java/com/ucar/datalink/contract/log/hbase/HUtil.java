package com.ucar.datalink.contract.log.hbase;

import java.nio.charset.Charset;

/**
 * Created by lubiao on 2017/11/15.
 */
public class HUtil {

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public static String toString(byte[] b) {
        return b == null?null:toString(b, 0, b.length);
    }

    public static String toString(byte[] b, int off, int len) {
        return b == null?null:(len == 0?"":new String(b, off, len, UTF8_CHARSET));
    }

    public static String toStringBinary(byte[] b) {
        return b == null ? "null" : toStringBinary(b, 0, b.length);
    }

    public static String toStringBinary(byte[] b, int off, int len) {
        StringBuilder result = new StringBuilder();
        if (off >= b.length) {
            return result.toString();
        } else {
            if (off + len > b.length) {
                len = b.length - off;
            }

            for (int i = off; i < off + len; ++i) {
                int ch = b[i] & 255;
                if ((ch < 48 || ch > 57) && (ch < 65 || ch > 90) && (ch < 97 || ch > 122) && " `~!@#$%^&*()-_=+[]{}|;:\'\",.<>/?".indexOf(ch) < 0) {
                    result.append(String.format("\\x%02X", new Object[]{Integer.valueOf(ch)}));
                } else {
                    result.append((char) ch);
                }
            }

            return result.toString();
        }
    }
}
