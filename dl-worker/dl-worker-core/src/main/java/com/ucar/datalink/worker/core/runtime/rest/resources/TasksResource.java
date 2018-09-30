package com.ucar.datalink.worker.core.runtime.rest.resources;

import com.google.common.collect.Lists;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.NotAssignedException;
import com.ucar.datalink.common.errors.RebalanceNeededException;
import com.ucar.datalink.domain.Position;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.worker.core.runtime.Keeper;
import com.ucar.datalink.worker.core.runtime.rest.errors.RestException;
import com.ucar.datalink.common.utils.FutureCallback;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TasksResource {
    private static final Logger log = LoggerFactory.getLogger(TasksResource.class);
    private static final long REQUEST_TIMEOUT_MS = 90 * 1000;

    private final Keeper keeper;
    @javax.ws.rs.core.Context
    private ServletContext context;

    public TasksResource(Keeper keeper) {
        this.keeper = keeper;
    }

    @POST
    @Path("/{task}/restart")
    public void restartTask(final @PathParam("task") String task,
                            final Position position) throws Throwable {
        FutureCallback<Void> cb = new FutureCallback<>();
        keeper.restartTask(task, position, cb);
        waitForComplete(cb);
    }

    @POST
    @Path("/{taskId}/clearMediaMappingCache")
    public void clearMediaMappingCache(final @PathParam("taskId") Long taskId) throws Throwable {
        MediaService mediaService = DataLinkFactory.getObject("mediaServiceImpl");
        mediaService.clearMediaMappingCache(taskId);
        log.info("Clear media mapping successfully for task " + taskId);

        TaskConfigService taskConfigService = DataLinkFactory.getObject(TaskConfigService.class);
        List<TaskInfo> followerTasks = taskConfigService.getFollowerTasksForLeaderTask(taskId);
        List<Long> tasks = Lists.newArrayList(taskId);
        if (CollectionUtils.isNotEmpty(followerTasks)) {
            followerTasks.stream().forEach(t -> tasks.add(t.getId()));
        }

        Map<Long, FutureCallback<Void>> cbs = new HashMap<>();
        tasks.stream().forEach(t -> {
            FutureCallback<Void> cb = new FutureCallback<>();
            keeper.restartTask(String.valueOf(t), null, cb);
            cbs.put(t, cb);
        });

        List<Throwable> exceptions = new ArrayList<>();
        cbs.entrySet().stream().forEach(c -> {
            try {
                waitForComplete(c.getValue());
                log.info("Restart Task successfully after clearing media mapping for task " + c.getKey());
            } catch (NotAssignedException ex) {
                log.info("Task {} is not assigned to this worker,not need restart after clearing media mapping.", c.getKey());
            } catch (Throwable tt) {
                log.error("Restart Task Failed for task " + c.getKey(), tt);
                exceptions.add(tt);
            }
        });

        if (exceptions.size() > 0) {
            throw exceptions.get(0);
        }
    }

    // Wait for a FutureCallback to complete.
    private <T, U> T waitForComplete(FutureCallback<T> cb) throws Throwable {
        try {
            return cb.get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof RebalanceNeededException) {
                throw new RestException(Response.Status.CONFLICT.getStatusCode(),
                        "Cannot complete request momentarily due to stale configuration (typically caused by a concurrent config change)");
            }

            throw cause;
        } catch (TimeoutException e) {
            // This timeout is for the operation itself. None of the timeout error codes are relevant, so internal server
            // error is the best option
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Request timed out");
        } catch (InterruptedException e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Request interrupted");
        }
    }

}
