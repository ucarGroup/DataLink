package com.ucar.datalink.flinker.core.admin.util;

import java.util.*;

/**
 * Created by yang.wang09 on 2018-05-28 13:59.
 */
public class ErrorRecord {

    private static final List<Throwable> ERR = new ArrayList<Throwable>();

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final int MAX_ERROR_LENGTH = 65535;


    public static void addError(Throwable t) {
        ERR.add(t);
    }

    public static void addError(String msg) {
        ERR.add(new RuntimeException(msg));
    }

    public List<Throwable> getErrs() {
        return ERR;
    }

    public static boolean hasErr() {
        return (ERR.size()>0) ? true : false;
    }

    public static String assembleError() {
        StringBuilder sb = new StringBuilder();
        for(Throwable t : ERR) {
            sb.append( getThrowableMessage(t) );
        }
        return sb.toString();
    }

    public static String assembleError(String msg, Throwable t) {
        ERR.add(t);
        return assembleError(msg);
    }

    public static String assembleError(String previousError) {
        StringBuilder sb = new StringBuilder();
        sb.append(previousError);
        sb.append("==============================");
        sb.append(LINE_SEPARATOR);
        sb.append( assembleError() );
        String msg = sb.toString();
        byte[] buf = msg.getBytes();
        if( buf.length > MAX_ERROR_LENGTH ) {
            byte[] tmp = new byte[MAX_ERROR_LENGTH-5];
            System.arraycopy(buf,0,tmp,0,MAX_ERROR_LENGTH-5);
            buf = tmp;
            msg = new String(buf);
        }
        return msg;
    }


    private static String getThrowableMessage(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getMessage());
        sb.append(LINE_SEPARATOR);
        StackTraceElement[] ste = t.getStackTrace();
        for(StackTraceElement s : ste) {
            sb.append(s.toString());
            sb.append(LINE_SEPARATOR);
        }
        sb.append("==============================");
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }


}
