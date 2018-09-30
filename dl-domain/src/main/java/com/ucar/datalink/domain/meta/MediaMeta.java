package com.ucar.datalink.domain.meta;

import com.ucar.datalink.domain.media.MediaSourceType;

import java.util.List;

/**
 * Created by user on 2017/7/7.
 */
public class MediaMeta {

    /**
     * 抽象的库的名称
     */
    private String nameSpace;

    /**
     * 抽象的表的名称
     */
    private String name;

    /**
     * 表下面所有的列元信息集合
     */
    private List<ColumnMeta> column;

    /**
     * 当前的表是属于哪种数据库
     */
    private MediaSourceType dbType;


    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnMeta> getColumn() {
        return column;
    }

    public void setColumn(List<ColumnMeta> column) {
        this.column = column;
    }

    public MediaSourceType getDbType() {
        return dbType;
    }

    public void setDbType(MediaSourceType dbType) {
        this.dbType = dbType;
    }


    @Override
    public String toString() {
        return "MediaMeta{" +
                "name='" + name + '\'' +
                '}';
    }

    public String iteratorColumns() {
        return "MediaMeta{" +
                "ame='" + name + '\'' +
                ", column=" + column +
                '}';
    }


}
