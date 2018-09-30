package com.ucar.datalink.worker.core.runtime.rest.resources;

/**
 * Created by user on 2017/6/30.
 */

import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.EventBus;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.event.*;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.domain.vo.HBaseParameterVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Path("/hbase")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HBaseMetaResource {

    private static final Logger logger = LoggerFactory.getLogger(HBaseMetaResource.class);



    @POST
    @Path("/getTables")
    public String getTables(HBaseParameterVO vo) {
        if(logger.isDebugEnabled()) {
            logger.debug("start getTables method");
        }
        String check = check(vo);
        if( !StringUtils.isEmpty(check) ) {
            return check;
        }
        EventBus eventBus = EventBusFactory.getEventBus();
        HBaseMediaSrcParameter hbaseParameter = new HBaseMediaSrcParameter();
        hbaseParameter.setZnodeParent(vo.getZnode());
        ZkMediaSrcParameter zkParameter = new ZkMediaSrcParameter();
        zkParameter.setServers(vo.getZkAddress());

        HBaseTablesEvent event = new HBaseTablesEvent(new FutureCallback(),hbaseParameter,zkParameter);
        eventBus.post(event);
        try {
            List<MediaMeta> tables = (List<MediaMeta>)event.getCallback().get();
            String result = JSONObject.toJSONString(tables);
            return result;
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            if(logger.isDebugEnabled()) {
                logger.debug("execute getTables method success");
            }
        }
        return "{}";
    }


    @POST
    @Path("/getColumns")
    public String getColumns(HBaseParameterVO vo) {
        if(logger.isDebugEnabled()) {
            logger.debug("start getColumns method");
        }
        String check = check(vo);
        if( !StringUtils.isEmpty(check) ) {
            return check;
        }
        EventBus eventBus = EventBusFactory.getEventBus();
        HBaseMediaSrcParameter hbaseParameter = new HBaseMediaSrcParameter();
        hbaseParameter.setZnodeParent(vo.getZnode());
        ZkMediaSrcParameter zkParameter = new ZkMediaSrcParameter();
        zkParameter.setServers(vo.getZkAddress());
        String tableName = vo.getTableName();

        if(tableName==null) {
            return "{}";
        }
        HBaseColumnsEvent event = new HBaseColumnsEvent(new FutureCallback(),hbaseParameter,zkParameter,tableName);
        eventBus.post(event);
        try {
            List<ColumnMeta> columns = (List<ColumnMeta>)event.getCallback().get();
            String result = JSONObject.toJSONString(columns);
            return result;
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            if(logger.isDebugEnabled()) {
                logger.debug("execute getColumns method success");
            }
        }
        return "{}";
    }


    /**
     * 这个接口用来检查指定的HBase MediaSourceInfo是否连接正常
     * @return
     */
    @POST
    @Path("/check/")
    public String checkConn(HBaseParameterVO vo) {
        if(logger.isDebugEnabled()) {
            logger.debug("start getColumns method");
        }
        String check = check(vo);
        if( !StringUtils.isEmpty(check) ) {
            return check;
        }
        EventBus eventBus = EventBusFactory.getEventBus();
        HBaseMediaSrcParameter hbaseParameter = new HBaseMediaSrcParameter();
        hbaseParameter.setZnodeParent(vo.getZnode());
        ZkMediaSrcParameter zkParameter = new ZkMediaSrcParameter();
        zkParameter.setServers(vo.getZkAddress());
        String tableName = vo.getTableName();

        HBaseConnCheckEvent event = new HBaseConnCheckEvent(new FutureCallback(),hbaseParameter,zkParameter);
        eventBus.post(event);
        try {
            String result = (String)event.getCallback().get();
            return result;
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            if(logger.isDebugEnabled()) {
                logger.debug("execute getColumns method success");
            }
        }
        return "failure";

    }


    @POST
    @Path("/count")
    public String caclRegionCount(HBaseParameterVO vo) {
        if(logger.isDebugEnabled()) {
            logger.debug("start getColumns method");
        }
        String check = check(vo);
        if( !StringUtils.isEmpty(check) ) {
            return check;
        }
        EventBus eventBus = EventBusFactory.getEventBus();
        HBaseMediaSrcParameter hbaseParameter = new HBaseMediaSrcParameter();
        hbaseParameter.setZnodeParent(vo.getZnode());
        ZkMediaSrcParameter zkParameter = new ZkMediaSrcParameter();
        zkParameter.setServers(vo.getZkAddress());
        String tableName = vo.getTableName();
        if(tableName==null) {
            return "-1";
        }
        HBaseRegionCountEvent event = new HBaseRegionCountEvent(new FutureCallback(),hbaseParameter,zkParameter,tableName);
        eventBus.post(event);
        try {
            Integer count = (Integer)event.getCallback().get();
            return count.toString();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            if(logger.isDebugEnabled()) {
                logger.debug("execute getColumns method success");
            }
        }
        return "-1";
    }

    @POST
    @Path("/split")
    public String generateHBaseSplitInfo(HBaseParameterVO vo) {
        if(logger.isDebugEnabled()) {
            logger.debug("start getColumns method");
        }
        String check = check(vo);
        if( !StringUtils.isEmpty(check) ) {
            return check;
        }
        EventBus eventBus = EventBusFactory.getEventBus();
        HBaseMediaSrcParameter hbaseParameter = new HBaseMediaSrcParameter();
        hbaseParameter.setZnodeParent(vo.getZnode());
        ZkMediaSrcParameter zkParameter = new ZkMediaSrcParameter();
        zkParameter.setServers(vo.getZkAddress());
        String tableName = vo.getTableName();
        int splitCount = vo.getSplitCount();
        HBaseSplitEvent event = new HBaseSplitEvent(new FutureCallback(),hbaseParameter,zkParameter,tableName,splitCount);
        eventBus.post(event);
        try {
            Map<String,Object> map = (Map<String,Object>)event.getCallback().get();
            return JSONObject.toJSONString(map, true);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            if(logger.isDebugEnabled()) {
                logger.debug("execute getColumns method success");
            }
        }
        return JSONObject.toJSONString(new HashMap<String,Object>(), true);
    }



    private String check(HBaseParameterVO vo) {
        if(vo == null) {
            return "{\"parameter vo is emtpy\"}";
        }
        if( StringUtils.isEmpty(vo.getZkAddress()) ) {
            return "{\"parameter zk address is empty\"}";
        }

//        if( StringUtils.isEmpty(vo.getPort()) ) {
//            return "{\"parameter zk port is empty\"}";
//        }
        if( StringUtils.isEmpty(vo.getZnode()) ) {
            return "{\"parameter znode is empty\"}";
        }
        return "";
    }

}
