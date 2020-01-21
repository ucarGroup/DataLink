package com.ucar.datalink.writer.hdfs.handle.stream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.junit.Test;

import java.net.URI;

/**
 * Created by lubiao on 2017/11/21.
 */
public class FileSystemTest {

    @Test
    public void testAuth() {
        FsPermission p = FsPermission.getFileDefault().applyUMask(
                FsPermission.getUMask(new Configuration())).applyUMask(FsPermission.getUMask(new Configuration()));
        System.out.println(p);

        FsPermission p1 = FsPermission.getDirDefault().applyUMask(
                FsPermission.getUMask(new Configuration())).applyUMask(FsPermission.getUMask(new Configuration()));
        System.out.println(p1);
    }

    @Test
    public void test() {
        System.out.println("01234567890123456789\r\n".getBytes().length);
    }

    @Test
    public void testURI() {
        URI uri = URI.create("hdfs://hadoop2cluster/user/mysql/binlog/ucar_order/t_scd_order/2017-11-21/t_scd_order-17-00.txt");
        System.out.println(uri.getScheme());
        System.out.println(uri.getAuthority());
    }

    @Test
    public void testWrite() throws Exception {
        URI hdfsUri = URI.create("hdfs://hadoop2cluster");

        Configuration configuration = HBaseConfiguration.create();
        configuration.set("fs.defaultFS", "hdfs://hadoop2cluster");
        configuration.set("dfs.support.append", "true");
        configuration.set("hbase.zookeeper.quorum", "10.101.22.31");
        configuration.set("hbase.zookeeper.property.clientPort", "5181");
        configuration.set("dfs.client-write-packet-size", String.valueOf(1024 * 10));

        // 高可用设置
        String key = hdfsUri.getAuthority();
        configuration.set("dfs.nameservices", key);
        configuration.set(String.format("dfs.ha.namenodes.%s", key), "nn1,nn2");
        configuration.set(String.format("dfs.namenode.rpc-address.%s.nn1", key), "10.104.104.127:9001");
        configuration.set(String.format("dfs.namenode.rpc-address.%s.nn2", key), "10.104.104.128:9001");
        configuration.set(String.format("dfs.client.failover.proxy.provider.%s", key), "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");

        FileSystem fileSystem = FileSystem.get(
                hdfsUri,
                configuration,
                "hadoop");

        Path path1 = new Path("/user/biao.lu/writetest1.txt");
        FSDataOutputStream fileStream1;
        if (!fileSystem.exists(path1)) {
            fileStream1 = fileSystem.create(path1, false,
                    configuration.getInt(CommonConfigurationKeysPublic.IO_FILE_BUFFER_SIZE_KEY,
                            CommonConfigurationKeysPublic.IO_FILE_BUFFER_SIZE_DEFAULT),
                    (short) 3, 64 * 1024 * 1024L);
        } else {
            fileStream1 = fileSystem.append(path1);
        }
        for (int i = 0; i < 100; i++) {
            //fileStream1.write("0123456789012345678901234567890123456789vvvvvvvv\r\n".getBytes("UTF-8"));
            fileStream1.write("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\r\n".getBytes("UTF-8"));
        }


        Path path2 = new Path("/user/biao.lu/writetest2.txt");
        FSDataOutputStream fileStream2;
        if (!fileSystem.exists(path2)) {
            fileStream2 = fileSystem.create(new Path("/user/biao.lu/writetest2.txt"), false,
                    configuration.getInt(CommonConfigurationKeysPublic.IO_FILE_BUFFER_SIZE_KEY,
                            CommonConfigurationKeysPublic.IO_FILE_BUFFER_SIZE_DEFAULT),
                    (short) 3, 64 * 1024 * 1024L);
        } else {
            fileStream2 = fileSystem.append(path2);
        }
        for (int i = 0; i < 300; i++) {
            //fileStream2.write("0123456789012345678901234567890123456789vvvvvvvv\r\n".getBytes("UTF-8"));
            fileStream2.write("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu\r\n".getBytes("UTF-8"));
        }

        System.in.read();
    }
}
