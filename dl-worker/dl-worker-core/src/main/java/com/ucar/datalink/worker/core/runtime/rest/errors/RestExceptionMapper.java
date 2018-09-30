package com.ucar.datalink.worker.core.runtime.rest.errors;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.NotFoundException;
import com.ucar.datalink.worker.core.runtime.rest.entities.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RestExceptionMapper implements ExceptionMapper<DatalinkException> {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionMapper.class);

    @Override
    public Response toResponse(DatalinkException exception) {
        log.debug("Uncaught errors in REST call: ", exception);

        if (exception instanceof RestException) {
            RestException restException = (RestException) exception;
            return Response.status(restException.statusCode())
                    .entity(new ErrorMessage(restException.errorCode(), restException.getMessage()))
                    .build();
        }

        if (exception instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), exception.getMessage()))
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getMessage()))
                .build();
    }
}
