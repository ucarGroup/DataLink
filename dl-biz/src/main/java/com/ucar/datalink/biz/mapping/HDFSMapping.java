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
public class HDFSMapping extends AbstractMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSMapping.class);

    /**
     * 将HDFS(hive)中的一列转换成elastic search中的一列
     */
    private static final Map<String, String> toES = new ConcurrentHashMap<>();

    /**
     * 将HDFS(hive)中的一列转换成datax内部保存的一列
     */
    private static final Map<String,String> toDataX = new ConcurrentHashMap<String,String>();

    static {
        /**
         * 将HDFS中的一列转换成elastic search中的一列
         */
        toES.put("date","date");
        toES.put("timestamp","date");
        toES.put("bigint","long");
        toES.put("smallint","integer");
        toES.put("int","integer");
        toES.put("string","string");
        toES.put("float","double");
        toES.put("double","double");
        toES.put("decimal","string");

        /**
         * HDFS中的数据存在在ORC文件中，数据对应的类型
         */
        toDataX.put("tinyint","Long");
        toDataX.put("smallint","Long");
        toDataX.put("int","Long");
        toDataX.put("bigint","Long");
        toDataX.put("float","Double");
        toDataX.put("double","Double");
        toDataX.put("string","String");
        toDataX.put("boolean","Boolean");
        toDataX.put("date","Date");
        toDataX.put("timestamp","Date");
        toDataX.put("integer","Long");
        toDataX.put("varchar","String");
        toDataX.put("decimal","Double");

        toDataX.put("bigint identity", "Long");
        toDataX.put("bigint unsigned", "Long");
        toDataX.put("int identity", "Long");
        toDataX.put("char","String");
        toDataX.put("text","String");
        toDataX.put("clob", "String");
        toDataX.put("blob", "String");
        toDataX.put("float", "Double");
        toDataX.put("double","Double");
        toDataX.put("datetime","Date");
        toDataX.put("timestamp","Date");
        toDataX.put("date", "Date");
        toDataX.put("mediumtext","String");
        toDataX.put("integer", "Long");
        toDataX.put("longvarchar","String");
        toDataX.put("nvarchar","String");
        toDataX.put("datetime2","Date");
    }


    @Override
    public void processMetaMapping(MetaMappingInfo info) {
        if(MediaSourceType.ELASTICSEARCH.name().toUpperCase().equals(info.getTargetMediaSourceType())) {
            toES.put(info.getSrcMappingType(), info.getTargetMappingType());
        }
        if("DATAX".equalsIgnoreCase(info.getTargetMediaSourceType())) {
            toDataX.put(info.getSrcMappingType(), info.getTargetMappingType());
        }
    }


    @Override
    public ColumnMeta toDataX(ColumnMeta meta) {
        check(meta);
        String name = meta.getType().toLowerCase();
        String type = toDataX.get(name);
        if(type == null) {
            LOGGER.error("unsupport transform "+name+" (to toDataX)");
            return createEmtpyColumnMeta(meta.getName());
        }
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        target.setType(type);
        return target;
    }

    @Override
    public ColumnMeta toRDBMS(ColumnMeta meta) {
        check(meta);
        ColumnMeta target = new ColumnMeta();
        return cloneColumnMeta(meta);
    }

    @Override
    public ColumnMeta toES(ColumnMeta meta) {
        check(meta);
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        return target;
    }

    @Override
    public ColumnMeta toHBase(ColumnMeta meta) {
        check(meta);
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        target.setType("Bytes");
        target.setLength(0);
        target.setDecimalDigits(0);
        return target;
    }

    @Override
    public ColumnMeta toHDFS(ColumnMeta meta) {
        check(meta);
        return cloneColumnMeta(meta);
    }


}
