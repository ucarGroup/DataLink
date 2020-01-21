package com.ucar.datalink.biz.job;

import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.job.JobConfigInfo;
import org.apache.commons.io.FileUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by user on 2017/7/13.
 */
public class JobConfigTest {


    public static void main(String[] args)throws Exception {
        JobConfigTest t = new JobConfigTest();
        //t.selectById();
        //t.insert();
        //t.delete();
        //t.count();;
        //t.update();
        //t.pagingQuery();
        //t.go();
        t.queryByApplyID();
    }


    public void queryByApplyID() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService servivce = DataLinkFactory.getObject(JobService.class);
        List<JobConfigInfo> list = servivce.queryJobConfigDBTypeByPaging("MYSQL",
                "MYSQL","test1","tset2","xx-table",
                null,44,0,"all",0,"","");
        for(JobConfigInfo info : list) {
            System.out.println(info);
        }
    }

    public void go() {
        String[] s = new String[3];
        s[0] = "1111";
        s[1] = "222";
        s[2] = "333";
        String x = java.util.Arrays.toString(s);
        if(x.contains("[")) {
            x = x.replaceAll("\\[","");
        }
        if(x.contains("]")) {
            x = x.replaceAll("\\]","");
        }
        System.out.println(x);

        System.out.println("=================");
        String name = "demo";
        String[] names = name.split("\\.");
        if(names.length == 2) {
            System.out.println(names[1]);
        }
        int a=1,b=2;
        a=22;b=44;

    }


    public void pagingQuery() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService servivce = DataLinkFactory.getObject(JobService.class);
        List<JobConfigInfo> list = servivce.queryJobConfigsByPaging(0, 3);
        for(JobConfigInfo info : list) {
            System.out.println(info);
        }

    }



    public void update() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService servivce = DataLinkFactory.getObject(JobService.class);
        JobConfigInfo info = new JobConfigInfo();
        info.setId(2);
        info.setJob_desc("7月14日周五!!!");
        servivce.modifyJobConfig(info);
        System.out.println("更新ok~");
    }

    public void count() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService servivce = DataLinkFactory.getObject(JobService.class);
        int x = servivce.getJobConfigCount();
        System.out.println(x);
    }


    public void delete() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService servivce = DataLinkFactory.getObject(JobService.class);
        servivce.deleteJobConfigById(2);
    }

    public void insert() throws IOException {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService servivce = DataLinkFactory.getObject(JobService.class);
        //JobConfigInfo info = servivce.getJobConfigById(1);
        //System.out.println(info);

        String content = FileUtils.readFileToString( new File("E:\\open-source\\datax\\job\\test") );

        JobConfigInfo info = new JobConfigInfo();
        info.setJob_name("job_test_2");
        info.setIs_delete(false);
        info.setJob_content(content);
        info.setJob_desc("第二个job任务");
        info.setJob_media_name("HDFS");
        info.setJob_src_media_source_id(33);
        info.setJob_target_media_source_id(34);
        info.setTiming_expression("");
        info.setTiming_on_yn(false);
        info.setTiming_yn(false);
        info.setTiming_parameter("");
        info.setTiming_target_worker("");
        info.setTiming_transfer_type(JobConfigInfo.TIMING_TRANSFER_TYPE_FULL);

        servivce.createJobConfig(info);
        System.out.println("创建ok~");

    }

    public void selectById() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService servivce = DataLinkFactory.getObject(JobService.class);
        JobConfigInfo info = servivce.getJobConfigById(1);

        System.out.println(info);

    }
}
