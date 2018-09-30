package com.ucar.datalink.manager.core.web.controller.sync;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.SyncRelationService;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.relationship.SqlCheckResult;
import com.ucar.datalink.domain.relationship.SqlCheckTree;
import com.ucar.datalink.domain.relationship.SyncNode;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.dto.syncRelation.TreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
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

    @RequestMapping(value = "/show")
    public ModelAndView show() {
        ModelAndView mav = new ModelAndView("sync/relation/show");
        mav.addObject("mediaSourceList", mediaSourceService.getList());
        return mav;
    }

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
        mav.addObject("mediaSourceList", mediaSourceService.getList());
        return mav;
    }

    @RequestMapping(value = "/checkSql")
    @ResponseBody
    public List<TreeView> checkSql(@RequestBody Map<String, String> map) {
        Long mediaSourceId = Long.valueOf(map.get("mediaSourceId"));
        String sqls = map.get("sqls");
        List<SqlCheckResult> results = syncRelationService.checkSqls(mediaSourceId, sqls);
        if (!results.isEmpty()) {
            return buildTreeViews(results);
        }
        return Lists.newArrayList();
    }

    @RequestMapping(value = "/checkSql_4_dbms")
    @ResponseBody
    @LoginIgnore
    public List<TreeView> checkSql_4_dbms(String ip, int port, String schema, String sqls) {
        logger.info(String.format("Receive a check request: \r\n IP is %s , \r\n Port is %s, \r\n Schema is %s, \r\n sqls is %s",
                ip, port, schema, sqls));

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
        }
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
        nodeView.setName(String.format("【%s】%s", node.getMediaSource().getType(), node.getMediaSource().getName()));
        nodeView.setTableAliasName(node.getTableNameAlias());

        List<SyncNode> childNodes = node.getChildren();
        if (childNodes != null && !childNodes.isEmpty()) {
            List<TreeView.NodeView> childrenTreeNodes = new ArrayList<>();
            for (SyncNode childNode : childNodes) {
                childrenTreeNodes.add(buildTreeNode(childNode));
            }
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
