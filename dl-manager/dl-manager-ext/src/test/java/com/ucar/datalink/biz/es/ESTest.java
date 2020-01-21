package com.ucar.datalink.biz.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ucar.datalink.biz.dal.JobRunQueueDAO;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.meta.ElasticSearchUtil;
import com.ucar.datalink.biz.service.JobRunQueueService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.domain.job.JobRunQueueInfo;
import com.ucar.datalink.domain.job.JobRunQueueState;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2017/7/4.
 */
public class ESTest {

    public static void main(String[] args) {

        ESTest t = new ESTest();
        //t.go2();
        //t.go3();
        //t.go4();
        //t.go5();
        //t.go6();
        //t.go7();
        //t.go8();
        t.go10();
    }

    public void go10() {
        String str = "11-234";
        String[] ss = str.split(",");
        System.out.println(ss.length);
    }

    public void go8() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobRunQueueService service = DataLinkFactory.getObject(JobRunQueueService.class);
        service.removeJobRunQueueInfo(4);
        System.out.println("删除ok~ ");
    }

    public void go7() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobRunQueueService service = DataLinkFactory.getObject(JobRunQueueService.class);
        JobRunQueueInfo info = new JobRunQueueInfo();
        info.setId(4);
        info.setCurrentPorcessId("11-142,12-143");
        info.setQueueState(JobRunQueueState.PROCESSING);
        //info.setSuccessCount(1);
        info.setSuccessList("11");
        service.modifyJobRunQueueInfo(info);
        System.out.println("modify ~ ");
    }

    public void go6() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobRunQueueService service = DataLinkFactory.getObject(JobRunQueueService.class);
        List<JobRunQueueInfo> list = service.getAllJobRunQueueInfo();
//        List<JobRunQueueInfo> list = service.getAllJobRunQueueInfoByState(JobRunQueueState.UNEXECUTE);
        for(JobRunQueueInfo i : list) {
            System.out.println(i.toString());
        }
//        JobRunQueueInfo info = service.getJobRunQueueInfoById(4);
//        System.out.println(info.toString());

    }


    public void go5() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        JobRunQueueService service = DataLinkFactory.getObject(JobRunQueueService.class);
        JobRunQueueInfo info = new JobRunQueueInfo();

        info.setJobIdList("1,2,4,5,9,10");
        info.setJobCount(6);
        service.createJobRunQueueInfo(info);

        info.setJobIdList("9527,9528,9529");
        info.setJobCount(3);
        service.createJobRunQueueInfo(info);

        info.setJobIdList("76,77,78,79");
        info.setJobCount(4);
        service.createJobRunQueueInfo(info);


        info.setJobIdList("10,11,12,13,14,15");
        info.setJobCount(6);
        //info.setQueueState(JobRunQueueState.HAS_FAILED);
        service.createJobRunQueueInfo(info);

        System.out.println("创建 ok~");
    }




    public void go4() {
        MediaSourceInfo info = new MediaSourceInfo();
//        info.getParameterObj();
        RdbMediaSrcParameter p = new RdbMediaSrcParameter();
        RdbMediaSrcParameter.ReadConfig r = new RdbMediaSrcParameter.ReadConfig();
        r.setEncryptPassword("ucar_deptest");
        r.setPassword("ucar_deptest");
        r.setEtlHost("10.104.105.156");
        List<String> li = new ArrayList<>();
        li.add("10.104.105.156");
        r.setHosts(li);
        r.setUsername("ucar_dep_w");
        p.setReadConfig(r);
        p.setNamespace("uardb");

        RdbMediaSrcParameter.WriteConfig w = new RdbMediaSrcParameter.WriteConfig();
        w.setEncryptPassword("ucar_deptest");
        w.setPassword("ucar_deptest");
        w.setWriteHost("10.104.105.156");
        w.setUsername("ucar_dep_w");
        p.setWriteConfig(w);

        String parmeter = JSONObject.toJSONString(p);
        info.setParameter(parmeter);

        //Object x = SqlServerUtil.getTables(info);
        //Object x = SqlServerUtil.xx(info);
        //System.out.println(x);

    }


    public void go3() {
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        MediaDAO dao = DataLinkFactory.getObject(MediaDAO.class);
        MediaSourceInfo info = dao.findMediaSourceById(53L);
        List<MediaMeta> tables = ElasticSearchUtil.getTables(info);
//        for(MediaMeta t : tables) {
//            System.out.println(t.iteratorColumns());
//        }

        List<ColumnMeta> columns = ElasticSearchUtil.getColumns(info,"fcar_asset_car_300.t_asset_car_series");
        for(ColumnMeta c : columns) {
            System.out.println(c);
        }
    }


    public void go2() {
        //ElasticSearchJsonBean bean = new ElasticSearchJsonBean();
        //bean.index_name = "db_news";
        //bean.mappings = new ElasticSearchJsonBean.Mappping();

        //ElasticSearchJsonBean.Type type = new ElasticSearchJsonBean.Type();
        //type.type = "long";
        //bean.type = type;

        //String x = JSONObject.toJSONString(bean);
        //System.out.println(x);

        String url = "http://10.104.90.176:9200/db_news/_mapping?pretty";
        String json = URLConnectionUtil.getWithAuth(url,"","");
        System.out.println("有序遍历结果：");
        LinkedHashMap<String, String> jsonMap = JSON.parseObject(json, new TypeReference<LinkedHashMap<String, String>>() {
        });
        List<MediaMeta> tables = new ArrayList<>();
        for (Map.Entry<String, String> index : jsonMap.entrySet()) {
            //这里拿到的是index的名字，我们已经知道index名字了，所以继续遍历

            LinkedHashMap<String, String> mapping = JSON.parseObject(index.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
            });
            for(Map.Entry<String,String> mapping_content : mapping.entrySet()) {
                //这里拿到的e.getKey() 是mappings，这个值不需要继续遍历

                LinkedHashMap<String, String> types_info = JSON.parseObject(mapping_content.getValue(), new TypeReference<LinkedHashMap<String, String>>() {});
                for(Map.Entry<String,String> type_content : types_info.entrySet()) {
                    //这里拿到的key就是type的名字，value是type的所有属性，继续遍历getValue()获取type的属性
                    MediaMeta tm = new MediaMeta();
                    tm.setName(type_content.getKey());
                    tm.setDbType(MediaSourceType.ELASTICSEARCH);
                    List<ColumnMeta> columns = new ArrayList<>();

                    LinkedHashMap<String, String> properties = JSON.parseObject(type_content.getValue(), new TypeReference<LinkedHashMap<String, String>>() {});
                    for(Map.Entry<String,String> prop : properties.entrySet()) {
                        //这里的getKey()就是 properties，遍历value获取  {"author":{"type":"string"},"title":{"type":"string"},"content":{"type":"string"}}

                        LinkedHashMap<String, String> fields_info = JSON.parseObject(prop.getValue(), new TypeReference<LinkedHashMap<String, String>>() {});
                        for(Map.Entry<String,String> field_content : fields_info.entrySet()) {
                            //这里拿到的type_content.getKey()就是一个字段的名字，如上面的author，title
                            //value就是字段的类型，类型中可能会出现复杂类型，忽略这些复杂类型，只解析普通类型
                            ColumnMeta cm = new ColumnMeta();
                            String field_name = field_content.getKey();
                            cm.setName(field_name);

                            LinkedHashMap<String, String> field_types = JSON.parseObject(field_content.getValue(), new TypeReference<LinkedHashMap<String, String>>() {});
                            for(Map.Entry<String,String> field__type_info : field_types.entrySet()) {
                                //System.out.println("field info : "+field__type_info.getKey()+"  : "+field__type_info.getValue());
                                //这里解析出的 key-value可能是 "type" : "long" 这样的
                                //也可能是一个内嵌类型(名字是properties)，还有可能是 "index" : "not_analyzed"
                                //或者是  "format" : "strict_date_optional_time||epoch_millis"
                                //这里只需要解析type就可以了，type对应的value就是这个字段的类型，其他都忽略就可以
                                //将这个类型赋给 ColumnMeta，就完成了一个字段类型的解析
                                if("type".equals(field__type_info.getKey())) {
                                    cm.setType(field__type_info.getValue());
                                    break;
                                }
                            }
                            columns.add(cm);
                        }
                    }
                    tm.setColumn(columns);
                    tables.add(tm);
                }
            }// 完成JSON遍历

            for(MediaMeta t : tables) {
                System.out.println(t.iteratorColumns());
            }


        }


    }


    public void go() {

        String url = "http://10.104.90.176:9200/db_news/_mapping?pretty";

        String json = URLConnectionUtil.getWithAuth(url,"","");
        System.out.println(json);
        //StringBuilder sb = new StringBuilder();
        //String x = sb.toString().trim();
        //System.out.println(x);

    }


}
