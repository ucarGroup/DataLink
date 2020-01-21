package com.ucar.datalink.biz.mapping;

import com.ucar.datalink.domain.meta.ColumnMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by user on 2017/6/26.
 */
public class HBaseMapping extends AbstractMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(HBaseMapping.class);

    public ColumnMeta toRDBMS(ColumnMeta meta) {
        check(meta);
        ColumnMeta target = cloneColumnMeta(meta);
        target.setType("string");
        return target;
    }

    public ColumnMeta toES(ColumnMeta meta) {
        check(meta);
        ColumnMeta target = cloneColumnMeta(meta);
        target.setType("string");
        return target;
    }

    public ColumnMeta toHBase(ColumnMeta meta) {
        check(meta);
        ColumnMeta target = cloneColumnMeta(meta);
        target.setType("Bytes");
        return target;
    }

    public ColumnMeta toHDFS(ColumnMeta meta) {
        check(meta);
        ColumnMeta target = new ColumnMeta();
        target = cloneColumnMeta(meta);
        target.setColumnFamily(meta.getColumnFamily());
        target.setType("string");
        target.setLength(0);
        return target;
    }

}
