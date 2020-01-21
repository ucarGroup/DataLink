package com.ucar.datalink.writer.sddl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.writer.sddl.util.ProtostuffUtils;
import com.zuche.framework.sddl.util.DBUniqueCodeUtils;
import org.apache.commons.lang.ObjectUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 30/10/2017.
 */
public class SddlTest {

private static final AtomicInteger increParam = new AtomicInteger(0);
    private static final LoadingCache<String, String> dataSources;
    static {
        dataSources = CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
            @Override
            public String load(String sourceInfo) throws Exception {
                if (sourceInfo.equals("bb"))
                    throw new Exception("bb不是正确的！");
                else
                    return sourceInfo;
            }
        });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {


        // future();

        // shellExec();

        // createShardingNum();
    }


    private static void jsonTest () {
        JsonVo vo = new JsonVo();
        vo.setId(123l);
        vo.setName("chen");

        String json = JSON.toJSONString(vo);
        String jsonVo = JSON.toJSONString(vo, SerializerFeature.WriteMapNullValue);
        String jsonvo = JSON.toJSONString(vo, SerializerFeature.WriteClassName);

        JsonInte vo3 = (JsonInte) JSON.parse(jsonvo);
        JsonInte vo2 = (JsonInte) JSON.parseObject(jsonVo, JsonVo.class);
        JsonInte vo1 = (JsonInte) JSON.parse(json);

        System.out.println(json);
        System.out.println(jsonVo);
    }
    static class JsonVo implements JsonInte{
        private Long id;
        private String name;
        private String content;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    static interface JsonInte {

    }

    private static void future () throws ExecutionException, InterruptedException {

        ExecutorService es = Executors.newFixedThreadPool(4);

        List<FutureTask<String>> tasklist = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            int finalI = i;
            FutureTask<String> task = new FutureTask<String>(new Callable<String>() {

                @Override
                public String call() throws Exception {
                    Thread.sleep(500);
                    if (finalI == 2) {
                        Thread.sleep(2000);
                    }
                    // System.out.println(finalI);
                    return String.valueOf(finalI)+"排序";
                }
            });
            es.execute(task);
            tasklist.add(task);
        }

        for (FutureTask<String> task : tasklist) {
            System.out.println(new Date() + "-" +task.get());
        }

        System.out.println("end");

    }

    private static void coper () {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "butioy");
        map.put("key2", "protostuff");
        map.put("key3", "serialize");

        Map<String, CheckCompleteMode> sddlSyncDatas = new ConcurrentHashMap<>();
        for (int i=0; i < 100; i++) {
            CheckCompleteMode checkCompleteMode = new CheckCompleteMode();

            Map<String, HashSet<String>> sddlSyncDataSet = new ConcurrentHashMap<>();
            HashSet set = new HashSet<>();
            set.addAll(Arrays.asList("aaa"+i));
            sddlSyncDataSet.put("111"+i, set);

            checkCompleteMode.setSddlSyncDataSet(sddlSyncDataSet);

            sddlSyncDatas.put("taskid"+i, checkCompleteMode);

        }


        Long statT = System.currentTimeMillis();
        Map<String, CheckCompleteMode> sddlSyncDatas1 = ProtostuffUtils.coper(sddlSyncDatas);

        System.out.println(System.currentTimeMillis() - statT);
        System.out.println("反序列化map对象：" + sddlSyncDatas1);
    }

    public static class CheckCompleteMode implements Serializable {
        // <tableName, HashSet<id_accessoryHashColumn>>, must set the max capacity of queue is 100;
        private Map<String, HashSet<String>> sddlSyncDataSet = new ConcurrentHashMap<>();


        public Map<String, HashSet<String>> getSddlSyncDataSet() {
            return sddlSyncDataSet;
        }

        public void setSddlSyncDataSet(Map<String, HashSet<String>> sddlSyncDataSet) {
            this.sddlSyncDataSet = sddlSyncDataSet;
        }
    }

    public static void shellExec () {
        String executeShell = "sh /Users/michael/UCAR/worksapace/UCARDATALINK/trunk/dl-worker/dl-worker-writer-sddl/src/test/java/com/ucar/datalink/writer/sddl/shell/startup.sh";
        String executeShellLogFile = "/Users/michael/UCAR/worksapace" +
                "/UCARDATALINK/trunk/dl-worker/dl-worker-writer-sddl" +
                "/src/test/java/com/ucar/datalink/writer/sddl/shell/startup.log";
        String javaopts = "/Users/michael/UCAR/worksapace/UCARDATALINK/trunk/dl-worker/dl-worker-writer-sddl/src/test/java/com/ucar/datalink/writer/sddl/shell/javaopts.properties";

        Properties properties = null;
        try {
            // properties = PropertiesUtil.getProperties(javaopts);
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println(properties.get("-server"));

    }

    // 解决封装类（String，Integer等）的值传递问题，已ok
    public static void difficultQuestionOk () {
        String i = "12";
        TestVo info = new TestVo("11");
        getInte(i, info);
        System.out.println(i);
        System.out.println(JSON.toJSONString(info));
    }

    private static void getInte(String i, TestVo info) {

        i = new String("333");
        info = new TestVo("22");
        System.out.println(i);
        System.out.println(JSON.toJSONString(info));
    }


    public final static int incrementAndGet () {
        for (;;) {
            int curr = increParam.get();
            int next = curr + 1;
            if (increParam.compareAndSet(curr, next)) {
                return next;
            }
        }
    }

    public class SemaphoreSynchronousQueue<E> {
        E item = null;
        Semaphore sync0 = new Semaphore(0);
        Semaphore send1 = new Semaphore(1);
        Semaphore recv0 = new Semaphore(0);

        public E take() throws InterruptedException {
            recv0.acquire();
            E x = item;
            //sync0.release();
            send1.release();
            return x;
        }

        public void put (E x) throws InterruptedException{
            send1.acquire();
            item = x;
            recv0.release();
            //sync0.acquire();
        }
    }

    private static void createShardingNum() {
        int shardingNo = DBUniqueCodeUtils.createShardingNum(748);
        System.out.println(shardingNo);
    }


    private static void streamTest(){
        List<String> list = new ArrayList<String>();
        list.add("11");
        list.add("22");
        list.add("ccc.333");

        List<String> filterResult = list.parallelStream().filter(p -> !ObjectUtils.equals(p, null)).collect(Collectors.toList());

        filterResult.stream().forEach(p -> {
            if (p.equals("22"))


            System.out.println(p);
        });

        System.out.println("eeeeeee");
    }

}
