package com.ucar.datalink.biz.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 2017/11/2.
 */
public class LoadingCacheTest {

    private static final LoadingCache<Long, List<Long>> cache = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .expireAfterWrite(60L, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, List<Long>>() {
                @Override
                public List<Long> load(Long key) throws Exception {
                    return new LinkedList<>();
                }
            });

    @Test
    public void test1() {
        List<Long> list = cache.getUnchecked(1L);
        list.add(1L);
        list.add(2L);
        list.add(3L);

        Map<Long, List<Long>> copyMap = new HashMap<>();
        copyMap.putAll(cache.asMap());
        System.out.println(copyMap.get(1L).size());

        cache.getUnchecked(1L).add(4L);
        System.out.println(copyMap.get(1L).size());

        cache.invalidate(1L);
        System.out.println(copyMap.get(1L).size());
    }

    @Test
    public void test2() {
        List<Long> list = cache.getUnchecked(1L);
        list.add(1L);
        list.add(2L);
        list.add(3L);

        Map<Long, List<Long>> copyMap = new HashMap<>();
        copyMap.putAll(cache.getAllPresent(Lists.newArrayList(1L)));
        System.out.println(copyMap.get(1L).size());

        cache.getUnchecked(1L).add(4L);
        System.out.println(copyMap.get(1L).size());

        cache.invalidate(1L);
        System.out.println(copyMap.get(1L).size());
    }
}
