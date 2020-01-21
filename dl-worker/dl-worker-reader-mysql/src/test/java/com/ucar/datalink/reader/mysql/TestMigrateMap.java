package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import org.junit.Test;

import java.util.Map;
import java.util.Random;

/**
 * Created by lubiao on 2019/8/7.
 */
public class TestMigrateMap {

    @Test
    public void test() {
        Map<String, String> map = MigrateMap.makeComputingMap(new Function<String, String>() {

            public String apply(String destination) {
                return destination;
            }
        });

        map.get("67-main");


        int seed = "67-main".hashCode() + 2;
        System.out.println(seed);
        System.out.println(seed % 2);
        System.out.print(new Random().nextInt());
    }
}
