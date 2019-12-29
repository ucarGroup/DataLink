package com.ucar.datalink.worker.core.runtime.rest.resources;

import com.ucar.datalink.common.errors.RebalanceNeededException;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.Position;
import com.ucar.datalink.worker.core.runtime.Keeper;
import com.ucar.datalink.worker.core.runtime.rest.errors.RestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TasksResource {
    private static final long REQUEST_TIMEOUT_MS = 90 * 1000;
    private static final Logger logger = LoggerFactory.getLogger(TasksResource.class);

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
        logger.info("Receive a request for restart task,with id " + task);
        FutureCallback<Void> cb = new FutureCallback<>();
        keeper.restartTask(task, position, cb);
        waitForComplete(cb);
        logger.info("Restart task succeed,with id " + task);
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
