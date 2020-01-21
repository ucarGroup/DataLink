package com.ucar.datalink.writer.rdbms.operator;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.event.bi.RdbmsCountEvent;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.vo.RdbmsOperatorVO;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;

/**
 * Created by user on 2018/4/2.
 */
public class RdbmsOperatorUtil {

    public static void count(RdbmsCountEvent event) {
        try {
            long id = event.getMediaSourceId();
            String sql = event.getSql();
            MediaSourceService service = DataLinkFactory.getObject(MediaSourceService.class);
            MediaSourceInfo info = service.getById(id);
            DbDialect dialect = DbDialectFactory.getDbDialect(info);
            long result = dialect.getJdbcTemplate().queryForObject(sql,Long.class);
            event.getCallback().onCompletion(null, ""+result);
        } catch(Exception e) {
            event.getCallback().onCompletion(e, "-1");
        }
    }

    public static void main(String[] args) {
        //new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        String sql = "SELECT   COUNT(id)   FROM t_b_city";
        long id = 10;

        //id = 65;
        //sql = "SELECT count(*) FROM news";
        //go(id,sql);

        RdbmsOperatorVO vo = new RdbmsOperatorVO();
        vo.setMediaSourceId(id);
        vo.setSql(sql);
        String json = JSONObject.toJSONString(vo);
        System.out.println(json);

    }


}
