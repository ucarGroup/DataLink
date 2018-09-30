package com.ucar.datalink.biz.mapping;

import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MetaMappingInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by user on 2017/6/26.
 */
public class ElasticSearchMapping extends AbstractMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchMapping.class);

    /**
     * 将ES的一列转换成关系数据库中的列
     */
    private static final Map<String,String> toRDBMS = new ConcurrentHashMap<>();

    private static final Map<String,String> toHDFS = new ConcurrentHashMap<>();

    static {
        /**
         * toRDBMS是一个映射关系，key是ElasticSearch中的列类型，value是关系型数据库中的列类型
         */
        toRDBMS.put("long","bigint");
        toRDBMS.put("integer","int");
        toRDBMS.put("string","varchar");
        toRDBMS.put("double","decimal");
        toRDBMS.put("date","timestamp");

        toHDFS.put("integer", "Long");
        toHDFS.put("long", "Long");
        toHDFS.put("string","String");
        toHDFS.put("double","Double");
        toHDFS.put("date","Date");

    }


    @Override
    public void processMetaMapping(MetaMappingInfo info) {
        if(MediaSourceType.MYSQL.name().toUpperCase().equals(info.getTargetMediaSourceType()) || MediaSourceType.SQLSERVER.name().toUpperCase().equals(info.getTargetMediaSourceType()) || MediaSourceType.POSTGRESQL.name().toUpperCase().equals(info.getTargetMediaSourceType()) ) {
            toRDBMS.put(info.getSrcMappingType(), info.getTargetMappingType());
        }
        if("RDBMS".equals(info.getTargetMediaSourceType())) {
            toRDBMS.put(info.getSrcMappingType(), info.getTargetMappingType());
        }
        if(MediaSourceType.HDFS.name().toUpperCase().equals(info.getTargetMediaSourceType())) {
            toHDFS.put(info.getSrcMappingType(), info.getTargetMappingType());
        }
    }

    public ColumnMeta toRDBMS(ColumnMeta meta) {
        check(meta);
        String filterName = filterVerticalLine(meta.getName());
        meta.setName(filterName);
        String name = meta.getType().toLowerCase();
        String type = toRDBMS.get(name);
        if(type == null) {
            LOGGER.error("unsupport transform "+name);
            return createEmtpyColumnMeta(meta.getName());
        }
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        target.setType(type);
        return target;
    }

    public ColumnMeta toES(ColumnMeta meta) {
        check(meta);
        String filterName = filterVerticalLine(meta.getName());
        meta.setName(filterName);
        return cloneColumnMeta(meta);
    }

    public ColumnMeta toHBase(ColumnMeta meta) {
        check(meta);
        String filterName = filterVerticalLine(meta.getName());
        meta.setName(filterName);
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        target.setType("Bytes");
        target.setLength(0);
        target.setDecimalDigits(0);
        return target;
    }

    public ColumnMeta toHDFS(ColumnMeta meta) {
        check(meta);
        String filterName = filterVerticalLine(meta.getName());
        meta.setName(filterName);
        String name = meta.getType().toLowerCase();
        String type = toHDFS.get(name);
        if(type == null) {
            LOGGER.error("unsupport transform "+name);
            return createEmtpyColumnMeta(meta.getName());
        }
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        target.setType(type);
        return target;
    }

    private static String filterVerticalLine(String content) {
        if(StringUtils.isBlank(content)) {
            return content;
        }
        if(content.contains("|")) {
            String[] str = content.split("\\|");
            return str[1];
        }
        return content;
    }


}
