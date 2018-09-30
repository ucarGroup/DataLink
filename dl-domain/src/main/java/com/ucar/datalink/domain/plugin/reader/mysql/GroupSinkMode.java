package com.ucar.datalink.domain.plugin.reader.mysql;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 当使用group模式时,对Event进行Sink的方式
 * <p>
 * Created by lubiao on 2017/6/3.
 */
public enum GroupSinkMode {
    /**
     * 所有eventparser必须相互协同,保证event在时间序列上全局有序,保证所有分库必须都同时正常才进行数据同步
     * (生产环境需用该模式，保证数据全局有序，把所有分库看成一个整体)
     */
    Coordinate,
    /**
     * eventparser各自进行数据同步，不需要相互协同，event只是局部有序，一个分库出现问题不会影响其它分库数据同步
     * (测试和预生产推荐使用该模式，因为测试和预生产数据量比较小，很容易出现某个分库长时间没数据的情况,这种情况下会出现协调等待，其它分库的数据也无法同步了)
     */
    Separate;

    public static List<GroupSinkMode> getAll() {
        return Lists.newArrayList(Coordinate, Separate);
    }
}
