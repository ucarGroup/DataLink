package com.ucar.datalink.flinker.core.admin.rest;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.ZkClientX;
import com.ucar.datalink.domain.job.FlinkerMachineInfo;
import com.ucar.datalink.flinker.api.util.HostUtils;
import com.ucar.datalink.flinker.core.admin.Command;
import com.ucar.datalink.flinker.core.admin.DataxMachineUtil;
import com.ucar.datalink.flinker.core.admin.JobRunningController;
import com.ucar.datalink.flinker.core.admin.bean.FlowControlData;
import com.ucar.datalink.flinker.core.admin.record.JobExecution;
import com.ucar.datalink.flinker.core.admin.record.JobExecutionDbUtils;
import com.ucar.datalink.flinker.core.job.meta.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

/**
 * Created by user on 2017/7/11.
 */

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DataxResource {

    private static final Logger LOG = LoggerFactory.getLogger(DataxResource.class);

    private static JobRunningController jobRunningController;

    private static ZkClientX zkClient;

    private static final String COMMAND_ERROR_MESSAGE = "{\"msg\":\"command invalid\"}";

    private static final String SUCCESS_MESSAGE = "{\"msg\":\"success\"}";


    public static void setJobRunningController(JobRunningController jobRunningController) {
        DataxResource.jobRunningController = jobRunningController;
    }

    public static void setZkClient(ZkClientX zkClient) {
        DataxResource.zkClient = zkClient;
    }


    @POST
    @Path("/start")
    public Response start(String command) {
        try {
            Command comm = JSONObject.parseObject(command,Command.class);
            LOG.info("[DataxResource]create job executeion "+comm);
            long id = createJobExecution(comm);
            comm.setExecuteId(id);
            execute(comm);
            LOG.info("[DataxResource]execute ok "+comm);
            return Response.status(200).entity(id+"").build();
        } catch(Exception e) {
            LOG.error("DataxResource start failure!",e);
            return Response.status(400).entity("{\"msg\":\"failure->" + e.getMessage() + "\"}").build();
        }
    }


    @POST
    @Path("/stop")
    public Response stop(String command) {
        try {
            Command comm = JSONObject.parseObject(command,Command.class);
            LOG.warn("stop->"+comm.toString());
            execute(comm);
            return Response.status(200).entity(SUCCESS_MESSAGE).build();
        } catch(Exception e) {
            LOG.error("DataxResource stop failure!",e);
            return Response.status(400).entity("{\"msg\":\"" + e.getMessage() + "\"}").build();
        }
    }


    @POST
    @Path("/forcestop")
    public Response forceStop(String command) {
        try {
            Command comm = JSONObject.parseObject(command,Command.class);
            LOG.warn("force_stop->"+comm.toString());
            execute(comm);
            return Response.status(200).entity(SUCCESS_MESSAGE).build();
        } catch(Exception e) {
            LOG.error("DataxResource forcestop failure!",e);
            return Response.status(400).entity("{\"msg\":\"" + e.getMessage() + "\"}").build();
        }
    }


    @POST
    @Path("/state")
    public Response machineState() {
        FlinkerMachineInfo info = new FlinkerMachineInfo();
        try {
            long total = DataxMachineUtil.getMachineTotalMemory();
            long free = DataxMachineUtil.getMachineFreeMemory();
            info.setTotalMemory(total);
            info.setFreeMemory(free);
            String result = JSONObject.toJSONString(info);
            return Response.status(200).entity(result).build();
        } catch(Exception e) {
            LOG.error("DataxResource state failure!",e);
            String content = JSONObject.toJSONString(info);
            return Response.status(400).entity(content).build();
        }
    }


    @GET
    @Path("/flow")
    public Response flowControlRecordBySecond(String command) {
        try {
            FlowControlData data = JSONObject.parseObject(command,FlowControlData.class);
            String jsonString = JSONObject.toJSONString(data);
            String flowPath = DLinkZkPathDef.getMonitorNode(HostUtils.IP);
            zkClient.writeData(flowPath,jsonString);
            String result = JSONObject.toJSONString("ok");
            return Response.status(200).entity(result).build();
        } catch(Exception e) {
            LOG.error("DataxResource state failure!",e);
            String content = JSONObject.toJSONString(e.getMessage());
            return Response.status(400).entity(content).build();
        }
    }


    private void execute(Command command) {
        if(command==null ) {
            throw new RuntimeException("command is empty");
        }
        jobRunningController.handleCommand(command);
    }


    private Response template(Command command, Executor execute) {
        try {
            if(command==null ) {
                return Response.status(400).entity(COMMAND_ERROR_MESSAGE).build();
            }
            jobRunningController.handleCommand(command);
        } catch(Exception e) {
            LOG.error(e.getMessage(),e);
            return Response.status(400).entity("{\"msg\":\"" + e.getMessage() + "\"}").build();
        }
        return Response.status(204).entity(SUCCESS_MESSAGE).build();
    }


    interface Executor {
        void execute(int id);
    }


    private static long createJobExecution(Command command) throws SQLException {
        JobExecution execution = new JobExecution();
        execution.setJobId(command.getJobId());
        execution.setState(State.UNEXECUTE);
        execution.setStartTime(System.currentTimeMillis());
        execution.setJobQueueExecutionId(-1L);
        execution.setPid(-1);
        JobExecutionDbUtils.insertJobExecution(execution);
        long id = execution.getId();
        return id;
    }


}
