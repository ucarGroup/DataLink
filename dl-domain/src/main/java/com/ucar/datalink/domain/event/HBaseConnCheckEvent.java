package com.ucar.datalink.domain.event;

import com.ucar.datalink.common.event.CallbackEvent;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;

/**
 * Created by user on 2017/7/5.
 */
public class HBaseConnCheckEvent extends CallbackEvent {

    private HBaseMediaSrcParameter hbaseParameter;

    private ZkMediaSrcParameter zkParameter;

    public HBaseConnCheckEvent(FutureCallback event, HBaseMediaSrcParameter hbaseParameter, ZkMediaSrcParameter zkParameter) {
        super(event);
        this.hbaseParameter = hbaseParameter;
        this.zkParameter = zkParameter;
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

    @Override
    public String toString() {
        return "zkAddress:"+zkParameter.getServers()+"    znode:"+hbaseParameter.getZnodeParent();
    }

}
