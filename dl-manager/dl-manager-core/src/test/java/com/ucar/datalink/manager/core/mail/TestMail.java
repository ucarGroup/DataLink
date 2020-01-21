package com.ucar.datalink.manager.core.mail;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.MetaManager;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import org.I0Itec.zkclient.DataUpdater;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2017/8/23.
 */
public class TestMail {

    public static void main(String[] args) throws Exception {
        TestMail t = new TestMail();
        //t.go();
        //t.testZK();
        t.xxx();
    }

    public void xxx() {
        Map<String,String> map = new HashMap();
        map.put("aa","123");
        map.put("bb","555555");
        map.put("cc","77777");
        map.put("dd","99999");

        String x = JSONObject.toJSONString(map);
        System.out.println(x);

        Map<String,String> mm = JSONObject.parseObject(x,Map.class);
        System.out.println("map --> "+mm.toString());

    }


    public void testZK() {
        final String content = "{\"ip\":\"10.204.247.67\",\"jobId\":1573," +
                "\"jobName\":\"a4115_custom_business_new_201611\",\"jobQueueExecutionId\":-1,\"pid\":2718}";
        String path = "/datax/admin/jobs/running/t_scd_driver_h7Fdwzrc3R";
        ZkClient client = new ZkClient("10.99.40.97:2181", 10*1000, 10*1000);
        client.updateDataSerialized(path, new DataUpdater<Object>() {
            @Override
            public Object update(Object currentData) {
                return content.getBytes();
            }
        });
        System.out.println("ok~");
    }


    public static String send(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, String names) throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append("hi:").append("<br>").append("<br>");
        buf.append("&nbsp &nbsp &nbsp").append("您有个数据同步任务").append("从").append(srcInfo.getName()).append("(").append(srcInfo.getParameterObj().getMediaSourceType().name()).append(")  ")
                .append("同步到").append(destInfo.getName()).append("(").append(destInfo.getParameterObj().getMediaSourceType().name()).append(")  ").append(",请查看").append("<br>");

        if(StringUtils.isNotBlank(names)) {
            String[] tables = names.split(",");
            for(String t : tables) {
                buf.append("").append("<br>");
                buf.append("表名称:").append(t).append("<br>").append("").append("<br>");
                if(MediaSourceType.HBASE == srcInfo.getType()){
                    buf.append("<table border='1'>").append("<tr><td>列族名称</td><td>字段名称</td></tr>");
                }else{
                    buf.append("<table border='1'>").append("<tr><td>字段名称</td><td>字段类型</td><td>字段描述</td></tr>");
                }
                List<ColumnMeta> columns = MetaManager.getColumns(srcInfo,t);
                if(columns!=null && columns.size()>0) {
                    for(ColumnMeta cm : columns) {
                        if(MediaSourceType.HBASE == srcInfo.getType()) {
                            buf.append("<tr><td>").append(cm.getColumnFamily()).append("</td>").append("<td>").append(cm.getName()).append("</td></tr>");
                        }
                        else {
                            String columnDesc = "";
                            if(StringUtils.isNotBlank(cm.getColumnDesc())) {
                                columnDesc = cm.getColumnDesc();
                            }
                            buf.append("<tr><td>").append(cm.getName()).append("</td>").append("<td>").append(cm.getType()).append("</td>").append("<td>").append(columnDesc).append("</td></tr>");
                        }
                    }
                }

                buf.append("</table>");
                buf.append("<br/><br/>");
            }//end for
        }
        return buf.toString();
    }








}
