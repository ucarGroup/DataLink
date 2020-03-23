package com.ucar.datalink.flinker.core.admin.rest;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.flinker.api.util.GsonUtil;
import com.ucar.datalink.flinker.core.RunningData;
import com.ucar.datalink.flinker.core.admin.Command;
import com.ucar.datalink.flinker.core.admin.ProcessUtils;
import com.ucar.datalink.flinker.core.job.meta.State;
import com.ucar.datalink.flinker.core.util.container.CoreConstant;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2017/7/11.
 */
public class JobControllerUtil {

    private static Logger logger = LoggerFactory.getLogger(JobControllerUtil.class);


    public static void startJob(final ExtendCommand command) {
        String fileName = MessageFormat.format("{0}/{1}", CoreConstant.DATAX_JOB_HOME, command.getJobName());
        try {
            FileUtils.writeStringToFile( new File(fileName), command.getJob_content());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] cmdArray = getCmdArray(command);
                logger.info("command line is : " + cmdArray==null ? "": Arrays.toString(cmdArray));

                try {
                    Process proc = Runtime.getRuntime().exec(cmdArray);
                    logger.info("process is started for job : {}", command.getJobName());

                    int exitValue = proc.waitFor();
                    if (0 != exitValue) {
                        logProcessError(proc, command.getJobName());
                    }
                    logger.info("process is stopped for job : {}", command.getJobName());
                } catch (Exception e) {
                    logger.error("an error occurred when handle a process with command " + command, e);
                }

            }
        });
        t.start();
    }

        private static String[] getCmdArray(Command command) {
            List<String> cmdList = new ArrayList<String>();
            //cmdList.add("sudo");
            cmdList.add("python");
            cmdList.add(MessageFormat.format("{0}/datax.py", CoreConstant.DATAX_BIN_HOME));
            if (StringUtils.isNotBlank(command.getJvmArgs())) {
                cmdList.add("-j");
                cmdList.add(MessageFormat.format("{0} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath={1}/log", command.getJvmArgs(),
                        CoreConstant.DATAX_HOME));
            }
            cmdList.add("--jobid");
            cmdList.add(command.getJobId().toString());
            cmdList.add("--timingJobId");
            cmdList.add(command.getTimingJobId().toString());
            cmdList.add("--jqeid");
            cmdList.add(command.getJobQueueExecutionId().toString());
            if (command.isDebug()) {
                cmdList.add("-d");
            }
        if (command.isDynamicParam()) {
            logger.error("------isDynamicParam------"+ GsonUtil.toJson(command));
            buildDynParam(command.getMapParam(),cmdList);
        }
    cmdList.add(MessageFormat.format("{0}/{1}", CoreConstant.DATAX_JOB_HOME, command.getJobName()));

    int size = cmdList.size();
    return cmdList.toArray(new String[size]);
    }

    private static void buildDynParam(Map<String,String> mapParam,List<String> cmdList){
        if(mapParam==null || mapParam.size()==0){
            return;
        }
        cmdList.add("-p");
        StringBuffer buf = new StringBuffer();
        int i=0;
        for (Map.Entry<String, String> entry : mapParam.entrySet()) {
            i++;
            if(i==mapParam.size()){
                buf.append("-D" + entry.getKey() + "=" + entry.getValue());
            }else{
                buf.append("-D"+entry.getKey()+"="+entry.getValue()).append(" ");
            }
        }
        cmdList.add(buf.toString());
    }


    private static void logProcessError(Process proc, String jobName) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String errString = "process failed for job " + jobName + ".Error Details:\n";
            String line = null;

            while ((line = br.readLine()) != null) {
                errString = errString.concat(line).concat("\n");
            }
            logger.error(errString);
        } catch (Exception e) {
            logger.error("something goew wrong when get process error message.", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public static void stopJob(final Command command) {
        try {
            String data = null;
            byte[] bytes = "".getBytes();
            if (bytes != null) {
                data = new String(bytes);
            }
            if (StringUtils.isNotEmpty(data)) {
                RunningData runningData = JSONObject.parseObject(data, RunningData.class);
                if (command.isForceStop()) {
                    if (ProcessUtils.checkIfJobProcessExists(runningData.getPid())) {
                        ProcessUtils.killProcess(Integer.valueOf(runningData.getPid()));
                        return;
                    }
                    logger.info("the process with pid {} does not exist.", runningData.getPid());
                } else {
                    runningData.setState(State.KILLING);
                }
            }
        } catch (Exception e) {
            logger.error("something goes wrong when stop job : ." + command.getJobName(), e);
        }
    }

}
