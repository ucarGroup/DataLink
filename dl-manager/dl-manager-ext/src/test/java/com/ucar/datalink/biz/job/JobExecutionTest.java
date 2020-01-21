package com.ucar.datalink.biz.job;

import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.JobExecutionMonitor;
import com.ucar.datalink.domain.job.JobExecutionState;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017/7/14.
 */
public class JobExecutionTest {


    public static void main(String[] args) {
        JobExecutionTest t = new JobExecutionTest();

        //t.insert();;
        //t.select();
        //t.update();
        //t.count();
        //t.pagingQuery();
        //t.pageQuery();
        try {
            t.monitor();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void monitor() throws ParseException {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService service = DataLinkFactory.getObject(JobService.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = sdf.parse("2018-08-21 15:00:00");
        Timestamp t = new Timestamp(d.getTime());
        List<JobExecutionMonitor> list = service.getAllFailureByMonitorCat(t);
        for(JobExecutionMonitor j : list) {
            System.out.println(j.toString());
        }
    }



    public void pagingQuery() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService service = DataLinkFactory.getObject(JobService.class);
        service.getJobExecutionCount();
        List<JobExecutionInfo> list = service.queryJobExecutionByPaging(0,10);
        for(JobExecutionInfo info : list) {
            System.out.println(info);
        }
    }


    public void pageQuery() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService service = DataLinkFactory.getObject(JobService.class);

        int jobExecutionTotal = service.getJobExecutionCount();
        Set<String> set = new HashSet<String>();
        set.add(JobExecutionState.ABANDONED);
        set.add(JobExecutionState.FAILED);
        set.add(JobExecutionState.KILLED);
        set.add(JobExecutionState.RUNNING);
        set.add(JobExecutionState.SUCCEEDED);
        set.add(JobExecutionState.UNEXECUTE);

//        List<JobExecutionInfo> list = service.queryJobExecutionStateByPaging(set, -1, 0, jobExecutionTotal);
//        for(JobExecutionInfo info : list) {
//            System.out.println(info);
//        }

    }


    public void count() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService service = DataLinkFactory.getObject(JobService.class);

        int count = service.getJobExecutionCount();
        System.out.println(count);
    }

    public void update() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService service = DataLinkFactory.getObject(JobService.class);

        JobExecutionInfo info = new JobExecutionInfo();
        info.setByte_speed_per_second(500*1024*1024);
        info.setEnd_time(new Timestamp(System.currentTimeMillis()));
        info.setException("none");
        info.setId(2);;
        info.setJob_id(2);
        info.setJob_queue_execution_id(1);
        info.setOriginal_configuration(".....");
        info.setPercentage(1.0);
        info.setPid(123456);
        info.setRecord_speed_per_second(122 * 1024 * 1024);
        info.setStart_time(new Timestamp(System.currentTimeMillis()));
        info.setState(JobExecutionState.KILLED);
        info.setTask_communication_info("!!!!!!!!!!!!!!!!!!!!");
        info.setTotal_error_records(0L);
        info.setTotal_record(1234567890);
        info.setWait_reader_time(1.22);
        info.setWait_writer_time(3.14);
        info.setWorker_address("123.1.2.3");
        info.setOriginal_configuration("!!!!!!!!!!!!!!!!!");

        service.modifyJobExecution(info);
    }


    public void select() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService service = DataLinkFactory.getObject(JobService.class);

        JobExecutionInfo info = service.getJobExecutionById(2);
        System.out.println(info);
    }

    public void insert() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobService service = DataLinkFactory.getObject(JobService.class);

        JobExecutionInfo info = new JobExecutionInfo();
        info.setByte_speed_per_second(133 * 1024);
        info.setEnd_time(new Timestamp(System.currentTimeMillis()));
        info.setException("");
        info.setJob_id(2);
        info.setJob_queue_execution_id(-1);
        info.setOriginal_configuration(".....");
        info.setPercentage(0.8);
        info.setPid(9527);
        info.setRecord_speed_per_second(200 * 1024);
        info.setStart_time(new Timestamp(System.currentTimeMillis()));
        info.setState(JobExecutionState.SUCCEEDED);
        info.setTask_communication_info("none");
        info.setTotal_error_records(123L);
        info.setTotal_record(1234599L);
        info.setWait_reader_time(2.22);
        info.setWait_writer_time(0.44);
        info.setWorker_address("10.1.2.3");

        service.createJobExecution(info);
        System.out.println("创建ok~");
    }



}
