package com.ucar.datalink.domain.event;

import com.ucar.datalink.common.event.CallbackEvent;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;

/**
 * Created by user on 17-6-29.
 */
public class HBaseColumnsEvent extends CallbackEvent {

    private HBaseMediaSrcParameter hbaseParameter;

    private ZkMediaSrcParameter zkParameter;

    private String tableName;

    public HBaseColumnsEvent(FutureCallback event, HBaseMediaSrcParameter hbaseParameter, ZkMediaSrcParameter zkParameter,String tableName) {
        super(event);
        this.hbaseParameter = hbaseParameter;
        this.zkParameter = zkParameter;
        this.tableName = tableName;
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


    @Override
    public String toString() {
        return "zkAddress:"+zkParameter.getServers()+"    znode:"+hbaseParameter.getZnodeParent() + "    table name:"+tableName;
    }

}
