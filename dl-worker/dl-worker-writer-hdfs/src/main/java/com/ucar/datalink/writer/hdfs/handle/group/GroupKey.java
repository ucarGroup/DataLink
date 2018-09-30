package com.ucar.datalink.writer.hdfs.handle.group;

import java.io.Serializable;

/**
 * Created by lubiao on 2017/12/15.
 */
public class GroupKey implements Serializable {
    private String namespace;
    private String name;

    public GroupKey(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupKey groupKey = (GroupKey) o;

        if (!namespace.equals(groupKey.namespace)) return false;
        return name.equals(groupKey.name);

    }

    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
