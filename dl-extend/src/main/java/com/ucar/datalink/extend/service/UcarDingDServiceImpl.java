package com.ucar.datalink.extend.service;

import com.ucar.datalink.biz.service.UcarAlarmPlugin;
import com.ucar.datalink.domain.alarm.AlarmMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 发送钉钉
 */
@Service
public class UcarDingDServiceImpl implements UcarAlarmPlugin {

    private static final Logger logger = LoggerFactory.getLogger(UcarDingDServiceImpl.class);

    private static String DD_URL = "https://oapi.dingtalk.com/robot/send?access_token=6cd01f33b373558753cef3d9fe10035697cd52dc4f8e014eb2a3ddd7ca68d077";

    @Override
    public String getPluginName() {
        return "dingD";
    }

    @Override
    public void doSend(AlarmMessage message) {
        try {
            String textMsg = "{" +
                    "\"msgtype\": \"markdown\"," +
                    "\"markdown\": {\"title\":\""+message.getSubject()+"\"," +
                    "\"text\":\""+message.getContent()+"\"" +
                    "}," +
                    "\"at\": {" +
                    "\"atMobiles\": [" +
                    "\"13241643559\"" +
                    "]," +
                    " \"isAtAll\": false" +
                    "}" +
                    "}";

            String result = com.ucar.datalink.common.utils.HttpUtils.doPost(DD_URL,textMsg);
            logger.info("钉钉报警返回结果:"+result);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
}
