package com.ucar.datalink.domain.event;

import com.ucar.datalink.common.event.CallbackEvent;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;

/**
 * Created by lubiao on 17-6-29.
 */
public class HBaseColumnsEvent extends CallbackEvent {

    private HBaseMediaSrcParameter hbaseParameter;

    private ZkMediaSrcParameter zkParameter;

    private String tableName;

    /**
     * 从hbases服务端取出指定的条数，再根据返回的这些记录数解析hbase表结构
     * 如果有元数据管理平台，从元数据平台取最合适
     */
    private int onceFetchAmount;

    public HBaseColumnsEvent(FutureCallback event, HBaseMediaSrcParameter hbaseParameter,
                             ZkMediaSrcParameter zkParameter, String tableName, int onceFetchAmount) {
        super(event);
        this.hbaseParameter = hbaseParameter;
        this.zkParameter = zkParameter;
        this.tableName = tableName;
        this.onceFetchAmount = onceFetchAmount;
    }

    public HBaseMediaSrcParameter getHbaseParameter() {
        return hbaseParameter;
    }

    public void setHbaseParameter(HBaseMediaSrcParameter hbaseParameter) {
        this.hbaseParameter = hbaseParameter;
    }

    public ZkMediaSrcParameter getZkParameter() {
        return zkParameter;
    }

    public void setZkParameter(ZkMediaSrcParameter zkParameter) {
        this.zkParameter = zkParameter;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getOnceFetchAmount() {
        return onceFetchAmount;
    }

    public void setOnceFetchAmount(int onceFetchAmount) {
        this.onceFetchAmount = onceFetchAmount;
    }

    @Override
    public String toString() {
        return "zkAddress:" + zkParameter.getServers() + "    znode:" + hbaseParameter.getZnodeParent() + "    table name:" + tableName;
    }

}
