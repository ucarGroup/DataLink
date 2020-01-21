package com.ucar.datalink.biz.helper;

import com.google.common.collect.Lists;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;

/**
 * Created by user on 2017/4/27.
 */
public class TestHelper {

    public static MediaSourceInfo buildMediaSource() {
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
        return mediaSourceInfo;
    }
}
