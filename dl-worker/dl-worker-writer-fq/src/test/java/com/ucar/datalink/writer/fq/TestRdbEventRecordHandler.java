package com.ucar.datalink.writer.fq;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.zuche.framework.cache.customize.DBEventType;
import com.zuche.framework.cache.customize.DBTableRowVO;
import org.apache.commons.lang.math.RandomUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TestRdbEventRecordHandler {


    public static void main(String[] args) {

        List<RdbEventRecord> records = getRecoreds();
        Map<String, List<RdbEventRecord>> rdbEventRecordByTables = new HashMap<>();
        for(RdbEventRecord record  : records){
            String key = record.getSchemaName() + record.getTableName();
            if(!rdbEventRecordByTables.containsKey(key)){
                rdbEventRecordByTables.put(key,new ArrayList<>());
            }
            rdbEventRecordByTables.get(key).add(record);
        }

        ExecutorService executorService = new ThreadPoolExecutor(
                5,
               5,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory(
                        "test"));



        List<Future> results = new ArrayList<>();
        for(Map.Entry<String, List<RdbEventRecord>> rdbEventRecordByTable :  rdbEventRecordByTables.entrySet()){
            List<RdbEventRecord> rdbEventRecords = rdbEventRecordByTable.getValue();
            results.add(executorService.submit(() ->{
                rdbEventRecords.forEach(record -> {
                    DBTableRowVO rowVO = new DBTableRowVO();
                    rowVO.setDatabaseName(record.getSchemaName());
                    rowVO.setTableName(record.getTableName());
                    int random = RandomUtils.nextInt(1000);
                    System.out.println("sleep:" + random);
                    try {
                        Thread.sleep(random);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                });

            }));
        }

        Throwable ex = null;
        for (int i = 0; i < results.size(); i++) {
            Future result = results.get(i);
            try {
                Object obj = result.get();
                if (obj instanceof Throwable) {
                    ex = (Throwable) obj;
                }
            } catch (Throwable e) {
                ex = e;
            }
        }
        if (ex != null) {
            throw new DatalinkException("11111111111", ex);
        }



    }

    public static List<RdbEventRecord> getRecoreds(){
        List<RdbEventRecord> records = new ArrayList<>();
        for(int i = 0; i < 30; i++){
            RdbEventRecord rdbEventRecord = new RdbEventRecord();
            rdbEventRecord.setSchemaName("bbb");
            rdbEventRecord.setTableName("aaa" + i);

            RdbEventRecord rdbEventRecord2 = new RdbEventRecord();
            rdbEventRecord2.setSchemaName("bbb");
            rdbEventRecord2.setTableName("aaa" + i);

            RdbEventRecord rdbEventRecord3 = new RdbEventRecord();
            rdbEventRecord3.setSchemaName("bbb");
            rdbEventRecord3.setTableName("aaa" + i);
            records.add(rdbEventRecord);
            records.add(rdbEventRecord2);
            records.add(rdbEventRecord3);
        }
        return records;
    }

}
