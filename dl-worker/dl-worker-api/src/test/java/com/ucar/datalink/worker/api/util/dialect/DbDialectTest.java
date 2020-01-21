package com.ucar.datalink.worker.api.util.dialect;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;
import org.junit.Test;

import java.util.Map;

/**
 * Created by user on 2017/4/26.
 */
public class DbDialectTest {

    @Test
    public void testByte(){
        System.out.println(Byte.MAX_VALUE);
    }

    @Test
    public void testSplit() {
        String s = "1.2242342";
        System.out.println(s.split(".").length);
        /*String s = "a,b,c";
        String[] array = s.split(",");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }*/

    }

    @Test
    public void testJson() {
        String s = "{}";
        Object obj = JSONObject.parseObject(s, Object.class);
        System.out.println(obj.toString());
    }

    @Test
    public void testfindTable() {
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        mediaSourceInfo.setId(1L);
        mediaSourceInfo.setType(MediaSourceType.MYSQL);
        mediaSourceInfo.setName("ucar_datalink");
        RdbMediaSrcParameter parameter = new RdbMediaSrcParameter();
        parameter.setPort(3306);
        parameter.setName("ucar_datalink");
        parameter.setNamespace("ucar_datalink");
        parameter.setDriver("com.mysql.jdbc.Driver");
        parameter.setEncoding("UTF-8");
        parameter.setMediaSourceType(MediaSourceType.MYSQL);
        parameter.setDataSourceConfig(new BasicDataSourceConfig());
        RdbMediaSrcParameter.WriteConfig writeConfig = new RdbMediaSrcParameter.WriteConfig();
        writeConfig.setWriteHost("10.104.20.123");
        writeConfig.setUsername("ucar_dev_soa");
        writeConfig.setEncryptPassword("ucar_dev_soa");
        parameter.setWriteConfig(writeConfig);

        RdbMediaSrcParameter.ReadConfig readConfig = new RdbMediaSrcParameter.ReadConfig();
        readConfig.setHosts(Lists.newArrayList("10.104.20.123"));
        readConfig.setUsername("ucar_dev_soa");
        readConfig.setEncryptPassword("ucar_dev_soa");
        parameter.setReadConfig(readConfig);

        mediaSourceInfo.setParameter(parameter.toJsonString());
        DbDialect dbDialect = DbDialectFactory.getDbDialect(mediaSourceInfo);
        Table table = dbDialect.findTable("ucar_datalink", "t_dl_media");
        Index[] indexes = table.getIndices();
        if (indexes != null && indexes.length > 0) {
            for (Index index : indexes) {
                if (index instanceof UniqueIndex) {
                    System.out.println("Index-------------" + index.getName());
                    for (IndexColumn indexColumn : index.getColumns()) {
                        System.out.println("IndexColumn-------------" + indexColumn.getName());
                    }
                } else {
                    System.out.println("Index-------------" + index.getName());
                    for (IndexColumn indexColumn : index.getColumns()) {
                        System.out.println("IndexColumn-------------" + indexColumn.getName());
                    }
                }
            }
        }

        Map<String, Object> result = dbDialect.getJdbcTemplate().queryForMap("show create table t_dl_media_mapping");
        String sql = result.get("Create Table").toString();
        sql = StringUtils.substringAfter(sql, "(");
        sql = StringUtils.substringBeforeLast(sql, ")");
        String[] columns = StringUtils.split(sql, ",");
        for (String s : columns) {
            System.out.println(s);
        }
    }
}
