package com.ucar.datalink.worker.core.runtime.rest.resources;

import com.ucar.datalink.common.event.CommonEvent;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.common.jvm.JvmSnapshot;
import com.ucar.datalink.common.jvm.JvmUtils;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.worker.api.util.Constants;
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

    private static final String EXECUTE_SHELL = "nohup sh %s/bin/restartup.sh  >>  %s/logs/restartup.log &";

    private static final String WORKER_EXTEND_CONF_DIR = System.getProperty("worker.extend.conf.dir");

    private static final String JAVA_OPTS_PATH = System.getProperty("java.opts.file");

    private static final String JAVA_OPTS_PATH_EXTEND = WORKER_EXTEND_CONF_DIR + "/javaopts";

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
        } catch (Exception e) {
            logger.error("Failure for common event process.", e.getMessage());
        }
    }

    @POST
    @Path("/getJavaOpts/{workerId}")
    public Map<String, String> getJavaOpts(@PathParam("workerId") String workerId) throws Throwable {
        logger.info("getJavaOpts Conf, with workerId " + workerId);

        Map<String, String> result = new HashMap<>();
        try {
            Properties javaopts = PropertiesUtil.getProperties(JAVA_OPTS_PATH);
            Properties javaoptsExtend = PropertiesUtil.getProperties(JAVA_OPTS_PATH_EXTEND);
            Properties javaProperties = javaoptsExtend == null ? javaopts : javaoptsExtend;

            String javaoptsKey = (String) javaProperties.stringPropertyNames().toArray()[0];
            result.put("content", javaoptsKey + "=" + javaProperties.get(javaoptsKey));
        } catch (Exception e) {
            logger.error("getJavaOpts error:", e);
            throw e;
        }
        return result;
    }

    @POST
    @Path("/updateJavaOpts/{workerId}")
    public void updateJavaOpts(@PathParam("workerId") String workerId, final Map<String, String> request) throws Throwable {
        logger.info("updateJavaOpts Conf, with workerId " + workerId);
        String content = request.get("content");

        try {
            Properties javaopts = PropertiesUtil.getProperties(JAVA_OPTS_PATH);
            Properties javaoptsExtend = PropertiesUtil.getProperties(JAVA_OPTS_PATH_EXTEND);
            Properties javaProperties = javaoptsExtend == null ? javaopts : javaoptsExtend;

            String javaoptsKey = (String) javaProperties.stringPropertyNames().toArray()[0];
            String javaoptsContent = javaoptsKey + "=" + javaProperties.get(javaoptsKey);

            if (!content.equals(javaoptsContent)) {
                File extendConfDir = new File(WORKER_EXTEND_CONF_DIR);
                if (!extendConfDir.exists()) {
                    extendConfDir.mkdir();
                }
                PropertiesUtil.updateProperties(JAVA_OPTS_PATH_EXTEND, content);
            } else {
                logger.info("content is same, no need to update.");
            }
        } catch (Exception e) {
            logger.error("updateJavaOpts error:", e);
            throw e;
        }
    }


    @POST
    @Path("/restartWorker/{workerId}")
    public Map<String, String> restartWorker(@PathParam("workerId") String workerId) {
        logger.info("restartWorker, with workerId " + workerId);

        Map<String, String> result = new HashMap<>();
        try {
            String localTestSh = System.getProperty("java.opts.local.sh");

            String shellCommand;
            // 为了本地测试,添加特殊路径
            if (StringUtils.isNotEmpty(localTestSh)) {
                shellCommand = String.format(EXECUTE_SHELL, localTestSh, localTestSh);
            } else {
                String workerHome = System.getProperty(Constants.WORKER_HOME);
                shellCommand = String.format(EXECUTE_SHELL, workerHome, workerHome);
            }

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                try {
                    JavaShellUtil.executeShell(shellCommand);
                } catch (Exception e) {
                    logger.info("JavaShellUtil.executeShell is error, shellCommand:{}! error info:", shellCommand, e);
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
