package com.ucar.datalink.biz.datasource;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.biz.service.LabService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.HttpUtils;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.doublecenter.LabEnum;
import com.ucar.datalink.domain.lab.LabInfo;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSourceHolder {

    private DLinkZkUtils zkUtils;
    private String whoIsCentralUrl;

    public DataSourceHolder(DLinkZkUtils zkUtils,String whoIsCentralUrl){
        this.zkUtils = zkUtils;
        this.whoIsCentralUrl = whoIsCentralUrl;

    }

    public void init(){
        //创建中心机房
        String path = DLinkZkPathDef.centerLab;
        byte[] data = zkUtils.zkClient().readData(path, true);
        //第一次给默认机房
        if(data == null){
            //设置默认机房,json
            String labName = HttpUtils.doGet(whoIsCentralUrl);
            if(StringUtils.isBlank(labName)){
                labName = LabEnum.logicA.getCode();
            }
            Map<String,String> map = new HashMap<String,String>();
            map.put(Constants.WHOLE_SYSTEM,labName);
            byte[] bytes = JSON.toJSONBytes(map);
            try {
                zkUtils.zkClient().createPersistent(path, bytes, true);
            } catch (ZkNodeExistsException e) {
                //do nothing
            } catch (Exception e) {
                throw new DatalinkException("在zk上创建数据源节点出错", e);
            }
        }

        //初始化机房信息到zk
        LabService labService = DataLinkFactory.getObject(LabService.class);
        List<LabInfo> list = labService.findLabList();
        String labListPath = DLinkZkPathDef.labInfoList;
        byte[] labListData = zkUtils.zkClient().readData(labListPath, true);
        if(labListData == null){
            // 序列化，就此处数据源是写死的，其他的用数据库中的
            byte[] bytes = JSON.toJSONBytes(list);
            try {
                zkUtils.zkClient().createPersistent(labListPath, bytes, true);
            } catch (ZkNodeExistsException e) {
                //do nothing
            } catch (Exception e) {
                throw new DatalinkException("在zk上创建数据源节点出错", e);
            }
        }

    }

}
