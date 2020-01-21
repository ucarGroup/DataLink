package com.ucar.datalink.writer.sddl.handler;

import com.google.common.collect.Lists;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import org.junit.Test;

import java.util.List;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 29/11/2017.
 */
public class WriteDataTest {

    public static RecordChunk<RdbEventRecord> recordChunk; // 模拟RecordChunk对象
    static {
        List<RdbEventRecord> records = Lists.newArrayList();

        // default values
        List<String> defaultValues = Lists.newArrayList("1",    "2600"); // id=1<0号库>、d=2600<1号库>
        List<String> updateValues  = Lists.newArrayList("2000", "2001"); // id=2000、2001<2号库>


        /*// 1.1 insert_sharding
        List<RdbEventRecord> insertShardingRecords = InsertCase.getInsertShardingDetail(defaultValues);
        records.addAll(insertShardingRecords);
        // 1.2 insert_redundancy
        List<RdbEventRecord> insertRedundancyRecords = InsertCase.getInsertRedundancyCity(defaultValues);
        records.addAll(insertRedundancyRecords);

        // 2.1 update_sharding_driverId为空
        List<RdbEventRecord> updateShardDriverIdNullRecodes = UpdateCase.getUpdateShardDriverIdNullRecodes(defaultValues, updateValues, null, null);
        records.addAll(updateShardDriverIdNullRecodes);
        // 2.2 update_sharding_更改driverid由null变为有值
        List<RdbEventRecord> updateShardDriverIdNullToHaveRecodes = UpdateCase.getUpdateShardDriverIdNullRecodes(defaultValues, defaultValues, null, defaultValues);
        records.addAll(updateShardDriverIdNullToHaveRecodes);
        // 2.3 update_sharding_driverId不变且更改其他字段
        List<RdbEventRecord> updateShardDriverIdRecodes = UpdateCase.getUpdateShardDriverIdNullRecodes(defaultValues, updateValues, defaultValues, defaultValues);
        records.addAll(updateShardDriverIdRecodes);
        // 2.4 update_sharding_driverId改变且更改其他字段
        List<RdbEventRecord> updateShardDriverIdChangeRecodes = UpdateCase.getUpdateShardDriverIdNullRecodes(defaultValues, updateValues, defaultValues, updateValues);
        records.addAll(updateShardDriverIdChangeRecodes);
        // 2.5 update_sharding_更改driverid由有值变为null
        List<RdbEventRecord> updateShardDriverIdToNullRecodes = UpdateCase.getUpdateShardDriverIdNullRecodes(defaultValues, updateValues, updateValues, null);
        records.addAll(updateShardDriverIdToNullRecodes);*/
        /*// 2.6 update_redundancy
        List<RdbEventRecord> updateRedundancy = UpdateCase.getUpdateRedundancy(defaultValues, updateValues);
        records.addAll(updateRedundancy);*/

        /*// 3.1 delete_sharding
        List<RdbEventRecord> deleteShardingDetail = DeleteCase.getDeleteShardingDetail(updateValues);
        records.addAll(deleteShardingDetail);*/
        /*// 3.2 delete_redundancy
        List<RdbEventRecord> deleteRedundancyCity = DeleteCase.getDeleteRedundancyCity(defaultValues);
        records.addAll(deleteRedundancyCity);*/

        // 后续：待补充ddl和分表用例

        recordChunk = new RecordChunk<>(
                 records,
                10l,
                0
        );
    }

    @Test
    public void testWriteData (RdbEventRecordHandler handler, TaskWriterContext context) {

        handler.writeData(recordChunk, context);
    }

}
