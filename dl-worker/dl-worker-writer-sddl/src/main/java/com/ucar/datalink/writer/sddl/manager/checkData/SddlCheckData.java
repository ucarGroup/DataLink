package com.ucar.datalink.writer.sddl.manager.checkData;

import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.writer.sddl.dataSource.DataSourceCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlJdbcTemplate;
import com.ucar.datalink.writer.sddl.interceptor.CheckCompleteInterceptor;
import com.ucar.datalink.writer.sddl.threadpool.CallableExtend;
import com.ucar.datalink.writer.sddl.threadpool.ThreadPoolTools;
import com.zuche.framework.sddl.datasource.SddlCluster;
import com.zuche.framework.sddl.datasource.SddlDataSource;
import com.zuche.framework.sddl.util.DBUniqueCodeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: check data complete
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 12/03/2018.
 */
public class SddlCheckData {
    private static final Logger LOG = LoggerFactory.getLogger(SddlCheckData.class);

    public static final AtomicBoolean checkDataSwitch = new AtomicBoolean(false);
    public static final AtomicLong checkDataCount = new AtomicLong(1L);

    private static final AtomicLong checkedCurrentMinute = new AtomicLong(0L);

    private static ExecutorService threadPool = ThreadPoolTools.getCustomCache(100, "SDDL-checkData");

    public static synchronized void checkSyncShardingCompleteStart() {
        LOG.info("SddlCheckData Starting ");
        checkDataSwitch.set(true);

        while (checkDataSwitch.get()) {
            try {
                if (getCurrentMinute().equals(checkedCurrentMinute.get())) {

                    Thread.sleep(1000l);
                } else { // do work
                    Thread.sleep(5 * 1000);

                    checkedCurrentMinute.set(getCurrentMinute());

                    doCheckSyncSharding();
                    checkDataCount.incrementAndGet();
                }
            } catch (Throwable e) {
                LOG.error("SddlCheckData:{} is Error due to ", checkDataSwitch.get(), e);
            }

        }
        LOG.info("SddlCheckData Stopped ");
    }

    public static void checkSyncShardingCompleteEnd() {
        if (checkDataSwitch.get()) {
            checkDataSwitch.set(false);
            checkDataCount.set(0l);
        }
    }

    private static void doCheckSyncSharding() throws InterruptedException {
        if (MapUtils.isEmpty(CheckCompleteInterceptor.sddlSyncDatas))
            return;

        Map<String, CheckCompleteInterceptor.CheckCompleteMode> coperCheckData = new HashMap<>();
        coperCheckData.putAll(CheckCompleteInterceptor.sddlSyncDatas);
        CheckCompleteInterceptor.sddlSyncDatas.clear();

        for (Map.Entry<String, CheckCompleteInterceptor.CheckCompleteMode> entry : coperCheckData.entrySet()) {
            String taskId = entry.getKey();

            CheckCompleteInterceptor.CheckCompleteMode checkCompleteMode = entry.getValue();
            // 获取对应附属纬度的DS信息，并获取对应的附属维度数据源
            SddlCluster sddlCluster = DataSourceCluster.getSddlDs(checkCompleteMode.getSddlMediaSourceInfo()).getSddlLogicCluster().getSddlClusters().get(1);

            // HashSet<id_accessoryHashColumn>
            for (Map.Entry<String, Map<String, HashSet<String>>> tableInfo : checkCompleteMode.getSddlSyncDataSet().entrySet()) {
                String tableName = tableInfo.getKey();

                Map<String, HashSet<String>> accessoryColumns = tableInfo.getValue();

                Map<SddlDataSource, List<String>> execPlans = new HashMap();

                for (Map.Entry<String, HashSet<String>> columnInfo : accessoryColumns.entrySet()) {
                    String shardingColumnValue = columnInfo.getKey();
                    HashSet<String> ids = columnInfo.getValue();

                    int shardingNo = DBUniqueCodeUtils.createShardingNum(shardingColumnValue);
                    SddlDataSource dataSource = sddlCluster.getDataSource(shardingNo);

                    if (execPlans.containsKey(dataSource)) {
                        List<String> existExecPlan = execPlans.get(dataSource);

                        existExecPlan.addAll(ids);
                    } else {
                        List<String> execPlan = new ArrayList<>(ids);

                        execPlans.put(dataSource, execPlan);
                    }


                }

                CountDownLatch latch = new CountDownLatch(execPlans.size());

                for (Map.Entry<SddlDataSource, List<String>> dsExecInfo : execPlans.entrySet()) {
                    SddlDataSource dataSource = dsExecInfo.getKey();
                    List<String> ids = dsExecInfo.getValue();

                    threadPool.submit(new sddlCheckDataFT(taskId, tableName, ids, dataSource, latch));
                }

                latch.await();

            }
        }
    }

    private static List<String> doReadDB(SddlDataSource dataSource, String sql) {

        SddlJdbcTemplate sddlJdbcTemplate = DataSourceCluster.getSddlJdbcTemplate(dataSource.getDs());
        List<Map<String, Object>> results = sddlJdbcTemplate.getJdbcTemplate().queryForList(sql);

        List<String> resultIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (Map<String, Object> result : results) {
                resultIds.add(String.valueOf(result.get("id")));
            }
        }

        return resultIds;
    }

    private static Long getCurrentMinute() {
        Long current = System.currentTimeMillis();
        return current / (60 * 1000);
    }

    static class sddlCheckDataFT implements CallableExtend {
        private String taskId;

        private String tableName;
        private List<String> ids;
        private SddlDataSource dataSource;
        private CountDownLatch latch;

        public sddlCheckDataFT(String taskId, String tableName, List<String> ids, SddlDataSource dataSource, CountDownLatch latch) {
            this.taskId = taskId;
            this.tableName = tableName;
            this.ids = ids;
            this.dataSource = dataSource;
            this.latch = latch;
        }

        @Override
        public Object call() throws Exception {
            Map<String, String> reIds = new HashMap<>();
            try {
                //暂停3秒钟，保证数据已同步到目标库
                Thread.sleep(3000L);

                // select id from tableName where id in (id1,id2....)
                StringBuffer sqlSb = new StringBuffer("select id from ");
                sqlSb.append(tableName);
                sqlSb.append(" where id in (");
                int count = 0;
                for (String id : ids) {
                    ++count;

                    sqlSb.append(id);
                    if (count < ids.size()) {
                        sqlSb.append(",");
                    }
                }
                sqlSb.append(")");

                // get right id
                List<String> rightIds = doReadDB(dataSource, sqlSb.toString());

                if (CollectionUtils.isNotEmpty(rightIds)) {

                    reIds.putAll(
                            rightIds.stream().collect(
                                    Collectors.toMap(Function.identity(), Function.identity())));

                }
            } catch (Exception e) {
                LOG.error("sddlCheckDataFT is error! info : {}", toString(), e);
            } finally {
                ids.removeAll(reIds.keySet());

                if (ids.size() == 0) {
                    LOG.info("#### SddlCheckData<count:{}>, taskId:{}, tableName:{}, clusterNum:{}, the result is right!",
                            checkDataCount.get(), taskId, tableName, dataSource.getClusterNum());
                } else {
                    LOG.info("#### SddlCheckData<count:{}>, taskId:{}, tableName:{}, clusterNum:{}, the result is error, mistake:<{}>!",
                            checkDataCount.get(), taskId, tableName, dataSource.getClusterNum(), ids.toString());

                    AlarmService alarmService = DataLinkFactory.getObject(AlarmService.class);
                    alarmService.alarm("SddlCheckData<count:" + checkDataCount.get() + ">, taskId:" + taskId +
                            ", tableName:" + tableName + ", mistake ids is :" + ids.toString(),"DataLink报警",true);
                }

                latch.countDown();
            }

            return null;
        }

        @Override
        public void executeBufore(Thread t) {

        }

        @Override
        public void executeAfter(Throwable t) {

        }

        @Override
        public String toString() {
            return "sddlCheckDataFT{" +
                    "taskId='" + taskId + '\'' +
                    ", tableName='" + tableName + '\'' +
                    ", ids=" + ids +
                    ", dataSource=" + dataSource.getClusterNum() +
                    '}';
        }
    }

}
