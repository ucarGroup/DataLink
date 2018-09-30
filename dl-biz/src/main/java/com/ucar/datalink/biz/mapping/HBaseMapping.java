package com.ucar.datalink.biz.mapping;

import com.ucar.datalink.domain.meta.ColumnMeta;

/**
 * Created by user on 2017/6/26.
 */
public class HBaseMapping extends AbstractMapping {

    public ColumnMeta toRDBMS(ColumnMeta meta) {
        throw new UnsupportedOperationException();
    }

    public ColumnMeta toES(ColumnMeta meta) {
        throw new UnsupportedOperationException();
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
