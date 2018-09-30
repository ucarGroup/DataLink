package com.ucar.datalink.worker.core.util;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaShellUtil {

    /**
     * @Description: 执行shell
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 6:15 PM 02/02/2018
     * @param       shellCommand:"sh /tmp/sendKondorFile.sh"
     */
    public static String executeShell(String shellCommand) throws Exception {
        if (StringUtils.isEmpty(shellCommand))
            throw new IllegalArgumentException("executeShell : input shellCommand can not null!");

        StringBuffer stringBuffer = new StringBuffer("\r\n");
        BufferedReader bufferedReader = null;
        BufferedReader bufferedReaderError = null;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS ");
        try {
            stringBuffer.append(dateFormat.format(new Date()));

            Process pid = null;
            String[] cmd = {"/bin/sh", "-c", shellCommand};
            pid = Runtime.getRuntime().exec(cmd);

            if (pid != null) {
                stringBuffer.append("PID：").append(pid.toString()).append("\r\n");
                //bufferedReader用于读取Shell的输出内容
                bufferedReader = new BufferedReader(new InputStreamReader(pid.getInputStream()), 1024);
                bufferedReaderError = new BufferedReader(new InputStreamReader(pid.getErrorStream()), 1024);
            } else {
                stringBuffer.append("没有pid \r\n");
            }
            pid.waitFor();

            if (bufferedReader != null && bufferedReaderError != null) {
                stringBuffer.append(dateFormat.format(new Date())).append("Shell命令执行完毕!\n");
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line).append("\n");
                }


                while ((line = bufferedReaderError.readLine()) != null) {
                    stringBuffer.append("error info: ").append(line).append("\n");
                }
            }

        } catch (Exception ioe) {
            stringBuffer.append("执行Shell命令时发生异常：\n").append(ioe.getMessage()).append("\n");
        } finally {
            if (bufferedReader != null && bufferedReaderError != null) {
                bufferedReader.close();
                bufferedReaderError.close();

            }
        }
        return stringBuffer.toString();
    }
}
