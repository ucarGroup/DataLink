package com.ucar.datalink.worker.core.runtime.rest.resources;

import com.google.common.eventbus.EventBus;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.event.bi.RdbmsCountEvent;
import com.ucar.datalink.domain.vo.RdbmsOperatorVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by user on 2018/4/2.
 */

@Path("/rdbms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RdbmsOperatorResource {

    private static final Logger logger = LoggerFactory.getLogger(RdbmsOperatorResource.class);


    @POST
    @Path("/count")
    public String count(RdbmsOperatorVO vo) {
        logger.debug("start count method");
        EventBus eventBus = EventBusFactory.getEventBus();
        RdbmsCountEvent event = new RdbmsCountEvent(new FutureCallback(),vo.getMediaSourceId(),vo.getSql());
        eventBus.post(event);
        try {
            String result = (String)event.getCallback().get();
            return result;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            logger.debug("execute count method success");
        }
        return "-1";
    }

}
