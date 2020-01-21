package com.ucar.datalink.manager.core.web.controller.sync;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.meta.MetaManager;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.SyncRelationService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.relationship.SqlCheckResult;
import com.ucar.datalink.domain.relationship.SqlCheckTree;
import com.ucar.datalink.domain.relationship.SyncNode;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.vo.ResponseVo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.dto.syncRelation.TreeView;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2017/5/27.
 */
@Controller
@RequestMapping(value = "/sync/relation/")
public class SyncRelationController {

    private static final Logger logger = LoggerFactory.getLogger(SyncRelationController.class);

    @Autowired
    private MediaSourceService mediaSourceService;

    @Autowired
    private SyncRelationService syncRelationService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private TaskConfigService taskConfigService;

    @RequestMapping(value = "/show")
    public ModelAndView show() {
        ModelAndView mav = new ModelAndView("sync/relation/show");
        List<MediaSourceType> types = new ArrayList<MediaSourceType>();
        types.add(MediaSourceType.MYSQL);
        types.add(MediaSourceType.SDDL);
        types.add(MediaSourceType.SQLSERVER);
        types.add(MediaSourceType.POSTGRESQL);
        mav.addObject("mediaSourceList", mediaService.findMediaSourcesForSingleLab(types));
        return mav;
    }

    /**
     * 同步检测
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/getTrees")
    @ResponseBody
    public List<SyncNode> getTrees(@RequestBody Map<String, String> map) {
        Long mediaSourceId = Long.valueOf(map.get("mediaSourceId"));
        String mediaName = map.get("mediaName");
        return syncRelationService.getSyncRelationTrees(mediaSourceId == -1L ? null : mediaSourceId, mediaName);
    }

    @RequestMapping(value = "/toCheckSql")
    public ModelAndView toCheckSql() {
        ModelAndView mav = new ModelAndView("sync/relation/checkSql");
        List<MediaSourceType> types = new ArrayList<MediaSourceType>();
        types.add(MediaSourceType.MYSQL);
        types.add(MediaSourceType.SDDL);
        types.add(MediaSourceType.SQLSERVER);
        types.add(MediaSourceType.POSTGRESQL);
        mav.addObject("mediaSourceList", mediaService.findMediaSourcesForSingleLab(types));
        return mav;
    }

    /**
     * 脚本检测
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/checkSql")
    @ResponseBody
    public List<TreeView> checkSql(@RequestBody Map<String, String> map) {
        long currentTime = System.currentTimeMillis();
        Long mediaSourceId = Long.valueOf(map.get("mediaSourceId"));
        String sqls = map.get("sqls");
        List<SqlCheckResult> results = syncRelationService.checkSqls(mediaSourceId, sqls);
        if (!results.isEmpty()) {
            return buildTreeViews(results);
        }
        logger.info("脚本检测(checkSql),共花费{}秒。",(System.currentTimeMillis() - currentTime) / 1000);
        return Lists.newArrayList();
    }

    /**
     * 给dbms提供的接口
     *
     * @return
     */
    @RequestMapping(value = "/checkSql_4_dbms")
    @ResponseBody
    @LoginIgnore
    public List<TreeView> checkSql_4_dbms(String ip, int port, String schema, String sqls) {
        logger.info(String.format("Receive a check request: \r\n IP is %s , \r\n Port is %s, \r\n Schema is %s, \r\n sqls is %s",
                ip, port, schema, sqls));
        long currentTime = System.currentTimeMillis();
        try {
            Long mediaSourceId = null;

            List<MediaSourceInfo> rdbmsMediaSources = mediaSourceService.getListByType(
                    Sets.newHashSet(MediaSourceType.MYSQL, MediaSourceType.SQLSERVER, MediaSourceType.POSTGRESQL));
            for (MediaSourceInfo msInfo : rdbmsMediaSources) {
                if (isMatch(msInfo, ip, port, schema)) {
                    mediaSourceId = msInfo.getId();
                    break;
                }
            }
            if (mediaSourceId != null) {
                List<SqlCheckResult> results = syncRelationService.checkSqls(mediaSourceId, sqls);
                if (!results.isEmpty()) {
                    return buildTreeViews(results);
                } else {
                    return Lists.newArrayList();
                }
            }
            return Lists.newArrayList();
        } catch (Exception e) {
            logger.error("Sql Check Error:", e);
            throw e;
        }finally {
            logger.info("脚本检测(checkSql_4_dbms),共花费{}秒。",(System.currentTimeMillis() - currentTime) / 1000);
        }
    }

    /**
     * 生成添加字段的sql
     *
     * @param mappingId
     * @param columnName
     * @return
     */
    @RequestMapping(value = "/generateAddColumnSql")
    @ResponseBody
    public Object generateAddColumnSql(Long mappingId, String columnName) {

        Map<String,Object> resultMap = new HashMap<String,Object>();
        MediaMappingInfo mappingInfo = mediaService.findMediaMappingsById(mappingId);
        try{
            List<ColumnMeta> list = MetaManager.getColumns(mappingInfo.getSourceMedia().getMediaSource(), mappingInfo.getSourceMedia().getName());
            for (ColumnMeta columnMeta : list){
                if(StringUtils.equals(columnMeta.getName(),columnName)){
                    String sql = "alter table " + mappingInfo.getSourceMedia().getName() + " add " + columnMeta.getName() + " " + columnMeta.getType() + " (" + columnMeta.getLength() + "); ";
                    resultMap.put("sql",sql);
                    resultMap.put("mediaSourceId",mappingInfo.getTargetMediaSource().getId());
                    resultMap.put("mappingId",mappingId);
                    resultMap.put("success","true");
                }
            }
        }catch (Exception e){
            String msg = "获取表结构报错";
            logger.info(msg,e);
            resultMap.put("success","false");
            resultMap.put("msg",msg);
        }
        logger.info("生成添加字段的sql,返回结果是：{}", JSON.toJSONString(resultMap));
        return resultMap;
    }


    @RequestMapping(value = "/sync_to_es")
    @ResponseBody
    @LoginIgnore
    public String sync_to_es(Long mediaSourceId,Long mappingId, String sql) {
        logger.info(String.format("Receive a check request: \r\n mediaSourceId is %s ,\r\n mappingId is %s, \r\n sql is %s", mediaSourceId,mappingId,sql));
        ResponseVo responseVo = null;
        try {
            if(mediaSourceId == null){
                throw new ErrorException(CodeContext.MEDIASOURCEID_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.MEDIASOURCEID_ISNULL_ERROR_CODE));
            }
            if(mappingId == null){
                throw new ErrorException(CodeContext.MAPPINGID_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.MAPPINGID_ISNULL_ERROR_CODE));
            }
            if(StringUtils.isEmpty(sql)){
                throw new ErrorException(CodeContext.SQL_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.SQL_ISNULL_ERROR_CODE));
            }
            GroupMetadataManager groupMetadataManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
            ClusterState clusterState = groupMetadataManager.getClusterState();
            if (clusterState == null) {
                throw new ErrorException(CodeContext.CLUSTERSTATE_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.CLUSTERSTATE_ISNULL_ERROR_CODE));
            }
            List<ClusterState.MemberData> memberDatas = clusterState.getAllMemberData();
            if (memberDatas == null || memberDatas.size() == 0) {
                throw new ErrorException(CodeContext.MEMBERINFO_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.MEMBERINFO_ISNULL_ERROR_CODE));
            }

            MediaMappingInfo mediaMappingInfo = mediaService.findMediaMappingsById(mappingId);
            Long taskId = mediaMappingInfo.getTaskId();
            TaskInfo taskInfo = taskConfigService.getTask(taskId);
            Long taskGroupId = taskInfo.getGroupId();

            ClusterState.MemberData memberDataFinal = null;
            for (ClusterState.MemberData memberData:memberDatas) {
                 if(memberData.getGroupId().equals(String.valueOf(taskGroupId))) {
                     memberDataFinal = memberData;
                     break;
                 }
            }

            if(memberDataFinal == null) {
                throw new ErrorException(CodeContext.NOTFOUND_WORKER_ERROR_CODE);
            }

            String url = "http://"+ memberDataFinal.getWorkerState().url()+"/es/syncCloumn/"+mediaSourceId;
            logger.info("同步es的worker请求地址[ {} ]",url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> map = new HashMap<>();
            map.put("sql", sql);
            map.put("mappingId",mappingId);
            HttpEntity request = new HttpEntity(map, headers);
            responseVo = new RestTemplate(Lists.newArrayList(new FastJsonHttpMessageConverter())).postForObject(url,request,ResponseVo.class);
            if(responseVo==null){
                throw new ErrorException(CodeContext.RESULT_RETURN_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.RESULT_RETURN_ERROR_CODE));
            }
        } catch (ErrorException e) {
            logger.error("es同步异常:", e);
            responseVo = new ResponseVo();
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo = new ResponseVo();
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        String reslutJsonStr = JSON.toJSONString(responseVo);
        logger.info("请求返回结果 [ {} ]",reslutJsonStr);
        return reslutJsonStr;
    }
	
	
	@RequestMapping(value = "/sync_to_hive")
    @ResponseBody
    @LoginIgnore
    public String sync_to_hive(Long mediaSourceId,Long mappingId, String sql,String jobNum,String dbName) {
        logger.info(String.format("Receive a check request:  mediaSourceId is %s , mappingId is %s, sql is %s, jobNum is %s, dbName is %s", mediaSourceId,mappingId,sql,jobNum,dbName));
        ResponseVo responseVo = new ResponseVo();
        try {
            if(mediaSourceId == null){
                throw new ErrorException(CodeContext.MEDIASOURCEID_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.MEDIASOURCEID_ISNULL_ERROR_CODE));
            }
            if(mappingId == null){
                throw new ErrorException(CodeContext.MAPPINGID_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.MAPPINGID_ISNULL_ERROR_CODE));
            }
            if(StringUtils.isEmpty(sql)){
                throw new ErrorException(CodeContext.SQL_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.SQL_ISNULL_ERROR_CODE));
            }
            if(StringUtils.isEmpty(jobNum)){
                throw new ErrorException(CodeContext.JOBNUM_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.JOBNUM_ISNULL_ERROR_CODE));
            }
           if(StringUtils.isEmpty(dbName)){
                throw new ErrorException(CodeContext.DBNAME_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.DBNAME_ISNULL_ERROR_CODE));
           }

           this.syncRelationService.syncColumnToHive(mappingId,mediaSourceId,sql,jobNum,dbName);

        } catch (ErrorException e) {
            logger.error("hive同步异常:", e);
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        String reslutJsonStr = JSON.toJSONString(responseVo);
        logger.info("请求返回结果 [ {} ]",reslutJsonStr);
        return reslutJsonStr;
    }
	



    @RequestMapping(value = "/sync_to_kudu")
    @ResponseBody
    @LoginIgnore
    public String sync_to_kudu(Long mediaSourceId,Long mappingId, String sql) {
        logger.info(String.format("Receive a check request: \r\n mediaSourceId is %s ,\r\n mappingId is %s, \r\n sql is %s", mediaSourceId,mappingId,sql));
        ResponseVo responseVo = null;
        try {
            if(mediaSourceId == null){
                throw new ErrorException(CodeContext.MEDIASOURCEID_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.MEDIASOURCEID_ISNULL_ERROR_CODE));
            }
            if(mappingId == null){
                throw new ErrorException(CodeContext.MAPPINGID_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.MAPPINGID_ISNULL_ERROR_CODE));
            }
            if(StringUtils.isEmpty(sql)){
                throw new ErrorException(CodeContext.SQL_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.SQL_ISNULL_ERROR_CODE));
            }
            GroupMetadataManager groupMetadataManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
            ClusterState clusterState = groupMetadataManager.getClusterState();
            if (clusterState == null) {
                throw new ErrorException(CodeContext.CLUSTERSTATE_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.CLUSTERSTATE_ISNULL_ERROR_CODE));
            }
            List<ClusterState.MemberData> memberDatas = clusterState.getAllMemberData();
            if (memberDatas == null || memberDatas.size() == 0) {
                throw new ErrorException(CodeContext.MEMBERINFO_ISNULL_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.MEMBERINFO_ISNULL_ERROR_CODE));
            }
            ClusterState.MemberData memberData = memberDatas.get(0);
            String url = "http://"+ memberData.getWorkerState().url()+"/kudu/syncCloumn/"+mediaSourceId;
            logger.info("kudu请求worker请求地址{}",url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> map = new HashMap<>();
            map.put("sql", sql);
            map.put("mappingId",mappingId);
            HttpEntity request = new HttpEntity(map, headers);
            responseVo = new RestTemplate(Lists.newArrayList(new FastJsonHttpMessageConverter())).postForObject(url,request,ResponseVo.class);
            if(responseVo==null){
                throw new ErrorException(CodeContext.RESULT_RETURN_ERROR_CODE,CodeContext.getErrorDesc(CodeContext.RESULT_RETURN_ERROR_CODE));
            }
        } catch (ErrorException e) {
            logger.error("kudu同步异常:", e);
            responseVo = new ResponseVo();
            responseVo.setCode(e.getCode());
            responseVo.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("服务器异常:", e);
            responseVo = new ResponseVo();
            responseVo.setCode(CodeContext.SERVER_RUNTIME_ERROR_CODE);
            responseVo.setMessage(CodeContext.getErrorDesc(responseVo.getCode()));
        }
        String reslutJsonStr = JSON.toJSONString(responseVo);
        logger.info("请求返回结果 [ {} ]",reslutJsonStr);
        return reslutJsonStr;
    }
	
	
	
	





    private List<TreeView> buildTreeViews(List<SqlCheckResult> list) {
        List<TreeView> treeList = new ArrayList<>();
        for (SqlCheckResult result : list) {
            for (SqlCheckTree tree : result.getSqlCheckTrees()) {
                treeList.add(buildTree(result, tree));
            }
        }

        return treeList;
    }

    private TreeView buildTree(SqlCheckResult result, SqlCheckTree tree) {
        TreeView treeView = new TreeView();
        treeView.setSqlString(result.getSqlString());
        treeView.setTableName(tree.getTableName());
        treeView.setRootNode(buildTreeNode(tree.getRootNode()));
        //增加表别名
        treeView.getRootNode().setTableAliasName(tree.getTableName());
        treeView.setSqlExeDirection(tree.getSqlExeDirection());
        treeView.setSqlCheckNotes(tree.getSqlCheckNotes());
        return treeView;
    }

    private TreeView.NodeView buildTreeNode(SyncNode node) {
        TreeView.NodeView nodeView = new TreeView.NodeView();
        nodeView.setMediaSourceId(node.getMediaSource().getId());
        nodeView.setMediaSourceType(node.getMediaSourceType());
        nodeView.setMediaSourceName(node.getMediaSourceName());
        if(node.getMappingInfo()!=null){
            nodeView.setMappingId(node.getMappingInfo().getId());
        }
        if (node.getMediaSource().getType() == MediaSourceType.MYSQL && ((RdbMediaSrcParameter)node.getMediaSource().getParameterObj()).getIsTIDB()) {
            nodeView.setName(String.format("【%s】%s(TIDB)", node.getMediaSource().getType(), node.getMediaSource().getName()));
        } else {
            nodeView.setName(String.format("【%s】%s", node.getMediaSource().getType(), node.getMediaSource().getName()));
        }
        nodeView.setTableAliasName(node.getTableNameAlias());

        List<SyncNode> childNodes = node.getChildren();
        if (childNodes != null && !childNodes.isEmpty()) {
            List<TreeView.NodeView> childrenTreeNodes = new ArrayList<>();
            for (SyncNode childNode : childNodes) {
                childrenTreeNodes.add(buildTreeNode(childNode));
            }
            nodeView.setChildren(childrenTreeNodes);
        }
        //只是支持单节点的情况
        else {
            List<TreeView.NodeView> childrenTreeNodes = new ArrayList<>();
            nodeView.setChildren(childrenTreeNodes);
        }
        return nodeView;
    }

    private boolean isMatch(MediaSourceInfo msInfo, String ip, int port, String schema) {
        RdbMediaSrcParameter msParam = msInfo.getParameterObj();
        String writeHost = msParam.getWriteConfig().getWriteHost();
        int msPort = msParam.getPort();
        String msSchema = msParam.getNamespace();
        if (ip.equals(writeHost) && port == msPort && schema.equals(msSchema)) {
            return true;
        } else {
            return false;
        }
    }
}
