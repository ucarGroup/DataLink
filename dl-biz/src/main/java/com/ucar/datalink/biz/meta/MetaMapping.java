package com.ucar.datalink.biz.meta;

import com.ucar.datalink.biz.mapping.AbstractMapping;
import com.ucar.datalink.biz.mapping.MappingFactory;
import com.ucar.datalink.biz.service.MetaMappingService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.domain.meta.MetaMappingInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/6/26.
 */
public class MetaMapping {

    private static AbstractMapping esMapping = MappingFactory.createElasticSearchMapping();

    private static AbstractMapping hbaseMapping = MappingFactory.createHBaseMapping();

    private static AbstractMapping hdfsMapping = MappingFactory.createHDFSMapping();

    private static AbstractMapping rdbmsMapping = MappingFactory.creteRDBMSMapping();

    private static AbstractMapping sddlMapping = MappingFactory.creteSddlMapping();


    static {
        load();
    }

    public static void load() {
        MetaMappingService service = DataLinkFactory.getObject(MetaMappingService.class);
        List<MetaMappingInfo> list = service.queryAllMetaMapping();

        for(MetaMappingInfo info : list) {
            if( MediaSourceType.MYSQL.name().toUpperCase().equals(info.getSrcMappingType()) || MediaSourceType.SQLSERVER.name().toUpperCase().equals(info.getSrcMappingType()) || MediaSourceType.POSTGRESQL.name().toUpperCase().equals(info.getSrcMappingType())) {
                //rdbmsMapping.processMediaMapping(info);
            }
            if( "RDBMS".toUpperCase().equals(info.getSrcMappingType()) ) {
                rdbmsMapping.processMetaMapping(info);
            }
            else if( MediaSourceType.HBASE.name().toUpperCase().equals(info.getSrcMappingType()) ) {
                hbaseMapping.processMetaMapping(info);
            }
            else if( MediaSourceType.HDFS.name().toUpperCase().equals(info.getSrcMappingType()) ) {
                hdfsMapping.processMetaMapping(info);
            }
            else if( MediaSourceType.ELASTICSEARCH.name().toUpperCase().equals(info.getSrcMappingType()) ) {
                esMapping.processMetaMapping(info);
            }
            else if( MediaSourceType.SDDL.name().toUpperCase().equals(info.getSrcMappingType()) ) {
                sddlMapping.processMetaMapping(info);
            }
            if( "DATAX".equalsIgnoreCase(info.getSrcMappingType())) {
                hdfsMapping.processMetaMapping(info);
            }
            else {
                //ignore
            }
        }
    }



    /**
     * 将元数据转换成HDFS
     */
    public static MediaMeta transformToHDFS(MediaMeta meta) {
        if( meta.getDbType()==MediaSourceType.HDFS ) {
            //将HDFS转换成HDFS
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return hdfsMapping.toHDFS(c);
                }
            },MediaSourceType.HDFS);
        }
        else if( meta.getDbType()==MediaSourceType.ELASTICSEARCH ) {
            //将HDFS转换成ES
            throw new UnsupportedOperationException("不支持从elastic search转换成HDFS");
        }
        else if( meta.getDbType()==MediaSourceType.HBASE ) {
            //将HDFS转化成HBase
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return hbaseMapping.toHDFS(c);
                }
            },MediaSourceType.HDFS);
        }
        else if( meta.getDbType()==MediaSourceType.MYSQL || meta.getDbType()==MediaSourceType.ORACLE || meta.getDbType()==MediaSourceType.SQLSERVER || meta.getDbType()==MediaSourceType.POSTGRESQL ) {
            //将关系型数据库转换成HDFS
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return rdbmsMapping.toHDFS(c);
                }
            },MediaSourceType.HDFS);
        }
        else if( meta.getDbType()==MediaSourceType.SDDL ) {
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return sddlMapping.toHDFS(c);
                }
            },MediaSourceType.SDDL);
        }
        else {
            throw new UnsupportedOperationException("其他类型暂不支持");
        }
    }


    /**
     * 将元数据转换成关系型数据库
     */
    public static MediaMeta transformToRDBMS(MediaMeta meta) {
        if( meta.getDbType()==MediaSourceType.HDFS ) {
            //将HDFS 转换成 RDBMS
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return hdfsMapping.toRDBMS(c);
                }
            },MediaSourceType.MYSQL);
        }
        else if( meta.getDbType()==MediaSourceType.ELASTICSEARCH ) {
                //将ES 转换成RDBMS
                return transform(meta, new ColumnTransform() {
                    public ColumnMeta execute(ColumnMeta c) {
                        return esMapping.toRDBMS(c);
                    }
                },MediaSourceType.MYSQL);
        }
        else if( meta.getDbType()==MediaSourceType.HBASE ) {
            //目前不支持将HBase转换成 RDBMS
            throw new UnsupportedOperationException("不支持从HBase转换成关系型数据库");
        }
        else if( meta.getDbType()==MediaSourceType.MYSQL|| meta.getDbType()==MediaSourceType.ORACLE ||
        meta.getDbType()==MediaSourceType.SQLSERVER || meta.getDbType()==MediaSourceType.POSTGRESQL ) {
            //关系型数据库 转关系型数据库，这里就不用转换了
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return rdbmsMapping.toRDBMS(c);
                }
            },MediaSourceType.MYSQL);
        }
        else if( meta.getDbType()==MediaSourceType.SDDL ) {
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return sddlMapping.toRDBMS(c);
                }
            },MediaSourceType.SDDL);
        }
        else {
            //其他类型暂不支持
            throw new UnsupportedOperationException("其他类型暂不支持");

        }
    }

    /**
     * 将元数据转换成ES元数据
     */
    public static MediaMeta transformToES(MediaMeta meta) {
        if( meta.getDbType()==MediaSourceType.HDFS ) {
            //将HDFS 转换成 ES
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return hdfsMapping.toES(c);
                }
            },MediaSourceType.ELASTICSEARCH);
        }
        else if( meta.getDbType()==MediaSourceType.ELASTICSEARCH ) {
            //将ES 转换成ES
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return esMapping.toES(c);
                }
            },MediaSourceType.ELASTICSEARCH);
        }
        else if( meta.getDbType()==MediaSourceType.HBASE ) {
            //目前不支持将HBase转换成 ES
            throw new UnsupportedOperationException("不支持从HBase转换成ES");
        }
        else if( meta.getDbType()==MediaSourceType.MYSQL || meta.getDbType()==MediaSourceType.ORACLE ||
        meta.getDbType()==MediaSourceType.SQLSERVER || meta.getDbType()==MediaSourceType.POSTGRESQL ) {
            //关系型数据库 转换成ES
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return rdbmsMapping.toES(c);
                }
            },MediaSourceType.ELASTICSEARCH);
        }
        else if( meta.getDbType()==MediaSourceType.SDDL ) {
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return sddlMapping.toES(c);
                }
            },MediaSourceType.SDDL);
        }
        else {
            //其他类型暂不支持
            throw new UnsupportedOperationException("其他类型暂不支持");
        }
    }


    /**
     * 将元数据转换成ES元数据
     */
    public static MediaMeta transformToDataX(MediaMeta meta) {
        if( meta.getDbType()==MediaSourceType.HDFS ) {
            //将HDFS 转换成 datax
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return hdfsMapping.toDataX(c);
                }
            },MediaSourceType.HDFS);
        }
        else {
            //其他类型暂不支持
            throw new UnsupportedOperationException("其他类型暂不支持");
        }
    }


    public static MediaMeta transformToHBase(MediaMeta meta) {
        if( meta.getDbType()==MediaSourceType.HDFS ) {
            //将HDFS 转换成 HBase
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    c.setColumnFamily("default");
                    return hdfsMapping.toHBase(c);
                }
            },MediaSourceType.HBASE);
        }
        else if( meta.getDbType()==MediaSourceType.ELASTICSEARCH ) {
            //将ES 转换成HBase
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    c.setColumnFamily("default");
                    return esMapping.toHBase(c);
                }
            },MediaSourceType.HBASE);
        }
        else if( meta.getDbType()==MediaSourceType.HBASE ) {
            //目前不支持将HBase转换成 HBase
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    return hbaseMapping.toHBase(c);
                }
            },MediaSourceType.HBASE);
        }
        else if( meta.getDbType()==MediaSourceType.MYSQL || meta.getDbType()==MediaSourceType.ORACLE ||
        meta.getDbType()==MediaSourceType.SQLSERVER || meta.getDbType()==MediaSourceType.POSTGRESQL ) {
            //关系型数据库 转换成HBase
            //throw new UnsupportedOperationException("不支持从关系型数据库转换成HBase");
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    c.setColumnFamily("default");
                    return rdbmsMapping.toHBase(c);
                }
            },MediaSourceType.HBASE);
        }
        else if( meta.getDbType()==MediaSourceType.SDDL ) {
            return transform(meta, new ColumnTransform() {
                public ColumnMeta execute(ColumnMeta c) {
                    c.setColumnFamily("default");
                    return sddlMapping.toHBase(c);
                }
            },MediaSourceType.HBASE);
        }
        else {
            //其他类型暂不支持
            throw new UnsupportedOperationException("其他类型暂不支持");
        }

    }
    



    /**
     * 模板函数，提取了一些公用的代码，上面的一些函数会调用这个transform()函数
     * @param meta 需要转换的源表
     * @param ct 对单个列进行转换的回调接口
     * @param dbType 目标表转换成的DB类型
     * @return
     */
    public static MediaMeta transform(MediaMeta meta, ColumnTransform ct, MediaSourceType dbType) {
        MediaMeta target = new MediaMeta();
        target.setName(meta.getName());
        List<ColumnMeta> sourceColumns = meta.getColumn();
        List<ColumnMeta> targetColumns = new ArrayList<>();

        for(ColumnMeta c : sourceColumns) {
            targetColumns.add(ct.execute(c));
        }
        target.setColumn(targetColumns);
        target.setDbType(dbType);
        return target;
    }


    /**
     * 回调函数继承使用的接口，继承接口中的execute()函数，然后将指定的源CloumnMeta转换再返回
     */
    static interface ColumnTransform {
        public ColumnMeta execute(ColumnMeta c);
    }

    /**
     * 比较两个名字是否相等(忽略大小写)
     * @param name
     * @param toName
     * @return
     */
    public static boolean compareToName(String name, String toName) {
        if(name==null || toName==null) {
            return false;
        }
        return name.equalsIgnoreCase(toName);
    }


}
