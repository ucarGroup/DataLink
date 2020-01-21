package com.ucar.datalink.manager.core.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by lubiao on 2016/12/13.
 */
public class CacheBuilderTest {

    @Test
    public void test() throws ExecutionException {
        LoadingCache<String, String> watchersForKey = CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return "1";
            }
        });

        watchersForKey.get("1");
        watchersForKey.get("2");
        watchersForKey.get("3");
        watchersForKey.get("4");
        watchersForKey.put("1", "1111");
        System.out.println(watchersForKey.asMap());
        System.out.println(watchersForKey.asMap().values().size());
        System.out.println(watchersForKey.asMap().values().stream().mapToInt(item -> Integer.valueOf(item)).sum());
        System.out.println(watchersForKey.getIfPresent("1"));

        watchersForKey.invalidateAll();

        watchersForKey.getIfPresent("1");
        watchersForKey.getIfPresent("2");
        watchersForKey.getIfPresent("3");
        watchersForKey.getIfPresent("4");
        System.out.println(watchersForKey.asMap());
        System.out.println(watchersForKey.asMap().values().size());
        System.out.println(watchersForKey.asMap().values().stream().mapToInt(item -> Integer.valueOf(item)).sum());
    }
}
