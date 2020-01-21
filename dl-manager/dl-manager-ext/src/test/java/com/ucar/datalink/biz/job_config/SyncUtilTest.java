package com.ucar.datalink.biz.job_config;

import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.util.SyncUtil;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SyncUtilTest {

    //@Test
    public void get() throws Exception{
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobConfigInfo jobConfigInfo = new JobConfigInfo();
        jobConfigInfo.setJob_target_media_source_id(112);
        jobConfigInfo.setJob_media_name("t_dl_my_user");
        SyncUtil.sendCreateJobInfoToCDSE(jobConfigInfo);
    }

    public static void go3() {
        String url = "jdbc:mysql://10.212.17.5:3306/ucar_order";
        String [] ss = url.split("/");
        System.out.println(ss.toString());
    }

    public static void go(String json) {
        json += "!!!";
    }

    public static void go2(StringBuilder sb) {
        sb.append("!!!");
    }

    public static void main(String[] args) {
        String json = "xxxx";
        go(json);
        System.out.println(json);
        StringBuilder sb = new StringBuilder();
        sb.append("aaaaaa");
        go2(sb);
        System.out.println(sb.toString());
        go3();
    }
}
