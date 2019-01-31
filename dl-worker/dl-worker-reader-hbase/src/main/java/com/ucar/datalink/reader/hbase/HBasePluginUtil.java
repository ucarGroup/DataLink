package com.ucar.datalink.reader.hbase;

import com.ucar.datalink.domain.event.*;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by user on 2017/7/4.
 */
public class HBasePluginUtil {

    private static final Logger logger = LoggerFactory.getLogger(HBasePluginUtil.class);

    /**
     * 一次抓取的最大数量，1000条，然后根据这些记录分析出表结构
     */
    private static final long MAX_FETCH_NUM = 1000;
    /**
     *  根据传入的MediaSourceInfo的id，检查对应的hbase集群是否连接正常
     * @param event
     * @return
     */
    public static void checkHBaseConnection(HBaseConnCheckEvent event) {
        if(logger.isErrorEnabled()) {
            logger.debug("start executeTableEvent method "+event.toString());
        }
        HBaseMediaSrcParameter hbaseParameter = event.getHbaseParameter();
        ZkMediaSrcParameter zkParameter = event.getZkParameter();
        String znode = hbaseParameter.getZnodeParent();
        String address = zkParameter.parseServersToString();
        String port = zkParameter.parsePort()+"";
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", address);
        conf.set("hbase.zookeeper.property.clientPort", port);
        conf.set("zookeeper.znode.parent", znode);
        HBaseAdmin admin = null;
        try {
            admin = new HBaseAdmin(conf);
            event.getCallback().onCompletion(null, "success");
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            event.getCallback().onCompletion(e, "failure");
        } finally {
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    logger.error("close hbase admin failure",e);
                    event.getCallback().onCompletion(e, "failure");
                }
            }
        }
    }


    /**
     * 工具类的这个函数提供给HBaseTaskReaderListener使用，用来获取HBase中的所有表
     * @param event
     */
    public static void executeTableEvent(HBaseTablesEvent event) {
        if(logger.isErrorEnabled()) {
            logger.debug("start executeTableEvent method "+event.toString());
        }
        HBaseAdmin admin = null;
        List<String> tableNameList = new ArrayList<String>();
        List<MediaMeta> list = new ArrayList<>();

        try {
            HBaseMediaSrcParameter hbaseParameter = event.getHbaseParameter();
            //通过hbase Parameter获取ZK相关的
            ZkMediaSrcParameter zkParameter = event.getZkParameter();

            String port = zkParameter.parsePort() + "";
            String address = zkParameter.parseServersToString();
            String znode = hbaseParameter.getZnodeParent();
            org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", address);
            conf.set("hbase.zookeeper.property.clientPort", port);
            conf.set("zookeeper.znode.parent", znode);
            logger.info("executeTableEvent hbase admin begin connection.");
            admin = new HBaseAdmin(conf);
            logger.info("executeTableEvent hbase admin execute success "+admin.toString());
            String[] tables = admin.getTableNames();
            for (String s : tables) {
                tableNameList.add(s);
            }
            for (String name : tableNameList) {
                MediaMeta tm = new MediaMeta();
                tm.setName(name);
                tm.setDbType(MediaSourceType.HBASE);
                list.add(tm);
            }
            logger.info("hbase media_meta : "+list.toString());
            event.getCallback().onCompletion(null, list);
        } catch (IOException e) {
            logger.error("get HbaseTable error",e);
            event.getCallback().onCompletion(e, list);
        } catch(Exception e) {
            logger.error("unknown error ",e);
            event.getCallback().onCompletion(e, list);
        } finally {
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    logger.error("close hbase admin failure",e);
                    event.getCallback().onCompletion(e, list);
                }
            }
        }

    }


    /**
     * 工具类的这个函数提供给HBaseTaskReaderListener使用，用来获取HBase中某个表下的所有列元信息
     * @param event
     */
    public static void executeColumnEvent(HBaseColumnsEvent event) {
        if(logger.isDebugEnabled()) {
            logger.debug("start executeColumnEvent method "+event.toString());
        }
        List<ColumnMeta> columns = new ArrayList<>();
        try {
            HBaseMediaSrcParameter hbaseParameter = event.getHbaseParameter();
            String tableName = event.getTableName();
            ZkMediaSrcParameter zkParameter = event.getZkParameter();
            List<String> columnFamilys = getHbaseColumnFamilies(tableName,hbaseParameter,zkParameter);
            if(logger.isDebugEnabled()) {
                logger.debug("getHbaseColumnFamilies success "+columnFamilys.toString());
            }
            if(columnFamilys==null || columnFamilys.size()==0) {
                event.getCallback().onCompletion(null,columns);
                return;
            }

            for (String cf : columnFamilys) {
                Set<String> qualifiers = getHbaseQualifier(tableName, zkParameter.parseServersToString(), zkParameter.parsePort() + "", hbaseParameter.getZnodeParent(), cf);
                if(logger.isDebugEnabled()) {
                    logger.debug("getHbaseQualifier success "+qualifiers.toString());
                }
                if (qualifiers == null || qualifiers.size() == 0) {
                    ColumnMeta meta = new ColumnMeta();
                    meta.setColumnFamily(cf);
                    meta.setName("");
                    meta.setType("Bytes");
                    columns.add(meta);
                    continue;
                }
                for (String name : qualifiers) {
                    ColumnMeta meta = new ColumnMeta();
                    meta.setColumnFamily(cf);
                    meta.setName(name);
                    meta.setType("Bytes");
                    columns.add(meta);
                }
            }
            event.getCallback().onCompletion(null, columns);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            event.getCallback().onCompletion(e,columns);
        }
    }


    /**
     * 这个函数提供给 executeColumnEvent()使用的，用来获取HBase中一个表的所有列簇
     * @param table
     * @param hbaseParameter
     * @param zkParameter
     * @return
     */
    private static List<String> getHbaseColumnFamilies(String table, HBaseMediaSrcParameter hbaseParameter, ZkMediaSrcParameter zkParameter) {
        HBaseAdmin admin = null;
        List<String> columnFamiliesList = new ArrayList<String>();
        if (StringUtils.isBlank(table)) {
            return columnFamiliesList;
        }
        try {
            org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", zkParameter.parseServersToString());
            conf.set("hbase.zookeeper.property.clientPort", zkParameter.parsePort() + "");
            conf.set("zookeeper.znode.parent", hbaseParameter.getZnodeParent());
            logger.info("getHbaseColumnFamilies hbase admin begin connection.");
            admin = new HBaseAdmin(conf);
            logger.info("getHbaseColumnFamilies hbase admin execute success "+admin.toString());
            HTableDescriptor s = admin.getTableDescriptor(table.getBytes());
            HColumnDescriptor[] columns = s.getColumnFamilies();
            if (columns != null) {
                for (HColumnDescriptor c : columns) {
                    columnFamiliesList.add(c.getNameAsString());
                }
            }
        } catch (IOException e) {
            logger.error("get hbase column familiy error", e);
        } finally {
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    logger.error("close hbase admin failure",e);
                }
            }
        }
        return columnFamiliesList;
    }


    /**
     * 这个函数提供给executeColumnEvent()使用的，在表名，列簇名已知的情况下，获取这个列族中所有的动态列 qualifier，
     * 因为ualifier是动态生成的所以HBaseAdmin的API就无法获取了，只能通过HTable查询10条记录然后获取所有的qualifier
     * @param tableName
     * @param hosts
     * @param port
     * @param znode
     * @param columnFamily
     * @return
     */
    private static Set<String> getHbaseQualifier(String tableName, String hosts, String port, String znode, String columnFamily) {
        HBaseAdmin admin = null;
        ResultScanner rs = null;
        Set<String> columnList = new HashSet<>();
        if (StringUtils.isBlank(tableName)) {
            return columnList;
        }
        try {
            org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", hosts);
            conf.set("hbase.zookeeper.property.clientPort", port);
            conf.set("zookeeper.znode.parent", znode);
            logger.info("getHbaseQualifier hbase admin begin connection.");
            admin = new HBaseAdmin(conf);
            logger.info("getHbaseQualifier hbase admin execute success "+admin.toString());
            HTable hTable = new HTable(conf, tableName);
            logger.info("getHbaseQualifier hbase table execute success "+admin.toString());
            if(logger.isDebugEnabled()) {
                logger.debug("htable execute success "+hTable.toString());
            }
            Scan scan = new Scan();
            scan.addFamily(columnFamily.getBytes());
            scan.setCaching((int)MAX_FETCH_NUM);
            scan.setFilter(new PageFilter(MAX_FETCH_NUM));
            rs = hTable.getScanner(scan);
            Result r = rs.next();
            if (r == null) {
                logger.error("query data is empty Result:" + r);
                return columnList;
            }

            for (Cell cell : r.rawCells()) {
                columnList.add(Bytes.toString(cell.getQualifier()));
            }

        } catch (IOException e) {
            logger.error("get hbase qualifier error", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    logger.error("close hbase admin failure", e);
                }
            }
        }
        return columnList;
    }



    public static void caclRegionCount(HBaseRegionCountEvent event) {
        Configuration configuration = HBaseConfiguration.create();
        HBaseMediaSrcParameter hbaseParameter = event.getHbaseParameter();
        String tableName = event.getTableName();
        ZkMediaSrcParameter zkParameter = event.getZkParameter();
        String hosts = zkParameter.parseServersToString();
        String port = zkParameter.parsePort()+"";
        String znode = hbaseParameter.getZnodeParent();

        HBaseAdmin admin = null;
        HTable htable = null;
        int regionsCount = -1;
        try {
            configuration.set("hbase.zookeeper.quorum", hosts);
            configuration.set("hbase.zookeeper.property.clientPort", port);
            configuration.set("zookeeper.znode.parent", znode);
            admin = new HBaseAdmin(configuration);
            htable = new HTable(configuration, tableName);
            Pair<byte[][], byte[][]> regionRanges = htable.getStartEndKeys();
            regionsCount = regionRanges.getFirst().length;
            event.getCallback().onCompletion(null, new Integer(regionsCount));
        } catch(Exception e) {
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException ex) {
                    logger.error(e.getMessage(),e);
                    event.getCallback().onCompletion(e, new Integer(-1));
                }
            }
            event.getCallback().onCompletion(e, new Integer(-1));
        }
    }


    public static void generateHBaseSplitInfo(HBaseSplitEvent event) {
        Configuration configuration = HBaseConfiguration.create();
        HBaseMediaSrcParameter hbaseParameter = event.getHbaseParameter();
        String tableName = event.getTableName();
        ZkMediaSrcParameter zkParameter = event.getZkParameter();
        String hosts = zkParameter.parseServersToString();
        String port = zkParameter.parsePort()+"";
        String znode = hbaseParameter.getZnodeParent();
        int groupNbr = event.getSplitCount();

        HBaseAdmin admin = null;
        HTable htable = null;
        Map<String, Object> result = new HashMap<String, Object>();

        try {
            configuration.set("hbase.zookeeper.quorum", hosts);
            configuration.set("hbase.zookeeper.property.clientPort", port);
            configuration.set("zookeeper.znode.parent", znode);
            admin = new HBaseAdmin(configuration);
            htable = new HTable(configuration, tableName);
            Pair<byte[][], byte[][]> regionRanges = htable.getStartEndKeys();
            int regionsCount = regionRanges.getFirst().length;

            List<HBaseRange> list = new ArrayList<HBaseRange>();
            String currentStartRowKey = null;
            int counter = 0;
            for (int i = 0; i < regionsCount; i++) {
                if (currentStartRowKey == null) {
                    currentStartRowKey = Bytes.toStringBinary(regionRanges.getFirst()[i]);
                }
                counter++;
                if ((i + 1) % groupNbr == 0 || i == regionsCount - 1) {
                    HBaseRange current = new HBaseRange();
                    current.setCount(counter);
                    current.setStartRowkey(currentStartRowKey);
                    current.setEndRowkey(Bytes.toStringBinary(regionRanges.getSecond()[i]));
                    list.add(current);
                    currentStartRowKey = null;
                    counter = 0;
                }
            }
            result.put("totalRegionsCount", regionsCount);
            result.put("range", list);
            event.getCallback().onCompletion(null, result);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            event.getCallback().onCompletion(e, result);
        } finally {
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                    event.getCallback().onCompletion(e, result);
                }
            }
        }
    }

    public static void main(String args[]){
        Integer i =Integer.parseInt("2186734009");
        System.out.println(i);
    }

}
