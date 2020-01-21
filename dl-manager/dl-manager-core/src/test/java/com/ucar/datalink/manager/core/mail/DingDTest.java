package com.ucar.datalink.manager.core.mail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;

/**
 * Demo class
 *
 * @author Administrator
 * @date 2019/5/27
 */
public class DingDTest {
    private static Log logger = LogFactory.getLog(DingDTest.class);

    private static String DD_URL = "https://oapi.dingtalk.com/robot/send?access_token=e2bf21f6cb8ca0c42786d1e755b7ef126fd8b12213d31319b2e2a30b3ba8df04";

    @Test
    public void sendDingD() throws IOException {

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(DD_URL);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        String textMsg = "{\n" +
                "                        \"msgtype\": \"markdown\",\n" +
                "                        \"markdown\": {\"title\":\"异常报警\",\n" +
                "                        \"text\":\"测试\"\n" +
                "                        },\n" +
                "                        \"at\": {\n" +
                "                        \"atMobiles\": [\n" +
                "                        \"13241643559\"\n" +
                "                        ], \n" +
                "                       \"isAtAll\": false\n" +
                "                        }\n" +
                "                        }\n";
        StringEntity se = new StringEntity(textMsg, "utf-8");
        httppost.setEntity(se);
        HttpResponse response = httpclient.execute(httppost);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            logger.info("报警已经成功发送,,响应消息为:"+result);
        }

    }

}
