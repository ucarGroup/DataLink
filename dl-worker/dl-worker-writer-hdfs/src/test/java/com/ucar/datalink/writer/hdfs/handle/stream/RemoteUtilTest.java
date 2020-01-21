package com.ucar.datalink.writer.hdfs.handle.stream;

import org.junit.Test;


/**
 * Created by lubiao on 2017/11/21.
 */
public class RemoteUtilTest {

    @Test
    public void testColseInternal() {
        RemoteUtil.colseInternal(
                "hdfs://hadoop2cluster/user/mysql/binlog/ucar_order/t_scd_order/2017-11-21/t_scd_order-17-00.txt",
                "10.104.104.243"
        );
    }
}
