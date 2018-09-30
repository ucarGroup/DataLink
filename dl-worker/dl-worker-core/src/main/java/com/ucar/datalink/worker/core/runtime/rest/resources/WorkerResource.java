package com.ucar.datalink.worker.core.runtime.rest.resources;

import com.ucar.datalink.common.event.CommonEvent;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.common.jvm.JvmSnapshot;
import com.ucar.datalink.common.jvm.JvmUtils;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.worker.core.util.JavaShellUtil;
import com.ucar.datalink.worker.core.util.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sqq on 2017/7/7.
 */
@Path("/worker")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkerResource {
    private static final Logger logger = LoggerFactory.getLogger(WorkerResource.class);

    private static final String executeShell = "nohup sh /usr/local/frame/dl-worker/bin/restartup.sh  >> /usr/local/frame/dl-worker/logs/restartup.log &";

    @POST
    @Path("/toEditLogback/{workerId}")
    public Map<String, String> toEditLogback(@PathParam("workerId") String workerId) throws Throwable {
        logger.info("Receive a request to edit logback.xml, with workerId " + workerId);
        String path = System.getProperty("logback.configurationFile");
        SAXReader reader = new SAXReader();

        Map<String, String> result = new HashMap<>();
        try {
            Document document = reader.read(new File(path));
            result.put("content", document.asXML());
            return result;
        } catch (DocumentException e) {
            logger.info("reading logback.xml error:", e);
            result.put("content", e.getMessage());
        }
        return result;
    }

    @POST
    @Path("/doEditLogback/{workerId}")
    public void doEditLogback(@PathParam("workerId") String workerId, final Map<String, String> request) throws Throwable {
        String content = request.get("content");
        logger.info("Receive a request to save logback.xml, with workerId " + workerId + "\r\n with content " + content);

        String path = System.getProperty("logback.configurationFile");
        File file = new File(path);
        FileWriter fw;
        BufferedWriter bw = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), false);
            bw = new BufferedWriter(fw);
            bw.write(content);
            bw.flush();
        } catch (Exception e) {
            logger.info("writing logback.xml error:", e.getMessage());
        } finally {
            if (bw != null)
                bw.close();
        }
    }

    @POST
    @Path("/eventProcess")
    public void processCommonEvent(final Map<String, Object> request) throws Throwable {
        logger.info("Receive a request for common event process" + request);

        try {
            String eventName = String.valueOf(request.get(CommonEvent.EVENT_NAME_KEY));
            if (StringUtils.isBlank(eventName)) {
                throw new RuntimeException("event name can not be null.");
            }
            CommonEvent event = new CommonEvent(new FutureCallback(), eventName, request);
            EventBusFactory.getEventBus().post(event);
            //event.getCallback().get();不等待，直接返回
        } catch (Exception e) {
            logger.error("Failure for common event process.", e.getMessage());
        }
    }

    @POST
    @Path("/getJavaOpts/{workerId}")
    public Map<String, String> getJavaOpts(@PathParam("workerId") String workerId) throws Throwable {
        logger.info("getJavaOpts Conf, with workerId " + workerId);

        String path = System.getProperty("java.opts.file");

        Map<String, String> result = new HashMap<>();
        try {

            Properties javaProperties = PropertiesUtil.getProperties(path);
            String javaoptsKey = (String) javaProperties.stringPropertyNames().toArray()[0];
            result.put("content", javaoptsKey + " " + javaProperties.get(javaoptsKey));
        } catch (Exception e) {
            logger.info("getJavaOpts error:", e);
            result.put("content", e.getMessage());
        }
        return result;
    }

    @POST
    @Path("/updateJavaOpts/{workerId}")
    public void updateJavaOpts(@PathParam("workerId") String workerId, final Map<String, String> request) throws Throwable {
        logger.info("updateJavaOpts Conf, with workerId " + workerId);

        String path = System.getProperty("java.opts.file");
        String content = request.get("content");

        try {

            PropertiesUtil.updateProperties(path, content);
        } catch (Exception e) {
            logger.info("updateJavaOpts error:", e);
        }

    }


    @POST
    @Path("/restartWorker/{workerId}")
    public Map<String, String> restartWorker(@PathParam("workerId") String workerId) {
        logger.info("restartWorker, with workerId " + workerId);

        Map<String, String> result = new HashMap<>();
        try {
            String optsSh = System.getProperty("java.opts.local.sh");

            String shellCommand = executeShell;
            // 为了本地测试,添加特殊路径
            if (StringUtils.isNotEmpty(optsSh)) {
                shellCommand = "nohup sh " + optsSh + "bin/restartup.sh  >> " + optsSh + "logs/restartup.log &";
            }

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            String finalShellCommand = shellCommand;
            executorService.submit(() -> {

                try {
                    JavaShellUtil.executeShell(finalShellCommand);
                } catch (Exception e) {
                    logger.info("JavaShellUtil.executeShell is error, shellCommand:{}! error info:", finalShellCommand, e);
                }
            });

            result.put("content", "重启指令发送完成！");
        } catch (Exception e) {
            logger.info("restartWorker error:", e);
            result.put("content", e.getMessage());
        }
        return result;
    }

    @POST
    @Path("/getJvmSnapshot/{workerId}")
    public JvmSnapshot getJvmSnapshot(@PathParam("workerId") String workerId) {
        return JvmUtils.buildJvmSnapshot();
    }
}
