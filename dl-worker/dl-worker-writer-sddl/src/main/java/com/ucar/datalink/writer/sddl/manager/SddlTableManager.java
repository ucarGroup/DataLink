package com.ucar.datalink.writer.sddl.manager;

import com.google.common.collect.Lists;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.writer.sddl.dataSource.DataSourceCluster;
import com.ucar.datalink.writer.sddl.dataSource.SddlTaskInfo;
import com.ucar.datalink.writer.sddl.model.ClumnShardingTypeEnum;
import com.ucar.datalink.writer.sddl.model.ShardingColumnInfo;
import com.zuche.framework.sddl.datasource.SddlCluster;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 15/11/2017.
 */
public class SddlTableManager {
    public static final String SDDL_TABLE_SPACE_MARK = "_";
    private static final List<String> shardingTableSuffix = Lists.newArrayList();

    static {
        for (int i = 0; i < 8192; i++) {
            if (i < 10) {
                shardingTableSuffix.add("000"+i);
            } else if (i < 100) {
                shardingTableSuffix.add("00"+i);
            } else if (i < 1000) {
                shardingTableSuffix.add("0"+i);
            } else {
                shardingTableSuffix.add(String.valueOf(i));
            }
        }
    }


    /**
     * @return : 获取主维度sharding列详细信息
     *          ⚠注意️返回的结果元素要判断非空, 非空则为主维度sharding表
     *
     * @Description: shardingClumnInfo 这个对象返回的列对象
     * @Author : yongwang.chen@ucarinc.com
     * @Date : 11:26 AM 15/11/2017
     */
    public static ShardingColumnInfo getMainShardingClumn(String tableName, MediaSourceInfo sddlMediaSourceInfo) {

        Map<String, String> mainShardingTableInfos = DataSourceCluster.getSddlDs(sddlMediaSourceInfo)
                .getMainShardingTableInfos();

        if (MapUtils.isEmpty(mainShardingTableInfos))
            return null;

        return getShardingColumnInfo(mainShardingTableInfos, tableName);
    }

    /**
     * @return : 获取附属维度sharding列详细信息
     *          ⚠注意️返回的结果元素要判断非空, 非空则为附属维度sharding表
     *
     * @Description: shardingClumnInfo 这个对象返回的列对象
     * @Author : yongwang.chen@ucarinc.com
     * @Date : 11:26 AM 15/11/2017
     */
    public static ShardingColumnInfo getAccssoryShardingClumn(String tableName, SddlTaskInfo sddlTaskInfo) {

        Map<String, String> accessoryShardingTableInfos = DataSourceCluster.getSddlDs(sddlTaskInfo.getSddlMediaSourceInfo())
                .getAccessoryShardingTableInfos();

        if (MapUtils.isEmpty(accessoryShardingTableInfos))
            return null;


        return getShardingColumnInfo(accessoryShardingTableInfos, tableName);
    }

    private static ShardingColumnInfo getShardingColumnInfo (Map<String, String> shardingTableInfos, String tableName) {

        ShardingColumnInfo shardingColumnInfo = null;

        if (shardingTableInfos.containsKey(tableName)) {
            shardingColumnInfo = new ShardingColumnInfo();
            shardingColumnInfo.setShardingClumnName(shardingTableInfos.get(tableName).split("\\.")[1]);
            shardingColumnInfo.setClumnShardingTypeEnum(ClumnShardingTypeEnum.Sharding);

        }

        return shardingColumnInfo;
    }

    public static boolean isMainRedundancyTable(String tableName, MediaSourceInfo sddlMediaSourceInfo) {
        List<String> mainRedundancyTables = DataSourceCluster.getSddlDs(sddlMediaSourceInfo).getMainRedundancyTableInfos();

        if (CollectionUtils.isEmpty(mainRedundancyTables) || !mainRedundancyTables.contains(tableName)) {
            return false;
        }

        return true;
    }

    public static boolean isAccessoryRedundancyTable(String tableName, MediaSourceInfo sddlMediaSourceInfo) {
        List<String> accessorayRedundancyTables = DataSourceCluster.getSddlDs(sddlMediaSourceInfo).getAccessoryRedundancyTableInfos();

        if (CollectionUtils.isEmpty(accessorayRedundancyTables) || !accessorayRedundancyTables.contains(tableName)) {
            return false;
        }

        return true;
    }

    /**
     * @Description: 获取分表后缀(如：_0031)，若返回null，则为不分表；
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 4:07 PM 06/12/2017
     * @param sddlCluster
     * @param shardingNo  sharding值（0～8191）
     */
    public static String getShardingTableSuffix (SddlCluster sddlCluster,int shardingNo) {
        // 本库负责的所有shardingNO和分表的对应关系 ，如：{(2047,0000表),(4095,0002表)}
        Map<Integer,String> mpShardingTableNum = sddlCluster.getDataSource(shardingNo).getMpShardingTableNum();

        if (mpShardingTableNum != null && mpShardingTableNum.size() > 0) {
            return mpShardingTableNum.get(shardingNo);
        } else {
            return null;
        }
    }

    /**
     * @Description: 如果传入的表名是分表表名（即含有"_数字"），则去除后缀后返回；
     *                  若不含有，则原值返回；
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 5:09 PM 06/12/2017
     * @param   oldTable
     */
    public static String getTableNameOfRmSuffix (String oldTable) {
        String suffix = StringUtils.substringAfterLast(oldTable, SDDL_TABLE_SPACE_MARK);

        if (StringUtils.isEmpty(suffix))
            return oldTable;

        if (isSharingSuffix(suffix))
            return StringUtils.substringBeforeLast(oldTable, SDDL_TABLE_SPACE_MARK);

        return oldTable;
    }

    private static boolean isSharingSuffix(String str){
        if (shardingTableSuffix.contains(str)) { // regex性能不好，故使用此方法
            return true;
        }
        return false;
    }
}
