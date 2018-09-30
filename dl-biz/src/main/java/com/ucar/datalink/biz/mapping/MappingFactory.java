package com.ucar.datalink.biz.mapping;

/**
 * Created by user on 2017/6/27.
 */
public class MappingFactory {

    public static AbstractMapping createElasticSearchMapping() {
        return new ElasticSearchMapping();
    }

    public static AbstractMapping createHBaseMapping() {
        return new HBaseMapping();
    }

    public static AbstractMapping createHDFSMapping() {
        return new HDFSMapping();
    }

    public static AbstractMapping creteRDBMSMapping() {
        return new RDBMSMapping();
    }

    public static AbstractMapping creteSddlMapping() {
        return new SddlMapping();
    }
}
