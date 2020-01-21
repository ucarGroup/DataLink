package com.ucar.datalink.biz;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.utils.DbConfigEncryption;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * Created by user on 2017/9/5.
 */
public class GreenTest {

    public static void main(String[] args) {
        GreenTest t = new GreenTest();
        t.go();
    }


    public void go2() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        MediaSourceService service = DataLinkFactory.getObject(MediaSourceService.class);
        MediaSourceInfo info = service.getById(102L);
//        List<MediaMeta> list = GreenPlumUtil.getTables(info);
//        for(MediaMeta m : list) {
//            System.out.println(m);
//        }

        /*List<ColumnMeta> list = GreenPlumUtil.getColumns(info,"registration");
        for(ColumnMeta cm : list) {
            System.out.println(cm);
        }*/
    }

    public void go() {
        String password = "aUDxDuU#H8ff";
        String x = DbConfigEncryption.encrypt(password);
        System.out.println(x);
    }


}
