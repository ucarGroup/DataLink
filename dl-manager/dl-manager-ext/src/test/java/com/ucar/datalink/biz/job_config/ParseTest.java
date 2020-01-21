package com.ucar.datalink.biz.job_config;

import com.ucar.datalink.biz.job.JobConfigBuilder;
import com.ucar.datalink.biz.module.JobExtendProperty;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by user on 2017/7/19.
 */
public class ParseTest {

    public static void main(String[] args) {

        ParseTest t = new ParseTest();
        //t.go();
        //String x = DbConfigEncryption.encrypt("123456");
        //System.out.println(x);
//        StringBuilder sb = new StringBuilder();
//        sb.append("aa,bb,cc,dd,ee,");
//        if(sb.length() > 0) {
//            sb = sb.deleteCharAt(sb.length()-1);
//        }
//        System.out.println(sb.toString());

        t.go();
    }


    public void go() {
        long srcID = 87;
        long destID = 88;

        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        MediaSourceService mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        MediaSourceInfo srcInfo = mediaSourceService.getById( srcID );
        MediaSourceInfo destInfo = mediaSourceService.getById( destID );

        try {
            String mediaName = "aa";
//            List<ColumnMeta> src = MetaManager.getColumns(srcInfo, mediaName);
//            List<ColumnMeta> dest = MetaManager.getColumns(destInfo,mediaName);

            //DataxJobConfig srcConfig = DataxJobConfigBuilder.assembleReaderConfig(srcInfo, mediaName);
            //DataxJobConfig destConfig = DataxJobConfigBuilder.assembleWriterConfig(srcConfig,destInfo,"bb");
//            srcConfig.setTableName(mediaName);
//            destConfig.setTableName(mediaName);

            //String result = DataxJobConfigBuilder.buildJobJson(srcConfig, destConfig);
            String result = JobConfigBuilder.buildJson(srcInfo,mediaName,destInfo,mediaName,new JobExtendProperty());
            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}
