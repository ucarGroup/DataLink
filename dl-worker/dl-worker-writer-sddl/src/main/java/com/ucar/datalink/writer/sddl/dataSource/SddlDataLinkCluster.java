package com.ucar.datalink.writer.sddl.dataSource;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.writer.sddl.exception.SddlInitException;
import com.zuche.framework.sddl.datasource.SddlCluster;
import com.zuche.framework.sddl.datasource.SddlLogicCluster;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 20/11/2017.
 */
public class SddlDataLinkCluster {

    private SddlLogicCluster sddlLogicCluster;

    private Map<String, String> mainShardingTableInfos;       // T : <tableName, tableName.hashColumnName>;
    private List<String> mainRedundancyTableInfos;     // T : <tableName>;

    private Map<String, String> accessoryShardingTableInfos;  // T : <tableName, tableName.hashColumnName>;
    private List<String> accessoryRedundancyTableInfos;// T : <tableName>;

    private List<String> mainShardingTableSuffixs;     // 主维度分表后缀集合： null：为不分表，非null：为分表且是分表后缀集合
    private List<String> accessoryShardingTableSuffixs;// 附属维度分表后缀集合：null：为不分表，非null：为分表且是分表后缀集合

    public SddlLogicCluster getSddlLogicCluster() {
        return sddlLogicCluster;
    }

    public void setSddlLogicCluster(SddlLogicCluster sddlLogicCluster) {
        this.sddlLogicCluster = sddlLogicCluster;

        // 初始化主维度的sharding表
        this.mainShardingTableInfos = initMainShardingTable();

        // 初始化附属维度的sharding表
        this.accessoryShardingTableInfos = initAccessoryShardingTable();

        // 初始化主维度的redundancy表
        this.mainRedundancyTableInfos = initMainRedundancyTable();

        // 初始化附属维度的redundancy表
        this.accessoryRedundancyTableInfos = initAccessoryRedundancyTable();

        this.mainShardingTableSuffixs = initMainShardingTableSuffixs();
        this.accessoryShardingTableSuffixs = initAccessoryShardingTableSuffixs();
    }


    public Map<String, String> getMainShardingTableInfos() {
        return mainShardingTableInfos;
    }

    public void setMainShardingTableInfos(Map<String, String> mainShardingTableInfos) {
        this.mainShardingTableInfos = mainShardingTableInfos;
    }

    public Map<String, String> getAccessoryShardingTableInfos() {
        return accessoryShardingTableInfos;
    }

    public List<String> getAccessoryRedundancyTableInfos() {
        return accessoryRedundancyTableInfos;
    }

    public List<String> getMainRedundancyTableInfos() {
        return mainRedundancyTableInfos;
    }

    public List<String> getMainShardingTableSuffixs() {
        return mainShardingTableSuffixs;
    }

    public List<String> getAccessoryShardingTableSuffixs() {
        return accessoryShardingTableSuffixs;
    }

    private List<String> initAccessoryRedundancyTable() {
        if (sddlLogicCluster.getSddlClusters().size() == 2) {
            SddlCluster sddlCluster = sddlLogicCluster.getSddlClusters().get(1);
            return sddlCluster.getRedundancyTables();
        } else {
            return new ArrayList<>();
        }
    }

    private List<String> initMainRedundancyTable() {
        SddlCluster sddlCluster = sddlLogicCluster.getSddlClusters().get(0);

        return sddlCluster.getRedundancyTables();
    }

    private Map<String, String> initMainShardingTable() {
        SddlCluster sddlCluster = sddlLogicCluster.getSddlClusters().get(0);

        if (CollectionUtils.isNotEmpty(sddlCluster.getDbShardingClumn())) {
            Map<String, String> resultMap = new HashMap<>();

            resultMap.putAll(sddlCluster.getDbShardingClumn().parallelStream()
                    .collect(Collectors.toMap(t -> StringUtils.substringBefore(t, "."), Function.identity())));

            if (CollectionUtils.isNotEmpty(sddlCluster.getUniqueKeyForiegns()))
                resultMap.putAll(sddlCluster.getUniqueKeyForiegns().parallelStream()
                        .collect(Collectors.toMap(t -> StringUtils.substringBefore(t, "."), Function.identity())));

            return resultMap;
        }

        throw new SddlInitException("初始化获取MainShardingTableInfos异常：DataSourceCluster.initMainShardingTable, logicCluster:"
                + JSON.toJSONString(sddlLogicCluster));
    }

    private Map<String, String> initAccessoryShardingTable() {
        if (sddlLogicCluster.getSddlClusters().size() == 2) {
            SddlCluster sddlCluster = sddlLogicCluster.getSddlClusters().get(1);

            if (sddlCluster.getDbShardingClumn() != null && sddlCluster.getDbShardingClumn().size() > 0) {
                return sddlCluster.getDbShardingClumn().parallelStream()
                        .collect(Collectors.toMap(t -> StringUtils.substringBefore(t, "."), Function.identity()));
            }

            throw new SddlInitException("初始化获取AccessoryShardingTableInfos异常：DataSourceCluster.initAccessoryTable, logicCluster:"
                    + JSON.toJSONString(sddlLogicCluster));
        } else {
            return new HashMap<>();
        }
    }

    private List<String> initMainShardingTableSuffixs() {
        SddlCluster sddlCluster = sddlLogicCluster.getSddlClusters().get(0);

        return getShardingTableSuffix(sddlCluster);
    }

    private List<String> initAccessoryShardingTableSuffixs() {
        if (sddlLogicCluster.getSddlClusters().size() == 2) {
            SddlCluster sddlCluster = sddlLogicCluster.getSddlClusters().get(1);

            return getShardingTableSuffix(sddlCluster);
        } else {
            return new ArrayList<>();
        }
    }

    private static List<String> getShardingTableSuffix(SddlCluster sddlCluster) {
        Map<Integer, String> mpShardingTableNum = sddlCluster.getDataSource(0).getMpShardingTableNum();

        if (mpShardingTableNum != null && mpShardingTableNum.size() > 0) {
            List<String> valuesList = new ArrayList<String>(mpShardingTableNum.values());

            return removeDuplicate(valuesList);
        } else {
            return null;
        }
    }

    private static List removeDuplicate(List list) {
        HashSet h = new HashSet(list);
        list.clear();
        list.addAll(h);
        return list;
    }
}
