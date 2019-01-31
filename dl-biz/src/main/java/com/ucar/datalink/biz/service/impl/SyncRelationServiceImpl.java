package com.ucar.datalink.biz.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.dal.MediaSourceDAO;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.SyncRelationService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.DataSourceFactory;
import com.ucar.datalink.biz.utils.ddl.DdlUtils;
import com.ucar.datalink.biz.utils.ddl.DdlSqlUtils;
import com.ucar.datalink.biz.utils.ddl.SQLStatementHolder;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.relationship.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2017/5/23.
 */
@Service
public class SyncRelationServiceImpl implements SyncRelationService {

    private static final String ALL_MAPPING = "ALL_MAPPING";

    private LoadingCache<String, List<MediaMappingInfo>> mediaMappingCache = CacheBuilder.newBuilder().build(
            new CacheLoader<String, List<MediaMappingInfo>>() {
                @Override
                public List<MediaMappingInfo> load(String key) throws Exception {
                    List<MediaMappingInfo> list = mediaDAO.getAllMediaMappings();
                    return list == null ? Lists.newArrayList() : list;
                }
            }
    );

    private LoadingCache<Long, List<MediaMappingInfo>> taskMediaMappingCache = CacheBuilder.newBuilder().build(
            new CacheLoader<Long, List<MediaMappingInfo>>() {
                @Override
                public List<MediaMappingInfo> load(Long taskId) throws Exception {
                    List<MediaMappingInfo> allList = mediaMappingCache.getUnchecked(ALL_MAPPING);
                    return allList.stream().filter(i -> i.getTaskId().equals(taskId)).collect(Collectors.toList());
                }
            }
    );

    private LoadingCache<String, List<SyncNode>> treesCache = CacheBuilder.newBuilder().build(
            new CacheLoader<String, List<SyncNode>>() {
                @Override
                public List<SyncNode> load(String mediaName) throws Exception {
                    return generateTrees(mediaName);
                }
            }
    );

    @Override
    public void clearSyncRelationCache() {
        mediaMappingCache.invalidateAll();
        taskMediaMappingCache.invalidateAll();
        treesCache.invalidateAll();
    }

    @Autowired
    MediaDAO mediaDAO;

    @Autowired
    MediaSourceDAO mediaSourceDAO;

    @Override
    public List<SyncNode> getSyncRelationTrees(Long mediaSourceId, String mediaName) {
        Long sddlMediaSourceId = isSDDLSubDB(mediaSourceId);
        Long realMediaSourceId;
        if (sddlMediaSourceId != null) {
            realMediaSourceId = sddlMediaSourceId;
        } else {
            realMediaSourceId = mediaSourceId;
        }
        List<SyncNode> list = treesCache.getUnchecked(mediaName);
        return list.stream().filter(i -> isDbInNodeTree(realMediaSourceId, mediaName, i, true)).collect(Collectors.toList());
    }

    @Override
    public List<SqlCheckResult> checkSqls(Long mediaSourceId, String sqls) {
        Long sddlMediaSourceId = isSDDLSubDB(mediaSourceId);
        if (sddlMediaSourceId != null) {
            mediaSourceId = sddlMediaSourceId;
        }

        MediaSourceInfo mediaSourceInfo = mediaSourceDAO.getById(mediaSourceId);

        List<SQLStatementHolder> holders = DdlSqlUtils.buildSQLStatement(mediaSourceInfo.getType(), sqls);

        List<SqlCheckResult> list = new ArrayList<>();
        if (!holders.isEmpty()) {
            for (SQLStatementHolder holder : holders) {
                SqlCheckResult sqlCheckResult = checkOneSql(mediaSourceInfo, holder);
                if (sqlCheckResult != null) {
                    list.add(sqlCheckResult);
                }
            }
        }
        return list;
    }

    private SqlCheckResult checkOneSql(final MediaSourceInfo mediaSourceInfo, final SQLStatementHolder holder) {
        SqlCheckResult sqlCheckResult = new SqlCheckResult(holder.getSqlString());

        holder.check();

        if (holder.getSqlType().equals(SqlType.CreateTable)) {
            holder.getSqlCheckItems().forEach(i -> {
                        List<SqlCheckTree> trees = buildSqlCheckTrees(i, mediaSourceInfo);
                        if (!trees.isEmpty()) {
                            sqlCheckResult.getSqlCheckTrees().addAll(trees);
                            trees.forEach(t -> {
                                t.setSqlExeDirection(SqlExeDirection.Negative);

                                t.getSqlCheckNotes().add(new SqlCheckNote(
                                        "请按照同步关系的反向顺序执行脚本.",
                                        SqlCheckNote.RoleType.DBA,
                                        SqlCheckNote.NoteLevel.INFO));
                                if (containsSddlDataSource(t.getRootNode())) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "SDDL数据源内部先执行非0号库，再执行0号库.",
                                            SqlCheckNote.RoleType.DBA,
                                            SqlCheckNote.NoteLevel.INFO));
                                }

                                if (containsSddlDataSource(t.getRootNode())) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "SDDL增加数据表，请在sddladmin中做对应的配置变更.",
                                            SqlCheckNote.RoleType.DLA,
                                            SqlCheckNote.NoteLevel.WARN));
                                }

                                t.getSqlCheckNotes().addAll(buildSqlCheckNotes(mediaSourceInfo, i, t.getRootNode(), true));
                            });
                        }
                    }
            );
        } else if (holder.getSqlType().equals(SqlType.AlterTable)) {
            holder.getSqlCheckItems().forEach(i -> {
                        List<SqlCheckTree> trees = buildSqlCheckTrees(i, mediaSourceInfo);
                        if (!trees.isEmpty()) {
                            sqlCheckResult.getSqlCheckTrees().addAll(trees);
                            trees.forEach(t -> {
                                if (i.isContainsTableRename()) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "参与数据同步的表，不支持[Table-Rename]操作.",
                                            SqlCheckNote.RoleType.ALL,
                                            SqlCheckNote.NoteLevel.ERROR));
                                }

                                if (i.isContainsColumnRename()) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "参与数据同步的表，不支持[Column-Rename]操作",
                                            SqlCheckNote.RoleType.ALL,
                                            SqlCheckNote.NoteLevel.ERROR));
                                }

                                if (i.isContainsColumnDrop()) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "参与数据同步的表，不支持[Column-Drop]操作.",
                                            SqlCheckNote.RoleType.ALL,
                                            SqlCheckNote.NoteLevel.ERROR));
                                }

                                if (i.isContainsColumnAddAfter()) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "参与数据同步的表，不支持[Add-After-Column]操作.",
                                            SqlCheckNote.RoleType.ALL,
                                            SqlCheckNote.NoteLevel.ERROR));
                                }

                                if (i.isContainsColumnModifyAfter()) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "参与数据同步的表，不支持[Modify-After-Column]操作.",
                                            SqlCheckNote.RoleType.ALL,
                                            SqlCheckNote.NoteLevel.ERROR));
                                }

                                if (containsSddlDataSource(t.getRootNode())) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "SDDL数据源内部先执行非0号库，再执行0号库.",
                                            SqlCheckNote.RoleType.DBA,
                                            SqlCheckNote.NoteLevel.INFO));
                                    if (i.isContainsIndexesAdd()) {
                                        t.getSqlCheckNotes().add(new SqlCheckNote(
                                                "请确认都需要在哪些分库执行索引添加操作.",
                                                SqlCheckNote.RoleType.DBA,
                                                SqlCheckNote.NoteLevel.WARN
                                        ));
                                    }
                                }

                                if (i.getUniqueKeysDropInfo() != null && i.getUniqueKeysDropInfo().size() > 0) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "参与数据同步的表，源表索引被删除，请确认该索引是否唯一性索引，若是请在目标表也将其删除.",
                                            SqlCheckNote.RoleType.DBA,
                                            SqlCheckNote.NoteLevel.WARN));
                                }

                                t.setSqlExeDirection(SqlExeDirection.Negative);
                                t.getSqlCheckNotes().add(new SqlCheckNote(
                                        "请按照同步关系的反向顺序执行脚本.",
                                        SqlCheckNote.RoleType.DBA,
                                        SqlCheckNote.NoteLevel.INFO));

                                t.getSqlCheckNotes().addAll(buildSqlCheckNotes(mediaSourceInfo, i, t.getRootNode(), true));
                            });
                        }
                    }
            );
        } else if (holder.getSqlType().equals(SqlType.DropTable)) {
            holder.getSqlCheckItems().forEach(i -> {
                        List<SqlCheckTree> trees = buildSqlCheckTrees(i, mediaSourceInfo);
                        if (!trees.isEmpty()) {
                            sqlCheckResult.getSqlCheckTrees().addAll(trees);
                            trees.forEach(t -> {
                                t.getSqlCheckNotes().add(new SqlCheckNote(
                                        String.format("待删除的表[%s]涉及数据同步，请确认风险.", i.getTableName()),
                                        SqlCheckNote.RoleType.DLA,
                                        SqlCheckNote.NoteLevel.WARN));

                                t.setSqlExeDirection(SqlExeDirection.Positive);
                                t.getSqlCheckNotes().add(new SqlCheckNote(
                                        "请按照同步关系的正向顺序执行脚本",
                                        SqlCheckNote.RoleType.DBA,
                                        SqlCheckNote.NoteLevel.INFO));

                                if (containsSddlDataSource(t.getRootNode())) {
                                    t.getSqlCheckNotes().add(new SqlCheckNote(
                                            "SDDL数据源内部先执行0号库，再执行其它库.",
                                            SqlCheckNote.RoleType.DBA,
                                            SqlCheckNote.NoteLevel.INFO));
                                }

                                t.getSqlCheckNotes().addAll(buildSqlCheckNotes(mediaSourceInfo, i, t.getRootNode(), true));
                            });
                        }
                    }
            );
        }

        return sqlCheckResult.getSqlCheckTrees().isEmpty() ? null : sqlCheckResult;
    }

    private Long isSDDLSubDB(Long mediaSourceId) {
        Long sddlMediaSourceId = null;

        //必须先判断sddl，因为有些库是分布式数据库的子库
        List<MediaSourceInfo> sddlMediaSources = mediaSourceDAO.getListByType(Sets.newHashSet(MediaSourceType.SDDL));
        for (MediaSourceInfo msInfo : sddlMediaSources) {
            SddlMediaSrcParameter sddlParam = msInfo.getParameterObj();

            List<Long> primaryDbsId = sddlParam.getPrimaryDbsId();
            for (Long dbId : primaryDbsId) {
                if (Objects.equals(mediaSourceId, dbId)) {
                    sddlMediaSourceId = msInfo.getId();
                    break;
                }
            }

            if (sddlMediaSourceId == null) {
                List<Long> secondaryDbsId = sddlParam.getSecondaryDbsId();
                if (secondaryDbsId != null) {
                    for (Long dbId : secondaryDbsId) {
                        if (Objects.equals(mediaSourceId, dbId)) {
                            sddlMediaSourceId = msInfo.getId();
                            break;
                        }
                    }
                }
            }

            if (sddlMediaSourceId != null) {
                break;
            }
        }
        return sddlMediaSourceId;
    }

    private List<SqlCheckTree> buildSqlCheckTrees(SqlCheckItem sqlCheckItem, MediaSourceInfo mediaSourceInfo) {
        List<SyncNode> list = getSyncRelationTrees(mediaSourceInfo.getId(),
                sqlCheckItem.getSqlType().equals(SqlType.CreateTable) ? "(.*)" : sqlCheckItem.getTableName());

        List<SqlCheckTree> result = new ArrayList<>();
        for (SyncNode root : list) {
            SyncNode newNode = copyNode(mediaSourceInfo, root, sqlCheckItem, true);
            if (newNode != null && (CollectionUtils.isNotEmpty(newNode.getChildren()) || mediaSourceInfo.getType() == MediaSourceType.SDDL)) {
                result.add(new SqlCheckTree(newNode, sqlCheckItem.getTableName()));
            }
        }
        return result;
    }

    private SyncNode copyNode(MediaSourceInfo mediaSource4Check, SyncNode syncNode, SqlCheckItem sqlCheckItem, boolean isRootNode) {
        MediaSourceType mediaSourceType = syncNode.getMediaSourceType();

        //如果包含禁止的Sql操作，不进行任何节点过滤，防止过滤完之后是一颗空树，这样的话会提示用户不影响数据同步
        if (!sqlCheckItem.containsForbidden()) {

            if (SqlType.AlterTable.equals(sqlCheckItem.getSqlType())) {
                //白名单验证
                if (!isRootNode && ColumnMappingMode.INCLUDE.equals(syncNode.getMappingInfo().getColumnMappingMode())) {
                    if (!sqlCheckItem.isContainsColumnModify() && !sqlCheckItem.isContainsUniqueKeysDrop()
                            && (mediaSourceType.isRdbms() || mediaSourceType == MediaSourceType.SDDL)) {
                        return null;//对于关系型数据库，如果配置了白名单，并且不包含【列修改操作】和【唯一性约束的Drop操作】，则直接忽略
                    }
                    if (!sqlCheckItem.isContainsColumnModify() &&
                            (mediaSourceType == MediaSourceType.HBASE ||
                                    mediaSourceType == MediaSourceType.HDFS ||
                                    mediaSourceType == MediaSourceType.ELASTICSEARCH)
                            ) {
                        return null;//对于非关系型数据库，如果配置了白名单，并且不包含【列修改操作】，则直接忽略
                    }
                }

                //列变更验证
                if (!isRootNode && !sqlCheckItem.isAlterAffectColumn() &&
                        (mediaSourceType == MediaSourceType.HDFS ||
                                mediaSourceType == MediaSourceType.ELASTICSEARCH)) {
                    return null;//如果Alter操作不影响列，那么HDFS和ELASTICSEARCH类型的节点可忽略
                }

                //HBase验证
                if (!isRootNode && mediaSourceType == MediaSourceType.HBASE && CollectionUtils.isEmpty(syncNode.getChildren())) {
                    return null;//如果是HBase类型节点，并且没有子节点，则直接忽略
                }

                //添加索引验证
                if (isRootNode && !sqlCheckItem.isAlterAffectColumn() && !sqlCheckItem.isContainsUniqueKeysDrop()
                        && sqlCheckItem.isContainsIndexesAdd()) {
                    if (mediaSource4Check.getType() != MediaSourceType.SDDL) {
                        // 对于index-add操作，如果不是在sddl类型的数据源上操作，直接忽略即可
                        return null;
                    } else {
                        SyncNode sddlNode = findSyncNode(syncNode, mediaSource4Check);
                        if (sddlNode != null) {
                            SyncNode newNode = new SyncNode(sddlNode.getMediaSource(), sddlNode.getMappingInfo());
                            newNode.setTableNameAlias(sddlNode.getTableNameAlias());
                            return newNode;
                        }
                    }
                }
            }
        }

        SyncNode newNode = new SyncNode(syncNode.getMediaSource(), syncNode.getMappingInfo());
        newNode.setTableNameAlias(syncNode.getTableNameAlias());

        if (CollectionUtils.isNotEmpty(syncNode.getChildren())) {
            newNode.setChildren(new ArrayList<>());
            for (SyncNode n : syncNode.getChildren()) {
                SyncNode sn = copyNode(mediaSource4Check, n, sqlCheckItem, false);
                if (sn != null) {
                    newNode.getChildren().add(sn);
                }
            }
        }

        return newNode;
    }

    private Set<SqlCheckNote> buildSqlCheckNotes(MediaSourceInfo mediaSourceForCheck, SqlCheckItem sqlCheckItem, SyncNode syncNode, boolean isRootNode) {
        Set<SqlCheckNote> result = new HashSet<>();

        if (sqlCheckItem.getSqlType().equals(SqlType.CreateTable)) {
            MediaSourceInfo mediaSourceInfo = syncNode.getMediaSource();
            if (mediaSourceInfo.getType().equals(MediaSourceType.SDDL) && !((SddlMediaSrcParameter) mediaSourceInfo.getParameterObj()).getSecondaryDbsId().isEmpty()) {
                result.add(new SqlCheckNote(
                        String.format("请确认该表是否需要在SDDL数据源[%s]的二级库中创建.", mediaSourceInfo.getName()),
                        SqlCheckNote.RoleType.DLA,
                        SqlCheckNote.NoteLevel.WARN));
            }
        } else if (sqlCheckItem.getSqlType().equals(SqlType.AlterTable)) {
            if (!isRootNode && syncNode.getMappingInfo().getColumnMappingMode().equals(ColumnMappingMode.INCLUDE)) {
                result.add(new SqlCheckNote(
                        String.format("从[%s]到[%s]的同步配置了列名白名单，请确认是否需要在目标端执行Sql.",
                                syncNode.getMappingInfo().getSourceMedia().getMediaSource().getName(),
                                syncNode.getMappingInfo().getTargetMediaSource().getName()),
                        SqlCheckNote.RoleType.DLA,
                        SqlCheckNote.NoteLevel.WARN
                ));
            }

            if (!isRootNode && syncNode.getMappingInfo().getInterceptorId() != null) {
                result.add(new SqlCheckNote(
                        String.format("从[%s]到[%s]的同步配置了拦截器，请确认是否需要在目标端执行Sql.",
                                syncNode.getMappingInfo().getSourceMedia().getMediaSource().getName(),
                                syncNode.getMappingInfo().getTargetMediaSource().getName()),
                        SqlCheckNote.RoleType.DLA,
                        SqlCheckNote.NoteLevel.WARN
                ));
            }
        }

        if (isRootNode && !mediaSourceForCheck.equals(syncNode.getMediaSource())) {
            result.add(new SqlCheckNote(
                    String.format("数据源[%s]在同步关系中并不是根节点，请确认都需要在哪些库执行脚本.", mediaSourceForCheck.getName()),
                    SqlCheckNote.RoleType.DBA,
                    SqlCheckNote.NoteLevel.WARN
            ));
        }
        if (!isRootNode && syncNode.getMappingInfo().getTargetMediaSource().getType().equals(MediaSourceType.ELASTICSEARCH)) {
            result.add(new SqlCheckNote(
                    "请参照Sql脚本和同步关系在ElasticSearch中做相应变更.",
                    SqlCheckNote.RoleType.ESA,
                    SqlCheckNote.NoteLevel.INFO));
        }
        if (!isRootNode && syncNode.getMappingInfo().getTargetMediaSource().getType().equals(MediaSourceType.HDFS)) {
            result.add(new SqlCheckNote(
                    "请参照Sql脚本和同步关系在Hive中做相应变更.",
                    SqlCheckNote.RoleType.SPARKA,
                    SqlCheckNote.NoteLevel.INFO
            ));
        }
        if (!isRootNode && syncNode.getMappingInfo().getTargetMediaSource().getType().equals(MediaSourceType.HBASE)) {
            result.add(new SqlCheckNote(
                    "请参照Sql脚本和同步关系在HBase中做相应变更.",
                    SqlCheckNote.RoleType.HBASEA,
                    SqlCheckNote.NoteLevel.INFO
            ));
        }

        if (!isRootNode && StringUtils.isNotBlank(syncNode.getTableNameAlias())) {
            result.add(new SqlCheckNote(
                    String.format("表[%s]从[%s]同步到[%s]时配置了别名[%s],需要在目标数据源中使用别名进行变更.",
                            sqlCheckItem.getTableName(),
                            syncNode.getMappingInfo().getSourceMedia().getMediaSource().getName(),
                            syncNode.getMappingInfo().getTargetMediaSource().getName(),
                            syncNode.getMappingInfo().getTargetMediaName()),
                    SqlCheckNote.RoleType.getRoleTypeByMediaSourceType(syncNode.getMappingInfo().getTargetMediaSource().getType()),
                    SqlCheckNote.NoteLevel.INFO
            ));
        }

        List<SyncNode> children = syncNode.getChildren();
        if (children != null && !children.isEmpty()) {
            for (SyncNode childNode : children) {
                result.addAll(buildSqlCheckNotes(mediaSourceForCheck, sqlCheckItem, childNode, false));
            }
        }

        return result;
    }

    private boolean containsSddlDataSource(SyncNode syncNode) {
        if (syncNode.getMediaSource().getType().equals(MediaSourceType.SDDL)) {
            return true;
        }

        if (syncNode.getChildren() != null && !syncNode.getChildren().isEmpty()) {
            for (SyncNode childNode : syncNode.getChildren()) {
                if (containsSddlDataSource(childNode)) {
                    return true;
                }
            }
        }

        return false;
    }

    private SyncNode findSyncNode(SyncNode syncNode, MediaSourceInfo mediaSourceInfo) {
        if (syncNode.getMediaSource().equals(mediaSourceInfo)) {
            return syncNode;
        }

        if (syncNode.getChildren() != null && !syncNode.getChildren().isEmpty()) {
            for (SyncNode childNode : syncNode.getChildren()) {
                SyncNode temp = findSyncNode(childNode, mediaSourceInfo);
                if (temp != null) {
                    return temp;
                }
            }
        }

        return null;
    }

    private boolean isDbInNodeTree(Long mediaSourceId, String mediaName, SyncNode rootNode, Boolean isRootNode) {
        if (mediaSourceId == null) {
            return true;//如果没有指定具体数据源，则认为不需要过滤，直接返回true即可
        }

        if (isRootNode) {
            if (rootNode.getMediaSource().getId().equals(mediaSourceId) && (rootNode.getMappingInfo().getSourceMedia().getName().equalsIgnoreCase(mediaName) || rootNode.getMappingInfo().getSourceMedia().getName().equals("(.*)"))) {
                return true;
            }
        } else if (rootNode.getMediaSource().getId().equals(mediaSourceId) && rootNode.getMappingInfo().getTargetMediaName().equalsIgnoreCase(mediaName)) {
            return true;
        }

        if (rootNode.getChildren() != null && !rootNode.getChildren().isEmpty()) {
            for (SyncNode node : rootNode.getChildren()) {
                if (isDbInNodeTree(mediaSourceId, mediaName, node, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<SyncNode> generateTrees(String mediaName) {
        Map<SyncNode, Set<SyncNode>> nodeMap = new HashMap<>();
        List<MediaMappingInfo> mappingList = mediaMappingCache.getUnchecked(ALL_MAPPING);
        for (MediaMappingInfo mappingInfo : mappingList) {
            if (isMatch(mediaName, mappingInfo)) {
                SyncNode parentNode = buildParentNode(mappingInfo);
                SyncNode childNode = buildChildNode(mappingInfo);
                if (!parentNode.equals(childNode)) {
                    if (nodeMap.containsKey(parentNode)) {
                        nodeMap.get(parentNode).add(childNode);
                    } else {
                        Set<SyncNode> set = new HashSet<>();
                        set.add(childNode);
                        nodeMap.put(parentNode, set);
                    }
                }
            }
        }

        List<SyncNode> rootNodeList = new ArrayList<>();
        if (!nodeMap.isEmpty()) {
            List<SyncNode> nodeList = new ArrayList<>();
            for (Map.Entry<SyncNode, Set<SyncNode>> entry : nodeMap.entrySet()) {
                entry.getKey().setChildren(new ArrayList<>(entry.getValue()));
                nodeList.add(entry.getKey());
            }

            for (SyncNode n1 : nodeList) {
                boolean isRootNode = true;

                for (SyncNode n2 : nodeList) {
                    if (n2.equals(n1)) {
                        continue;
                    }
                    List<SyncNode> values = n2.getChildren();
                    for (int i = 0; i < values.size(); i++) {
                        SyncNode temp = values.get(i);
                        if (temp.equals(n1)) {
                            n1.setMappingInfo(temp.getMappingInfo());
                            values.set(i, n1);// 替换
                            isRootNode = false;
                            break;
                        }
                    }
                    if (!isRootNode) {
                        break;
                    }
                }
                if (isRootNode) {
                    rootNodeList.add(n1);
                }
            }
        }

        return rootNodeList;
    }

    private boolean isMatch(String mediaName, MediaMappingInfo mappingInfo) {
        MediaInfo mediaInfo = mappingInfo.getSourceMedia();
        if (mediaInfo.getNameMode().getMode().isSingle()) {
            return mediaInfo.getName().equalsIgnoreCase(mediaName);
        } else if (mediaInfo.getNameMode().getMode().isMulti()) {
            return (ModeUtils.indexIgnoreCase(mediaInfo.getNameMode().getMultiValue(), mediaName) != -1)
                    && !existOverride(mediaName, mappingInfo);
        } else if (mediaInfo.getNameMode().getMode().isWildCard()) {
            if ("(.*)".equals(mediaName)) {
                return true;
            } else {
                return ModeUtils.isWildCardMatch(mediaInfo.getName(), mediaName)
                        && (findTable(mediaName, mediaInfo.getMediaSource()) != null)
                        && !existOverride(mediaName, mappingInfo);
            }
        } else if (mediaInfo.getNameMode().getMode().isYearly()) {
            return ModeUtils.isYearlyMatch(mediaInfo.getName(), mediaName)
                    && !existOverride(mediaName, mappingInfo);
        } else if (mediaInfo.getNameMode().getMode().isMonthly()) {
            return ModeUtils.isMonthlyMatch(mediaInfo.getName(), mediaName)
                    && !existOverride(mediaName, mappingInfo);
        } else {
            throw new UnsupportedOperationException("unsupport mode:" + mediaInfo.getNameMode().getMode());
        }
    }

    //检测是否存在对通配符的重载配置
    //如：从A库同步到B库配置了(.*),但是也单独配置了一张表T,那么T的配置会重载掉(.*)的配置，发生同步时会以T为准
    private boolean existOverride(String mediaName, MediaMappingInfo mappingInfoForCheck) {
        return taskMediaMappingCache
                .getUnchecked(mappingInfoForCheck.getTaskId())
                .stream()
                .filter(
                        i -> i.getTargetMediaSourceId().equals(mappingInfoForCheck.getTargetMediaSourceId())
                                && i.getSourceMedia().getName().equals(mediaName)
                ).findFirst()
                .isPresent();
    }

    private Table findTable(String mediaName, MediaSourceInfo mediaSourceInfo) {
        DataSource dataSource = DataSourceFactory.getDataSource(mediaSourceInfo);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        MediaSrcParameter parameter = mediaSourceInfo.getParameterObj();
        RdbMediaSrcParameter rdbParameter;
        if (parameter instanceof RdbMediaSrcParameter) {
            rdbParameter = (RdbMediaSrcParameter) parameter;
        } else if (parameter instanceof SddlMediaSrcParameter) {
            Long proxyDbId = ((SddlMediaSrcParameter) parameter).getProxyDbId();
            rdbParameter = DataLinkFactory.getObject(MediaSourceService.class).getById(proxyDbId).getParameterObj();
        } else {
            throw new DatalinkException("Unknown MediaSrcParameter Type.");
        }

        try {
            return DdlUtils.findTable(jdbcTemplate, rdbParameter.getNamespace(), rdbParameter.getNamespace(), mediaName);
        } catch (Exception e) {
            throw new DatalinkException("something goes wrong when finding table.", e);
        }
    }

    private SyncNode buildParentNode(MediaMappingInfo mappingInfo) {
        MediaSourceInfo srcMediaSourceInfo = mappingInfo.getSourceMedia().getMediaSource();
        Long sddlMediaSourceId = isSDDLSubDB(srcMediaSourceInfo.getId());
        if (sddlMediaSourceId != null) {
            MediaSourceInfo sddlMediaSourceInfo = mediaSourceDAO.getById(sddlMediaSourceId);
            return new SyncNode(sddlMediaSourceInfo, mappingInfo);
        }
        return new SyncNode(srcMediaSourceInfo, mappingInfo);
    }

    private SyncNode buildChildNode(MediaMappingInfo mappingInfo) {
        MediaSourceInfo targetMediaSourceInfo = mappingInfo.getTargetMediaSource();
        Long sddlMediaSourceId = isSDDLSubDB(targetMediaSourceInfo.getId());
        if (sddlMediaSourceId != null) {
            MediaSourceInfo sddlMediaSourceInfo = mediaSourceDAO.getById(sddlMediaSourceId);
            return new SyncNode(sddlMediaSourceInfo, mappingInfo);
        }
        return new SyncNode(targetMediaSourceInfo, mappingInfo);
    }
}
