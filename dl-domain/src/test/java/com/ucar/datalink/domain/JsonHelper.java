package com.ucar.datalink.domain;

import com.google.common.collect.Lists;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameterFactory;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderParameter;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;

/**
 * Created by user on 2017/3/16.
 */
public class JsonHelper {
    public static void main(String args[]) {
        //System.out.println(generateMysqlReaderParameter().toJsonString());
        //System.out.println(generateMysqlMediaSrc().toJsonString());
        //System.out.println(generateRdbmsTaskWriter().toJsonString());

        System.out.println(generateSqlServerMediaSrc().toJsonString());
    }

    private static MysqlReaderParameter generateMysqlReaderParameter() {
        MysqlReaderParameter parameter = new MysqlReaderParameter();
        parameter.setMediaSourceId(1L);
        //parameter.setFilteredEventTypes(Lists.newArrayList(EventType.INSERT));
        return parameter;
    }

    private static RdbMediaSrcParameter generateMysqlMediaSrc() {
        RdbMediaSrcParameter parameter = MediaSrcParameterFactory.create(MediaSourceType.MYSQL);
        parameter.setNamespace("ucar_datalink");

        RdbMediaSrcParameter.WriteConfig wc = new RdbMediaSrcParameter.WriteConfig();
        wc.setUsername("ucar_dev_soa");
        wc.setPassword("ucar_dev_soa");
        wc.setWriteHost("10.104.20.123");

        RdbMediaSrcParameter.ReadConfig readConfig = new RdbMediaSrcParameter.ReadConfig();
        readConfig.setUsername("ucar_dev_soa");
        readConfig.setPassword("ucar_dev_soa");
        readConfig.setHosts(Lists.newArrayList("10.104.20.123", "10.104.20.123"));

        parameter.setWriteConfig(wc);
        parameter.setReadConfig(readConfig);

        return parameter;
    }

    private static RdbMediaSrcParameter generateSqlServerMediaSrc() {
        RdbMediaSrcParameter parameter = MediaSrcParameterFactory.create(MediaSourceType.SQLSERVER);
        parameter.setNamespace("ucar_datalink_test");

        RdbMediaSrcParameter.WriteConfig wc = new RdbMediaSrcParameter.WriteConfig();
        wc.setUsername("ucar_datalink_test");
        wc.setPassword("ucardatalinktest");
        wc.setWriteHost("10.104.20.42");

        RdbMediaSrcParameter.ReadConfig readConfig = new RdbMediaSrcParameter.ReadConfig();
        readConfig.setUsername("ucar_datalink_test");
        readConfig.setPassword("ucardatalinktest");
        readConfig.setHosts(Lists.newArrayList("10.104.20.42", "10.104.20.42"));

        parameter.setWriteConfig(wc);
        parameter.setReadConfig(readConfig);

        return parameter;
    }

    private static RdbmsWriterParameter generateRdbmsTaskWriter() {
        return new RdbmsWriterParameter();
    }
}
