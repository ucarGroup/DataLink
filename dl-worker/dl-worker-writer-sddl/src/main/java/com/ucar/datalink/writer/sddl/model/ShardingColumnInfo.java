package com.ucar.datalink.writer.sddl.model;

/**
 * @Description:
 *
 * @Author : yongwang.chen@ucarinc.com
 * @Date   : 11:14 AM 15/11/2017
 */
public class ShardingColumnInfo {

    private String shardingClumnName;

    private ClumnShardingTypeEnum clumnShardingTypeEnum;

    public String getShardingClumnName() {
        return shardingClumnName;
    }

    public void setShardingClumnName(String shardingClumnName) {
        this.shardingClumnName = shardingClumnName;
    }

    public ClumnShardingTypeEnum getClumnShardingTypeEnum() {
        return clumnShardingTypeEnum;
    }

    public void setClumnShardingTypeEnum(ClumnShardingTypeEnum clumnShardingTypeEnum) {
        this.clumnShardingTypeEnum = clumnShardingTypeEnum;
    }
}
