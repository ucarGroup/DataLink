package com.ucar.datalink.biz.mapping;

import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MetaMappingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by user on 2017/6/26.
 */
public class RDBMSMapping extends AbstractMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSMapping.class);

    /**
     * 将关系型数据库的一个列转换成HDFS(上层是提供给hive使用的)
     */
    private static final Map<String, String> toHDFS = new ConcurrentHashMap<>();

    /**
     * 将关系型数据库的一个列转换成ES
     */
    private static final Map<String,String> toES = new ConcurrentHashMap<>();


    static {
        /**
         * toHDFS是一个映射关系表，key是关系型数据库中的列类型，value对应的是HDFS(hive)中的列类型
         */
        toHDFS.put("date", "date");
        toHDFS.put("datetime", "timestamp");
        toHDFS.put("bigint", "bigint");
        toHDFS.put("tinyint", "smallint");
        toHDFS.put("smallint", "int");
        toHDFS.put("int", "int");
        toHDFS.put("char", "string");
        toHDFS.put("float", "float");
        toHDFS.put("double", "double");
        toHDFS.put("text", "string");
        toHDFS.put("clob", "string");
        toHDFS.put("blob", "binary");
        toHDFS.put("decimal", "decimal");
        toHDFS.put("bigint identity", "bigint");
        toHDFS.put("bigint unsigned", "bigint");
        toHDFS.put("int identity", "int");
        toHDFS.put("nvarchar", "string");
        toHDFS.put("varchar", "string");
        toHDFS.put("tinyint unsigned", "smallint");
        toHDFS.put("tinytext", "string");
        toHDFS.put("timestamp", "timestamp");
        toHDFS.put("bit","smallint");
        toHDFS.put("int unsigned","int");
        toHDFS.put("mediumint","bigint");
        toHDFS.put("time","timestamp");
        toHDFS.put("mediumtext","string");
        toHDFS.put("integer","int");
        toHDFS.put("longvarchar","string");
        toHDFS.put("int4","int");
        toHDFS.put("real","decimal");
        toHDFS.put("numeric","string");
        toHDFS.put("smallint identity","smallint");

        /**
         * toES是一个映射关系表，key是关系型数据库中的列类型，value是ElasticSearch中的列类型
         */
        toES.put("tinyint", "integer");
        toES.put("smallint", "integer");
        toES.put("int", "integer");
        toES.put("bigint","long");
        toES.put("tinyint","integer");
        toES.put("smallint","integer");
        toES.put("int","integer");
        toES.put("bigint identity", "long");
        toES.put("bigint unsigned", "long");
        toES.put("int identity", "integer");
        toES.put("varchar","string");
        toES.put("char","string");
        toES.put("text","string");
        toES.put("clob", "string");
        toES.put("blob", "string");
        toES.put("decimal","double");
        toES.put("float", "double");
        toES.put("double","double");
        toES.put("datetime","date");
        toES.put("timestamp","date");
        toES.put("date", "date");
        toES.put("mediumtext","string");
        toES.put("integer", "integer");
        toES.put("longvarchar","string");
        toES.put("datetime2","date");
        toES.put("nvarchar","string");
        toES.put("int4","long");
        toES.put("bit","integer");
    }


    @Override
    public void processMetaMapping(MetaMappingInfo info) {
        if(MediaSourceType.HDFS.name().toUpperCase().equals(info.getTargetMediaSourceType())) {
            toHDFS.put(info.getSrcMappingType(), info.getTargetMappingType());
        }
        else if(MediaSourceType.ELASTICSEARCH.name().toUpperCase().equals(info.getTargetMediaSourceType())) {
            toES.put(info.getSrcMappingType(), info.getTargetMappingType());
        }
    }

    @Override
    public ColumnMeta toRDBMS(ColumnMeta meta) {
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        return target;
    }

    @Override
    public ColumnMeta toES(ColumnMeta meta) {
        check(meta);
        String name = meta.getType().toLowerCase();
        String type = toES.get(name);
        if(type == null) {
            LOGGER.error("unsupport transform "+name+" (to ES)");
            return createEmtpyColumnMeta(meta.getName());
        }
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        target.setType(type);
        return target;
    }

    @Override
    public ColumnMeta toHBase(ColumnMeta meta) {
        check(meta);
        ColumnMeta target = cloneColumnMeta(meta);
        target.setType("Bytes");
        return target;
    }

    @Override
    public ColumnMeta toHDFS(ColumnMeta meta) {
        check(meta);
        String name = meta.getType().toLowerCase();
        String type = toHDFS.get(name);
        if(type == null) {
            LOGGER.error("unsupport transform "+name+" (to HDFS)");
            return createEmtpyColumnMeta(meta.getName());
        }
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        target.setType(type);
        return target;
    }

}
