package com.ucar.datalink.biz.hdfs;

import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.meta.HDFSUtil;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.hdfs.HDFSMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * Created by user on 2017/7/7.
 */
public class HDFSTest {



    public static void main(String[] args) {
        HDFSTest t = new HDFSTest();
        t.tables();
        //t.columns();

//        String name = "decimal(9527,13)";
//        if(name.startsWith("decimal")) {
//            String decimalDigits = name.substring(8,name.length()-1);
//            String[] arr = decimalDigits.split(",");
//            System.out.println("长度->"+arr[0]);
//            System.out.println("精度->"+arr[1]);
//        }

    }


    public void columns() {
//        String url = "http://sparkcubetest.10101111.com/sparkcube/schema/table/detailToSync?database=bi_dw&tableName=tracecoreindex";
//        String json = URLConnectionUtil.retryGET(url);
//        List<ColumnMeta> columns = HDFSUtil.parseColumnsJson(json);
//        for(ColumnMeta c : columns) {
//            System.out.println(c);
//        }
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        MediaDAO dao = DataLinkFactory.getObject(MediaDAO.class);
        MediaSourceInfo info = dao.findMediaSourceById(55L);
        List<ColumnMeta> columns = HDFSUtil.getColumns(info,"uc.t_distributor_settlement");
        for(ColumnMeta c : columns) {
            System.out.println(c);
        }
    }

    public void tables() {
//        String json = URLConnectionUtil.retryGET(url);
//        List<MediaMeta> tables = HDFSUtil.parseTablesJson(json);
//        for(MediaMeta t : tables) {
//            System.out.println(t.toString());
//        }

        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        MediaDAO dao = DataLinkFactory.getObject(MediaDAO.class);
        MediaSourceInfo info = dao.findMediaSourceById(89L);
        List<MediaMeta> tables = HDFSUtil.getTables(info);
        for(MediaMeta t : tables) {
            System.out.println(t);
        }

    }


}
