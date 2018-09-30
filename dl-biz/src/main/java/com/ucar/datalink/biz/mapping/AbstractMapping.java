package com.ucar.datalink.biz.mapping;

import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MetaMappingInfo;

/**
 * Created by user on 2017/6/26.
 * 这个类是所有映射关系的父类，这里面的函数是将源类型映射到哪个目标类型
 * 比如实现类是HBaseMapping，实现了toES() ，toHDFS()两个函数
 * 其作用就是将HBase中的列转换成ES中的列类型，以及转换成HDFS中的列类型
 * 如果有新的数据库xxx需要实现，就要给这个类再增加一个抽象函数toXXX()
 */
public abstract class AbstractMapping {

    public ColumnMeta toRDBMS(ColumnMeta meta) {
        throw new UnsupportedOperationException();
    }

    public ColumnMeta toES(ColumnMeta meta) {
        throw new UnsupportedOperationException();
    }

    public ColumnMeta toHBase(ColumnMeta meta) {
        throw new UnsupportedOperationException();
    }

    public ColumnMeta toHDFS(ColumnMeta meta) {
        throw new UnsupportedOperationException();
    }

    public void processMetaMapping(MetaMappingInfo info) {

    }


    /**
     * 这个函数跟其他几个不一样，在一些特殊的场景下需要用到
     * 比如源端是HDFS的时候就需要，HDFS(hive)存储的是ORC文件，里面只有数据没有文件，所以DataX拿不到类型定义，
     * 当然理论上也是可以拿到的，只有同时给出数据文件和表结构定义文件就可以了，可能因为这个DataX工具为了保证其他的一些兼容
     * 或者通用性就没有去实现了，后续我们自己可以实现
     * 所以当读取到HDFS的文件时，还需要一个中间文件，比如读取到bitint的时候需要告诉DataX这个类型在JVM中保存成什么样的类型
     * @param meta
     * @return
     */
    public ColumnMeta toDataX(ColumnMeta meta) {
        throw new UnsupportedOperationException();
    }

    /**
     * 克隆函数，表示源列到目标列没有任何变化，比如从HDFS到HDFS列类型肯定是不变的所有直接克隆一下就可以了
     * @param meta
     * @return
     */
    public ColumnMeta cloneColumnMeta(ColumnMeta meta) {
        ColumnMeta target = new ColumnMeta();
        target.setColumnDesc(meta.getColumnDesc());
        target.setColumnFamily(meta.getColumnFamily());
        target.setDecimalDigits(meta.getDecimalDigits());
        target.setLength(meta.getLength());
        target.setName(meta.getName());
        target.setType(meta.getType());
        target.setIsPrimaryKey(meta.isPrimaryKey());
        return target;
    }

    public ColumnMeta createEmtpyColumnMeta(String name) {
        ColumnMeta target = new ColumnMeta();
        target.setColumnDesc("");
        target.setColumnFamily("");
        target.setDecimalDigits(0);
        target.setLength(0);
        target.setName(name);
        target.setType("@NULL@");
        return target;
    }


    public void check(ColumnMeta meta) {
        if(meta == null) {
            throw new RuntimeException("meta is null");
        }
    }

}
