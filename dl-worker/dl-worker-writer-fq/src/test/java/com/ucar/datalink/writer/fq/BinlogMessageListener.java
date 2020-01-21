package com.ucar.datalink.writer.fq;

import com.alibaba.fastjson.JSONObject;
import com.zuche.framework.cache.customize.DBTableRowVO;
import com.zuche.framework.metaq.handler.DefaultExecutorMessageListener;
import com.zuche.framework.metaq.vo.MessageVO;
import com.zuche.framework.remote.nio.codec.HessianSerializerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sqq on 2017/5/18.
 */
public class BinlogMessageListener extends DefaultExecutorMessageListener {
    public static final Logger LOGGER = LoggerFactory.getLogger(BinlogMessageListener.class);
    @Override
    public void handlerMessage(MessageVO messageVO) {
        try {
            DBTableRowVO dbTableRowVO;
            byte[] data = messageVO.getData();
            if(data != null && data.length > 0){
                dbTableRowVO = (DBTableRowVO) HessianSerializerUtils.deserialize(data);
                String record = JSONObject.toJSON(dbTableRowVO).toString();
                LOGGER.error("Binlog消息：{}", record);
            }
        }catch (Exception e){
            LOGGER.error("Binlog消息消费异常", e);
        }
    }


}
