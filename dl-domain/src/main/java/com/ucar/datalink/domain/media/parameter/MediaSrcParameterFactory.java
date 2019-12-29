package com.ucar.datalink.domain.media.parameter;

import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;

/**
 * Created by user on 2017/3/15.
 */
public class MediaSrcParameterFactory {

    @SuppressWarnings("unchecked")
    public static <T extends MediaSrcParameter> T create(MediaSourceType sourceType) {
        if (sourceType == MediaSourceType.MYSQL) {
            RdbMediaSrcParameter parameter = new RdbMediaSrcParameter();
            parameter.setDriver("com.mysql.jdbc.Driver");
            parameter.setPort(3306);
            parameter.setMediaSourceType(MediaSourceType.MYSQL);
            parameter.setDataSourceConfig(new BasicDataSourceConfig());
            return (T) parameter;
        } else if (sourceType == MediaSourceType.SQLSERVER) {
            RdbMediaSrcParameter parameter = new RdbMediaSrcParameter();
            parameter.setDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            parameter.setPort(1433);
            parameter.setMediaSourceType(MediaSourceType.SQLSERVER);
            parameter.setDataSourceConfig(new BasicDataSourceConfig());
            return (T) parameter;
        }else if(sourceType == MediaSourceType.POSTGRESQL){
            RdbMediaSrcParameter parameter = new RdbMediaSrcParameter();
            parameter.setDriver("org.postgresql.Driver");
            parameter.setPort(5432);
            parameter.setMediaSourceType(MediaSourceType.POSTGRESQL);
            parameter.setDataSourceConfig(new BasicDataSourceConfig());
            return (T) parameter;
        }
        return null;
    }
}
