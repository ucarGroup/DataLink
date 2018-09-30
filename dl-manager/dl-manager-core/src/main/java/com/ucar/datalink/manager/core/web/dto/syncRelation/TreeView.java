package com.ucar.datalink.manager.core.web.dto.syncRelation;

import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.relationship.SqlCheckNote;
import com.ucar.datalink.domain.relationship.SqlExeDirection;

import java.util.*;

/**
 * Created by lubiao on 2017/7/17.
 */
public class TreeView {
    private String sqlString;
    private String tableName;
    private NodeView rootNode;
    private SqlExeDirection sqlExeDirection;
    private Set<SqlCheckNote> sqlCheckNotes;
    private List<String> hierarchy;

    public String getSqlString() {
        return sqlString;
    }

    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public NodeView getRootNode() {
        return rootNode;
    }

    public void setRootNode(NodeView rootNode) {
        this.rootNode = rootNode;
    }

    public SqlExeDirection getSqlExeDirection() {
        return sqlExeDirection;
    }

    public void setSqlExeDirection(SqlExeDirection sqlExeDirection) {
        this.sqlExeDirection = sqlExeDirection;
    }

    public Set<SqlCheckNote> getSqlCheckNotes() {
        return sqlCheckNotes;
    }

    public void setSqlCheckNotes(Set<SqlCheckNote> sqlCheckNotes) {
        this.sqlCheckNotes = sqlCheckNotes;
    }

    public List<String> getHierarchy() {
        if (hierarchy == null) {
            TreeMap<Integer, List<String>> map = new TreeMap<Integer, List<String>>();
            map.put(1, new ArrayList<>());
            buildHierarchy(rootNode, 1, map);

            hierarchy = new ArrayList<>();
            for (Map.Entry<Integer, List<String>> entry : map.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    hierarchy.add("第" + entry.getKey() + "层：" + entry.getValue().toString());
                }
            }
        }
        return hierarchy;
    }

    public void setHierarchy(List<String> hierarchy) {
        this.hierarchy = hierarchy;
    }

    private void buildHierarchy(NodeView node, int i, TreeMap<Integer, List<String>> map) {
        map.get(i).add(node.getMediaSourceName());

        List<NodeView> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            i++;
            if (!map.containsKey(i)) {
                map.put(i, new ArrayList<>());
            }
            for (NodeView cNode : children) {
                buildHierarchy(cNode, i, map);
            }
        }
    }

    public static class NodeView {
        private Long mediaSourceId;
        private MediaSourceType mediaSourceType;
        private String mediaSourceName;
        private String name;
        private String tableAliasName;
        private List<NodeView> children;

        public Long getMediaSourceId() {
            return mediaSourceId;
        }

        public void setMediaSourceId(Long mediaSourceId) {
            this.mediaSourceId = mediaSourceId;
        }

        public MediaSourceType getMediaSourceType() {
            return mediaSourceType;
        }

        public void setMediaSourceType(MediaSourceType mediaSourceType) {
            this.mediaSourceType = mediaSourceType;
        }

        public String getMediaSourceName() {
            return mediaSourceName;
        }

        public void setMediaSourceName(String mediaSourceName) {
            this.mediaSourceName = mediaSourceName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<NodeView> getChildren() {
            return children;
        }

        public void setChildren(List<NodeView> children) {
            this.children = children;
        }

        public String getTableAliasName() {
            return tableAliasName;
        }

        public void setTableAliasName(String tableAliasName) {
            this.tableAliasName = tableAliasName;
        }
    }
}
