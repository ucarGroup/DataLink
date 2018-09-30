package com.ucar.datalink.domain.media;

import com.ucar.datalink.domain.Storable;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 介质信息数据类(介质是一个抽象概念，可以是数据库的表、搜索引擎的索引、一个文件目录等等.
 * <p>
 * Created by lubiao on 2017/2/28.
 */
@Alias("media")
public class MediaInfo implements Serializable, Storable {

    //------------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------fields mapping to database-----------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private Long id;
    private String namespace;
    private String name;
    private Long mediaSourceId;
    private Date createTime;
    private Date modifyTime;

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------getter&setter methods for database fields-------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getMediaSourceId() {
        return mediaSourceId;
    }

    public void setMediaSourceId(Long mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaInfo mediaInfo = (MediaInfo) o;

        return id.equals(mediaInfo.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------fields for business------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private transient MediaSourceInfo mediaSource;
    private transient ModeValue namespaceMode;
    private transient ModeValue nameMode;

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------business methods---------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    public MediaSourceInfo getMediaSource() {
        return mediaSource;
    }

    public void setMediaSource(MediaSourceInfo mediaSource) {
        this.mediaSource = mediaSource;
    }

    public ModeValue getNamespaceMode() {
        if (namespaceMode == null) {
            namespaceMode = ModeUtils.parseMode(namespace);
        }

        return namespaceMode;
    }

    public ModeValue getNameMode() {
        if (nameMode == null) {
            nameMode = ModeUtils.parseMode(name);
        }

        return nameMode;
    }

    public static enum Mode {
        SINGLE(0), MULTI(1), WILDCARD(3), YEARLY(4), MONTHLY(5);

        private int value;

        Mode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public boolean isSingle() {
            return this == Mode.SINGLE;
        }

        public boolean isMulti() {
            return this == Mode.MULTI;
        }

        public boolean isWildCard() {
            return this == Mode.WILDCARD;
        }

        public boolean isYearly() {
            return this == Mode.YEARLY;
        }

        public boolean isMonthly() {
            return this == Mode.MONTHLY;
        }
    }

    // 模式，比如offer[1-128]代表offer1...offer128，128个配置定义
    public static class ModeValue implements Serializable {

        private static final long serialVersionUID = 54902778821522113L;
        private Mode mode;
        private List<String> values = new ArrayList<>();

        public ModeValue(Mode mode, List<String> values) {
            this.mode = mode;
            this.values = values;
        }

        public String getSingleValue() {
            return values.get(0);
        }

        public List<String> getMultiValue() {
            return values;
        }

        public Mode getMode() {
            return mode;
        }

        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
        }
    }

}
