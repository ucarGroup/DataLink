package com.ucar.datalink.worker.core.runtime.rest.resources;


import com.google.common.eventbus.EventBus;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.event.EsColumnSyncEvent;
import com.ucar.datalink.domain.event.KuduColumnSyncEvent;
import com.ucar.datalink.domain.event.KuduConfigClearEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * created by swb on 2018/12/04
 */
@Path("/kudu")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KuduOperatorResource {
    private static final Logger logger = LoggerFactory.getLogger(KuduOperatorResource.class);

    @POST
    @Path("/syncCloumn/{mediaSourceId}")
    public Object syncCloumns(@PathParam("mediaSourceId") Long mediaSourceId, final Map<String, Object> request){
        try {
            String sql = (String) request.get("sql");
            Long mappingId = Long.valueOf((Integer)request.get("mappingId"));
            EventBus eventBus = EventBusFactory.getEventBus();
            KuduColumnSyncEvent event = new KuduColumnSyncEvent(new FutureCallback(),mediaSourceId,mappingId,sql);
            eventBus.post(event);

            //清除缓存
            KuduConfigClearEvent kuduConfigClearEvent = new KuduConfigClearEvent(new FutureCallback(),null);
            eventBus.post(kuduConfigClearEvent);
            return  event.getCallback().get();
        } catch (InterruptedException e) {
            logger.info("中断异常 {}",e);
        } catch (ExecutionException e) {
            logger.info("执行异常 {}",e);
        }catch (Exception e){
            logger.info("其他异常 {}",e);
        }
        return null;
    }



}
